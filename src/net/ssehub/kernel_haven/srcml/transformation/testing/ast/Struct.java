package net.ssehub.kernel_haven.srcml.transformation.testing.ast;

import java.io.File;

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Represents the definition of a <tt>struct</tt> (outside of a function).
 * @author El-Sharkawy
 *
 */
public class Struct extends SyntaxElementWithChildreen {

    private SyntaxElement structDeclaration;
    
    public Struct(@NonNull Formula presenceCondition, File sourceFile, SyntaxElement structDeclaration) {
        super(presenceCondition, sourceFile);
        this.structDeclaration = structDeclaration;
    }

    @Override
    protected String elementToString() {
        return "Struct:\n" + (structDeclaration == null ? "\t\t\t\tnull"
            : structDeclaration.toString("\t\t\t\t")); // TODO
    }

}
