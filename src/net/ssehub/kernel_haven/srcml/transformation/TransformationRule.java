package net.ssehub.kernel_haven.srcml.transformation;

import net.ssehub.kernel_haven.srcml.model.SrcMlSyntaxElement;

/**
 * A single transformation rule for transforming an {@link SrcMlSyntaxElement} tree.
 * 
 * @author Adam
 */
public interface TransformationRule {
    
    /**
     * Applies this transformation to the given element. This method is called on each element in the tree (root-first)
     * by the engine.
     * 
     * @param element The element to transform.
     * 
     * @return The result of the transformation. The unchanged input element if this rule does not apply.
     */
    public SrcMlSyntaxElement transform(SrcMlSyntaxElement element);

}
