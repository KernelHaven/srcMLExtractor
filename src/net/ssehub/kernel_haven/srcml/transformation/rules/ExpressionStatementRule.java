package net.ssehub.kernel_haven.srcml.transformation.rules;

import net.ssehub.kernel_haven.srcml.model.ExpressionStatement;
import net.ssehub.kernel_haven.srcml.model.SrcMlSyntaxElement;
import net.ssehub.kernel_haven.srcml.transformation.OtherElementMatcher;
import net.ssehub.kernel_haven.srcml.transformation.TransformationRule;

public class ExpressionStatementRule implements TransformationRule {

    private static final OtherElementMatcher MATCHER = new OtherElementMatcher("expr_stmt",
            new OtherElementMatcher("expr", (OtherElementMatcher[]) null),
            new OtherElementMatcher("text:;"));

    @Override
    public SrcMlSyntaxElement transform(SrcMlSyntaxElement element) {
        
        if (MATCHER.matches(element)) {
            
            element = new ExpressionStatement(element.getLineStart(), element.getLineEnd(), element.getSourceFile(),
                    element.getCondition(), element.getPresenceCondition(), element.getNestedElement(0));
            
        }
        
        return element;
    }
    
}
