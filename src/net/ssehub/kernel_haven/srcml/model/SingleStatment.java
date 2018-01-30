package net.ssehub.kernel_haven.srcml.model;

import java.io.File;

import net.ssehub.kernel_haven.srcml.model.annotations.CountableStatement;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.logic.Formula;

@CountableStatement
public class SingleStatment extends SrcMlSyntaxElement {
    
    private IExpression expression;

    public SingleStatment(Formula presenceCondition) {
        super(presenceCondition);
    }
    
    public SingleStatment(int lineStart, int lineEnd, File sourceFile, Formula condition, Formula presenceCondition) {
        super(lineStart, lineEnd, sourceFile, condition, presenceCondition);
    }

    @Override
    public int getNestedElementCount() {
        return 0;
    }

    @Override
    protected void addNestedElement(SrcMlSyntaxElement element) {
        Logger.get().logError(SingleStatment.class.getSimpleName() + " should nest " + element
            + "via the addNestMethod, which isn't supported. There is probably an error in the resulting");
    }

    @Override
    public void setNestedElement(int index, SrcMlSyntaxElement element) {
        Logger.get().logError(SingleStatment.class.getSimpleName() + " should nest " + element
            + "via the setNestMethod, which isn't supported. There is probably an error in the resulting");
    }

    @Override
    public SrcMlSyntaxElement getNestedElement(int index) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException("SingleStatment has no nested elements, requested was index: " + index);
    }
    
    public void setExpression(IExpression expression) {
        this.expression = expression;
    }
    
    public IExpression getExpression() {
        return expression;
    }

    @Override
    protected String elementToString() {
        String className = this.getClass().getSimpleName();
        return (null != expression) ? className + ": " + expression : className;
    }

}
