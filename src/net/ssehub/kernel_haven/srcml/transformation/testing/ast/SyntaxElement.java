package net.ssehub.kernel_haven.srcml.transformation.testing.ast;

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

public abstract class SyntaxElement {

    private @NonNull Formula presenceCondition;
    
    public SyntaxElement(@NonNull Formula presenceCondition) {
        this.presenceCondition = presenceCondition;
    }
    
    public Formula getPresenceCondition() {
        return presenceCondition;
    }
    
    public @NonNull SyntaxElement getNestedElement(int index) {
        throw new IndexOutOfBoundsException();
    }
    
    public int getNestedElementCount() {
        return 0;
    }
    
    public void addNestedElement(@NonNull SyntaxElement element) {
        throw new IndexOutOfBoundsException();
    }
    
    @Override
    public @NonNull String toString() {
        return toString("");
    }
    
    protected abstract String elementToString();
    
    protected String toString(String indentation) {
        StringBuilder result = new StringBuilder();
        
        Formula condition = this.presenceCondition;
        String conditionStr = condition == null ? "<null>" : condition.toString();
        if (conditionStr.length() > 64) {
            conditionStr = "...";
        }
        
        result.append(indentation).append("[").append(conditionStr).append("] ");
        
        result.append(elementToString()).append('\n');
        
        indentation += '\t';
        
        for (int i = 0; i < getNestedElementCount(); i++) {
            SyntaxElement child = getNestedElement(i);
            result.append(child != null ? child.toString(indentation) : indentation + "null\n");
        }
        
        return result.toString();
    }
    
}
