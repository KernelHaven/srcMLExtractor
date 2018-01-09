package net.ssehub.kernel_haven.srcml.transformation.rules;

import net.ssehub.kernel_haven.srcml.model.EmptyStatement;
import net.ssehub.kernel_haven.srcml.model.SrcMlSyntaxElement;
import net.ssehub.kernel_haven.srcml.transformation.OtherElementMatcher;
import net.ssehub.kernel_haven.srcml.transformation.TransformationRule;

public class EmptyStatementRule implements TransformationRule {

    private static final OtherElementMatcher MATCHER = new OtherElementMatcher("empty_stmt", (OtherElementMatcher[]) null);
    
    @Override
    public SrcMlSyntaxElement transform(SrcMlSyntaxElement element) {
        
        if (MATCHER.matches(element)) {
            element = new EmptyStatement(element.getLineStart(), element.getLineEnd(), element.getSourceFile(),
                    element.getCondition(), element.getPresenceCondition());
        }
        
        return element;
    }

}
