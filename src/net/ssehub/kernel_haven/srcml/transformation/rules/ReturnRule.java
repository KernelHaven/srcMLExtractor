package net.ssehub.kernel_haven.srcml.transformation.rules;

import net.ssehub.kernel_haven.srcml.model.ReturnStatement;
import net.ssehub.kernel_haven.srcml.model.SrcMlSyntaxElement;
import net.ssehub.kernel_haven.srcml.transformation.OtherElementMatcher;
import net.ssehub.kernel_haven.srcml.transformation.TransformationRule;

public class ReturnRule implements TransformationRule {

    private static final OtherElementMatcher MATCHER = new OtherElementMatcher("return", (OtherElementMatcher[]) null);
    
    private static final OtherElementMatcher EXPR = new OtherElementMatcher("expr", (OtherElementMatcher[]) null);
    
    @Override
    public SrcMlSyntaxElement transform(SrcMlSyntaxElement element) {
        
        if (MATCHER.matches(element)) {
            ReturnStatement ret = new ReturnStatement(element.getLineStart(), element.getLineEnd(),
                    element.getSourceFile(), element.getCondition(), element.getPresenceCondition());
            
            if (element.getNestedElementCount() == 1) {
                element = ret;
                
            } else if (element.getNestedElementCount() == 3 && EXPR.matches(element.getNestedElement(1))) {
                ret.setNestedElement(0, element.getNestedElement(1));
                element = ret;
                
            }
            
        }
        
        return element;
    }

}
