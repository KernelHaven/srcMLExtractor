package net.ssehub.kernel_haven.srcml.transformation.rules;

import net.ssehub.kernel_haven.srcml.model.File;
import net.ssehub.kernel_haven.srcml.model.OtherSyntaxElement;
import net.ssehub.kernel_haven.srcml.model.SrcMlSyntaxElement;
import net.ssehub.kernel_haven.srcml.transformation.RuleTransformationEngine;
import net.ssehub.kernel_haven.srcml.transformation.TransformationRule;

public class TranslationUnitRule implements TransformationRule {

    @Override
    public SrcMlSyntaxElement transform(SrcMlSyntaxElement element) {
        
        if (RuleTransformationEngine.isOtherSyntaxElementWithName(element, "unit")) {
            OtherSyntaxElement ose = (OtherSyntaxElement) element;
            
            element = new File(element.getLineStart(), element.getLineEnd(), element.getSourceFile(),
                    element.getCondition(), element.getPresenceCondition());
            
            for (SrcMlSyntaxElement child : ose.iterateNestedOtherSyntaxElements()) {
                element.addNestedElement(child);
            }
        }
        
        return element;
    }

}
