package net.ssehub.kernel_haven.srcml.transformation.testing.ast;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import net.ssehub.kernel_haven.srcml.transformation.testing.CodeUnit;
import net.ssehub.kernel_haven.srcml.transformation.testing.ITranslationUnit;
import net.ssehub.kernel_haven.srcml.transformation.testing.PreprocessorBlock;
import net.ssehub.kernel_haven.srcml.transformation.testing.PreprocessorElse;
import net.ssehub.kernel_haven.srcml.transformation.testing.PreprocessorIf;
import net.ssehub.kernel_haven.srcml.transformation.testing.TranslationUnit;
import net.ssehub.kernel_haven.srcml.transformation.testing.ast.CppBlock.Type;
import net.ssehub.kernel_haven.srcml.transformation.testing.ast.Loop.LoopType;
import net.ssehub.kernel_haven.srcml.transformation.testing.ast.TypeDefinition.TypeDefType;
import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.True;

/**
 * Translates the {@link ITranslationUnit}-structure into a {@link SyntaxElement} (AST). This is the final parsing step.
 * @author Adam
 * @author El-Sharkawy
 *
 */
public class TranslationUnitToAstConverter {

    private Stack<Formula> cppConditions;
    private java.io.File sourceFile;
    
    public TranslationUnitToAstConverter(java.io.File sourceFile) {
        cppConditions = new Stack<>();
        this.sourceFile = sourceFile;
    }
    
    /**
     * Returns the current presence condition, considering the conditions of all surrounding CPP blocks, but won't pop
     * any elements from the stack.
     * @return The current presence condition (may be {@link True#INSTANCE} in case of no presence condition).
     */
    private Formula getPc() {
        return cppConditions.isEmpty() ? True.INSTANCE : cppConditions.peek();
    }
    
    /**
     * Will compute a new presence condition based on the given formula on the current state of the stack and pushes
     * the result on top of the stack.
     * @param cppCondition
     */
    private void pushFormula(Formula cppCondition) {
        if (cppConditions.isEmpty()) {
            cppConditions.push(cppCondition);
        } else {
            // TODO SE: Use of a cache?
            cppConditions.push(new Conjunction(cppConditions.peek(), cppCondition));
        }
    }
    
    /**
     * Translates the {@link ITranslationUnit}-structure into a {@link SyntaxElement} (AST).
     * @param element The root element representing the complete file.
     * @return The translated AST still representing the complete file.
     */
    public SyntaxElement convert(ITranslationUnit element) {
        
        if (element instanceof TranslationUnit) {
            return convertTranslationUnit((TranslationUnit) element);
        } else if (element instanceof PreprocessorIf || element instanceof PreprocessorElse) {
            return convertPreprocessorBlock((PreprocessorBlock) element);
//        } else if (element instanceof PreprocessorElse) {
//            return convertPreprocessorElse((PreprocessorElse) element);
        }
        
        throw new IllegalArgumentException("Illegal element " + element.getClass().getName());
    }
    
    private SyntaxElement convertTranslationUnit(TranslationUnit unit) {
        Formula pc = getPc();
        
        switch (unit.getType()) {
        
        case "decl_stmt": // falls through
        case "expr_stmt": // falls through
        case "continue":  // falls through
        case "break":     // falls through
        case "goto":      // falls through 
        case "return":    // falls through 
        case "empty_stmt": 
            return new SingleStatement(pc, sourceFile, makeCode(unit, 0, unit.size() - 1));
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
            IfStructure ifStatement = new IfStructure(pc, sourceFile, makeCode(unit, 0, lastConditionElement));
            for (int i = lastConditionElement + 1; i < unit.size(); i++) {
                SyntaxElement converted = convert(unit.getNestedElement(i)); // TODO SE: handle else and elseif
                if (converted != null) {
                    ifStatement.addNestedElement(converted);
                }
            }
            return ifStatement;
        
        case "else":
            // TODO SE: @Adam Proper data type required, elseif still missing
            CompoundStatement elseBlock = new CompoundStatement(pc, sourceFile);
            for (int i = 0; i < unit.size(); i++) {
                ITranslationUnit child = unit.getNestedElement(i);
                if (child instanceof CodeUnit) {
                    // ignore { and }
                } else {
                    SyntaxElement converted = convert(child); // TODO
                    if (converted != null) {
                        elseBlock.addNestedElement(converted);
                    }
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
//            Struct struct = new Struct(pc, sourceFile, makeCode(unit, 0, unit.size() - 3));
//            SyntaxElement structContent = convert(unit.getNestedElement(unit.size() - 2)); // TODO
//            if (structContent != null) {
//                struct.addNestedElement(structContent);
//            }
//            return struct;
            
        case "unit": {
            File file = new File(pc, sourceFile);
            for (int i = 0; i < unit.size(); i++) {
                SyntaxElement converted = convert(unit.getNestedElement(i)); // TODO
                if (converted != null) {
                    file.addNestedElement(converted);
                }
            }
            
            return file;
        }
        
        case "function": {
            Function f = new Function(pc, sourceFile, makeCode(unit, 0, unit.size() - 2)); // last nested is the function block
            
            SyntaxElement converted = convert(unit.getNestedElement(unit.size() - 1)); // TODO
            if (converted != null) {
                f.addNestedElement(converted);
            }
            
            return f;
        }
        
        case "block": {
            CompoundStatement block = new CompoundStatement(pc, sourceFile);
            for (int i = 0; i < unit.size(); i++) {
                ITranslationUnit child = unit.getNestedElement(i);
                if (child instanceof CodeUnit) {
                    // ignore { and }
                } else {
                    SyntaxElement converted = convert(child); // TODO
                    if (converted != null) {
                        block.addNestedElement(converted);
                    }
                }
            }
            
            return block;
        }
        
        }

        // TODO
        return null;
    }

    private SyntaxElement createTypeDef(TranslationUnit unit, Formula pc, TypeDefType type, int blockIndex, int condStartIndex, int condEndIndex) {
        TypeDefinition typeDef = new TypeDefinition(pc, sourceFile, makeCode(unit, condStartIndex, condEndIndex), type);
        SyntaxElement typeDefContent = convert(unit.getNestedElement(blockIndex)); // TODO
        if (typeDefContent != null) {
            typeDef.addNestedElement(typeDefContent);
        }
        return typeDef;
    }

    private Loop createLoop(TranslationUnit unit, LoopType type, int blockIndex, int condStartIndex, int condEndIndex) {
        
        SyntaxElement condition = makeCode(unit, condStartIndex, condEndIndex);
        Loop cStructure = new Loop(getPc(), sourceFile, condition, type);
        SyntaxElement loopBlock = convert(unit.getNestedElement(blockIndex)); // TODO
        if (loopBlock != null) {
            cStructure.addNestedElement(loopBlock);
        }
        return cStructure;
    }
    
    private SyntaxElement convertPreprocessorBlock(PreprocessorBlock cppBlock) {
        Formula condition = cppBlock.getEffectiveCondition();
        pushFormula(condition);
        Type type = Type.valueOf(cppBlock.getType());
        CppBlock translatedBlock = new CppBlock(getPc(), sourceFile, condition, type);
        
        for (int i = 0; i < cppBlock.size(); i++) {
            ITranslationUnit child = cppBlock.getNestedElement(i);
            SyntaxElement converted = convert(child); // TODO
            if (converted != null) {
                translatedBlock.addNestedElement(converted);
            }
        }
        
        return translatedBlock;
    }
    
    private SyntaxElement makeCode(ITranslationUnit unit, int start, int end) {
        
        StringBuilder code = new StringBuilder();
        List<SyntaxElement> result = new LinkedList<>();
        
        for (int i = start; i <= end; i++) {
            ITranslationUnit child = unit.getNestedElement(i);
//            System.out.println(child.getType());
            
            if (child instanceof CodeUnit) {
                if (code.length() != 0) {
                    code.append(' ');
                }
                code.append(((CodeUnit) child).getCode());
            } else if (child instanceof PreprocessorBlock) {
                Formula condition = ((PreprocessorBlock) child).getEffectiveCondition();
                result.add(new Code(getPc(), sourceFile, code.toString()));
                
                pushFormula(condition);
                code = new StringBuilder();
                Type type = Type.valueOf(((PreprocessorBlock) child).getType());
                CppBlock cppif = new CppBlock(getPc(), sourceFile, condition, type);
                SyntaxElement nested = makeCode(child, 0, child.size() - 1);
                if (nested instanceof CodeList) {
                    for (int j = 0; j < nested.getNestedElementCount(); j++) {
                        cppif.addNestedElement(nested.getNestedElement(j));
                    }
                } else {
                    cppif.addNestedElement(nested);
                }
                
                result.add(cppif);
                cppConditions.pop();
            } else {
                throw new RuntimeException("makeCode() Expected "
                    + CodeUnit.class.getSimpleName() + "CodeUnit or " + PreprocessorBlock.class.getSimpleName()
                    + ", got " + unit.getClass().getSimpleName());
                // ignore
            }
        }
        
        if (code.length() > 0) {
            result.add(new Code(getPc(), sourceFile, code.toString()));
        }
        
        if (result.size() == 0) {
            throw new RuntimeException("makeCode() Found no elements to make code");
            
        } else if (result.size() == 1) {
            return result.get(0);
            
        } else {
            CodeList list = new CodeList(getPc(), sourceFile);
            for (SyntaxElement r : result) {
                list.addNestedElement(r);
            }
            return list;
        }
        
    }
    
}
