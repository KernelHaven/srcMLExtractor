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
    
}
