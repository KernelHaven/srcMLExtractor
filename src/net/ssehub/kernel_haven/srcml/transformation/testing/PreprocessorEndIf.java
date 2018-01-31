package net.ssehub.kernel_haven.srcml.transformation.testing;

import java.util.Collections;
import java.util.Iterator;

public class PreprocessorEndIf implements ITranslationUnit {

    public PreprocessorEndIf() {
    }

    @Override
    public Iterator<ITranslationUnit> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public String getType() {
        return "ENDIF";
    }

    @Override
    public void replaceNested(ITranslationUnit oldUnit, ITranslationUnit newUnit) {
        //
    }

    @Override
    public void removeNested(ITranslationUnit oldUnit) {
        //
    }
    
    @Override
    public String toString() {
        return "#" + getType();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public ITranslationUnit getNestedElement(int index) {
        throw new IndexOutOfBoundsException(PreprocessorEndIf.class.getSimpleName() + " has no nested elements, "
            + "requested was: " + index);
    }
}