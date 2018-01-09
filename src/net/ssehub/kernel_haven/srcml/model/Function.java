package net.ssehub.kernel_haven.srcml.model;

import java.util.ArrayList;
import java.util.List;

import net.ssehub.kernel_haven.code_model.CodeElement;
import net.ssehub.kernel_haven.util.logic.Formula;

public class Function extends SrcMlSyntaxElement {
    
    // TODO: return type, parameter types
    
    private List<SrcMlSyntaxElement> body;
    
    private List<String> parameters;
    
    private String name;
    
    public Function(String name, Formula presenceCondition) {
        super(presenceCondition);
        
        body = new ArrayList<>();
        parameters = new ArrayList<>();
        this.name = name;
    }
    
    public Function(String name, int lineStart, int lineEnd, java.io.File sourceFile, Formula condition,
            Formula presenceCondition) {
        super(lineStart, lineEnd, sourceFile, condition, presenceCondition);
        
        body = new ArrayList<>();
        parameters = new ArrayList<>();
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public void addParameter(String parameter) {
        parameters.add(parameter);
    }
    
    public int getNumParamters() {
        return parameters.size();
    }
    
    public String getParameter(int index) {
        return parameters.get(index);
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
        StringBuilder paramString = new StringBuilder("(");
        
        if (!parameters.isEmpty()) {
            for (String param : parameters) {
                paramString.append(param).append(", ");
            }
            paramString.replace(paramString.length() - 2, paramString.length(), ""); // remove trailing ", "
        }
        paramString.append(")");
        
        return "Function " + name + paramString.toString();
    }

}
