package net.ssehub.kernel_haven.srcml.transformation.testing.ast;

import java.io.File;

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

public class CompoundStatement extends SyntaxElementWithChildreen {

    public CompoundStatement(@NonNull Formula presenceCondition, File sourceFile) {
        super(presenceCondition, sourceFile);
    }

    @Override
    protected String elementToString() {
        return "CompoundStatement";
    }

}
