/*
 * Copyright 2019 University of Hildesheim, Software Systems Engineering
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.ssehub.kernel_haven.srcml;

import static net.ssehub.kernel_haven.srcml.XmlUserData.CONVERTED;
import static net.ssehub.kernel_haven.srcml.XmlUserData.LINE_END;
import static net.ssehub.kernel_haven.srcml.XmlUserData.LINE_START;
import static net.ssehub.kernel_haven.srcml.XmlUserData.NODE_REFERENCE;
import static net.ssehub.kernel_haven.srcml.XmlUserData.PREVIOUS_CPP_BLOCK;
import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.maybeNull;
import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.ssehub.kernel_haven.code_model.ast.BranchStatement;
import net.ssehub.kernel_haven.code_model.ast.CaseStatement;
import net.ssehub.kernel_haven.code_model.ast.CaseStatement.CaseType;
import net.ssehub.kernel_haven.code_model.ast.Code;
import net.ssehub.kernel_haven.code_model.ast.CodeList;
import net.ssehub.kernel_haven.code_model.ast.Comment;
import net.ssehub.kernel_haven.code_model.ast.CompoundStatement;
import net.ssehub.kernel_haven.code_model.ast.CppBlock;
import net.ssehub.kernel_haven.code_model.ast.CppStatement;
import net.ssehub.kernel_haven.code_model.ast.CppStatement.Type;
import net.ssehub.kernel_haven.code_model.ast.ErrorElement;
import net.ssehub.kernel_haven.code_model.ast.File;
import net.ssehub.kernel_haven.code_model.ast.Function;
import net.ssehub.kernel_haven.code_model.ast.ICode;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.code_model.ast.Label;
import net.ssehub.kernel_haven.code_model.ast.LoopStatement;
import net.ssehub.kernel_haven.code_model.ast.LoopStatement.LoopType;
import net.ssehub.kernel_haven.code_model.ast.ReferenceElement;
import net.ssehub.kernel_haven.code_model.ast.SingleStatement;
import net.ssehub.kernel_haven.code_model.ast.SwitchStatement;
import net.ssehub.kernel_haven.code_model.ast.TypeDefinition;
import net.ssehub.kernel_haven.code_model.ast.TypeDefinition.TypeDefType;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;
import net.ssehub.kernel_haven.util.logic.parser.Parser;
import net.ssehub.kernel_haven.util.logic.parser.VariableCache;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Converts the XML output of srcML to KernelHaven's AST structure.
 *
 * @author Adam
 */
class XmlToAstConverter {
    
    private java.io.@NonNull File baseFile;
    
    /**
     * The current stack of C-preprocessor conditions. Always contains a {@link True} at the bottom of the stack.
     */
    private @NonNull Deque<@NonNull Formula> conditions;
    
    private @NonNull Parser<@NonNull Formula> cppConditionParser;
    
    /**
     * The current stack of elements that are being transformed. This is used to set
     * {@link ISyntaxElement#setContainsErrorElement(boolean)} so it should contain all parents that need to have this
     * flag set when an {@link ErrorElement} is encountered.
     */
    private @NonNull Deque<@NonNull ISyntaxElement> elementStack;
    
    /**
     * The currently enclosing {@link SwitchStatement}s. This is used to map the {@link CaseStatement} to their
     * corresponding switch.
     */
    private @NonNull Deque<@NonNull SwitchStatement> switchStack;
    
    /**
     * The currently enclosing {@link BranchStatement} (only starting if). This is used to map the else and elseif
     * {@link BranchStatement} to their starting if.
     */
    private @NonNull Deque<@NonNull BranchStatement> ifStack;
    
    /**
     * Contains all {@link ReferenceElement}s where the referred-to element was not yet converted. The keys are the
     * referred to XMl nodes, the value contains all {@link ReferenceElement} that reference this node.
     */
    private @NonNull Map<@NonNull Node, @NonNull List<@NonNull ReferenceElement>> referencesToResolve;
    
    /**
     * Creates an XML output converter for the given base source file that is being parsed.
     * 
     * @param baseFile The source file that is parsed.
     */
    public XmlToAstConverter(java.io.@NonNull File baseFile) {
        this.baseFile = baseFile;
        this.conditions = new LinkedList<>();
        this.conditions.push(True.INSTANCE);
        
        this.cppConditionParser = new Parser<>(new SrcMlConditionGrammar(new VariableCache()));
        
        this.elementStack = new LinkedList<>();
        this.switchStack = new LinkedList<>();
        this.ifStack = new LinkedList<>();
        this.referencesToResolve = new HashMap<>();
    }
    
    /**
     * Starts the conversion process. Pass the top-level document node here.
     * 
     * @param node The top-level node; this must be a {@code <unit language="C">}.
     * 
     * @return The converted AST.
     */
    public @NonNull File convertFile(@NonNull Node node) {
        File result = new File(getPc(), baseFile);
        postCreation(result, node);
        
        elementStack.push(result);
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = notNull(children.item(i));
            result.addNestedElement(convertSafe(child));
        }
        elementStack.pop();
        
        resolveReferencesToResolve();
        
        return result;
    }
    
    /**
     * Post-processing step: resolve all {@link ReferenceElement}s that were not yet resolved.
     * 
     * @see #referencesToResolve
     */
    private void resolveReferencesToResolve() {
        for (Map.Entry<@NonNull Node, @NonNull List<@NonNull ReferenceElement>> toResolve
                : referencesToResolve.entrySet()) {
            
            Node node = notNull(toResolve.getKey());
            ISyntaxElement converted = (ISyntaxElement) node.getUserData(CONVERTED);
            
            for (@NonNull ReferenceElement reference : notNull(toResolve.getValue())) {
                if (converted != null) {
                    reference.setReferenced(converted);
                } else {
                    ErrorElement error = new ErrorElement(reference.getPresenceCondition(),
                            "Referred element was not converted");
                    postCreation(error, node);
                    error.setCondition(reference.getCondition());
                    reference.setReferenced(error);
                    // TODO: markErrorElement() doesn't work, since elementsStack is now empty
                }
            }
        }
    }
    
    /**
     * Converts a single node. If the node can't be converted, an {@link ErrorElement} is created instead.
     *  
     * @param node The XML node to convert.
     * 
     * @return The corresponding AST representation.
     */
    private @NonNull ISyntaxElement convertSafe(@NonNull Node node) {
        ISyntaxElement result;
        
        try {
            result = convert(node);
        } catch (FormatException e) {
            result = new ErrorElement(getPc(), notNull(e.getMessage()));
            postCreation(result, node);
            
            elementStack.push(result);
            markErrorElement();
            
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                result.addNestedElement(convertSafe(notNull(children.item(i))));
            }
            
            elementStack.pop();
        }
        
        return result;
    }
    
    /**
     * Converts a single node. Basically a huge switch-case which calls the appropriate convert*() method.
     *  
     * @param node The XML node to convert.
     * 
     * @return The corresponding AST representation.
     * 
     * @throws FormatException If converting the node fails.
     */
    //CHECKSTYLE:OFF // method too long
    private @NonNull ISyntaxElement convert(@NonNull Node node) throws FormatException {
    //CHECKSTYLE:ON
        ISyntaxElement result;
        
        if (node.getNodeType() == Node.TEXT_NODE) {
            Code text = new Code(getPc(), notNull(node.getTextContent().trim()));
            postCreation(text, node);
            result = text;
            
        } else {
            switch (node.getNodeName()) {
            
            case "cpp:empty":
            case "cpp:line":
            case "cpp:warning":
            case "cpp:error":
            case "cpp:pragma":
            case "cpp:include":
            case "cpp:undef":
            case "cpp:define":
                result = convertCppStatement(node);
                break;
                
            case "cpp:if":
            case "cpp:ifdef":
            case "cpp:ifndef":
            case "cpp:elif":
            case "cpp:else":
                result = convertCppIf(node, false);
                break;
                
            case "comment":
                result = convertComment(node);
                break;
                
            case "expr_stmt":
            case "continue":
            case "break":
            case "goto": 
            case "return":
            case "empty_stmt":
            case "function_decl":
            case "struct_decl":
            case "union_decl":
            case "decl_stmt":
            case "macro":
                result = convertStatement(node);
                break;
                
            case "label":
                result = convertLabel(node);
                break;
                
            case "case":
            case "default":
                result = convertCase(node);
                break;
                
            case "block":
                result = convertBlock(node);
                break;
                
            case "function":
                result = convertFunction(node);
                break;
                
            case "while":
            case "for":
            case "do":
                result = convertLoop(node);
                break;
                
            case "if":
                result = convertIf(node);
                break;
                
            case "elseif":
                result = convertElseIf(node);
                break;
                
            case "else":
                result = convertElse(node);
                break;
                
            case "switch":
                result = convertSwitch(node);
                break;
                
            case "enum":
            case "struct":
            case "union":
            case "typedef":
                result = convertTypedef(node);
                break;
                
            case "extern":
                result = convertExtern(node);
                break;
                
            case "kh:reference":
                result = convertReference(node);
                break;
                
            default:
                throw makeException(node, "Unknown tag <" + node.getNodeName() + ">");
            }
        }
        
        return result;
        
    }
    
    /**
     * Converts a C preprocessor statement, other than {@code #if} etc.
     * 
     * @param node The XML node representing the CPP statement. Must start with {@code cpp:}
     * 
     * @return The converted AST element.
     * 
     * @throws FormatException If converting fails.
     */
    private @NonNull CppStatement convertCppStatement(@NonNull Node node) throws FormatException {
        String typeString = node.getNodeName().substring("cpp:".length());
        
        CppStatement.Type type;
        try {
            type = CppStatement.Type.valueOf(typeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw makeException(node, "Invalid CPP statement type: " + typeString);
        }
        
        ICode expression = null;
        if (type != Type.EMPTY) {
            expression = convertChildrenToCode(node, 2, node.getChildNodes().getLength());
        }
        
        CppStatement result = new CppStatement(getPc(), type, expression);
        postCreation(result, node);
        if (expression != null && expression.containsErrorElement()) {
            result.setContainsErrorElement(true);
        }
        
        return result;
    }
    
    /**
     * Converts a C preprocessor {@code #if} (etc.) statement.
     * 
     * @param node The XML node representing the CPP statement. Must start with {@code cpp:}
     * @param convertToCode Whether to call {@link #convertChildrenToCode(Node)} instead of {@link #convert(Node)} on
     *      the child nodes.
     * 
     * @return The converted AST element.
     * 
     * @throws FormatException If converting fails.
     */
    //CHECKSTYLE:OFF // TODO: method too long
    private @NonNull CppBlock convertCppIf(@NonNull Node node, boolean convertToCode) throws FormatException {
    //CHECKSTYLE:ON
        /*
         * Children:
         * [0] = #
         * [1] = <cpp:directive>
         * [2] = <expr>  (except for <cpp:else>)
         * [...] = nested elements
         */
        
        String typeString = node.getNodeName().substring("cpp:".length());
        
        CppBlock.Type type;
        
        switch (typeString) {
        case "if":
            type = CppBlock.Type.IF;
            break;
        case "ifdef":
            type = CppBlock.Type.IFDEF;
            break;
        case "ifndef":
            type = CppBlock.Type.IFNDEF;
            break;
        case "elif":
            type = CppBlock.Type.ELSEIF;
            break;
        case "else":
            type = CppBlock.Type.ELSE;
            break;
            
        default:
            throw makeException(node, "Found invalid prepreocessor if: <" + node.getNodeName() + ">");
        }

        Formula formula;
        int nestedStartIndex;
        if (type != CppBlock.Type.ELSE) {
            nestedStartIndex = 3;
            
            String formulaStr = notNull(node.getChildNodes().item(2).getTextContent());
            if (type == CppBlock.Type.IFDEF) {
                formulaStr = "defined(" + formulaStr + ")";
            } else if (type == CppBlock.Type.IFNDEF) {
                formulaStr = "!defined(" + formulaStr + ")";
            }
            
            try {
                formula = cppConditionParser.parse(formulaStr);
            } catch (ExpressionFormatException e) {
                throw makeException(node, "Can't parse <" + node.getNodeName() + "> condition", e);
            }
        } else {
            formula = null;
            nestedStartIndex = 2;
        }
        
        CppBlock previousBlock = null;
        if (type == CppBlock.Type.ELSE || type == CppBlock.Type.ELSEIF) {
            Node previousNode = (Node) node.getUserData(PREVIOUS_CPP_BLOCK);
            if (previousNode == null) {
                throw makeException(node, "Can't find previous #if");
            }
            
            ISyntaxElement previousElement = (ISyntaxElement) previousNode.getUserData(CONVERTED);
            if (!(previousElement instanceof CppBlock)) {
                throw makeException(node, "Can't find previous #if");
            }
            
            previousBlock = (CppBlock) previousElement;
            
            // first element is the #if, so use it's immediate condition
            CppBlock sibling = previousBlock.getSibling(0);
            if (sibling.getType() != CppBlock.Type.IF && sibling.getType() != CppBlock.Type.IFDEF
                    && sibling.getType() != CppBlock.Type.IFNDEF) {
                throw makeException(node, "First sibling must be a <cpp:if>, <cpp:ifdef> or <cpp:ifndef>, but is "
                        + sibling.getType());
            }
            if (sibling.getCondition() == null) {
                throw makeException(node, "Sibling must have condition");
            }
            Formula allPreviousNegated = new Negation(notNull(sibling.getCondition()));
            
            for (int i = 1; i < previousBlock.getSiblingCount(); i++) {
                sibling = previousBlock.getSibling(i);
                if (sibling.getType() != CppBlock.Type.ELSEIF) {
                    throw makeException(node, "Sibling must be an <cpp:elif>, but is" + sibling.getType());
                }
                
                if (!(sibling.getCondition() instanceof Conjunction) && sibling.getCondition() == null) {
                    throw makeException(node, "Previous <cpp:elif> condition must have a conjunction as top-level");
                }
                Conjunction siblingCondition = (Conjunction) notNull(sibling.getCondition());
                allPreviousNegated = new Conjunction(allPreviousNegated, new Negation(siblingCondition.getRight()));
            }
            
            if (type == CppBlock.Type.ELSE) {
                formula = allPreviousNegated;
            } else if (type == CppBlock.Type.ELSEIF) {
                formula = new Conjunction(allPreviousNegated, notNull(formula));
            }
        }
        
        if (formula != null) {
            conditions.push(formula);
        }
        
        CppBlock result = new CppBlock(getPc(), formula, type);
        postCreation(result, node);
        
        if (type == CppBlock.Type.ELSE || type == CppBlock.Type.ELSEIF) {
            previousBlock = notNull(previousBlock);
            for (int i = 0; i < previousBlock.getSiblingCount(); i++) {
                CppBlock sibling = previousBlock.getSibling(i);
                if (sibling != result) {
                    sibling.addSibling(result);
                    result.addSibling(sibling);
                }
            }
        }
        result.addSibling(result);

        // don't convert content of #if 0
        if (formula != False.INSTANCE) {
            elementStack.push(result);
            
            NodeList children = node.getChildNodes();
            if (convertToCode) {
                result.addNestedElement(convertChildrenToCode(node, nestedStartIndex, children.getLength()));
                
            } else {
                for (int i = nestedStartIndex; i < children.getLength(); i++) {
                    Node child = notNull(children.item(i));
                    result.addNestedElement(convertSafe(child));
                }
            }
            
            elementStack.pop();
        }
        
        if (formula != null) {
            conditions.pop();
        }
        return result;
    }
    
    /**
     * Converts a comment.
     * 
     * @param node The XML node.
     * 
     * @return The converted AST element.
     * 
     * @throws FormatException If conversion fails.
     */
    private @NonNull Comment convertComment(@NonNull Node node) throws FormatException {
        Comment result = new Comment(getPc(), convertChildrenToCode(node));
        postCreation(result, node);
        if (result.getComment().containsErrorElement()) {
            result.setContainsErrorElement(true);
        }
        return result;
    }
    
    /**
     * Converts a statement.
     * 
     * @param node The XML node to convert.
     * 
     * @return The AST representation.
     * 
     * @throws FormatException If conversion fails.
     */
    private @NonNull SingleStatement convertStatement(@NonNull Node node) throws FormatException {
        SingleStatement.Type type;
        switch (node.getNodeName()) {
        
        case "expr_stmt": // falls through
        case "continue":  // falls through
        case "break":     // falls through
        case "goto":      // falls through 
        case "return":    // falls through
        case "empty_stmt":
            type = SingleStatement.Type.INSTRUCTION;
            break;
            
        case "function_decl":
            type = SingleStatement.Type.FUNCTION_DECLARATION;
            break;
            
        case "struct_decl": // falls through
        case "decl_stmt":
            type = SingleStatement.Type.DECLARATION;
            break;
            
        case "macro":
            type = SingleStatement.Type.PREPROCESSOR_MACRO;
            break;
            
        default:
            throw makeException(node, "Can't determine type of statement <" + node.getNodeName() + ">");
        }
        
        ICode code = convertChildrenToCode(node);
        
        SingleStatement result = new SingleStatement(getPc(), code, type);
        postCreation(result, node);
        if (code.containsErrorElement()) {
            result.setContainsErrorElement(true);
        }
        
        return result;
    }
    
    /**
     * Converts a label.
     * 
     * @param node The XML node.
     * 
     * @return The converted AST element.
     * 
     * @throws FormatException If conversion fails.
     */
    private @NonNull Label convertLabel(@NonNull Node node) throws FormatException {
        Label result = new Label(getPc(), convertChildrenToCode(node));
        postCreation(result, node);
        if (result.getCode().containsErrorElement()) {
            result.setContainsErrorElement(true);
        }
        return result;
    }
    
    /**
     * Converts a case or default statement.
     * 
     * @param node The XML node.
     * 
     * @return The converted AST element.
     * 
     * @throws FormatException If conversion fails.
     */
    private @NonNull CaseStatement convertCase(@NonNull Node node) throws FormatException {
        CaseStatement.CaseType type;
        switch (node.getNodeName()) {
        case "case":
            type = CaseType.CASE;
            break;
        case "default":
            type = CaseType.DEFAULT;
            break;
        default:
            throw makeException(node, "Can't determine case type of <" + node.getNodeName() + ">");
        }
        
        SwitchStatement parentSwitch = maybeNull(switchStack.peek());
        if (parentSwitch == null) {
            throw makeException(node, "Found <" + node.getNodeName() + "> without surounding <switch>");
        }
        
        int nestedIndex;
        if (type == CaseStatement.CaseType.CASE) {
            checkChildText(node, 0, "case");
            checkChildName(node, 1, "expr");
            checkChildText(node, 2, ":");
            nestedIndex = 3;
        } else {
            checkChildText(node, 0, "default:");
            nestedIndex = 1;
        }
        
        CaseStatement result = new CaseStatement(getPc(), convertChildrenToCode(node, 0, nestedIndex),
                type, parentSwitch);
        postCreation(result, node);
        parentSwitch.addCase(result);
        if (notNull(result.getCaseCondition()).containsErrorElement()) {
            result.setContainsErrorElement(true);
        }
        
        elementStack.push(result);
        NodeList children = node.getChildNodes();
        for (int i = nestedIndex; i < children.getLength(); i++) {
            result.addNestedElement(convertSafe(notNull(children.item(i))));
        }
        elementStack.pop();
        
        return result;
    }
    
    /**
     * Converts a block (CompoundStatement).
     * 
     * @param node The XML node to convert.
     * 
     * @return The AST representation.
     * 
     * @throws FormatException If conversion fails.
     */
    private @NonNull CompoundStatement convertBlock(@NonNull Node node) throws FormatException {
        CompoundStatement result = new CompoundStatement(getPc());
        postCreation(result, node);
        
        NodeList children = node.getChildNodes();
        
        int start = 0;
        int end = children.getLength();
        
        // pseudo blocks do not have { and }
        Node typeAttribute = node.getAttributes().getNamedItem("type");
        if (children.getLength() > 0 && (typeAttribute == null || !typeAttribute.getTextContent().equals("pseudo"))) {
            // first is "{", last is "}"
            
            Node first = children.item(0);
            if (first.getNodeType() == Node.TEXT_NODE && first.getTextContent().startsWith("{")) {
                start++;
            }
            Node last = children.item(children.getLength() - 1);
            if (last.getNodeType() == Node.TEXT_NODE && last.getTextContent().endsWith("}")) {
                end--;
            }
            
            // check special case: last is a reference to a "}" text node
            if (last.getNodeName().equals("kh:reference")) {
                Node referred = (Node) last.getUserData(NODE_REFERENCE);
                if (referred != null && referred.getNodeType() == Node.TEXT_NODE
                        && referred.getTextContent().equals("}")) {
                    end--;
                }
            }
        }
        
        elementStack.push(result);
        for (int i = start; i < end; i++) {
            Node child = notNull(children.item(i));
            result.addNestedElement(convertSafe(child));
        }
        elementStack.pop();
        
        return result;
    }
    
    /**
     * Converts a function.
     * 
     * @param node The XML node representing the function.
     * 
     * @return The AST function element. 
     * 
     * @throws FormatException If converting fails.
     */
    private @NonNull Function convertFunction(@NonNull Node node) throws FormatException {
        int i = 0;
        NodeList children = node.getChildNodes();
        while (i < children.getLength() && children.item(i).getNodeName().equals("specifier")) {
            i++;
        }
        
        checkChildName(node, i++, "type");
        int nameIndex = i;
        checkChildName(node, i++, "name");
        checkChildName(node, i++, "parameter_list");
        
        String name = notNull(notNull(children.item(nameIndex)).getTextContent());
        
        Function result = new Function(getPc(), name, convertChildrenToCode(node, 0, 3));
        postCreation(result, node);
        if (result.getHeader().containsErrorElement()) {
            result.setContainsErrorElement(true);
        }
        
        elementStack.push(result);
        // there now follow <decl_stmt> and <block> only
        for (; i < children.getLength(); i++) {
            ISyntaxElement converted = convertSafe(notNull(children.item(i)));
            result.addNestedElement(converted);
        }
        elementStack.pop();
        
        return result;
    }
    
    /**
     * Converts a loop.
     * 
     * @param node The XML node.
     * 
     * @return The converted AST element.
     * 
     * @throws FormatException If conversion fails.
     */
    private @NonNull LoopStatement convertLoop(@NonNull Node node) throws FormatException {
        int nestedStartIndex;
        int nestedEndIndex;
        int conditionIndex;
        NodeList children = node.getChildNodes();
        
        LoopType type;
        switch (node.getNodeName()) {
        case "for":
            checkChildText(node, 0, "for");
            checkChildName(node, 1, "control");
            nestedStartIndex = 2;
            nestedEndIndex = children.getLength();
            conditionIndex = 1;
            
            type = LoopType.FOR;
            break;
        case "while":
            checkChildText(node, 0, "while");
            checkChildName(node, 1, "condition");
            nestedStartIndex = 2;
            nestedEndIndex = children.getLength();
            conditionIndex = 1;
            
            type = LoopType.WHILE;
            break;
        case "do":
            checkChildText(node, 0, "do");
            checkChildText(node, children.getLength() - 3, "while");
            checkChildName(node, children.getLength() - 2, "condition");
            checkChildText(node, children.getLength() - 1, ";");
            nestedStartIndex = 1;
            nestedEndIndex = children.getLength() - 3;
            conditionIndex = children.getLength() - 2;
            
            type = LoopType.DO_WHILE;
            break;
        default:
            throw makeException(node, "Can't determine loop type of <" + node.getNodeName() + ">");
        }
        
        ICode condition = convertChildrenToCode(node, conditionIndex, conditionIndex + 1);
        LoopStatement result = new LoopStatement(getPc(), condition, type);
        postCreation(result, node);
        if (condition.containsErrorElement()) {
            result.setContainsErrorElement(true);
        }
        
        elementStack.push(result);
        for (int i = nestedStartIndex; i < nestedEndIndex; i++) {
            result.addNestedElement(convertSafe(notNull(children.item(i))));
        }
        elementStack.pop();
        
        return result;
    }
    
    /**
     * Converts an if statement.
     * 
     * @param node The XML node.
     * 
     * @return The converted AST element.
     * 
     * @throws FormatException If conversion fails.
     */
    private @NonNull BranchStatement convertIf(@NonNull Node node) throws FormatException {
        checkChildText(node, 0, "if");
        checkChildName(node, 1, "condition");
        
        BranchStatement result = new BranchStatement(getPc(), BranchStatement.Type.IF,
                convertChildrenToCode(node, 0, 2));
        postCreation(result, node);
        if (notNull(result.getIfCondition()).containsErrorElement()) {
            result.setContainsErrorElement(true);
        }
        
        result.addSibling(result);
        
        elementStack.push(result);
        ifStack.push(result);
        
        NodeList children = node.getChildNodes();
        for (int i = 2; i < children.getLength(); i++) {
            Node child = notNull(children.item(i));
            if (child.getNodeName().equals("then")) {
                NodeList thenChildren = child.getChildNodes();
                for (int j = 0; j < thenChildren.getLength(); j++) {
                    result.addNestedElement(convertSafe(notNull(thenChildren.item(j))));
                }
                
            } else {
                result.addNestedElement(convertSafe(child));
            }
        }
        
        ifStack.pop();
        elementStack.pop();

        for (int i = 1; i < result.getSiblingCount(); i++) {
            BranchStatement sibling = result.getSibling(i);
            for (int j = 0; j < result.getSiblingCount(); j++) {
                sibling.addSibling(result.getSibling(j));
            }
        }
        
        return result;
    }
    
    /**
     * Converts an else-if statement.
     * 
     * @param node The XML node.
     * 
     * @return The converted AST element.
     * 
     * @throws FormatException If conversion fails.
     */
    private @NonNull BranchStatement convertElseIf(@NonNull Node node) throws FormatException {
        if (ifStack.isEmpty()) {
            throw makeException(node, "Found <elseif> outside of <if>");
        }
        
        checkChildText(node, 0, "else");
        checkChildName(node, 1, "if");
        NodeList children = node.getChildNodes();
        if (children.getLength() > 2) {
            throw makeException(node, "Found <elseif> with more than \"else\" and <if> in it");
        }

        Node ifChild = notNull(children.item(1));
        checkChildText(ifChild, 0, "if");
        checkChildName(ifChild, 1, "condition");
            
        BranchStatement result = new BranchStatement(getPc(), BranchStatement.Type.ELSE_IF,
                convertChildrenToCode(ifChild, 0, 2));
        postCreation(result, node);
        if (notNull(result.getIfCondition()).containsErrorElement()) {
            result.setContainsErrorElement(true);
        }
        
        notNull(ifStack.peek()).addSibling(result);

        elementStack.push(result);
        NodeList ifChildChildren = ifChild.getChildNodes();
        for (int i = 2; i < ifChildChildren.getLength(); i++) {
            Node child = notNull(ifChildChildren.item(i));
            if (child.getNodeName().equals("then")) {
                NodeList thenChildren = child.getChildNodes();
                for (int j = 0; j < thenChildren.getLength(); j++) {
                    result.addNestedElement(convertSafe(notNull(thenChildren.item(j))));
                }
                
            } else {
                result.addNestedElement(convertSafe(child));
            }
        }
        elementStack.pop();
        
        return result;
    }
    
    /**
     * Converts an else statement.
     * 
     * @param node The XML node.
     * 
     * @return The converted AST element.
     * 
     * @throws FormatException If conversion fails.
     */
    private @NonNull BranchStatement convertElse(@NonNull Node node) throws FormatException {
        if (ifStack.isEmpty()) {
            throw makeException(node, "Found <else> outside of <if>");
        }
        checkChildText(node, 0, "else");
        
        BranchStatement result = new BranchStatement(getPc(), BranchStatement.Type.ELSE, null);
        postCreation(result, node);
        
        notNull(ifStack.peek()).addSibling(result);
        
        elementStack.push(result);
        NodeList children = node.getChildNodes();
        for (int i = 1; i < children.getLength(); i++) {
            result.addNestedElement(convertSafe(notNull(children.item(i))));
        }
        elementStack.pop();
        
        return result;
    }
    
    /**
     * Converts a switch statement.
     * 
     * @param node The XML node.
     * 
     * @return The converted AST element.
     * 
     * @throws FormatException If conversion fails.
     */
    private @NonNull SwitchStatement convertSwitch(@NonNull Node node) throws FormatException {
        checkChildText(node, 0, "switch");
        checkChildName(node, 1, "condition");
        
        SwitchStatement result = new SwitchStatement(getPc(), convertChildrenToCode(node, 0, 2));
        postCreation(result, node);
        if (result.getHeader().containsErrorElement()) {
            result.setContainsErrorElement(true);
        }
        
        elementStack.push(result);
        switchStack.push(result);
        
        NodeList children = node.getChildNodes();
        for (int i = 2; i < children.getLength(); i++) {
            result.addNestedElement(convertSafe(notNull(children.item(i))));
        }
        switchStack.pop();
        elementStack.pop();
        
        return result;
    }
    
    /**
     * Converts a type definition.
     * 
     * @param node The XML node.
     * 
     * @return The converted AST element.
     * 
     * @throws FormatException If conversion fails.
     */
    private @NonNull TypeDefinition convertTypedef(@NonNull Node node) throws FormatException {
        TypeDefType type;
        switch (node.getNodeName()) {
        case "enum":
            type = TypeDefType.ENUM;
            break;
        case "struct":
            type = TypeDefType.STRUCT;
            break;
        case "union":
            type = TypeDefType.UNION;
            break;
        case "typedef":
            type = TypeDefType.TYPEDEF;
            break;
        default:
            throw makeException(node, "Can't determine typedef type of <" + node.getNodeName() + ">");
        }
        
        List<@NonNull Node> nestedBlockNodes = new LinkedList<>();
        String ignoreChild = null;
        if (type != TypeDefType.ENUM) {
            for (Node block : getChildren(node, "block")) {
                nestedBlockNodes.add(block);
            }
            ignoreChild = "block";
        }
        
        ICode code;
        if (ignoreChild != null) {
            code = convertChildrenToCode(node, ignoreChild);
        } else {
            code = convertChildrenToCode(node);
        }
        
        TypeDefinition result = new TypeDefinition(getPc(), code, type);
        postCreation(result, node);
        if (code.containsErrorElement()) {
            result.setContainsErrorElement(true);
        }
        
        elementStack.push(result);
        for (Node nestedBlockNode : nestedBlockNodes) {
            result.addNestedElement(convertSafe(nestedBlockNode));
        }
        elementStack.pop();
        
        
        return result;
    }
    
    /**
     * Converts an extern statement.
     * 
     * @param node The XML node.
     * 
     * @return The converted AST element.
     * 
     * @throws FormatException If conversion fails.
     */
    private @NonNull ISyntaxElement convertExtern(@NonNull Node node) throws FormatException {
        // just convert the last element (either a <block> or a <function>)
        if (node.getLastChild() == null) {
            throw makeException(node, "<extern> without children");
        }
        return convertSafe(notNull(node.getLastChild()));
    }
    
    /**
     * Converts a reference element.
     * 
     * @param node The XML node to convert.
     * 
     * @return The converted AST element.
     * 
     * @throws FormatException If conversion fails.
     */
    private @NonNull ReferenceElement convertReference(@NonNull Node node) throws FormatException {
        Node referred = (Node) node.getUserData(NODE_REFERENCE);
        if (referred == null) {
            throw makeException(node, "No reference set for <kh:reference>");
        }
        
        ISyntaxElement converted = (ISyntaxElement) referred.getUserData(CONVERTED);
        
        @SuppressWarnings("null") // we will fix the case converted == null in resolveReferencesToResolve()
        ReferenceElement result = new ReferenceElement(getPc(), converted);
        postCreation(result, node);
        
        if (converted == null) {
            // reference was not yet converted, queue up for post-processing
            referencesToResolve.putIfAbsent(referred, new LinkedList<>());
            notNull(referencesToResolve.get(referred)).add(result);
        }
        
        return result;
    }
    
    /**
     * Converts the nested child elements of the given parent to node to {@link Code}.
     * 
     * @param parent The parent element of the XML nodes to convert.
     * 
     * @return The child nodes represented as {@link Code}.
     */
    private @NonNull ICode convertChildrenToCode(@NonNull Node parent) {
        return convertChildrenToCode(parent, 0, parent.getChildNodes().getLength());
    }
    
    /**
     * Converts the nested child elements of the given parent to node to {@link Code}.
     * 
     * @param parent The parent element of the XML nodes to convert.
     * @param ignoreNodes Ignore all direct child nodes with any of these node names.
     * 
     * @return The child nodes represented as {@link Code}.
     */
    private @NonNull ICode convertChildrenToCode(@NonNull Node parent, @NonNull String ... ignoreNodes) {
        return convertChildrenToCode(parent, 0, parent.getChildNodes().getLength(), ignoreNodes);
    }
    
    /**
     * Converts the nested child elements of the given parent to node to {@link Code}.
     * 
     * @param parent The parent element of the XML nodes to convert.
     * @param startIndex The index of the first child to convert, inclusive.
     * @param endIndex The index of the last child to convert, exclusive.
     * @param ignoreNodes Ignore all direct child nodes with any of these node names.
     * 
     * @return The child nodes represented as {@link Code}.
     */
    private @NonNull ICode convertChildrenToCode(@NonNull Node parent, int startIndex, int endIndex,
            @NonNull String ... ignoreNodes) {
        
        NodeList children = parent.getChildNodes();
        @NonNull Node[] nodes = new @NonNull Node[endIndex - startIndex];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = notNull(children.item(i + startIndex));
        }
        
        return convertToCode(nodes, ignoreNodes);
    }
    
    /**
     * Converts the given XML nodes to {@link Code}.
     * 
     * @param nodes The XML nodes to convert.
     * @param ignoreNodes All nodes in the list with this nodename are ignored.
     * 
     * @return The given nodes converted to {@link Code}.
     */
    // CHECKSTYLE:OFF // TODO: metho too long
    private @NonNull ICode convertToCode(@NonNull Node @NonNull [] nodes, @NonNull String ... ignoreNodes) {
   // CHECKSTYLE:ON
        StringBuilder str = new StringBuilder();
        int strLineStart = -1;
        int strLineEnd = -1;
        List<@NonNull ICode> list = new LinkedList<>();
        
        outer: for (Node node : nodes) {
            for (String ignoreNode : ignoreNodes) {
                if (node.getNodeName().equals(ignoreNode)) {
                    continue outer;
                }
            }
            if (node.getNodeType() == Node.TEXT_NODE) {
                if (str.length() > 0) {
                    str.append(' ');
                } else {
                    strLineStart = (int) node.getUserData(LINE_START);
                }
                strLineEnd = (int) node.getUserData(LINE_END);
                str.append(node.getTextContent().trim());
                
            } else if (node.getNodeName().startsWith("cpp:if") || node.getNodeName().startsWith("cpp:el")) {
                if (str.length() > 0) {
                    Code untilThisPart = new Code(getPc(), notNull(str.toString()));
                    untilThisPart.setSourceFile(baseFile);
                    untilThisPart.setCondition(conditions.peek());
                    untilThisPart.setLineStart(strLineStart);
                    untilThisPart.setLineEnd(strLineEnd);
                    str = new StringBuilder();
                    strLineStart = -1;
                    strLineEnd = -1;
                    list.add(untilThisPart);
                }
                ICode convertedIf;
                try {
                    convertedIf = convertCppIf(node, true);
                } catch (FormatException e) {
                    convertedIf = new ErrorElement(getPc(), notNull(e.getMessage()));
                    postCreation(convertedIf, node);
                    elementStack.push(convertedIf);
                    markErrorElement();
                    
                    convertedIf.addNestedElement(convertChildrenToCode(node));
                    
                    elementStack.pop();
                }
                list.add(convertedIf);
                
            } else {
                ICode nested = convertChildrenToCode(node, 0, node.getChildNodes().getLength());

                if (str.length() > 0) {
                    Code untilThisPart = new Code(getPc(), notNull(str.toString()));
                    untilThisPart.setSourceFile(baseFile);
                    untilThisPart.setCondition(conditions.peek());
                    untilThisPart.setLineStart(strLineStart);
                    untilThisPart.setLineEnd(strLineEnd);
                    str = new StringBuilder();
                    strLineStart = -1;
                    strLineEnd = -1;
                    list.add(untilThisPart);
                }
                
                if (nested instanceof CodeList) {
                    CodeList nestedList = (CodeList) nested;
                    for (ISyntaxElement inner : nestedList) {
                        list.add((ICode) inner);
                    }
                    
                } else {
                    list.add(nested);
                }
            }
        }
        
        if (str.length() > 0) {
            Code untilThisPart = new Code(getPc(), notNull(str.toString()));
            untilThisPart.setSourceFile(baseFile);
            untilThisPart.setCondition(conditions.peek());
            untilThisPart.setLineStart(strLineStart);
            untilThisPart.setLineEnd(strLineEnd);
            list.add(untilThisPart);
        }
        
        ICode result;
        if (list.size() == 0) {
            result = new Code(getPc(), "");
            
        } else if (list.size() == 1) {
            result = notNull(list.get(0));
            
        } else {
            // If a Code follows a Code, join them together
            for (int i = 0; i < list.size() - 1; i++) {
                if (list.get(i) instanceof Code) {
                    // Check which elements belong together
                    Code first = (Code) list.get(i);
                    int endIndex = i;
                    Code last = first;
                    boolean containsErrorElement = first.containsErrorElement();
                    
                    for (int j = i + 1; j < list.size(); j++) {
                        if (!(list.get(j) instanceof Code)
                                || !first.getPresenceCondition().equals(notNull(list.get(j)).getPresenceCondition())) {
                            break;
                        }
                        endIndex = j;
                        last = (Code) list.get(j);
                        containsErrorElement |= last.containsErrorElement();
                    }
                    if (i != endIndex) {
                        // Merge elements together
                        StringJoiner sj = new StringJoiner(" ");
                        while (endIndex >= i) {
                            Code code = (Code) list.get(i);
                            sj.add(code.getText());
                            list.remove(i);
                            endIndex--;
                        }
                        Code newCode = new Code(first.getPresenceCondition(), notNull(sj.toString()));
                        newCode.setSourceFile(baseFile);
                        newCode.setCondition(first.getCondition());
                        newCode.setLineStart(first.getLineStart());
                        newCode.setLineEnd(last.getLineEnd());
                        newCode.setContainsErrorElement(containsErrorElement);
                        
                        list.add(i, newCode);
                    }
                }
            }
            
            if (list.size() > 1) {
                result = new CodeList(getPc());
                result.setSourceFile(baseFile);
                result.setCondition(conditions.peek());
                result.setLineStart(notNull(list.get(0)).getLineStart());
                result.setLineEnd(notNull(list.get(list.size() - 1)).getLineEnd());
                boolean containsErrorElement = false;
                for (ICode element : list) {
                    result.addNestedElement(element);
                    containsErrorElement |= element.containsErrorElement();
                }
                result.setContainsErrorElement(containsErrorElement);
                
            } else {
                result = notNull(list.get(0));
            }
        }
        
        return result;
    }
    
    /**
     * Call this after creating an AST element. This method sets the condition, start and end lines properly.
     * 
     * @param element The element that was just created.
     * @param node The XML node that this element stems from.
     */
    private void postCreation(@NonNull ISyntaxElement element, @NonNull Node node) {
        element.setSourceFile(baseFile);
        element.setCondition(conditions.peek());
        element.setLineStart((int) node.getUserData(LINE_START));
        element.setLineEnd((int) node.getUserData(LINE_END));
        
        node.setUserData(CONVERTED, element, null);
    }
    
    /**
     * Triggered when an {@link ErrorElement} is created. This marks all elements in {@link #elementStack} as
     * {@link ISyntaxElement#containsErrorElement()}.
     */
    private void markErrorElement() {
        for (ISyntaxElement element : elementStack) {
            element.setContainsErrorElement(true);
        }
    }
    
    /**
     * Calculates the current presence condition. Use this in the constructor of new AST elements.
     * 
     * @return The current presence condition.
     */
    private @NonNull Formula getPc() {
        Formula result;
        if (conditions.size() == 1) {
            result = notNull(conditions.peek());
        } else {
            Iterator<@NonNull Formula> it = conditions.iterator();
            result = notNull(it.next());
            while (it.hasNext()) {
                Formula next = notNull(it.next());
                if (next != True.INSTANCE) {
                    result = new Conjunction(result, next);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Check that the given child has the given expected name.
     * 
     * @param parent The parent node.
     * @param index The child index to check.
     * @param expectedNames The list of expected names.
     * 
     * @throws FormatException If the element doesn't exist or has none of the expected names.
     */
    private void checkChildName(@NonNull Node parent, int index, @NonNull String ... expectedNames)
            throws FormatException {
        
        Node child = parent.getChildNodes().item(index);
        if (child == null) {
            throw makeException(parent, "Parent has no child with index " + index);
        }
        
        boolean matchesAny = false;
        for (String expectedName : expectedNames) {
            if (child.getNodeName().equals(expectedName)) {
                matchesAny = true;
                break;
            }
        }
        if (!matchesAny) {
            throw makeException(child, "Expected child with node name <" + expectedNames[0] + ">, but got <"
                    + child.getNodeName() + ">");
        }
    }
    
    /**
     * Check that the given child is a text node and has the given text content.
     * 
     * @param parent The parent node.
     * @param index The child index to check.
     * @param expectedText The expected text content.
     * 
     * @throws FormatException If the element doesn't exist, isn't a text node, or doesn't have the expected text.
     */
    private void checkChildText(@NonNull Node parent, int index, @NonNull String expectedText) throws FormatException {
        Node child = parent.getChildNodes().item(index);
        if (child == null) {
            throw makeException(parent, "Parent has no child with index " + index);
        }
        
        if (child.getNodeType() != Node.TEXT_NODE) {
            throw makeException(child, "Expected text node as child with index " + index);
        }
        
        if (!child.getTextContent().equals(expectedText)) {
            throw makeException(child, "Expected child with text \"" + expectedText + "\", but got \""
                    + child.getTextContent() + "\"");
        }
    }
    
    /**
     * Gets all direct child nodes under parent with the given name.
     * 
     * @param parent The parent node of the children to search.
     * @param name The name to search.
     * 
     * @return A list with all found nodes. Empty if no nodes are found.
     */
    private static @NonNull List<@NonNull Node> getChildren(@NonNull Node parent, @NonNull String name) {
        List<@NonNull Node> result = new LinkedList<>();
        
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals(name)) {
                result.add(child);
            }
        }
        
        return result;
    }
    
    /**
     * Creates an exception with properly points to the offending code.
     * 
     * @param cause The node that caused the exception. Used for locating the error.
     * @param message The message to print into the exception.
     * 
     * @return The created exception.
     */
    private @NonNull FormatException makeException(@NonNull Node cause, @NonNull String message) {
        Object line = cause.getUserData(LINE_START);
        String prefix;
        if (line != null && line instanceof Integer) {
            prefix = baseFile.getPath() + ":" + line;
        } else {
            prefix = baseFile.getPath();
        }
        return new FormatException(prefix + " " + message);
    }
    
    /**
     * Creates an exception with properly points to the offending code.
     * 
     * @param cause The node that caused the exception. Used for locating the error.
     * @param message The message to print into the exception.
     * @param nested A nested exception.
     * 
     * @return The created exception.
     */
    private @NonNull FormatException makeException(@NonNull Node cause, @NonNull String message,
            @NonNull Throwable nested) {
        Object line = cause.getUserData(LINE_START);
        String prefix;
        if (line != null && line instanceof Integer) {
            prefix = baseFile.getPath() + ":" + line;
        } else {
            prefix = baseFile.getPath();
        }
        return new FormatException(prefix + " " + message, nested);
    }
    
}
