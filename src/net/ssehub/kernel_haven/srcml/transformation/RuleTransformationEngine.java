package net.ssehub.kernel_haven.srcml.transformation;

import java.util.ArrayList;
import java.util.List;

import net.ssehub.kernel_haven.code_model.CodeElement;
import net.ssehub.kernel_haven.srcml.model.OtherSyntaxElement;
import net.ssehub.kernel_haven.srcml.model.SrcMlSyntaxElement;
import net.ssehub.kernel_haven.srcml.transformation.rules.CppElifRule;
import net.ssehub.kernel_haven.srcml.transformation.rules.CppElseRule;
import net.ssehub.kernel_haven.srcml.transformation.rules.CppEndifRule;
import net.ssehub.kernel_haven.srcml.transformation.rules.CppIfRule;
import net.ssehub.kernel_haven.srcml.transformation.rules.CppIncludeRule;
import net.ssehub.kernel_haven.srcml.transformation.rules.FunctionRule;
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
        rules.add(new FunctionRule());
        
        rules.add(new CppIncludeRule());
        
        rules.add(new CppIfRule());
        rules.add(new CppElifRule());
        rules.add(new CppElseRule());
        rules.add(new CppEndifRule());
    }
    
    /**
     * Runs this engine on the given {@link SrcMlSyntaxElement} hierarchy.
     * 
     * @param element The element to transform.
     * 
     * @return The transformed hierarchy.
     */
    public SrcMlSyntaxElement transform(SrcMlSyntaxElement element) {
        for (TransformationRule rule : rules) {
            element = rule.transform(element);
        }
        
        for (int i = 0; i < element.getNestedElementCount(); i++) {
            CodeElement child = element.getNestedElement(i);
            if (child instanceof SrcMlSyntaxElement) {
                child = transform((SrcMlSyntaxElement) child);
                element.setNestedElement(i, (SrcMlSyntaxElement) child);
            }
        }
        
        return element;
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
