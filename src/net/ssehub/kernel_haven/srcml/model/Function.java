package net.ssehub.kernel_haven.srcml.model;

import java.util.ArrayList;
import java.util.List;

import net.ssehub.kernel_haven.code_model.CodeElement;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.True;

public class Function extends SrcMlSyntaxElement {
    
    // TODO: return type, parameter types
    
    private Statement body;
    
    private List<String> parameters;
    
    private String name;
    
    public Function(String name, Formula presenceCondition) {
        super(presenceCondition);
        
        body = new EmptyStatement(True.INSTANCE);
        parameters = new ArrayList<>();
        this.name = name;
    }
    
    public Function(String name, int lineStart, int lineEnd, java.io.File sourceFile, Formula condition,
            Formula presenceCondition) {
        super(lineStart, lineEnd, sourceFile, condition, presenceCondition);

        body = new EmptyStatement(True.INSTANCE);
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
        return 1;
    }

    @Override
    public CodeElement getNestedElement(int index) throws IndexOutOfBoundsException {
        if (index != 0) {
            throw new IndexOutOfBoundsException();
        }
        return body;
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
        if (!(element instanceof Statement)) {
            throw new IllegalArgumentException();
        }
        body = (Statement) element;
    }
    
    public void setBody(Statement body) {
        this.body = body;
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
