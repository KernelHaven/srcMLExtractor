package net.ssehub.kernel_haven.srcml.xml;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.util.FormatException;

/**
 * Converts the XML output of <a href="http://www.srcml.org/">srcML</a> into an AST.
 * See <a href="http://www.srcml.org/documentation.html">srcML - Documentation</a> for the documentation of the XML
 * structure of srcML.
 * @author El-Sharkawy
 *
 */
public class XmlToAstConverter {
    private AbstractAstConverter converter;
    private SAXParser saxParser;
    private InputSource in;
    
    /**
     * Constructor to convert a String containing the whole parser output into an AST.
     * @param xml The parser output for a complete file.
     * @param converter The converter to be used (strategy/visitor pattern)
     * @throws ParserConfigurationException if a parser cannot be created which satisfies the requested configuration
     *     (should not occur). 
     * @throws SAXException If a SAX error occurs (should not occur).
     */
    public XmlToAstConverter(String xml, AbstractAstConverter converter) throws ParserConfigurationException,
        SAXException {
        
        this.converter = converter;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        this.saxParser = factory.newSAXParser();
        this.in = new InputSource(new StringReader(xml));
    }
    
    /**
     * Parses the input and returns the parsed AST.
     * @return An AST representing the parsed file.
     * @throws FormatException If the input could not be parsed due to an invalid input.
     */
    public SourceFile parseToAst() throws FormatException {
        try {
            saxParser.parse(in, converter);
        } catch (SAXException | IOException e) {
            throw new FormatException("Invalid XML content passed as an AST, cause: " + e.getMessage());
        }
        return converter.getResult();
    }

}
