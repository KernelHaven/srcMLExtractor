package net.ssehub.kernel_haven.srcml.transformation.rules;

import java.util.LinkedList;
import java.util.List;

import net.ssehub.kernel_haven.code_model.CodeElement;
import net.ssehub.kernel_haven.srcml.model.CppIf;
import net.ssehub.kernel_haven.srcml.model.OtherSyntaxElement;
import net.ssehub.kernel_haven.srcml.model.SrcMlSyntaxElement;
import net.ssehub.kernel_haven.srcml.transformation.OtherElementMatcher;
import net.ssehub.kernel_haven.srcml.transformation.TransformationRule;
import net.ssehub.kernel_haven.util.logic.Formula;

public class CppIfRule implements TransformationRule {

    private static final OtherElementMatcher IF = new OtherElementMatcher("cpp:if", (OtherElementMatcher[]) null);
    private static final OtherElementMatcher IFDEF = new OtherElementMatcher("cpp:ifdef", (OtherElementMatcher[]) null);
    private static final OtherElementMatcher IFNDEF = new OtherElementMatcher("cpp:ifndef", (OtherElementMatcher[]) null);
    
    private static final OtherElementMatcher ELIF = new OtherElementMatcher("cpp:elif", (OtherElementMatcher[]) null);
    private static final OtherElementMatcher ELSE = new OtherElementMatcher("cpp:else", (OtherElementMatcher[]) null);
    private static final OtherElementMatcher ENDIF = new OtherElementMatcher("cpp:endif", (OtherElementMatcher[]) null);
    
    @Override
    public SrcMlSyntaxElement transform(SrcMlSyntaxElement element) {
        
        int ifNode = -1;
        List<Integer> elifNodes = new LinkedList<>();
        int elseNode = -1;
        
        int depth = 0;
        for (int i = 0; i < element.getNestedElementCount(); i++) {
            CodeElement child = element.getNestedElement(i);
            
            if (IF.matches(child) || IFDEF.matches(child) || IFNDEF.matches(child)) {
                depth++;
                
                if (depth == 1) {
                    if (ifNode != -1) {
                        throw new IllegalStateException("Already got an if node for this depth!");
                    }
                    ifNode = i;
                }
            }
            
            if (ELIF.matches(child) && depth == 1) {
                elifNodes.add(i);
            }
            
            if (ELSE.matches(child) && depth == 1) {
                if (elseNode != -1) {
                    throw new IllegalStateException("Already got an else node for this depth!");
                }
                elseNode = i;
            }
            
            if (ENDIF.matches(child)) {
                depth--;
                
                if (depth == 0) {
                    CppIf cppIf = buildIf(element, ifNode, elifNodes, elseNode, i);
                    element.setNestedElement(ifNode, cppIf);
                    
                    for (int j = ifNode + 1; j <= i; j++) {
                        element.setNestedElement(ifNode + 1, null);
                    }
                    
                    ifNode = -1;
                    elseNode = -1;
                    elifNodes.clear();
                }
            }
            
        }
        
        return element;
    }
    
    private CppIf buildIf(SrcMlSyntaxElement parent, int ifPos, List<Integer> elifPos, int elsePos, int endifPos) {
        OtherSyntaxElement ifNode = (OtherSyntaxElement) parent.getNestedElement(ifPos);
        
        CppIf cppIf = new CppIf(ifNode.getLineStart(), ifNode.getLineEnd(), ifNode.getSourceFile(),
                ifNode.getCondition(), ifNode.getPresenceCondition());
        
        cppIf.setIfCondition((Formula) ifNode.getAttribute("condition"));
        
        cppIf.setNumElifBlocks(elifPos.size());
        for (int i = 0; i < elifPos.size(); i++) {
            OtherSyntaxElement elifNode = (OtherSyntaxElement) parent.getNestedElement(elifPos.get(i));
            cppIf.setElifCondition(i, (Formula) elifNode.getAttribute("condition"));
        }
        
        for (int i = ifPos + 1; i < endifPos; i++) {
            SrcMlSyntaxElement child = (SrcMlSyntaxElement) parent.getNestedElement(i);
            
            boolean found = false;
            boolean matchesCppNode = false;
            
            if (elsePos != -1 && i > elsePos) {
                found = true;
                cppIf.addElseElement(child);
            }
            if (elsePos != -1 && i == elsePos) {
                matchesCppNode = true;
            }

            for (int j = elifPos.size() - 1; j >= 0 && !found && !matchesCppNode; j--) {
                if (i == elifPos.get(j)) {
                    matchesCppNode = true;
                }
                if (i > elifPos.get(j)) {
                    found = true;
                    cppIf.addElifElement(j, child);
                }
            }
            
            if (!found  && !matchesCppNode) {
                cppIf.addThenElement(child);
            }
            
        }
        
        return cppIf;
    }

}
