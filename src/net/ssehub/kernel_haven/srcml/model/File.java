package net.ssehub.kernel_haven.srcml.model;

import java.util.ArrayList;
import java.util.List;

import net.ssehub.kernel_haven.code_model.CodeElement;
import net.ssehub.kernel_haven.util.logic.Formula;

public class File extends SrcMlSyntaxElement {
    
    private List<SrcMlSyntaxElement> elements;
    
    public File(Formula presenceCondition) {
        super(presenceCondition);
        
        elements = new ArrayList<>();
    }
    
    public File(int lineStart, int lineEnd, java.io.File sourceFile, Formula condition, Formula presenceCondition) {
        super(lineStart, lineEnd, sourceFile, condition, presenceCondition);
        
        elements = new ArrayList<>();
    }

    @Override
    public int getNestedElementCount() {
        return elements.size();
    }

    @Override
    public CodeElement getNestedElement(int index) throws IndexOutOfBoundsException {
        return elements.get(index);
    }

    @Override
    protected void addNestedElement(SrcMlSyntaxElement element) {
        elements.add(element);
    }
    
    @Override
    public void setNestedElement(int index, SrcMlSyntaxElement element) {
        elements.set(index, element);
    }

    @Override
    protected String elementToString() {
        return "File";
    }

}
