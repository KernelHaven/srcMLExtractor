package net.ssehub.kernel_haven.srcml.model;

import java.io.File;
import java.util.List;

import net.ssehub.kernel_haven.code_model.CodeElement;
import net.ssehub.kernel_haven.code_model.SyntaxElement;
import net.ssehub.kernel_haven.util.logic.Formula;

/**
 * Super-class for classes that are used to represent the code model created by srcML.
 * <p>
 * The future plan is to replace {@link SyntaxElement} with this class, and remove the old TypeChef-based variant.
 * </p>
 * 
 * @author Adam
 */
public abstract class SrcMlSyntaxElement implements CodeElement {

    private int lineStart;
    
    private int lineEnd;
    
    private File sourceFile;
    
    private Formula condition;
    
    private Formula presenceCondition;
    
    public SrcMlSyntaxElement(Formula presenceCondition) {
        this(-1, -1, null, null, presenceCondition);
    }

    public SrcMlSyntaxElement(int lineStart, int lineEnd, File sourceFile, Formula condition,
            Formula presenceCondition) {
        
        this.lineStart = lineStart;
        this.lineEnd = lineEnd;
        this.sourceFile = sourceFile;
        this.condition = condition;
        this.presenceCondition = presenceCondition;
    }

    @Override
    public void addNestedElement(CodeElement element) {
        if (!(element instanceof SrcMlSyntaxElement)) {
            throw new IllegalArgumentException("Can only add SrcMlSyntaxElement as child of SrcMlSyntaxElement");
        }
        
        addNestedElement((SrcMlSyntaxElement) element);
    }
    
    /**
     * Adds a nested element to the end of the list.
     * 
     * @param element The element to add.
     */
    protected abstract void addNestedElement(SrcMlSyntaxElement element);
    
    /**
     * Changes the child nested in this element.
     * 
     * @param index The child to replace.
     * @param element The new child. <code>null</code> to remove this child.
     */
    public abstract void setNestedElement(int index, SrcMlSyntaxElement element);
    
    @Override
    public int getLineStart() {
        return lineStart;
    }
    
    @Override
    public int getLineEnd() {
        return lineEnd;
    }
    
    @Override
    public File getSourceFile() {
        return sourceFile;
    }
    
    @Override
    public Formula getCondition() {
        return condition;
    }
    
    @Override
    public Formula getPresenceCondition() {
        return presenceCondition;
    }
    
    @Override
    public List<String> serializeCsv() {
        throw new UnsupportedOperationException("CSV serialization is not yet implemented.");
    }
    
    @Override
    public String toString() {
        return toString("");
    }
    
    /**
     * Turns this node into a string with the given indentation. Recursively walks through its children with increased
     * indentation.
     * 
     * @param indentation The indentation. Contains only tabs. Never null.
     * 
     * @return This element as a string. Never null.
     */
    private String toString(String indentation) {
        StringBuilder result = new StringBuilder(indentation);
        
        if (getCondition() != null) {
            String conditionStr = getCondition().toString();
            
            if (conditionStr.length() > 64) {
                conditionStr = "...";
            }
            
            result.append("[").append(conditionStr).append("] ");
        }
        
        result.append(elementToString()).append('\n');
        
        indentation += '\t';
        
        for (int i = 0; i < getNestedElementCount(); i++) {
            result.append(((SrcMlSyntaxElement) getNestedElement(i)).toString(indentation));
        }
        
        return result.toString();
    }
    
    /**
     * Creates a single line string that describes this element. This will be used in the hierarchy display of
     * {@link #toString()}.
     * 
     * @return A string describing this element.
     */
    protected abstract String elementToString();
    
}
