package net.ssehub.kernel_haven.srcml.model;

import net.ssehub.kernel_haven.util.logic.Formula;

public class EmptyStatement extends Statement {
    
    public EmptyStatement(Formula presenceCondition) {
        super(presenceCondition);
    }
    
    public EmptyStatement(int lineStart, int lineEnd, java.io.File sourceFile, Formula condition, Formula presenceCondition) {
        super(lineStart, lineEnd, sourceFile, condition, presenceCondition);
    }

    @Override
    public int getNestedElementCount() {
        return 0;
    }

    @Override
    public SrcMlSyntaxElement getNestedElement(int index) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException();
    }

    @Override
    protected void addNestedElement(SrcMlSyntaxElement element) {
        throw new IndexOutOfBoundsException();
    }
    
    @Override
    public void setNestedElement(int index, SrcMlSyntaxElement element) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    protected String elementToString() {
        return "Empty Statement";
    }

    
}
