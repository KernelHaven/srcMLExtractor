package net.ssehub.kernel_haven.srcml.transformation.testing;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic {@link ITranslationUnit}, which does not require any special treatment.<br/>
 * <b>Note:</b> All elements are initially parsed either as {@link TranslationUnit} or as {@link CodeUnit} and may be
 * refined in an alter step (or kept if no further treatment is required).
 * @author El-Sharkawy
 *
 */
public class TranslationUnit implements ITranslationUnit {
    private String type;
    private List<ITranslationUnit> nestedElements = new ArrayList<>();
    
    /**
     * Sole constructor for this type.
     * @param type Denotes what kind of element is represented by this {@link TranslationUnit}, should be one of the
     *     top elements from the <a href="http://www.srcml.org/doc/c_srcML.html">srcML XML snytax</a>.
     */
    public TranslationUnit(String type) {
        this.type = type;
    }
    
    /**
     * Adds a nested element.
     * @param unit The nested element to add.
     */
    public void add(ITranslationUnit unit) {
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
