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
import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.True;

public class TranslationUnitToAstConverter {

    private Stack<Formula> cppConditions;
    
    public TranslationUnitToAstConverter() {
        cppConditions = new Stack<>();
    }
    
    private Formula getPc() {
        if (cppConditions.isEmpty()) {
            return True.INSTANCE;
        } else {
            
            Formula f = cppConditions.get(0);
            for (int i = 1; i < cppConditions.size(); i++) {
                f = new Conjunction(f, cppConditions.get(i));
            }

            return f;
        }
    }
    
    public SyntaxElement convert(ITranslationUnit element) {
        
        if (element instanceof TranslationUnit) {
            return convertTranslationUnit((TranslationUnit) element);
        } else if (element instanceof PreprocessorIf) {
            return convertPreprocessorIf((PreprocessorIf) element);
        } else if (element instanceof PreprocessorElse) {
            return convertPreprocessorElse((PreprocessorElse) element);
        }
        
        throw new IllegalArgumentException("Illegal element " + element.getClass().getName());
    }
    
    private SyntaxElement convertTranslationUnit(TranslationUnit unit) {
        Formula pc = getPc();
        
        switch (unit.getType()) {
        
        case "decl_stmt":
        case "expr_stmt":
        case "continue":
        case "break":
        case "goto":
        case "empty_stmt":
            return new SingleStatement(pc, makeCode(unit, 0, unit.size() - 1));
        
        case "unit": {
            File file = new File(pc);
            
            for (ITranslationUnit child : unit) {
                SyntaxElement converted = convert(child); // TODO
                if (converted != null) {
                    file.addNestedElement(converted);
                }
            }
            
            return file;
        }
        
        case "function": {
            Function f = new Function(pc, makeCode(unit, 0, unit.size() - 2)); // lat nested is the function block
            
            SyntaxElement converted = convert(unit.getNestedElement(unit.size() - 1)); // TODO
            if (converted != null) {
                f.addNestedElement(converted);
            }
            
            return f;
        }
        
        case "block": {
            CompoundStatement block = new CompoundStatement(pc);
            for (ITranslationUnit child : unit) {
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
    
    private SyntaxElement convertPreprocessorIf(PreprocessorIf cppIf) {
        return null;
    }
    
    private SyntaxElement convertPreprocessorElse(PreprocessorElse cppIf) {
        return null;
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
                result.add(new Code(getPc(), code.toString()));
                code = new StringBuilder();
                
                CppIf cppif = new CppIf(getPc(), True.INSTANCE); // TODO 
                SyntaxElement nested = makeCode(child, 0, child.size() - 1);
                if (nested instanceof CodeList) {
                    for (int j = 0; j < nested.getNestedElementCount(); j++) {
                        cppif.addNestedElement(nested.getNestedElement(j));
                    }
                } else {
                    cppif.addNestedElement(nested);
                }
                
                result.add(cppif);
                
            } else {
                throw new RuntimeException("makeCode() Expected CodeUnit or PreprocessorBlock, got " + unit.getClass().getSimpleName());
                // ignore
            }
        }
        
        if (code.length() > 0) {
            result.add(new Code(getPc(), code.toString()));
        }
        
        if (result.size() == 0) {
            throw new RuntimeException("makeCode() Found no elements to make code");
            
        } else if (result.size() == 1) {
            return result.get(0);
            
        } else {
            CodeList list = new CodeList(getPc());
            for (SyntaxElement r : result) {
                list.addNestedElement(r);
            }
            return list;
        }
        
    }
    
}
