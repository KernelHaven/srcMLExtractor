package net.ssehub.kernel_haven.srcml.transformation.rules;

import net.ssehub.kernel_haven.srcml.model.OtherSyntaxElement;
import net.ssehub.kernel_haven.srcml.model.SrcMlSyntaxElement;
import net.ssehub.kernel_haven.srcml.transformation.OtherElementMatcher;
import net.ssehub.kernel_haven.srcml.transformation.TransformationRule;

public class CppIncludeRule implements TransformationRule {

    private static final OtherElementMatcher MATCHER = new OtherElementMatcher("cpp:include",
            new OtherElementMatcher("text:#"),
            new OtherElementMatcher("cpp:directive", 
                    new OtherElementMatcher("text:include")
            ),
            new OtherElementMatcher("cpp:file", (OtherElementMatcher[]) null)
    );
    
    @Override
    public SrcMlSyntaxElement transform(SrcMlSyntaxElement element) {
        
        if (MATCHER.matches(element)) {
            OtherSyntaxElement fileNode = (OtherSyntaxElement) element.getNestedElement(2);
            
            OtherSyntaxElement fileNameNode;
            if (fileNode.getNestedElementCount() == 3) {
                fileNameNode = (OtherSyntaxElement) fileNode.getNestedElement(1);
            } else {
                fileNameNode = (OtherSyntaxElement) fileNode.getNestedElement(0);
            }

            element = new OtherSyntaxElement("Include: " + fileNameNode.getName(), element.getPresenceCondition());
        }
            
        
        return element;
    }

}
