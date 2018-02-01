package net.ssehub.kernel_haven.srcml.transformation.testing;

import java.util.ArrayList;
import java.util.List;

public class TranslationUnit implements ITranslationUnit {
    private String type;
    private List<ITranslationUnit> nestedElements = new ArrayList<>();
    
    public TranslationUnit(String type) {
        this.type = type;
    }
    
    public void addToken(CodeUnit token) {
        nestedElements.add(token);
    }
    
    public void addTranslationUnit(TranslationUnit unit) {
        nestedElements.add(unit);
    }
    
    @Override
    public String getType() {
        return type;
    }
    
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(type.toUpperCase());
        result.append(":");
        boolean lastWasUnit = false;
        for (Object elem : nestedElements) {
            if (elem instanceof CodeUnit) {
                if (lastWasUnit) {
                    result.append("\n");
                } else {
                    result.append(" ");
                }
                result.append(elem.toString().replace("\n", "\n    "));
            } else {
                lastWasUnit = true;
                result.append("\n    ");
                result.append(elem.toString().replace("\n", "\n    "));
            }
        }
        return result.toString();
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
