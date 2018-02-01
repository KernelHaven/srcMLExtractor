package net.ssehub.kernel_haven.srcml.transformation.testing;

/**
 * Marker to indicate the end of a preprocessor if-elif-else structure, which is only needed as long the surrounded
 * elements are not nested inside the {@link PreprocessorBlock}s (flat structure vs hierarchy).
 * @author El-Sharkawy
 *
 */
public class PreprocessorEndIf implements ITranslationUnit {

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
