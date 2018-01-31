package net.ssehub.kernel_haven.srcml.transformation.testing.ast;

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

public class CppIf extends SyntaxElementWithChildreen {

    private Formula condition;
    
    public CppIf(@NonNull Formula presenceCondition, Formula condition) {
        super(presenceCondition);
        this.condition = condition;
    }
    
    public Formula getCondition() {
        return condition;
    }

    @Override
    protected String elementToString() {
        return "#if " + condition.toString();
    }

}
