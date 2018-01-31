package net.ssehub.kernel_haven.srcml.transformation.testing;

import java.util.Iterator;

public class PreprocessorEndIf implements ITranslationUnit {

    public PreprocessorEndIf() {
    }

    @Override
    public Iterator<ITranslationUnit> iterator() {
        // TODO Auto-generated method stub
        return null;
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

}
