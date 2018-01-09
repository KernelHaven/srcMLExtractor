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
    
    private static final OtherElementMatcher PARAMETER_MATCHER = new OtherElementMatcher("parameter",
            new OtherElementMatcher("decl", 
                    new OtherElementMatcher("type", (OtherElementMatcher[]) null),
                    new OtherElementMatcher("name", new OtherElementMatcher(null))
            )
    );
    
    @Override
    public SrcMlSyntaxElement transform(SrcMlSyntaxElement element) {
        
        if (MATCHER.matches(element)) {
            
            OtherSyntaxElement nameElement = (OtherSyntaxElement) element.getNestedElement(1).getNestedElement(0);
            String name = nameElement.getName().substring("text:".length());
            
            // TODO: return type, parameter types
            
            Function function = new Function(name, element.getLineStart(), element.getLineEnd(),
                    element.getSourceFile(), element.getCondition(), element.getPresenceCondition());
            
            for (CodeElement paramElement : element.getNestedElement(2).iterateNestedElements()) {
                if (PARAMETER_MATCHER.matches(paramElement)) {
                    OtherSyntaxElement paramNameElement = (OtherSyntaxElement) paramElement.getNestedElement(0)
                            .getNestedElement(1).getNestedElement(0);
                    
                    String paramName = paramNameElement.getName().substring("text:".length());
                    
                    function.addParameter(paramName);
                }
            }
            
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
            
            // if the function is empty, it contains one node with "text:{\n\n}"
            if (body.size() == 1 && body.get(0) instanceof OtherSyntaxElement) {
                OtherSyntaxElement ose = (OtherSyntaxElement) body.get(0);
                if (ose.getName().startsWith("text:{") && ose.getName().endsWith("}")) {
                    body.remove(0);
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
