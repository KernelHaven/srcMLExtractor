package net.ssehub.kernel_haven.srcml.transformation;

import net.ssehub.kernel_haven.util.null_checks.NonNull;

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
    public @NonNull String getType() {
        return "ENDIF";
    }

    @Override
    public void replaceNested(@NonNull ITranslationUnit oldUnit, @NonNull ITranslationUnit newUnit) {
        //
    }

    @Override
    public void removeNested(@NonNull ITranslationUnit oldUnit) {
        //
    }
    
    @Override
    public void removeNested(int index) {
        //
    }
    
    @Override
    public @NonNull String toString() {
        return "#" + getType();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public @NonNull ITranslationUnit getNestedElement(int index) {
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

    @Override
    public void insertNested(int index, @NonNull ITranslationUnit newUnit) {
    }
}
