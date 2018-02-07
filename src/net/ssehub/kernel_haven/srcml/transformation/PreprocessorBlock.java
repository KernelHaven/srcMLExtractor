package net.ssehub.kernel_haven.srcml.transformation;

import java.util.ArrayList;
import java.util.List;

import net.ssehub.kernel_haven.srcml.xml.SrcMlConditionGrammar;
import net.ssehub.kernel_haven.util.logic.Formula;

/**
 * Abstract super class for preprocessor if and else blocks.
 * @author El-Sharkawy
 *
 */
public abstract class PreprocessorBlock implements ITranslationUnit {
    
    private int startLine;
    private int endLine;
    
    /**
     * Denotes the exact kind of preprocessor element.
     * @author El-Sharkawy
     *
     */
    public static enum Type {
        IF, IFDEF, IFNDEF, ELSEIF, ELSE;
    }
    
    private Type type;
    
    /**
     * Unparsed condition as found in code.
     * <tt>#elif B -> B</tt>
     */
    private String condition;
    
    /**
     * Parsed condition, considering previous if's and elif's, will also compute the condition for an else, but
     * won't consider the enclosing conditions.
     * <tt>elif B -> !A && B</tt>
     */
    private Formula parsedCondition;
    private List<ITranslationUnit> nestedElements = new ArrayList<>();
    
    /**
     * Sole constructor for sub classes.
     * @param type Denotes the exact CPP statement.
     * @param condition The condition of the if, should be in form that the {@link SrcMlConditionGrammar} can handle it,
     *     may be <tt>null</tt> in case of an <tt>&#35;else</tt>-statement.
     */
    public PreprocessorBlock(Type type, String condition) {
        this.type = type;
        this.condition = condition;
    }

    /**
     * Returns the condition as retrieved from the parser, will be <tt>null</tt> for an else statement.
     * @return The condition.
     */
    public String getCondition() {
        return condition;
    }
    
    /**
     * Returns the parsed effective condition, considering previous if's and elif's, will also compute the condition
     * for an else, but won't consider the enclosing conditions.
     * <tt>elif B -> !A && B</tt>
     * @return The effective condition not considering surrounding blocks.
     */
    public Formula getEffectiveCondition() {
        return parsedCondition;
    }

    /**
     * Sets the effective condition (for all <tt>&#35;else</tt> and <tt>&#35;elif</tt> statements
     * it considers also the negation of previous blocks).
     * @param effectiveCondition The effective condition considering previous elements (not the presence condition, also
     *     considering surrounding blocks!).
     */
    public void setEffectiveCondition(Formula parsedCondition) {
        this.parsedCondition = parsedCondition;
    }

    /**
     * Adds a nested element, which is enclosed by this by preprocessor block.
     * @param child The nested element to add.
     */
    @Override
    public void add(ITranslationUnit child) {
        nestedElements.add(child);
    }
    
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("#");
        result.append(type.name());
        if (null != parsedCondition.toString()) {
            result.append(" ");
            result.append(parsedCondition.toString());
        }
        for (ITranslationUnit elem : nestedElements) {
            result.append("\n    ");
            result.append(elem.toString().replace("\n", "\n    "));
        }
        return result.toString();
    }

    @Override
    public String getType() {
        return type.toString();
    }
    
    @Override
    public void replaceNested(ITranslationUnit oldUnit, ITranslationUnit newUnit) {
        int index = nestedElements.indexOf(oldUnit);
        if (index != -1) {
            nestedElements.set(index, newUnit);
        }
    }
    
    @Override
    public void removeNested(ITranslationUnit oldUnit) {
        nestedElements.remove(oldUnit);
    }
    
    @Override
    public int size() {
        return nestedElements.size();
    }

    @Override
    public ITranslationUnit getNestedElement(int index) {
        return nestedElements.get(index);
    }
    
    @Override
    public void setStartLine(int rowIndex) {
        this.startLine = rowIndex;
    }
    
    @Override
    public int getStartLine() {
        return startLine;
    }
    
    @Override
    public void setEndLine(int rowIndex) {
        this.endLine = rowIndex;
    }
    
    @Override
    public int getEndLine() {
        return endLine;
    }
}
