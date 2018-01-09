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
            
            String filename;
            if (fileNode.getNestedElementCount() == 3) {
                OtherSyntaxElement fileNameNode = (OtherSyntaxElement) fileNode.getNestedElement(1);
                filename = fileNameNode.getName().substring("text:".length());
            } else {
                OtherSyntaxElement fileNameNode = (OtherSyntaxElement) fileNode.getNestedElement(0);
                filename = fileNameNode.getName().substring("text:".length());
                if (filename.startsWith("\"") && filename.endsWith("\"")) {
                    filename = filename.substring(1, filename.length() - 1);
                }
            }

            // TODO: created specialized CppInclude syntax element?
            element = new OtherSyntaxElement("Include: " + filename, element.getLineStart(), element.getLineEnd(),
                    element.getSourceFile(), element.getCondition(), element.getPresenceCondition());
        }
            
        
        return element;
    }

}
