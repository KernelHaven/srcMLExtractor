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
package net.ssehub.kernel_haven.srcml.xml;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Converts the XML output of <a href="https://www.srcml.org/">srcML</a> into an AST.
 * See <a href="https://www.srcml.org/documentation.html">srcML - Documentation</a> for the documentation of the XML
 * structure of srcML.
 * @author El-Sharkawy
 *
 */
public class XmlToAstConverter {
    private @NonNull AbstractAstConverter converter;
    private @NonNull SAXParser saxParser;
    private @NonNull InputSource in;
    
    /**
     * Constructor to convert a String containing the whole parser output into an AST.
     * @param xml The parser output for a complete file.
     * @param converter The converter to be used (strategy/visitor pattern)
     * @throws ParserConfigurationException if a parser cannot be created which satisfies the requested configuration
     *     (should not occur). 
     * @throws SAXException If a SAX error occurs (should not occur).
     */
    public XmlToAstConverter(@NonNull InputStream xml, @NonNull AbstractAstConverter converter)
            throws ParserConfigurationException, SAXException {
        
        this.converter = converter;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        this.saxParser = notNull(factory.newSAXParser());
        this.in = new InputSource(xml);
    }
    
    /**
     * Parses the input and returns the parsed AST.
     * @return An AST representing the parsed file.
     * @throws FormatException If the input could not be parsed due to an invalid input.
     */
    public @NonNull SourceFile<ISyntaxElement> parseToAst() throws FormatException {
        try {
            saxParser.parse(in, converter);
        } catch (SAXException | IOException e) {
            throw new FormatException("Invalid XML content passed as an AST, cause: " + e.getMessage());
        }
        return converter.getResult();
    }

}
