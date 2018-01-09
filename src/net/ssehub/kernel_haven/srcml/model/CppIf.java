package net.ssehub.kernel_haven.srcml.model;

import net.ssehub.kernel_haven.code_model.CodeElement;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.True;

public class CppIf extends CppStatement {
    
    private Formula ifCondition = True.INSTANCE;
    
    public CppIf(Formula presenceCondition) {
        super(presenceCondition);
    }
    
    public CppIf(int lineStart, int lineEnd, java.io.File sourceFile, Formula condition, Formula presenceCondition) {
        super(lineStart, lineEnd, sourceFile, condition, presenceCondition);
    }
    
    public void setIfCondition(Formula ifCondition) {
        this.ifCondition = ifCondition;
    }
    
    public Formula getIfCondition() {
        return ifCondition;
    }
    
    @Override
    public int getNestedElementCount() {
        return 0;
    }

    @Override
    public CodeElement getNestedElement(int index) throws IndexOutOfBoundsException {
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
        return "#if " + ifCondition.toString();
    }

}
