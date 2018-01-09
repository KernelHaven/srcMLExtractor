package net.ssehub.kernel_haven.srcml.model;

import java.util.ArrayList;
import java.util.List;

import net.ssehub.kernel_haven.code_model.CodeElement;
import net.ssehub.kernel_haven.util.logic.Formula;

public class Function extends SrcMlSyntaxElement {
    
    // TODO: parameter, return type
    
    private List<SrcMlSyntaxElement> body;
    
    private String name;
    
    public Function(String name, Formula presenceCondition) {
        super(presenceCondition);
        
        body = new ArrayList<>();
        this.name = name;
    }
    
    public Function(String name, int lineStart, int lineEnd, java.io.File sourceFile, Formula condition,
            Formula presenceCondition) {
        super(lineStart, lineEnd, sourceFile, condition, presenceCondition);
        
        body = new ArrayList<>();
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    @Override
    public int getNestedElementCount() {
        return body.size();
    }

    @Override
    public CodeElement getNestedElement(int index) throws IndexOutOfBoundsException {
        return body.get(index);
    }

    @Override
    protected void addNestedElement(SrcMlSyntaxElement element) {
        body.add(element);
    }
    
    @Override
    public void setNestedElement(int index, SrcMlSyntaxElement element) {
        body.set(index, element);
    }

    @Override
    protected String elementToString() {
        return "Function " + name;
    }

}
