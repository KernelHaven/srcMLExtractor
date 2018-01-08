package net.ssehub.kernel_haven.srcml.transformation;

import java.io.File;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import net.ssehub.kernel_haven.srcml.model.OtherSyntaxElement;
import net.ssehub.kernel_haven.srcml.model.SrcMlSyntaxElement;
import net.ssehub.kernel_haven.srcml.xml.AbstractAstConverter;
import net.ssehub.kernel_haven.srcml.xml.CppHandler;

public class XmlToSyntaxElementConverter extends AbstractAstConverter {

    private OtherSyntaxElement topElement;
    
    private Stack<OtherSyntaxElement> elements;
    
    private CppHandler cppHandler;
    
    public XmlToSyntaxElementConverter(File path) {
        super(path);
        
        elements = new Stack<>();
        cppHandler = new CppHandler();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        OtherSyntaxElement element = new OtherSyntaxElement(qName, -1, -1, null, cppHandler.getCondition(),
                cppHandler.getPc());
        
        if (elements.empty()) {
            topElement = element;
        } else {
            elements.peek().addNestedElement(element);
        }
        
        elements.push(element);
        cppHandler.onNormalElementAdded();
        
        if (qName.startsWith("cpp:") || cppHandler.inCpp()) {
            cppHandler.startElement(qName, attributes);
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        elements.pop();
        cppHandler.onNormalElementRemoved();
        
        if (qName.startsWith("cpp:") || cppHandler.inCpp()) {
            cppHandler.endElement(qName);
        }
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String str = new String(ch, start, length);
        
        if (cppHandler.inCpp()) {
            cppHandler.characters(str);
        }
        
        str = str.trim();
        if (!str.isEmpty()) {
            elements.peek().addNestedElement(new OtherSyntaxElement("text:" + str, -1, -1, null,
                    cppHandler.getCondition(), cppHandler.getPc()));
        }
    }
    
    @Override
    protected SrcMlSyntaxElement getAst() {
        RuleTransformationEngine engine = new RuleTransformationEngine();
        
        return engine.transform(topElement);
    }

}
