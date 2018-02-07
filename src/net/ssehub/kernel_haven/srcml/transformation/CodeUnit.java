package net.ssehub.kernel_haven.srcml.transformation;

/**
 * Represent unparsed code fragments, e.g., a function signature or a (partial) expression of a statement.
 * These are the texts between the XML tags in <a href="http://www.srcml.org/doc/c_srcML.html">srcML</a> and will be
 * gathered through by SAX's {@link org.xml.sax.ContentHandler#characters(char[], int, int)}.  
 * @author El-Sharkawy
 *
 */
public class CodeUnit implements ITranslationUnit {
    
    private String code;
    private int startLine;
    
    /**
     * Sole constructor for this class.
     * @param code A single char sequence/token, which is not intended to be parsed in a later step.
     */
    public CodeUnit(String code) {
        this.code = code;
    }
    
    public String getCode() {
        return code;
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

    @Override
    public void setStartLine(int rowIndex) {
        this.startLine = rowIndex;
    }
    
    @Override
    public int getStartLine() {
        return startLine;
    }
}
