package net.ssehub.kernel_haven.srcml.xml;

import java.io.File;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import net.ssehub.kernel_haven.code_model.LiteralSyntaxElement;
import net.ssehub.kernel_haven.code_model.SyntaxElement;
import net.ssehub.kernel_haven.util.logic.True;

/**
 * A converter for translating C-files including their preprocessor statements.
 * 
 * @author El-Sharkawy
 * @author Adam
 */
public class CXmlHandler extends AbstractAstConverter {
    
    private File sourceFile;
    
    private SyntaxElement topElement;
    
    private Stack<SyntaxElement> elements;
    
    public CXmlHandler(File path) {
        super(path);
        this.sourceFile = path;
        elements = new Stack<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (null != qName) {
            if (qName.startsWith("cpp:")) {
                // TODO: handle preprocessor
                
            } else {
                SyntaxElement element = new SyntaxElement(new LiteralSyntaxElement(qName),
                        True.INSTANCE, True.INSTANCE);
                element.setSourceFile(sourceFile);
                
                if (elements.empty()) {
                    topElement = element;
                } else {
                    elements.peek().addNestedElement(element);
                }
                elements.push(element);
            }
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (null != qName) {
            if (qName.startsWith("cpp:")) {
                // TODO: handle preprocessor
            } else {
                elements.pop();
            }
        }
    }
    
    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        new String(ch, start, length);
    }

    @Override
    public SyntaxElement getAst() {
        return topElement;
    }

}
