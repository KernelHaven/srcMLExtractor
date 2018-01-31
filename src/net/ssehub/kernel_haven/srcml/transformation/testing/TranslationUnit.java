package net.ssehub.kernel_haven.srcml.transformation.testing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TranslationUnit implements ITranslationUnit {
    private String type;
    //private List<Object> elements = new ArrayList<>();
    private List<String> tokens = new ArrayList<>();
    private List<ITranslationUnit> nestedElements = new ArrayList<>();
    
    public TranslationUnit(String type) {
        this.type = type;
    }
    
    public void addToken(CodeUnit token) {
        tokens.add(token.getCode());
//        elements.add(token);
        nestedElements.add(token);
    }
    
    public List<String> getTokenList() {
        return Collections.unmodifiableList(tokens);
    }
    
    public void addTranslationUnit(TranslationUnit unit) {
//        elements.add(unit);
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
    public Iterator<ITranslationUnit> iterator() {
        return nestedElements.iterator();
    }

    @Override
    public void replaceNested(ITranslationUnit oldUnit, ITranslationUnit newUnit) {
        int index = nestedElements.indexOf(oldUnit);
        if (index != -1) {
            nestedElements.set(index, newUnit);
        }
//        index = elements.indexOf(oldUnit);
//        if (index != -1) {
//            elements.set(index, newUnit);
//        }
    }
    
    @Override
    public void removeNested(ITranslationUnit oldUnit) {
        nestedElements.remove(oldUnit);
//        elements.remove(oldUnit);
    }

}
