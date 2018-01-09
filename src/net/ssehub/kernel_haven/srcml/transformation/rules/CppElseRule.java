package net.ssehub.kernel_haven.srcml.transformation.rules;

import net.ssehub.kernel_haven.srcml.model.CppElse;
import net.ssehub.kernel_haven.srcml.model.SrcMlSyntaxElement;
import net.ssehub.kernel_haven.srcml.transformation.OtherElementMatcher;
import net.ssehub.kernel_haven.srcml.transformation.TransformationRule;

public class CppElseRule implements TransformationRule {

    private static final OtherElementMatcher MATCHER = new OtherElementMatcher("cpp:else", (OtherElementMatcher[]) null);
    
    @Override
    public SrcMlSyntaxElement transform(SrcMlSyntaxElement element) {
        
        if (MATCHER.matches(element)) {
            element = new CppElse(element.getLineStart(), element.getLineEnd(), element.getSourceFile(),
                    element.getCondition(), element.getPresenceCondition());
        }
        
        return element;
    }

}
