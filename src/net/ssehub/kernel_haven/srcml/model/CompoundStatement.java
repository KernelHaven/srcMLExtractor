package net.ssehub.kernel_haven.srcml.model;

import java.util.LinkedList;
import java.util.List;

import net.ssehub.kernel_haven.util.logic.Formula;

public class CompoundStatement extends Statement {
    
    private List<SrcMlSyntaxElement> elements;
    
    public CompoundStatement(Formula presenceCondition) {
        super(presenceCondition);
        
        elements = new LinkedList<>();
    }
    
    public CompoundStatement(int lineStart, int lineEnd, java.io.File sourceFile, Formula condition, Formula presenceCondition) {
        super(lineStart, lineEnd, sourceFile, condition, presenceCondition);
        
        elements = new LinkedList<>();
    }

    @Override
    public int getNestedElementCount() {
        return elements.size();
    }

    @Override
    public SrcMlSyntaxElement getNestedElement(int index) throws IndexOutOfBoundsException {
        return elements.get(index);
    }

    @Override
    protected void addNestedElement(SrcMlSyntaxElement element) {
        elements.add(element);
    }
    
    @Override
    public void setNestedElement(int index, SrcMlSyntaxElement element) {
        if (element == null) {
            elements.remove(index);
        } else {
            elements.set(index, element);
        }
    }

    @Override
    protected String elementToString() {
        return "Compound Statement";
    }

    
}
