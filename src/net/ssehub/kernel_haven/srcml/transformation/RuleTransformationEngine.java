package net.ssehub.kernel_haven.srcml.transformation;

import java.util.ArrayList;
import java.util.List;

import net.ssehub.kernel_haven.code_model.CodeElement;
import net.ssehub.kernel_haven.srcml.model.OtherSyntaxElement;
import net.ssehub.kernel_haven.srcml.model.SrcMlSyntaxElement;
import net.ssehub.kernel_haven.srcml.transformation.rules.CppIncludeRule;
import net.ssehub.kernel_haven.srcml.transformation.rules.TranslationUnitRule;

/**
 * A class for transforming a tree of {@link SrcMlSyntaxElement}s based on a set of given rules.
 * 
 * @author Adam
 */
public class RuleTransformationEngine {

    private List<TransformationRule> rules;
    
    /**
     * Creates this engine with the default set of rules.
     */
    public RuleTransformationEngine() {
        rules = new ArrayList<>();
        
        // TODO add rules here
        rules.add(new TranslationUnitRule());
        rules.add(new CppIncludeRule());
    }
    
    /**
     * Runs this engine on the given {@link SrcMlSyntaxElement} hierarchy.
     * 
     * @param topElement The root element of the hierarchy.
     * 
     * @return The transformed hierarchy.
     */
    public SrcMlSyntaxElement transform(SrcMlSyntaxElement topElement) {
        for (TransformationRule rule : rules) {
            topElement = rule.transform(topElement);
        }
        
        int i = 0;
        for (CodeElement child : topElement.iterateNestedElements()) {
            if (child instanceof SrcMlSyntaxElement) {
                child = transform((SrcMlSyntaxElement) child);
                topElement.setNestedElement(i, (SrcMlSyntaxElement) child);
            }
            i++;
        }
        
        return topElement;
    }
    
    /**
     * Checks if the given element is an instance of {@link OtherSyntaxElement} and has the given name.
     * 
     * @param element The element to check.
     * @param expectedName The name to check.
     * 
     * @return Whether the given element is an {@link OtherSyntaxElement} and has the given name.
     */
    public static boolean isOtherSyntaxElementWithName(CodeElement element, String expectedName) {
        return element instanceof OtherSyntaxElement && ((OtherSyntaxElement) element).getName().equals(expectedName);
    }
    
}
