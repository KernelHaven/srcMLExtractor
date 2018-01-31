package net.ssehub.kernel_haven.srcml.transformation.testing;

import java.util.ArrayList;
import java.util.List;

public class PreprocessorIf extends PreprocessorBlock {
    
    private List<PreprocessorBlock> siblings = new ArrayList<>();
    
    public PreprocessorIf(Type type, String condition) {
        super(type, condition);
    }
    
    public void addSibling(PreprocessorBlock sibling) {
        siblings.add(sibling);
    }
    
}
