package net.ssehub.kernel_haven.srcml.transformation;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Represent unparsed code fragments, e.g., a function signature or a (partial) expression of a statement.
 * These are the texts between the XML tags in <a href="https://www.srcml.org/doc/c_srcML.html">srcML</a> and will be
 * gathered through by SAX's {@link org.xml.sax.ContentHandler#characters(char[], int, int)}.  
 * @author El-Sharkawy
 *
 */
public class CodeUnit implements ITranslationUnit {
    
    private @NonNull String code;
    private int startLine;
    private int endLine;
    
    /**
     * Sole constructor for this class.
     * @param code A single char sequence/token, which is not intended to be parsed in a later step.
     */
    public CodeUnit(@NonNull String code) {
        this.code = code;
    }
    
    public @NonNull String getCode() {
        return code;
    }

    @Override
    public @NonNull String getType() {
        return notNull(this.getClass().getSimpleName());
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
        return code;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public @NonNull ITranslationUnit getNestedElement(int index) {
        throw new IndexOutOfBoundsException(CodeUnit.class.getSimpleName() + " has no nested elements, "
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
