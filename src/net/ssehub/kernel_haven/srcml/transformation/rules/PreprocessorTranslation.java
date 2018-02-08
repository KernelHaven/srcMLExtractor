package net.ssehub.kernel_haven.srcml.transformation.rules;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ssehub.kernel_haven.srcml.transformation.CodeUnit;
import net.ssehub.kernel_haven.srcml.transformation.ITranslationUnit;
import net.ssehub.kernel_haven.srcml.transformation.PreprocessorBlock;
import net.ssehub.kernel_haven.srcml.transformation.PreprocessorBlock.Type;
import net.ssehub.kernel_haven.srcml.transformation.PreprocessorElse;
import net.ssehub.kernel_haven.srcml.transformation.PreprocessorEndIf;
import net.ssehub.kernel_haven.srcml.transformation.PreprocessorIf;
import net.ssehub.kernel_haven.srcml.transformation.TranslationUnit;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Translates preprocessor control structures into a simpler form for parsing. More precisely it translates:<br/>
 * <tt>#ifdef, #if, #else, #elseif, #endif</tt><br/>
 * This rule converts {@link TranslationUnit}s into {@link PreprocessorBlock}s
 * @author El-Sharkawy
 *
 */
public class PreprocessorTranslation implements ITransformationRule {
    
    private static final @NonNull Pattern VARIABLE_PATTERN = notNull(Pattern.compile("[A-Za-z0-9_]+"));
    
    private @NonNull Deque<@NonNull PreprocessorIf> parents = new ArrayDeque<>();

    @Override
    public void transform(@NonNull ITranslationUnit base) throws FormatException {
        for (int i = 0; i < base.size(); i++) {
            replaceCPPs(base, base.getNestedElement(i));
        }
    }

    /**
     * Recursive translation function.
     * @param parent The parent (translated elements will be exchanges in the parent).
     * @param child The child (to check and to translate)
     */
    private void replaceCPPs(@NonNull ITranslationUnit parent, @NonNull ITranslationUnit child) {
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
                PreprocessorEndIf endIf = new PreprocessorEndIf();
                endIf.setStartLine(oldUnit.getStartLine());
                endIf.setEndLine(oldUnit.getEndLine());
                parent.replaceNested(oldUnit, endIf);
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
    private void createElse(@NonNull ITranslationUnit parent, @NonNull TranslationUnit child) {
        PreprocessorIf startingIf = notNull(parents.peekFirst());
        PreprocessorElse newUnit = null;
        switch (((CodeUnit)child.getNestedElement(1)).getCode()) {
        case "else":
            newUnit = new PreprocessorElse(Type.ELSE, null, startingIf);
            startingIf.addSibling(newUnit);
            break;
        case "elif":
            newUnit = new PreprocessorElse(Type.ELSEIF, getCondition(child, true), startingIf);
            startingIf.addSibling(newUnit);
            break;
        }
        if (null != newUnit) {
            newUnit.setStartLine(child.getStartLine());
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
    private void createIf(@NonNull ITranslationUnit parent, @NonNull TranslationUnit child) {
        PreprocessorIf newUnit = null;
        switch (((CodeUnit)child.getNestedElement(1)).getCode()) {
        case "ifdef":
            newUnit = new PreprocessorIf(Type.IFDEF, "defined(" + getCondition(child, false) + ")");
            break;
        case "ifndef":
            newUnit = new PreprocessorIf(Type.IFNDEF, "!defined(" + getCondition(child, false) + ")");
            break;
        case "if":
            newUnit = new PreprocessorIf(Type.IF, getCondition(child, true));
            break;
        }
        if (null != newUnit) {
            newUnit.setStartLine(child.getStartLine());
            parent.replaceNested(child, newUnit);
            parents.addFirst(newUnit);
        }
    }
    
    private @NonNull String getCondition(@NonNull TranslationUnit unit, boolean replaceMissingdefined) {
        List<String> parts = new ArrayList<>(unit.size() - 2);
        
        for (int i = 2; i < unit.size(); i++) {
            ITranslationUnit conditionPart = unit.getNestedElement(i);
            
            // Here, we skip comments
            if (conditionPart instanceof CodeUnit) {
                String codePart = ((CodeUnit) conditionPart).getCode();
                
                // Further, we skip continuations
                // Unfortunately treats srcML the slash already as part of the next line -> check with previous element
                boolean isContinuation = "\\".equals(codePart) && i > 2
                    && unit.getNestedElement(i - 1).getStartLine() < conditionPart.getStartLine();
                
                if (!isContinuation) {
                    parts.add(codePart);
                }
            }
        }
        
        if (replaceMissingdefined) {
            for (int i = 0; i < parts.size(); i++) {
                
                // skip fields that are "defined" followed by a "(", since these aren't variables
                if (parts.get(i).equals("defined") && i + 1 < parts.size() && parts.get(i + 1).equals("(")) {
                    continue;
                }
                
                Matcher m = VARIABLE_PATTERN.matcher(parts.get(i));
                if (m.matches()) {
                    // we found a variable, check if there is a "defined()" call around it
                    if (i < 2 || !parts.get(i - 2).equals("defined") || !parts.get(i - 1).equals("(")
                            || i + 1 >= parts.size() || !parts.get(i + 1).equals(")")) {
                        
                        // we found a variable without a defined() call around it; replace with false
                        parts.set(i, "0");
                    }
                    
                }
            }
        }
        
        StringBuffer condition = new StringBuffer();
        for (String s : parts) {
            condition.append(s);
        }
        return notNull(condition.toString());
    }
    
}
