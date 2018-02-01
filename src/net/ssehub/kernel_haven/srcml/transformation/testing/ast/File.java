package net.ssehub.kernel_haven.srcml.transformation.testing.ast;

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

public class File extends SyntaxElementWithChildreen {

    public File(@NonNull Formula presenceCondition, java.io.File sourceFile) {
        super(presenceCondition, sourceFile);
    }

    @Override
    protected String elementToString() {
        return "File";
    }

}
