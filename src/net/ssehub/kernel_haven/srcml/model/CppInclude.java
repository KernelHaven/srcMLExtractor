package net.ssehub.kernel_haven.srcml.model;

import net.ssehub.kernel_haven.util.logic.Formula;

public class CppInclude extends CppStatement {
    
    private String filename;
    
    public CppInclude(Formula presenceCondition) {
        super(presenceCondition);
    }
    
    public CppInclude(int lineStart, int lineEnd, java.io.File sourceFile, Formula condition, Formula presenceCondition) {
        super(lineStart, lineEnd, sourceFile, condition, presenceCondition);
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public String getFileName() {
        return filename;
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
        return "#include " + filename;
    }
    
}
