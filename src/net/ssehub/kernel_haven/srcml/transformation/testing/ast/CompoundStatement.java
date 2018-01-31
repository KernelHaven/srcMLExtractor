package net.ssehub.kernel_haven.srcml.transformation.testing.ast;

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

public class CompoundStatement extends SyntaxElementWithChildreen {

    public CompoundStatement(@NonNull Formula presenceCondition) {
        super(presenceCondition);
    }

    @Override
    protected String elementToString() {
        return "CompoundStatement";
    }

}
