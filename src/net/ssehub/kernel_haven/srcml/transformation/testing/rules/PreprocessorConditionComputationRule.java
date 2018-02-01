package net.ssehub.kernel_haven.srcml.transformation.testing.rules;

import java.util.ArrayList;
import java.util.List;

import net.ssehub.kernel_haven.srcml.transformation.testing.ITranslationUnit;
import net.ssehub.kernel_haven.srcml.transformation.testing.PreprocessorBlock.Type;
import net.ssehub.kernel_haven.srcml.transformation.testing.PreprocessorElse;
import net.ssehub.kernel_haven.srcml.transformation.testing.PreprocessorIf;

public class PreprocessorConditionComputationRule implements ITransformationRule {

    @Override
    public void transform(ITranslationUnit node) {
        for (int j = 0; j < node.size(); j++) {
            ITranslationUnit nested = node.getNestedElement(j);
            
            if (nested instanceof PreprocessorElse) {
                PreprocessorElse elseStatement = (PreprocessorElse) nested;
                PreprocessorIf start = elseStatement.getStartingIf();
                List<PreprocessorElse> previous = new ArrayList<>();
                boolean found = false;
                for (int i = 0; i < start.getNumberOfElseBlocks() && !found; i++) {
                    PreprocessorElse sibling = start.getElseBlock(i);
                    if (sibling != elseStatement) {
                        previous.add(sibling);
                    } else {
                        found = true;
                    }
                }
                StringBuffer effectiveCondition = new StringBuffer("!");
                effectiveCondition.append(start.getCondition());
                for (PreprocessorElse preprocessorBlock : previous) {
                    effectiveCondition.append(" && !");
                    effectiveCondition.append(preprocessorBlock.getCondition());
                }
                if (!elseStatement.getType().equals(Type.ELSE.name())) {
                    effectiveCondition.append(" && !");
                    effectiveCondition.append(elseStatement.getCondition());
                }
                elseStatement.setEffectiveCondition(effectiveCondition.toString());
            }
            
            transform(nested);
        }
        
    }

}
