package net.ssehub.kernel_haven.srcml.model;

import java.io.File;

import net.ssehub.kernel_haven.util.logic.Formula;

public abstract class CppStatement extends SrcMlSyntaxElement {

    public CppStatement(Formula presenceCondition) {
        super(presenceCondition);
    }
    
    public CppStatement(int lineStart, int lineEnd, File sourceFile, Formula condition, Formula presenceCondition) {
        super(lineStart, lineEnd, sourceFile, condition, presenceCondition);
    }

}
