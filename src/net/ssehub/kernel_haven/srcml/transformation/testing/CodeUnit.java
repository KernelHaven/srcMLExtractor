package net.ssehub.kernel_haven.srcml.transformation.testing;

import java.util.Collections;
import java.util.Iterator;

public class CodeUnit implements ITranslationUnit {
    
    private String code;
    
    public CodeUnit(String code) {
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }

    @Override
    public Iterator<ITranslationUnit> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public String getType() {
        return this.getClass().getSimpleName();
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
        return code;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public ITranslationUnit getNestedElement(int index) {
        throw new IndexOutOfBoundsException(CodeUnit.class.getSimpleName() + " has no nested elements, "
            + "requested was: " + index);
    }
}
