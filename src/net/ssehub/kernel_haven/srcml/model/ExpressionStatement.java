package net.ssehub.kernel_haven.srcml.model;

import java.io.File;

import net.ssehub.kernel_haven.util.logic.Formula;

public class ExpressionStatement extends Statement {

    private SrcMlSyntaxElement expr;
    
    public ExpressionStatement(Formula presenceCondition, SrcMlSyntaxElement expr) {
        super(presenceCondition);
        this.expr = expr;
    }
    
    public ExpressionStatement(int lineStart, int lineEnd, File sourceFile, Formula condition,
            Formula presenceCondition, SrcMlSyntaxElement expr) {
        super(lineStart, lineEnd, sourceFile, condition, presenceCondition);
        
        this.expr = expr;
    }

    @Override
    public int getNestedElementCount() {
        return 1;
    }

    @Override
    public SrcMlSyntaxElement getNestedElement(int index) throws IndexOutOfBoundsException {
        if (index != 0) {
            throw new IndexOutOfBoundsException();
        }
        
        return expr;
    }

    @Override
    protected void addNestedElement(SrcMlSyntaxElement element) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void setNestedElement(int index, SrcMlSyntaxElement element) {
        if (index != 0) {
            throw new IndexOutOfBoundsException();
        }
        
        expr = element;
    }

    @Override
    protected String elementToString() {
        return "Expression Statement";
    }

}
