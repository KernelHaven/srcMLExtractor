package net.ssehub.kernel_haven.srcml.transformation;

/**
 * Marker to indicate the end of a preprocessor if-elif-else structure, which is only needed as long the surrounded
 * elements are not nested inside the {@link PreprocessorBlock}s (flat structure vs hierarchy).
 * @author El-Sharkawy
 *
 */
public class PreprocessorEndIf implements ITranslationUnit {
    
    private int startLine;
    private int endLine;

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
    
    @Override
    public void setStartLine(int rowIndex) {
        this.startLine = rowIndex;
    }
    
    @Override
    public int getStartLine() {
        return startLine;
    }
    
    @Override
    public void setEndLine(int rowIndex) {
        this.endLine = rowIndex;
    }
    
    @Override
    public int getEndLine() {
        return endLine;
    }
}
