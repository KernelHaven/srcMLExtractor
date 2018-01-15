package net.ssehub.kernel_haven.srcml.model;

import java.io.File;

import net.ssehub.kernel_haven.code_model.CodeElement;
import net.ssehub.kernel_haven.util.logic.Formula;

public class ReturnStatement extends Statement {

    private SrcMlSyntaxElement expression;
    
    public ReturnStatement(Formula presenceCondition) {
        super(presenceCondition);
    }
    
    public ReturnStatement(int lineStart, int lineEnd, File sourceFile, Formula condition, Formula presenceCondition) {
        super(lineStart, lineEnd, sourceFile, condition, presenceCondition);
    }

    @Override
    public int getNestedElementCount() {
        return expression == null ? 0 : 1;
    }

    @Override
    public CodeElement getNestedElement(int index) throws IndexOutOfBoundsException {
        if (expression == null || index != 0) {
            throw new IndexOutOfBoundsException();
        }
        return expression;
    }

    @Override
    protected void addNestedElement(SrcMlSyntaxElement element) {
        if (expression == null) {
            throw new IndexOutOfBoundsException();
        }
        expression = element;
    }

    @Override
    public void setNestedElement(int index, SrcMlSyntaxElement element) {
        if (index != 0) {
            throw new IndexOutOfBoundsException();
        }
        
        expression = element;
    }

    @Override
    protected String elementToString() {
        return "Return";
    }

}
