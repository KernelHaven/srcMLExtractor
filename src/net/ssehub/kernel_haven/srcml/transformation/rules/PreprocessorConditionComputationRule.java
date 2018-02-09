package net.ssehub.kernel_haven.srcml.transformation.rules;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.util.ArrayList;
import java.util.List;

import net.ssehub.kernel_haven.srcml.transformation.ExceptionUtil;
import net.ssehub.kernel_haven.srcml.transformation.ITranslationUnit;
import net.ssehub.kernel_haven.srcml.transformation.PreprocessorBlock;
import net.ssehub.kernel_haven.srcml.transformation.PreprocessorBlock.Type;
import net.ssehub.kernel_haven.srcml.transformation.PreprocessorElse;
import net.ssehub.kernel_haven.srcml.transformation.PreprocessorIf;
import net.ssehub.kernel_haven.srcml.xml.SrcMlConditionGrammar;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;
import net.ssehub.kernel_haven.util.logic.parser.Parser;
import net.ssehub.kernel_haven.util.logic.parser.VariableCache;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Computes the effective conditions for all {@link PreprocessorBlock}s, i.e., consideration of negated conditions of
 * previous siblings for all <tt>&#35;else</tt> and <tt>&#35;elif</tt> blocks. However, this won't compute the
 * presence conditions, i.e., consideration of surrounding blocks.
 * @author El-Sharkawy
 *
 */
public class PreprocessorConditionComputationRule implements ITransformationRule {
    
    /**
     * This parser (and its cache) are used for the whole file and are reseted by parsing a new file as long this
     * class won't become static.
     */
    private @NonNull Parser<@NonNull Formula> parser = new Parser<>(new SrcMlConditionGrammar(new VariableCache()));

    @Override
    public void transform(@NonNull ITranslationUnit node) throws FormatException {
        for (int j = 0; j < node.size(); j++) {
            ITranslationUnit nested = node.getNestedElement(j);
            
            if (nested instanceof PreprocessorIf) {
                // Only parsing of condition required
                PreprocessorIf ifStatement = (PreprocessorIf) nested;
                // ifStatement.getCondition() is not null since its an if
                parseAndSetCondition(ifStatement, notNull(ifStatement.getCondition()));
            } else if (nested instanceof PreprocessorElse) {
                // Computation of effective condition
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
                effectiveCondition.append('(').append(start.getCondition()).append(')');
                for (PreprocessorElse preprocessorBlock : previous) {
                    effectiveCondition.append(" && !");
                    effectiveCondition.append('(').append(preprocessorBlock.getCondition()).append(')');
                }
                if (elseStatement.getType().equals(Type.ELSEIF.name())) {
                    effectiveCondition.append(" && ");
                    effectiveCondition.append('(').append(elseStatement.getCondition()).append(')');
                }
                
                // Parse and set the computed condition
                parseAndSetCondition(elseStatement, notNull(effectiveCondition.toString()));
            }
            
            transform(nested);
        }
    }

    /**
     * Parses the passed condition into a Formula and sets it directly to the given {@link PreprocessorBlock}. Will
     * report any parsing error to the Logger.
     * @param block The block to which the condition belongs to.
     * @param condition The unparsed condition in a form that the {@link SrcMlConditionGrammar} can handle it.
     */
    private void parseAndSetCondition(@NonNull PreprocessorBlock block, @NonNull String condition)
            throws FormatException {
        
        try {
            Formula parsedCondition = parser.parse(condition);
            block.setEffectiveCondition(parsedCondition);
        } catch (ExpressionFormatException exc) {
            throw ExceptionUtil.makeException("Could not parse effective expression: " + condition, exc, block);
        }
    }
}
