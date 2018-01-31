package net.ssehub.kernel_haven.srcml.transformation.testing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class PreprocessorBlock implements ITranslationUnit {
    
    public static enum Type {
        IF, IFDEF, IFNDEF, ELSEIF, ELSE;
    }
    
    private Type type;
    String condition;
    private List<ITranslationUnit> nestedElements = new ArrayList<>();
    
    public PreprocessorBlock(Type type, String condition) {
        this.type = type;
        this.condition = condition;
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
        if (null != condition) {
            result.append(" ");
            result.append(condition);
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
}
