package net.ssehub.kernel_haven.srcml.transformation.rules;

import net.ssehub.kernel_haven.srcml.model.CppElif;
import net.ssehub.kernel_haven.srcml.model.OtherSyntaxElement;
import net.ssehub.kernel_haven.srcml.model.SrcMlSyntaxElement;
import net.ssehub.kernel_haven.srcml.transformation.OtherElementMatcher;
import net.ssehub.kernel_haven.srcml.transformation.TransformationRule;
import net.ssehub.kernel_haven.util.logic.Formula;

public class CppElifRule implements TransformationRule {

    private static final OtherElementMatcher MATCHER = new OtherElementMatcher("cpp:elif", (OtherElementMatcher[]) null);
    
    @Override
    public SrcMlSyntaxElement transform(SrcMlSyntaxElement element) {
        
        if (MATCHER.matches(element)) {
            CppElif elifElement = new CppElif(element.getLineStart(), element.getLineEnd(), element.getSourceFile(),
                    element.getCondition(), element.getPresenceCondition());
            
            Formula condition = (Formula) ((OtherSyntaxElement) element).getAttribute("condition");
            if (condition != null) {
                elifElement.setElifCondition(condition);
            }
            
            element = elifElement;
        }
        
        return element;
    }

}
