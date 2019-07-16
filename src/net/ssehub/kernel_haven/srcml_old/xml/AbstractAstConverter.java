/*
 * Copyright 2017-2019 University of Hildesheim, Software Systems Engineering
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.ssehub.kernel_haven.srcml_old.xml;

import java.io.File;

import org.xml.sax.Locator;
import org.xml.sax.helpers.DefaultHandler;

import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Basis implementation of visitors/strategies for the SAXparser, which are responsible for the translation of the
 * XML output to an AST.
 * @author El-Sharkawy
 *
 */
public abstract class AbstractAstConverter extends DefaultHandler {
    
    private @NonNull SourceFile<ISyntaxElement> file;
    
    private Locator locator;
    
    /**
     * Sole constructor for sub classes.
     * @param path The relative path to the source file in the source tree. Must
     *             not be <code>null</code>.
     */
    protected AbstractAstConverter(@NonNull File path) {
        this.file = new SourceFile<>(path);
    }
    
    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }
    
    /**
     * Returns the currently parsed line number of the XML document.
     * @return The current line number within the document.
     * @see <a 
     * href="https://www.java2s.com/Tutorials/Java/XML/SAX/Output_line_number_for_SAX_parser_event_handler_in_Java.htm">
     * Tutorial: Output line number for SAX parser event handler in Java</a>
     */
    protected int getLineNumber() {
        // Do not count XML header
        return locator.getLineNumber() - 1;
    }
    
    /**
     * Returns the top level element of the complete AST.
     * @return The file, which is currently processed.
     */
    protected @NonNull SourceFile<ISyntaxElement> getFile() {
        return file;
    }
    
    /**
     * Returns the parsed AST.
     * 
     * @return The {@link SourceFile} representing the parsed file.
     * 
     * @throws FormatException If parsing the AST fails.
     */
    public @NonNull SourceFile<ISyntaxElement> getResult() throws FormatException {
        file.addElement(getAst());
        return file;
    }

    /**
     * Returns the AST node representing the completed file.
     * 
     * @return The AST node that is the result of the conversion.
     * 
     * @throws FormatException If parsing the AST fails.
     */
    protected abstract @NonNull ISyntaxElement getAst() throws FormatException;
    
}
