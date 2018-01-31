package net.ssehub.kernel_haven.srcml.transformation.testing.ast;

import net.ssehub.kernel_haven.util.logic.Formula;

public class Code extends SyntaxElement {

    private String text;
    
    public Code(Formula presenceCondition, String text) {
        super(presenceCondition);
        this.text = text;
    }
    
    public String getText() {
        return text;
    }
    
    @Override
    protected String elementToString() {
        return text;
    }
    
}
