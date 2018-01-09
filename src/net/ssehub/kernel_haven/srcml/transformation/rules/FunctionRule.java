package net.ssehub.kernel_haven.srcml.transformation.rules;

import java.util.LinkedList;
import java.util.List;

import net.ssehub.kernel_haven.code_model.CodeElement;
import net.ssehub.kernel_haven.srcml.model.Function;
import net.ssehub.kernel_haven.srcml.model.OtherSyntaxElement;
import net.ssehub.kernel_haven.srcml.model.SrcMlSyntaxElement;
import net.ssehub.kernel_haven.srcml.transformation.OtherElementMatcher;
import net.ssehub.kernel_haven.srcml.transformation.TransformationRule;

public class FunctionRule implements TransformationRule {

    private static final OtherElementMatcher MATCHER = new OtherElementMatcher("function",
            new OtherElementMatcher("type", (OtherElementMatcher[]) null),
            new OtherElementMatcher("name", new OtherElementMatcher(null)),
            new OtherElementMatcher("parameter_list", (OtherElementMatcher[]) null),
            new OtherElementMatcher("block", (OtherElementMatcher[]) null)
    );
    
    @Override
    public SrcMlSyntaxElement transform(SrcMlSyntaxElement element) {
        
        if (MATCHER.matches(element)) {
            
            OtherSyntaxElement nameElement = (OtherSyntaxElement) element.getNestedElement(1).getNestedElement(0);
            String name = nameElement.getName().substring("text:".length());
            
            // TODO: return type and parameter list
            
            Function function = new Function(name, element.getLineStart(), element.getLineEnd(),
                    element.getSourceFile(), element.getCondition(), element.getPresenceCondition());
            
            List<CodeElement> body = new LinkedList<>();
            for (CodeElement bodyElement : element.getNestedElement(3).iterateNestedElements()) {
                body.add(bodyElement);
            }
            
            // remove { and } text nodes
            CodeElement first = body.get(0);
            if (first instanceof OtherSyntaxElement) {
                OtherSyntaxElement firstOse = (OtherSyntaxElement) first;
                if (firstOse.getName().equals("text:{")) {
                    body.remove(0);
                }
            }
            CodeElement last = body.get(body.size() - 1);
            if (last instanceof OtherSyntaxElement) {
                OtherSyntaxElement lastOse = (OtherSyntaxElement) last;
                if (lastOse.getName().equals("text:}")) {
                    body.remove(body.size() - 1);
                }
            }
            
            for (CodeElement bodyElement : body) {
                function.addNestedElement(bodyElement);
            }
            
            element = function;
        }
        
        return element;
    }

}
