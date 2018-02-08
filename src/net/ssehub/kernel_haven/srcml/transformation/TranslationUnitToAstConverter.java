package net.ssehub.kernel_haven.srcml.transformation;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import net.ssehub.kernel_haven.code_model.ast.BranchStatement;
import net.ssehub.kernel_haven.code_model.ast.CaseStatement;
import net.ssehub.kernel_haven.code_model.ast.CaseStatement.CaseType;
import net.ssehub.kernel_haven.code_model.ast.Code;
import net.ssehub.kernel_haven.code_model.ast.CodeList;
import net.ssehub.kernel_haven.code_model.ast.Comment;
import net.ssehub.kernel_haven.code_model.ast.CompoundStatement;
import net.ssehub.kernel_haven.code_model.ast.CppBlock;
import net.ssehub.kernel_haven.code_model.ast.CppBlock.Type;
import net.ssehub.kernel_haven.code_model.ast.CppStatement;
import net.ssehub.kernel_haven.code_model.ast.File;
import net.ssehub.kernel_haven.code_model.ast.Function;
import net.ssehub.kernel_haven.code_model.ast.ICode;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.code_model.ast.Label;
import net.ssehub.kernel_haven.code_model.ast.LoopStatement;
import net.ssehub.kernel_haven.code_model.ast.LoopStatement.LoopType;
import net.ssehub.kernel_haven.code_model.ast.SingleStatement;
import net.ssehub.kernel_haven.code_model.ast.SwitchStatement;
import net.ssehub.kernel_haven.code_model.ast.TypeDefinition;
import net.ssehub.kernel_haven.code_model.ast.TypeDefinition.TypeDefType;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Translates the {@link ITranslationUnit}-structure into a {@link ISyntaxElement} (AST). This is the final parsing step.
 * @author Adam
 * @author El-Sharkawy
 *
 */
public class TranslationUnitToAstConverter {

//    private static final Logger LOGGER = Logger.get();
    
    private @NonNull Deque<@NonNull Formula> cppPresenceConditions;
    private @NonNull Deque<@NonNull Formula> cppEffectiveConditions;
    
    /**
     * Holds all switch statements while preserving nesting. This is needed to find the correct switch a
     * {@link CaseStatement} belongs to.
     * 
     * @see #convertCaseStatement(TranslationUnit, int, CaseType)
     */
    private @NonNull Deque<@NonNull SwitchStatement> switchs;
    
    private java.io. @NonNull File sourceFile;
    
    public TranslationUnitToAstConverter(java.io. @NonNull File sourceFile) {
        cppPresenceConditions = new ArrayDeque<>();
        cppEffectiveConditions = new ArrayDeque<>();
        switchs = new ArrayDeque<>();
        this.sourceFile = sourceFile;
    }
    
    /**
     * Returns the current presence condition, considering the conditions of all surrounding CPP blocks, but won't pop
     * any elements from the stack.
     * @return The current presence condition (may be {@link True#INSTANCE} in case of no presence condition).
     */
    private @NonNull Formula getPc() {
        return cppPresenceConditions.isEmpty() ? True.INSTANCE : notNull(cppPresenceConditions.peek());
    }
    
    private @NonNull Formula getEffectiveCondition() {
        return cppEffectiveConditions.isEmpty() ? True.INSTANCE : notNull(cppEffectiveConditions.peek());
    }
    
    /**
     * Will compute a new presence condition based on the given formula on the current state of the stack and pushes
     * the result on top of the stack.
     * @param cppCondition
     */
    private void pushFormula(@NonNull Formula cppCondition) {
        cppEffectiveConditions.push(cppCondition);
        if (cppPresenceConditions.isEmpty()) {
            cppPresenceConditions.push(cppCondition);
        } else {
            // TODO SE: Use of a cache?
            cppPresenceConditions.push(new Conjunction(cppPresenceConditions.peek(), cppCondition));
        }
    }
    
    private void popFormula() {
        cppPresenceConditions.pop();
        cppEffectiveConditions.pop();
    }
    
    /**
     * Translates the {@link ITranslationUnit}-structure into a {@link ISyntaxElement} (AST).
     * @param element The root element representing the complete file.
     * @return The translated AST still representing the complete file.
     */
    public @NonNull ISyntaxElement convert(@NonNull ITranslationUnit element) throws FormatException {
        
        if (element instanceof TranslationUnit) {
            return convertTranslationUnit((TranslationUnit) element);
        } else if (element instanceof PreprocessorIf || element instanceof PreprocessorElse) {
            return convertPreprocessorBlock((PreprocessorBlock) element);
        }
        
        throw ExceptionUtil.makeException("Illegal element " + element.getClass().getName(), element);
    }
    
    private @NonNull ISyntaxElement convertTranslationUnit(@NonNull TranslationUnit unit) throws FormatException {
        Formula pc = getPc();
        
        switch (unit.getType()) {
        
        case "function_decl":   // falls through
        case "decl_stmt":       // falls through
        case "expr_stmt":       // falls through
        case "continue":        // falls through
        case "break":           // falls through
        case "goto":            // falls through 
        case "return":          // falls through 
        case "empty_stmt": 
            SingleStatement singleStatement = new SingleStatement(pc, makeCode(unit, 0, unit.size() - 1));
            singleStatement.setSourceFile(sourceFile);
            singleStatement.setCondition(getEffectiveCondition());
            singleStatement.setLineStart(unit.getStartLine());
            singleStatement.setLineEnd(unit.getEndLine());
            return singleStatement;
        
        case "label":
            Label label = new Label(pc, makeCode(unit, 0, unit.size() - 1));
            label.setSourceFile(sourceFile);
            label.setCondition(getEffectiveCondition());
            return label;
        
        case "comment":
            Comment comment = new Comment(pc, makeCode(unit, 0, unit.size() - 1));
            comment.setSourceFile(sourceFile);
            comment.setCondition(getEffectiveCondition());
            return comment;
        
        case "for":
            // Last nested is the loop block, everything before is the condition
            return createLoop(unit, LoopType.FOR, unit.size() - 1, 0, unit.size() - 2);
            
        case "while":
            // Last nested is the loop block, everything before is the condition
            return createLoop(unit, LoopType.WHILE, unit.size() - 1, 0, unit.size() - 2);
        
        case "do":
            // 2nd is block everything after is condition, we skip last element (a semicolon)
            return createLoop(unit, LoopType.DO_WHILE, 1, 3, unit.size() - 2);
        
        case "if":
            // (Multiple) statements come after last condition element (CodeUnit)
            int lastConditionElement = -1;
            for (int i = 0; i < unit.size() && lastConditionElement == -1; i++) {
                if (!(unit.getNestedElement(i) instanceof CodeUnit)) {
                    lastConditionElement = (i - 1);
                }
            }
            BranchStatement ifStatement = new BranchStatement(pc, BranchStatement.Type.IF,
                    makeCode(unit, 0, lastConditionElement));
            ifStatement.setSourceFile(sourceFile);
            ifStatement.setCondition(getEffectiveCondition());
            ifStatement.addSibling(ifStatement);
            for (int i = lastConditionElement + 1; i < unit.size(); i++) {
                ISyntaxElement child = convert(unit.getNestedElement(i));
                
                addIfSibling(child, ifStatement);
                
                ifStatement.addNestedElement(child);
            }
            
            // add sibling references to all siblings
            for (int i = 1; i < ifStatement.getSiblingCount(); i++) {
                ifStatement.getSibling(i).addSibling(ifStatement);
                for (int j = 1; j < ifStatement.getSiblingCount(); j++) {
                    ifStatement.getSibling(i).addSibling(ifStatement.getSibling(j));
                }
            }
            
            return ifStatement;
            
        case "elseif":
            // Determine last code element
            int lastCodeElement = -1;
            while (unit.size() > (lastCodeElement + 1) &&
                !(unit.getNestedElement((lastCodeElement + 1)) instanceof TranslationUnit) &&
                !(unit.getNestedElement((lastCodeElement + 1)) instanceof PreprocessorBlock)) {
                
                lastCodeElement++;
            }
            
            if (lastCodeElement >= 0) {
                ICode condition = makeCode(unit, 0, lastCodeElement);
                BranchStatement elifBlock = new BranchStatement(pc, BranchStatement.Type.ELSE_IF, condition);
                elifBlock.setSourceFile(sourceFile);
                elifBlock.setCondition(getEffectiveCondition());
                for (int i = 0; i < unit.size(); i++) {
                    ITranslationUnit child = unit.getNestedElement(i);
                    if (child instanceof CodeUnit) {
                        // ignore { and }
                    } else {
                        elifBlock.addNestedElement(convert(child));
                    }
                }
                return elifBlock;
            } else {
                throw ExceptionUtil.makeException("Unexpected structure of elseif-statement: " + unit.toString(), unit);
            }
            
        case "else":
            BranchStatement elseBlock = new BranchStatement(pc, BranchStatement.Type.ELSE, null);
            elseBlock.setSourceFile(sourceFile);
            elseBlock.setCondition(getEffectiveCondition());
            for (int i = 0; i < unit.size(); i++) {
                ITranslationUnit child = unit.getNestedElement(i);
                if (child instanceof CodeUnit) {
                    // ignore { and }
                } else {
                    elseBlock.addNestedElement(convert(child));
                }
            }
            
            return elseBlock;
        
        case "enum":
            return createTypeDef(unit, pc, TypeDefType.ENUM);
            
        case "struct":
            return createTypeDef(unit, pc, TypeDefType.STRUCT);
            
        case "typedef":
            return createTypeDef(unit, pc, TypeDefType.TYPEDEF);
            
        case "union":
            return createTypeDef(unit, pc, TypeDefType.UNION);
            
        case "unit": {
            File file = new File(pc);
            file.setSourceFile(sourceFile);
            file.setCondition(getEffectiveCondition());
            for (int i = 0; i < unit.size(); i++) {
                file.addNestedElement(convert(unit.getNestedElement(i)));
            }
            
            return file;
        }
        
        case "function": {
            Function f = new Function(pc, notNull(unit.getFunctionName()), // notNull, since this is a function
                    makeCode(unit, 0, unit.size() - 2)); // last nested is the function block
            f.setSourceFile(sourceFile);
            f.setCondition(getEffectiveCondition());
            
            f.setLineStart(unit.getStartLine());
            f.setLineEnd(unit.getEndLine());
            
            f.addNestedElement(convert(unit.getNestedElement(unit.size() - 1)));
            
            return f;
        }
        
        case "block": {
            CompoundStatement block = new CompoundStatement(pc);
            block.setSourceFile(sourceFile);
            block.setCondition(getEffectiveCondition());
            for (int i = 0; i < unit.size(); i++) {
                ITranslationUnit child = unit.getNestedElement(i);
                if (child instanceof CodeUnit) {
                    // ignore { and }
                } else {
                    block.addNestedElement(convert(child));
                }
            }
            
            return block;
        }
        
        case "switch":
            /*
             * Last element is switch-Block, before comes the condition
             */
            SwitchStatement switchStatement = new SwitchStatement(getPc(), makeCode(unit, 0, unit.size() - 2));
            switchStatement.setSourceFile(sourceFile);
            switchStatement.setCondition(getEffectiveCondition());
            switchs.push(switchStatement);
            switchStatement.addNestedElement(convert(unit.getNestedElement(unit.size() - 1)));
            switchs.pop();
            return switchStatement;
        
        case "case":
            /*
             * 3-4 Elements belong to the condition, afterwards come nested statements
             */
            int lastElementIndex = 2;
            boolean colonFound = false;
            
            // Search the colon
            while (unit.size() > lastElementIndex && !colonFound) {
                ITranslationUnit nested = unit.getNestedElement(lastElementIndex);
                if (nested instanceof CodeUnit && !":".equals(((CodeUnit)nested).getCode())) {
                    lastElementIndex++;
                } else {
                    colonFound = true;                    
                }
            }
            return convertCaseStatement(unit, lastElementIndex, CaseType.CASE);
        
        case "default":
            /*
             * First Element is the condition, afterwards come nested statements
             */
            return convertCaseStatement(unit, 0, CaseType.DEFAULT);
            
            
        case "cpp:define":
            return convertCppStatement(unit, CppStatement.Type.DEFINE);

        case "cpp:undef":
            return convertCppStatement(unit, CppStatement.Type.UNDEF);
            
        case "cpp:include":
            return convertCppStatement(unit, CppStatement.Type.INCLUDE);
            
        case "cpp:pragma":
            return convertCppStatement(unit, CppStatement.Type.PRAGMA);
            
        case "cpp:error":
            return convertCppStatement(unit, CppStatement.Type.ERROR);
            
        case "cpp:warning":
            return convertCppStatement(unit, CppStatement.Type.WARNING);
            
        case "cpp:line":
            return convertCppStatement(unit, CppStatement.Type.LINE);
            
        case "cpp:empty":
            return convertCppStatement(unit, CppStatement.Type.EMPTY);
        }

        throw ExceptionUtil.makeException("Unexpected unit type: " + unit.getType(), unit);
    }
    
    private @NonNull CppStatement convertCppStatement(@NonNull TranslationUnit unit, CppStatement. @NonNull Type type)
            throws FormatException {
        ICode expression = null;
        // first two strings inside are # and the type, skip these
        if (unit.size() > 2) {
            expression = makeCode(unit, 2, unit.size() - 1);
        }
        
        CppStatement statement = new CppStatement(getPc(), type, expression);
        statement.setSourceFile(sourceFile);
        statement.setCondition(getEffectiveCondition());
        return statement;
    }

    private @NonNull CaseStatement convertCaseStatement(@NonNull TranslationUnit unit, int condEndIndex,
            @NonNull CaseType type) throws FormatException {
        
        if (switchs.isEmpty()) {
            throw ExceptionUtil.makeException("Found " + type + " outside of switch ", unit);
        }
        SwitchStatement switchStmt = notNull(switchs.peek());
        CaseStatement caseStatement = new CaseStatement(getPc(), makeCode(unit, 0, condEndIndex), type, switchStmt);
        caseStatement.setSourceFile(sourceFile);
        caseStatement.setCondition(getEffectiveCondition());
        switchStmt.addCase(caseStatement);
        for (int i = condEndIndex + 1; i < unit.size(); i++) {
            caseStatement.addNestedElement(convert(unit.getNestedElement(i)));
        }
        return caseStatement;
    }

    private @NonNull TypeDefinition createTypeDef(@NonNull TranslationUnit unit, @NonNull Formula pc,
            @NonNull TypeDefType type) throws FormatException {
        /*
         * Find last <block> element
         * (<block> refers to anything that isn't code, e.g. also <struct>)
         */
        int blockIndex = -1;
        for (int i = unit.size() - 1; i >= 0; i--) {
            if (!(unit.getNestedElement(i) instanceof CodeUnit)) {
                blockIndex = i;
                break;
            }
        }
        
        // heuristic: everything up to the <block> is relevant
        // TODO: this heuristic does not work for e.g.   typedef struct { int a; } MY_STRUCT;
        int endIndex;
        if (blockIndex == -1) {
            endIndex = unit.size() - 1;
        } else {
            endIndex = blockIndex - 1;
        }
        
        ICode code = makeCode(unit, 0, endIndex);
        ISyntaxElement nested = null;
        
        if (blockIndex != -1) {
            ITranslationUnit block = unit.getNestedElement(blockIndex);
            
            if (type == TypeDefType.ENUM) {
                // don't parse the <block> of enums as a statement
                ICode blockCode = makeCode(block, 0, block.size() - 1);
                
                // join blockCode with previous declaration
                if (code instanceof Code && blockCode instanceof Code) {
                    Code newCode = new Code(code.getPresenceCondition(), ((Code) code).getText() + ' '
                            + ((Code) blockCode).getText());
                    newCode.setCondition(code.getCondition());
                    newCode.setSourceFile(sourceFile);
                    
                    code = newCode;
                } else {
                    CodeList newCode = new CodeList(code.getPresenceCondition());
                    newCode.setCondition(code.getCondition());
                    newCode.setSourceFile(sourceFile);
                    
                    if (code instanceof CodeList) {
                        CodeList list = (CodeList) code;
                        for (int i = 0; i < list.getNestedElementCount(); i++) {
                            newCode.addNestedElement(list.getNestedElement(i));
                        }
                    } else {
                        newCode.addNestedElement(code);
                    }
                    
                    if (blockCode instanceof CodeList) {
                        CodeList list = (CodeList) blockCode;
                        for (int i = 0; i < list.getNestedElementCount(); i++) {
                            newCode.addNestedElement(list.getNestedElement(i));
                        }
                    } else {
                        newCode.addNestedElement(blockCode);
                    }
                    
                    code = newCode;
                }
                
            } else {
                // add the <block> as a parsed statement to everything else
                nested = convert(block);
            }
        }
        
        TypeDefinition typeDef = new TypeDefinition(pc, code, type);
        typeDef.setSourceFile(sourceFile);
        typeDef.setCondition(getEffectiveCondition());
        if (nested != null) {
            typeDef.addNestedElement(nested);
        }
        
        return typeDef;
    }

    private @NonNull LoopStatement createLoop(@NonNull TranslationUnit unit, @NonNull LoopType type, int blockIndex,
            int condStartIndex, int condEndIndex) throws FormatException {
        
        ICode condition = makeCode(unit, condStartIndex, condEndIndex);
        LoopStatement loop = new LoopStatement(getPc(),  condition, type);
        loop.setSourceFile(sourceFile);
        loop.setCondition(getEffectiveCondition());
        loop.addNestedElement(convert(unit.getNestedElement(blockIndex)));
        return loop;
    }
    
    private @NonNull CppBlock convertPreprocessorBlock(@NonNull PreprocessorBlock cppBlock) throws FormatException {
        Formula condition = notNull(cppBlock.getEffectiveCondition()); // all CPP blocks have effective conditions now
        pushFormula(condition);
        Type type = Type.valueOf(cppBlock.getType());
        CppBlock translatedBlock = new CppBlock(getPc(), condition, type);
        translatedBlock.setSourceFile(sourceFile);
        translatedBlock.setCondition(getEffectiveCondition());
        
        for (int i = 0; i < cppBlock.size(); i++) {
            ITranslationUnit child = cppBlock.getNestedElement(i);
            translatedBlock.addNestedElement(convert(child));
        }
        popFormula();
        
        return translatedBlock;
    }
    
    /**
     * Adds the given element as a sibling to ifStatement, if its a {@link BranchStatement}. If element is a
     * {@link CppBlock}, then search for {@link BranchStatement}s inside of it.
     * 
     * @param element The element that may be added as a sibling to ifStatement.
     * @param ifStatement The ifStatement that element may be a sibling of.
     */
    private void addIfSibling(ISyntaxElement element, BranchStatement ifStatement) throws FormatException {
        if (element instanceof BranchStatement) {
            ifStatement.addSibling((BranchStatement) element);
            
        } else if (element instanceof CppBlock) {
            for (int i = 0; i < element.getNestedElementCount(); i++) {
                addIfSibling(element.getNestedElement(i), ifStatement);
            }
        }
    }
    
    private @NonNull ICode makeCode(@NonNull ITranslationUnit unit, int start, int end) throws FormatException {
        
        StringBuilder code = new StringBuilder();
        List<@NonNull ICode> result = new LinkedList<>();
        
        for (int i = start; i <= end; i++) {
            ITranslationUnit child = unit.getNestedElement(i);
            
            if (child instanceof CodeUnit) {
                if (code.length() != 0) {
                    code.append(' ');
                }
                code.append(((CodeUnit) child).getCode());
            } else if ("comment".equals(child.getType())) {
                Comment comment = (Comment) convert(child);
                result.add(comment);
                
            } else if (child instanceof PreprocessorBlock) {
                if (code.length() > 0) {
                    Code codeElement = new Code(getPc(), notNull(code.toString()));
                    codeElement.setSourceFile(sourceFile);
                    codeElement.setCondition(getEffectiveCondition());
                    result.add(codeElement);
                    code = new StringBuilder();
                }
                
                // all CPP blocks have effective conditions now
                Formula condition = notNull(((PreprocessorBlock) child).getEffectiveCondition());
                pushFormula(condition);
                Type type = Type.valueOf(((PreprocessorBlock) child).getType());
                CppBlock cppif = new CppBlock(getPc(), condition, type);
                cppif.setSourceFile(sourceFile);
                cppif.setCondition(getEffectiveCondition());
                ICode nested = makeCode(child, 0, child.size() - 1);
                if (nested instanceof CodeList) {
                    for (int j = 0; j < nested.getNestedElementCount(); j++) {
                        cppif.addNestedElement(nested.getNestedElement(j));
                    }
                } else {
                    cppif.addNestedElement(nested);
                }
                
                result.add(cppif);
                popFormula();
            } else {
                throw ExceptionUtil.makeException("makeCode() Expected "
                        + CodeUnit.class.getSimpleName() + " or " + PreprocessorBlock.class.getSimpleName()
                        + ", got " + unit.getClass().getSimpleName(), unit);
            }
        }
        
        if (code.length() > 0) {
            Code codeElement = new Code(getPc(), notNull(code.toString()));
            codeElement.setSourceFile(sourceFile);
            codeElement.setCondition(getEffectiveCondition());
            result.add(codeElement);
        }
        
        if (result.size() == 0) {
            throw ExceptionUtil.makeException("makeCode() Found no elements to make code", unit);
            
        } else if (result.size() == 1) {
            return notNull(result.get(0));
            
        } else {
            CodeList list = new CodeList(getPc());
            list.setSourceFile(sourceFile);
            list.setCondition(getEffectiveCondition());
            for (ICode r : result) {
                list.addNestedElement(r);
            }
            return list;
        }
        
    }
    
}
