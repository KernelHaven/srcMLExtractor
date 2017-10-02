package net.ssehub.kernel_haven.srcml.xml;

import java.io.File;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import net.ssehub.kernel_haven.code_model.Block;
import net.ssehub.kernel_haven.srcml.xml.element_parser.FunctionDefVisitor;
import net.ssehub.kernel_haven.srcml.xml.element_parser.IElementVisitor;

/**
 * A converter for translating C-files including their preprocessor statements.
 * @author El-Sharkawy
 *
 */
public class CXmlHandler extends AbstractAstConverter {
    private Block parent;
    private IElementVisitor elementParser;
    
    public CXmlHandler(File path) {
        super(path);
        parent = null;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (null != qName) {
            if (qName.startsWith("cpp:")) {
                
            } else {
                System.out.println(qName);
                switch (qName) {
                // Toplevel elements: Declarations & Definitions
                case "function":
                    // Function definition
                    elementParser = new FunctionDefVisitor();
                    break;
                // All other elements are handles by specific visitors / delegates
                default:
                    if (null != elementParser) {
                        elementParser.startElement(qName, attributes);                            
                    }
                }
            }
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (null != qName) {
            if (qName.startsWith("cpp:")) {
                
            } else {
                switch (qName) {
                // Toplevel elements: Declarations & Definitions
                case "function":
                    // Function definition
                    parent = elementParser.getAstElement();
                    System.out.println(elementParser);
                    elementParser = null;
                    break;
                // All other elements are handles by specific visitors / delegates
                default:
                    if (null != elementParser) {
                        elementParser.endElement(qName);                            
                    }
                }
            }
        }
    }
    
    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        if (null != elementParser) {
            // Do not create a String here, let the sub visitor decide if this is really necessary
            elementParser.characters(ch, start, length);
        }
        // Ignore characters of unsupported elements
    }

    @Override
    public void parseAST() {
        // TODO Auto-generated method stub
    }

}
