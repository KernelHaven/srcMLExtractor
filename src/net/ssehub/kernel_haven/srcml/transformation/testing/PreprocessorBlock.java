package net.ssehub.kernel_haven.srcml.transformation.testing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class PreprocessorBlock implements ITranslationUnit {
    
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
     * Unparsed condition, considering previous if's and elif's, will also compute the condition for an else, but
     * won't consider the enclosing conditions.
     * <tt>elif B -> !A && B</tt>
     */
    private String effectiveCondition;
    private List<ITranslationUnit> nestedElements = new ArrayList<>();
    
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
     * Returns the unparsed effective condition, considering previous if's and elif's, will also compute the condition
     * for an else, but won't consider the enclosing conditions.
     * <tt>elif B -> !A && B</tt>
     * @return The effective condition not considering surrounding blocks.
     */
    public String getEffectiveCondition() {
        return effectiveCondition;
    }
    
    protected void setEffectiveCondition(String effectiveCondition) {
        this.effectiveCondition = effectiveCondition;
    }
    
    @Override
    public Iterator<ITranslationUnit> iterator() {
        return nestedElements.iterator();
    }
    
    public void add(ITranslationUnit child) {
        nestedElements.add(child);
    }
    
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("#");
        result.append(type.name());
        if (null != getEffectiveCondition()) {
            result.append(" ");
            result.append(getEffectiveCondition());
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
}
