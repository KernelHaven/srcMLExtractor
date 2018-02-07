package net.ssehub.kernel_haven.srcml.transformation;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

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
import net.ssehub.kernel_haven.util.Logger;
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

    private @NonNull Stack<@NonNull Formula> cppPresenceConditions;
    private @NonNull Stack<@NonNull Formula> cppEffectiveConditions;
    
    private java.io. @NonNull File sourceFile;
    
    public TranslationUnitToAstConverter(java.io. @NonNull File sourceFile) {
        cppPresenceConditions = new Stack<>();
        cppEffectiveConditions = new Stack<>();
        this.sourceFile = sourceFile;
    }
    
    /**
     * Returns the current presence condition, considering the conditions of all surrounding CPP blocks, but won't pop
     * any elements from the stack.
     * @return The current presence condition (may be {@link True#INSTANCE} in case of no presence condition).
     */
    private Formula getPc() {
        return cppPresenceConditions.isEmpty() ? True.INSTANCE : cppPresenceConditions.peek();
    }
    
    private Formula getEffectiveCondition() {
        return cppEffectiveConditions.isEmpty() ? True.INSTANCE : cppEffectiveConditions.peek();
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
    public ISyntaxElement convert(ITranslationUnit element) {
        
        if (element instanceof TranslationUnit) {
            return convertTranslationUnit((TranslationUnit) element);
        } else if (element instanceof PreprocessorIf || element instanceof PreprocessorElse) {
            return convertPreprocessorBlock((PreprocessorBlock) element);
//        } else if (element instanceof PreprocessorElse) {
//            return convertPreprocessorElse((PreprocessorElse) element);
        }
        
        throw new IllegalArgumentException("Illegal element " + element.getClass().getName() + " while translating "
            + sourceFile.getAbsolutePath());
    }
    
    private ISyntaxElement convertTranslationUnit(TranslationUnit unit) {
        Formula pc = getPc();
        
        switch (unit.getType()) {
        
        case "decl_stmt": // falls through
        case "expr_stmt": // falls through
        case "continue":  // falls through
        case "break":     // falls through
        case "goto":      // falls through 
        case "return":    // falls through 
        case "empty_stmt": 
            SingleStatement singleStatement = new SingleStatement(pc, makeCode(unit, 0, unit.size() - 1));
            singleStatement.setSourceFile(sourceFile);
            singleStatement.setCondition(getEffectiveCondition());
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
            while (!(unit.getNestedElement((lastCodeElement + 1)) instanceof TranslationUnit)) {
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
                Logger.get().logError("Unexpected structure of elseif-statement: " + unit.toString());
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
            /*
             * 2nd last nested is the enum block (definition of literals).
             * Last is a semicolon, which is no longer needed -> will be removed
             */
            return createTypeDef(unit, pc, TypeDefType.ENUM, unit.size() - 2, 0, unit.size() - 3);
        case "struct":
            /*
             * 2nd last nested is the struct block (definition of attributes).
             * Last is a semicolon, which is no longer needed -> will be removed
             */
            return createTypeDef(unit, pc, TypeDefType.STRUCT, unit.size() - 2, 0, unit.size() - 3);
        case "typedef":
            /*
             * * A typedef maybe has a block-statement at the second definition, if it combines the definition of a
             *   struct with an alias/typedef of the new struct. Thus, we need to check if we also need to parse the
             *   second nested element.
             * * Last is a semicolon, which is no longer needed -> will be removed
             */
            int blockIndex = (unit.size() >= 2 && !(unit.getNestedElement(1) instanceof CodeUnit)) ? 1 : -1;
            int startIndex = (blockIndex != -1) ? blockIndex + 1: 0;
            return createTypeDef(unit, pc, TypeDefType.TYPEDEF, blockIndex, startIndex, unit.size() - 2);
        case "union":
            /*
             * 2nd last nested is the union block (definition of attributes).
             * Last is a semicolon, which is no longer needed -> will be removed
             */
            return createTypeDef(unit, pc, TypeDefType.UNION, unit.size() - 2, 0, unit.size() - 3);
            
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
            Function f = new Function(pc, unit.getFunctionName(),
                    makeCode(unit, 0, unit.size() - 2)); // last nested is the function block
            f.setSourceFile(sourceFile);
            f.setCondition(getEffectiveCondition());
            
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
             * Last element is switch-BLock, before comes the condition
             */
            SwitchStatement switchStatement = new SwitchStatement(getPc(), makeCode(unit, 0, unit.size() - 2));
            switchStatement.setSourceFile(sourceFile);
            switchStatement.setCondition(getEffectiveCondition());
            switchStatement.addNestedElement(convert(unit.getNestedElement(unit.size() - 1)));
            return switchStatement;
        
        case "case":
            /*
             * 3 Elements belong to the condition, afterwards come nested statements
             */
            return convertCaseStatement(unit, 2, CaseType.CASE);
        
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

        throw new RuntimeException("Unexpected unit type: " + unit.getType());
    }
    
    private CppStatement convertCppStatement(TranslationUnit unit, CppStatement.Type type) {
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

    private CaseStatement convertCaseStatement(TranslationUnit unit, int condEndIndex, CaseType type) {
        CaseStatement caseStatement = new CaseStatement(getPc(), makeCode(unit, 0, condEndIndex), type);
        caseStatement.setSourceFile(sourceFile);
        caseStatement.setCondition(getEffectiveCondition());
        for (int i = condEndIndex + 1; i < unit.size(); i++) {
            caseStatement.addNestedElement(convert(unit.getNestedElement(i)));
        }
        return caseStatement;
    }

    private boolean isInline(TranslationUnit unit) {
        boolean isInline = false;
        if (unit.size() > 0) {
            ITranslationUnit lastElement = unit.getNestedElement(unit.size() - 1);
            if (!(lastElement instanceof CodeUnit && ";".equals(((CodeUnit) lastElement).getCode()))) {
                isInline = true;
            }
        }
        
        return isInline;
    }

    private TypeDefinition createTypeDef(TranslationUnit unit, Formula pc, TypeDefType type, int blockIndex, int declStartIndex, int declEndIndex) {
        /*
         * blockIndex and declEndIndex are computed from the end, expecting as last element a semicolon, which is only
         * optional. This will fix the indices.
         */
        if (isInline(unit)) {
            if (-1 != blockIndex) {
                blockIndex++;
            }
            declEndIndex++;
        }
        
        TypeDefinition typeDef = new TypeDefinition(pc, makeCode(unit, declStartIndex, declEndIndex), type);
        typeDef.setSourceFile(sourceFile);
        typeDef.setCondition(getEffectiveCondition());
        if (-1 != blockIndex) {
            typeDef.addNestedElement(convert(unit.getNestedElement(blockIndex)));
        }
        return typeDef;
    }

    private LoopStatement createLoop(TranslationUnit unit, LoopType type, int blockIndex, int condStartIndex, int condEndIndex) {
        
        ICode condition = makeCode(unit, condStartIndex, condEndIndex);
        LoopStatement loop = new LoopStatement(getPc(),  condition, type);
        loop.setSourceFile(sourceFile);
        loop.setCondition(getEffectiveCondition());
        loop.addNestedElement(convert(unit.getNestedElement(blockIndex)));
        return loop;
    }
    
    private CppBlock convertPreprocessorBlock(PreprocessorBlock cppBlock) {
        Formula condition = cppBlock.getEffectiveCondition();
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
    private void addIfSibling(ISyntaxElement element, BranchStatement ifStatement) {
        if (element instanceof BranchStatement) {
            ifStatement.addSibling((BranchStatement) element);
            
        } else if (element instanceof CppBlock) {
            for (int i = 0; i < element.getNestedElementCount(); i++) {
                addIfSibling(element.getNestedElement(i), ifStatement);
            }
        }
    }
    
    private ICode makeCode(ITranslationUnit unit, int start, int end) {
        
        StringBuilder code = new StringBuilder();
        List<ICode> result = new LinkedList<>();
        
        for (int i = start; i <= end; i++) {
            ITranslationUnit child = unit.getNestedElement(i);
//            System.out.println(child.getType());
            
            if (child instanceof CodeUnit) {
                if (code.length() != 0) {
                    code.append(' ');
                }
                code.append(((CodeUnit) child).getCode());
            } else if (child instanceof PreprocessorBlock) {
                if (code.length() > 0) {
                    Code codeElement = new Code(getPc(), code.toString());
                    codeElement.setSourceFile(sourceFile);
                    codeElement.setCondition(getEffectiveCondition());
                    result.add(codeElement);
                    code = new StringBuilder();
                }
                
                Formula condition = ((PreprocessorBlock) child).getEffectiveCondition();
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
                throw new RuntimeException("makeCode() Expected "
                    + CodeUnit.class.getSimpleName() + "CodeUnit or " + PreprocessorBlock.class.getSimpleName()
                    + ", got " + unit.getClass().getSimpleName() + " while parsing file: "
                    + sourceFile.getAbsolutePath());
                // ignore
            }
        }
        
        if (code.length() > 0) {
            Code codeElement = new Code(getPc(), code.toString());
            codeElement.setSourceFile(sourceFile);
            codeElement.setCondition(getEffectiveCondition());
            result.add(codeElement);
        }
        
        if (result.size() == 0) {
            throw new RuntimeException("makeCode() Found no elements to make code");
            
        } else if (result.size() == 1) {
            return result.get(0);
            
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
