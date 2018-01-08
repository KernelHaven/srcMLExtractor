package net.ssehub.kernel_haven.srcml.transformation;

import java.util.Arrays;
import java.util.List;

import net.ssehub.kernel_haven.code_model.CodeElement;
import net.ssehub.kernel_haven.srcml.model.OtherSyntaxElement;

public class OtherElementMatcher {

    private String expectedName;
    
    private List<OtherElementMatcher> childreen;
    
    public OtherElementMatcher(String expectedName, OtherElementMatcher... expectedChildreen) {
        this.expectedName = expectedName;
        if (expectedChildreen != null) {
            childreen = Arrays.asList(expectedChildreen);
        }
    }
    
    public boolean matches(CodeElement element) {
        boolean result = false;
        
        if (element instanceof OtherSyntaxElement) {
            OtherSyntaxElement ose = (OtherSyntaxElement) element;
            
            if (expectedName == null || ose.getName().equals(expectedName)) {
                
                if (childreen == null) {
                    result = true;
                    
                } else if (ose.getNestedElementCount() == childreen.size()) {
                    result = true;
                    
                    for (int i = 0; i < ose.getNestedElementCount(); i++) {
                        result &= childreen.get(i).matches(ose.getNestedElement(i));
                    }
                }
                
            }
            
        }
        
        return result;
    }
    
}
