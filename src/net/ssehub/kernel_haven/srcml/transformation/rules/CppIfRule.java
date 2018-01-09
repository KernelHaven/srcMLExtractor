package net.ssehub.kernel_haven.srcml.transformation.rules;

import net.ssehub.kernel_haven.srcml.model.CppIf;
import net.ssehub.kernel_haven.srcml.model.OtherSyntaxElement;
import net.ssehub.kernel_haven.srcml.model.SrcMlSyntaxElement;
import net.ssehub.kernel_haven.srcml.transformation.OtherElementMatcher;
import net.ssehub.kernel_haven.srcml.transformation.TransformationRule;
import net.ssehub.kernel_haven.util.logic.Formula;

public class CppIfRule implements TransformationRule {

    private static final OtherElementMatcher MATCHER1 = new OtherElementMatcher("cpp:if", (OtherElementMatcher[]) null);
    private static final OtherElementMatcher MATCHER2 = new OtherElementMatcher("cpp:ifdef", (OtherElementMatcher[]) null);
    private static final OtherElementMatcher MATCHER3 = new OtherElementMatcher("cpp:ifndef", (OtherElementMatcher[]) null);
    
    @Override
    public SrcMlSyntaxElement transform(SrcMlSyntaxElement element) {
        
        if (MATCHER1.matches(element) || MATCHER2.matches(element) || MATCHER3.matches(element)) {
            CppIf ifElement = new CppIf(element.getLineStart(), element.getLineEnd(), element.getSourceFile(),
                    element.getCondition(), element.getPresenceCondition());
            
            Formula condition = (Formula) ((OtherSyntaxElement) element).getAttribute("condition");
            if (condition != null) {
                ifElement.setIfCondition(condition);
            }
            
            element = ifElement;
        }
        
        return element;
    }

}
