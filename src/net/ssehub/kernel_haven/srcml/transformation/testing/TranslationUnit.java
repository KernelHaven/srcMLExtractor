package net.ssehub.kernel_haven.srcml.transformation.testing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TranslationUnit implements ITranslationUnit {
    private String type;
    private List<Object> elements = new ArrayList<>();
    private List<String> tokens = new ArrayList<>();
    private List<ITranslationUnit> nestedElements = new ArrayList<>();
    
    public TranslationUnit(String type) {
        this.type = type;
    }
    
    public void addToken(String token) {
        token = token.trim();
        if (!token.isEmpty()) {
            tokens.add(token);
            elements.add(token);
        }
    }
    
    public List<String> getTokenList() {
        return Collections.unmodifiableList(tokens);
    }
    
    public void addTranslationUnit(TranslationUnit unit) {
        elements.add(unit);
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
        for (Object elem : elements) {
            if (elem instanceof String) {
                if (lastWasUnit) {
                    result.append("\n");                    
//                    result.append("\ncontinued ");                    
//                    result.append(type.toUpperCase());                    
//                    result.append(": ");
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
//        StringBuffer result = new StringBuffer();
//        result.append(type);
//        result.append(":");
//        for (String token : tokens) {
//            result.append(" ");
//            result.append(token);
//        }
//        for (TranslationUnit translationUnit : nestedElements) {
//            result.append("\n  ");
//            result.append(translationUnit);
//        }
//        return result.toString();
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
        index = elements.indexOf(oldUnit);
        if (index != -1) {
            elements.set(index, newUnit);
        }
    }

}
