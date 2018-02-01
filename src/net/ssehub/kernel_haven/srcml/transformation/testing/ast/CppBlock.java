package net.ssehub.kernel_haven.srcml.transformation.testing.ast;

import java.io.File;

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

public class CppBlock extends SyntaxElementWithChildreen {
    
    /**
     * Denotes the exact kind of preprocessor element.
     * @author El-Sharkawy
     *
     */
    public static enum Type {
        IF, IFDEF, IFNDEF, ELSEIF, ELSE;
    }

    private Formula condition;  
    private Type type;
    
    public CppBlock(@NonNull Formula presenceCondition, File sourceFile, Formula condition, @NonNull Type type) {
        super(presenceCondition, sourceFile);
        this.condition = condition;
        this.type = type;
    }
    
    public Formula getCondition() {
        return condition;
    }

    @Override
    protected String elementToString() {
        return "#" + type.name() + " " + condition.toString();
    }

}
