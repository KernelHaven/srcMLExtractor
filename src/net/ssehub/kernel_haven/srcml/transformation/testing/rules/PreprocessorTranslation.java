package net.ssehub.kernel_haven.srcml.transformation.testing.rules;

import java.util.ArrayDeque;
import java.util.Deque;

import net.ssehub.kernel_haven.srcml.transformation.testing.CodeUnit;
import net.ssehub.kernel_haven.srcml.transformation.testing.ITranslationUnit;
import net.ssehub.kernel_haven.srcml.transformation.testing.PreprocessorBlock;
import net.ssehub.kernel_haven.srcml.transformation.testing.PreprocessorBlock.Type;
import net.ssehub.kernel_haven.srcml.transformation.testing.PreprocessorElse;
import net.ssehub.kernel_haven.srcml.transformation.testing.PreprocessorEndIf;
import net.ssehub.kernel_haven.srcml.transformation.testing.PreprocessorIf;
import net.ssehub.kernel_haven.srcml.transformation.testing.TranslationUnit;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Translates preprocessor control structures into a simpler form for parsing. More precisely it translates:<br/>
 * <tt>#ifdef, #if, #else, #elseif, #endif</tt><br/>
 * This rule converts {@link TranslationUnit}s into {@link PreprocessorBlock}s
 * @author El-Sharkawy
 *
 */
public class PreprocessorTranslation implements ITransformationRule {
    private Deque<PreprocessorIf> parents = new ArrayDeque<>();

    @Override
    public void transform(ITranslationUnit base) {
        for (int i = 0; i < base.size(); i++) {
            replaceCPPs(base, base.getNestedElement(i));
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
                parents.removeFirst();
                parent.replaceNested(oldUnit, new PreprocessorEndIf());
            }
        }
        
        for (int i = 0; i < child.size(); i++) {
            replaceCPPs(child, child.getNestedElement(i));
        }
    }

    /**
     * Translates an <tt>&#35;else</tt> or <tt>&#35;elif</tt> statement into a {@link PreprocessorElse}.
     * @param parent The parent to exchange the translated element.
     * @param child An <tt>&#35;else</tt> or <tt>&#35;elif</tt> statement to be translated
     *     into a {@link PreprocessorElse}
     */
    private void createElse(@NonNull ITranslationUnit parent, TranslationUnit child) {
        PreprocessorIf startingIf = parents.peekFirst();
        PreprocessorElse newUnit = null;
        StringBuffer condition = new StringBuffer();
        for (int i = 2; i < child.size(); i++) {
            condition.append(((CodeUnit)child.getNestedElement(i)).getCode());
        }
        switch (((CodeUnit)child.getNestedElement(1)).getCode()) {
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

    /**
     * Translates an <tt>&#35;if</tt>, <tt>&#35;ifdef</tt>, or <tt>&#35;ifndef</tt>
     * statement into a {@link PreprocessorIf}.
     * @param parent The parent to exchange the translated element.
     * @param child An <tt>&#35;if</tt>, <tt>&#35;ifdef</tt>, or <tt>&#35;ifndef</tt> statement to be translated
     *     into a {@link PreprocessorIf}
     */
    private void createIf(ITranslationUnit parent, TranslationUnit child) {
        PreprocessorIf newUnit = null;
        StringBuffer condition = new StringBuffer();
        for (int i = 2; i < child.size(); i++) {
            condition.append(((CodeUnit)child.getNestedElement(i)).getCode());
        }
        switch (((CodeUnit)child.getNestedElement(1)).getCode()) {
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
            parents.addFirst(newUnit);
        }
    }
}
