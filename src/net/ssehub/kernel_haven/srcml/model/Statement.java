package net.ssehub.kernel_haven.srcml.model;

import java.io.File;

import net.ssehub.kernel_haven.util.logic.Formula;

public abstract class Statement extends SrcMlSyntaxElement {

    public Statement( Formula presenceCondition) {
        super(presenceCondition);
    }
    
    public Statement(int lineStart, int lineEnd, File sourceFile, Formula condition, Formula presenceCondition) {
        super(lineStart, lineEnd, sourceFile, condition, presenceCondition);
    }

}
