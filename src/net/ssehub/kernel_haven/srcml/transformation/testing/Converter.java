package net.ssehub.kernel_haven.srcml.transformation.testing;

import java.util.List;

import net.ssehub.kernel_haven.srcml.transformation.testing.PreprocessorIf.Type;

public class Converter {

    public void replaceCPPs(ITranslationUnit base) {
        for (ITranslationUnit iTranslationUnit : base) {
            replaceCPPs(base, iTranslationUnit);
        }
    }

    private void replaceCPPs(ITranslationUnit parent, ITranslationUnit child) {
        if (child.getType().startsWith("cpp:if") && child instanceof TranslationUnit) {
            TranslationUnit oldUnit = (TranslationUnit) child;
            List<String> tokens = oldUnit.getTokenList();
            PreprocessorIf newUnit = null;
            StringBuffer condition = new StringBuffer();
            for (int i = 2; i < tokens.size(); i++) {
                condition.append(tokens.get(i));
            }
            switch (tokens.get(1)) {
            case "ifdef":
                newUnit = new PreprocessorIf(Type.IFDEF, condition.toString());
                break;
            case "ifndef":
                newUnit = new PreprocessorIf(Type.IFDEF, "!" + condition.toString());
                break;
            case "if":
                newUnit = new PreprocessorIf(Type.IF, condition.toString());
                break;
            }
            if (null != newUnit) {
                parent.replaceNested(oldUnit, newUnit);
            }
        } else {
            for (ITranslationUnit iTranslationUnit : child) {
                replaceCPPs(child, iTranslationUnit);
            }
        }
    }
}
