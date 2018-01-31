package net.ssehub.kernel_haven.srcml.transformation.testing;

import java.util.ArrayList;
import java.util.List;

public class PreprocessorIf extends PreprocessorBlock {
    
    private List<PreprocessorElse> siblings = new ArrayList<>();
    
    public PreprocessorIf(Type type, String condition) {
        super(type, condition);
    }
    
    public void addSibling(PreprocessorElse sibling) {
        siblings.add(sibling);
    }
    
    public int getNumberOfElseBlocks() {
        return siblings.size();
    }
    
    public PreprocessorElse getElseBlock(int index) {
        return siblings.get(index);
    }
    
    @Override
    public String getEffectiveCondition() {
        return getCondition();
    }
}
