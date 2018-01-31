package net.ssehub.kernel_haven.srcml.transformation.testing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PreprocessorIf implements ITranslationUnit{
    
    public static enum Type {
        IF, ELIF, IFDEF, IFNDEF;
    }
    
    private Type type;
//    String pc;
    String condition;
    private List<ITranslationUnit> nestedElements = new ArrayList<>();
    
    public PreprocessorIf(Type type, String condition) {
        this.type = type;
        this.condition = condition;
    }
    
    @Override
    public Iterator<ITranslationUnit> iterator() {
        return nestedElements.iterator();
    }
    
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("#");
        result.append(type.name());
        result.append(" ");
        result.append(condition);
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
}
