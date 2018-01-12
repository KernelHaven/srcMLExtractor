package net.ssehub.kernel_haven.srcml.transformation;

import java.util.ArrayList;
import java.util.List;

import net.ssehub.kernel_haven.code_model.CodeElement;
import net.ssehub.kernel_haven.srcml.model.CppIf;
import net.ssehub.kernel_haven.srcml.model.OtherSyntaxElement;
import net.ssehub.kernel_haven.srcml.model.SrcMlSyntaxElement;
import net.ssehub.kernel_haven.srcml.transformation.rules.CppIfRule;
import net.ssehub.kernel_haven.srcml.transformation.rules.CppIncludeRule;
import net.ssehub.kernel_haven.srcml.transformation.rules.EmptyStatementRule;
import net.ssehub.kernel_haven.srcml.transformation.rules.FunctionRule;
import net.ssehub.kernel_haven.srcml.transformation.rules.TranslationUnitRule;

/**
 * A class for transforming a tree of {@link SrcMlSyntaxElement}s based on a set of given rules.
 * 
 * @author Adam
 */
public class RuleTransformationEngine {

    private List<TransformationRule> rulesPass0;
    
    private List<TransformationRule> rulesPass1;
    
    /**
     * Creates this engine with the default set of rules.
     */
    public RuleTransformationEngine() {
        rulesPass0 = new ArrayList<>();
        rulesPass1 = new ArrayList<>();
        
        // TODO add rules here
        rulesPass0.add(new CppIfRule());
        
        
        rulesPass1.add(new CppIncludeRule());
        
        rulesPass1.add(new TranslationUnitRule());
        rulesPass1.add(new FunctionRule());
        
        rulesPass1.add(new EmptyStatementRule());
    }
    
    /**
     * Runs this engine on the given {@link SrcMlSyntaxElement} hierarchy.
     * 
     * @param element The element to transform.
     * 
     * @return The transformed hierarchy.
     */
    public SrcMlSyntaxElement transform(SrcMlSyntaxElement element) {
        return transformImpl(transformImpl(element, rulesPass0), rulesPass1);
    }
    
    private SrcMlSyntaxElement transformImpl(SrcMlSyntaxElement element, List<TransformationRule> rules) {
        for (TransformationRule rule : rules) {
            element = rule.transform(element);
        }
        
        for (int i = 0; i < element.getNestedElementCount(); i++) {
            CodeElement child = element.getNestedElement(i);
            if (child instanceof SrcMlSyntaxElement) {
                child = transformImpl((SrcMlSyntaxElement) child, rules);
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
    
    /**
     * Recursively checks whether there are any {@link CppIf} elements nested inside this element.
     * 
     * @param element The element to search in.
     * 
     * @return Whether there was a {@link CppIf} inside the given element or not.
     */
    public static boolean containsCppIf(CodeElement element) {
        if (element instanceof CppIf) {
            return true;
        }
        
        for (int i = 0; i < element.getNestedElementCount(); i++) {
            if (containsCppIf(element.getNestedElement(i))) {
                return true;
            }
        }
        
        return false;
    }
    
}
