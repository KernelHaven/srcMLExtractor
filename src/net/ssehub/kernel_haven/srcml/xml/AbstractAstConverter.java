package net.ssehub.kernel_haven.srcml.xml;

import java.io.File;

import org.xml.sax.helpers.DefaultHandler;

import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.code_model.SyntaxElement;

/**
 * Basis implementation of visitors/strategies for the SAXparser, which are responsible for the translation of the
 * XML output to an AST.
 * @author El-Sharkawy
 *
 */
public abstract class AbstractAstConverter extends DefaultHandler {
    
    private SourceFile file;
    
    /**
     * Sole constructor for sub classes.
     * @param path The relative path to the source file in the source tree. Must
     *             not be <code>null</code>.
     */
    protected AbstractAstConverter(File path) {
        this.file = new SourceFile(path);
    }
    
    /**
     * Returns the top level element of the complete AST.
     * @return The file, which is currently processed.
     */
    protected SourceFile getFile() {
        return file;
    }
    
    /**
     * Returns the parsed AST.
     * @return The {@link SourceFile} representing the parsed file.
     */
    public SourceFile getResult() {
        file.addElement(getAst());
        return file;
    }

    /**
     * Returns the AST node representing the completed file.
     * 
     * @return The AST node that is the result of the coversion.
     */
    protected abstract SyntaxElement getAst();
}
