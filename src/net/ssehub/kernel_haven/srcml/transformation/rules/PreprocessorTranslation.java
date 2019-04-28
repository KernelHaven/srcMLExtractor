/*
 * Copyright 2017-2019 University of Hildesheim, Software Systems Engineering
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.ssehub.kernel_haven.srcml.transformation.rules;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.util.ArrayDeque;
import java.util.Deque;

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
 * Translates preprocessor control structures into a simpler form for parsing. More precisely it translates:
 * <p>
 * <tt>#ifdef, #if, #else, #elseif, #endif</tt>
 * <p>
 * This rule converts {@link TranslationUnit}s into {@link PreprocessorBlock}s
 * @author El-Sharkawy
 *
 */
public class PreprocessorTranslation implements ITransformationRule {
    
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
        switch (((CodeUnit) child.getNestedElement(1)).getCode()) {
        case "else":
            newUnit = new PreprocessorElse(Type.ELSE, null, startingIf);
            startingIf.addSibling(newUnit);
            break;
        case "elif":
            newUnit = new PreprocessorElse(Type.ELSEIF, getCondition(child), startingIf);
            startingIf.addSibling(newUnit);
            break;
        default:
            // ignore
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
        switch (((CodeUnit) child.getNestedElement(1)).getCode()) {
        case "ifdef":
            newUnit = new PreprocessorIf(Type.IFDEF, "defined(" + getCondition(child) + ")");
            break;
        case "ifndef":
            newUnit = new PreprocessorIf(Type.IFNDEF, "!defined(" + getCondition(child) + ")");
            break;
        case "if":
            newUnit = new PreprocessorIf(Type.IF, getCondition(child));
            break;
        default:
            // ignore
        }
        if (null != newUnit) {
            newUnit.setStartLine(child.getStartLine());
            parent.replaceNested(child, newUnit);
            parents.addFirst(newUnit);
        }
    }
    
    /**
     * Returns the condition string of the given #ifdef, #ifndef or #if unit. This skips comments and considers
     * line continuation.
     * 
     * @param unit The unit holding the condition.
     * 
     * @return The condition of the unit.
     */
    private @NonNull String getCondition(@NonNull TranslationUnit unit) {
        StringBuilder condition = new StringBuilder();
        
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
                    condition.append(codePart).append(' ');
                }
            }
        }
        
        // remove trailing ' '
        if (condition.length() > 0) {
            condition.replace(condition.length() - 1, condition.length(), "");
        }
        
        return notNull(condition.toString());
    }
    
}
