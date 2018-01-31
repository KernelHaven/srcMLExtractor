package net.ssehub.kernel_haven.srcml.transformation.testing.rules;

import java.util.List;
import java.util.Stack;

import net.ssehub.kernel_haven.srcml.transformation.testing.ITranslationUnit;
import net.ssehub.kernel_haven.srcml.transformation.testing.PreprocessorBlock;
import net.ssehub.kernel_haven.srcml.transformation.testing.PreprocessorElse;
import net.ssehub.kernel_haven.srcml.transformation.testing.PreprocessorEndIf;
import net.ssehub.kernel_haven.srcml.transformation.testing.PreprocessorIf;
import net.ssehub.kernel_haven.srcml.transformation.testing.TranslationUnit;
import net.ssehub.kernel_haven.srcml.transformation.testing.PreprocessorBlock.Type;

/**
 * Translates preprocessor control structures into a simpler form for parsing. More precisely it translates:<br/>
 * <tt>#ifdef, #if, #else, #elseif, #endif</tt><br/>
 * This rule converts {@link TranslationUnit}s into {@link PreprocessorBlock}s
 * @author El-Sharkawy
 *
 */
public class PreprocessorTranslation implements ITransformationRule {
    private Stack<PreprocessorIf> parents = new Stack<>();

    @Override
    public void transform(ITranslationUnit base) {
        for (ITranslationUnit nested : base) {
            replaceCPPs(base, nested);
        }
    }

    /**
     * Recursive translation function.
     * @param parent The parent (translated elements will be exchanges in the parent).
     * @param child The child (to check and to translate)
     */
    private void replaceCPPs(ITranslationUnit parent, ITranslationUnit child) {
        if (child instanceof TranslationUnit) {
            TranslationUnit oldUnit = (TranslationUnit) child;
            if (oldUnit.getType().startsWith("cpp:if")) {
                // if, ifdef, ifndef
                createIf(parent, oldUnit);
            } else if (oldUnit.getType().startsWith("cpp:el")) {
                // else, elif
                createElse(parent, oldUnit);
            } else if (oldUnit.getType().startsWith("cpp:endif")) {
                // endif
                parents.pop();
                parent.replaceNested(oldUnit, new PreprocessorEndIf());
            }
        }
        
        for (ITranslationUnit nested : child) {
            replaceCPPs(child, nested);
        }
    }

    private void createElse(ITranslationUnit parent, TranslationUnit child) {
        PreprocessorIf startingIf = parents.peek();
        List<String> tokens = child.getTokenList();
        PreprocessorBlock newUnit = null;
        StringBuffer condition = new StringBuffer();
        for (int i = 2; i < tokens.size(); i++) {
            condition.append(tokens.get(i));
        }
        switch (tokens.get(1)) {
        case "else":
            newUnit = new PreprocessorElse(Type.ELSE, null, startingIf);
            startingIf.addSibling(newUnit);
            break;
        case "elif":
            newUnit = new PreprocessorElse(Type.ELSEIF, condition.toString(), startingIf);
            startingIf.addSibling(newUnit);
            break;
        }
        if (null != newUnit) {
            parent.replaceNested(child, newUnit);
        }
    }

    private void createIf(ITranslationUnit parent, TranslationUnit child) {
        List<String> tokens = child.getTokenList();
        PreprocessorIf newUnit = null;
        StringBuffer condition = new StringBuffer();
        for (int i = 2; i < tokens.size(); i++) {
            condition.append(tokens.get(i));
        }
        switch (tokens.get(1)) {
        case "ifdef":
            newUnit = new PreprocessorIf(Type.IFDEF, "defined(" + condition.toString() + ")");
            break;
        case "ifndef":
            newUnit = new PreprocessorIf(Type.IFDEF, "!defined(" + condition.toString() + ")");
            break;
        case "if":
            newUnit = new PreprocessorIf(Type.IF, condition.toString());
            break;
        }
        if (null != newUnit) {
            parent.replaceNested(child, newUnit);
            parents.push(newUnit);
        }
    }
}
