package net.ssehub.kernel_haven.srcml.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import net.ssehub.kernel_haven.code_model.ErrorSyntaxElement;
import net.ssehub.kernel_haven.code_model.ISyntaxElementType;
import net.ssehub.kernel_haven.code_model.LiteralSyntaxElement;
import net.ssehub.kernel_haven.code_model.SyntaxElement;
import net.ssehub.kernel_haven.code_model.SyntaxElementTypes;
import net.ssehub.kernel_haven.util.logic.True;

/**
 * A converter for translating C-files including their preprocessor statements.
 * 
 * @author El-Sharkawy
 * @author Adam
 */
public class CXmlHandler extends AbstractAstConverter {
    
    private static final Map<String, ISyntaxElementType> NAME_TYPE_MAPPING = new HashMap<>();
    
    private static final Map<String, String> NAME_RELATION_MAPPING = new HashMap<>();
    
    static {
        NAME_TYPE_MAPPING.put("name", SyntaxElementTypes.ID);
        NAME_TYPE_MAPPING.put("unit", SyntaxElementTypes.TRANSLATION_UNIT);
        
        NAME_RELATION_MAPPING.put("init", "Value");
    }
    
    private File sourceFile;
    
    private SyntaxElement topElement;
    
    private Stack<SyntaxElement> elements;
    
    private boolean wantCharacters;
    
    private String relation = "";
    
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
                String relation = null;
                ISyntaxElementType type = NAME_TYPE_MAPPING.get(qName);
                if (type == null) {
                    relation = NAME_RELATION_MAPPING.get(qName);
                    if (relation == null) {
//                        System.out.println("Unknown xml name: " + qName);
                        type = new ErrorSyntaxElement(qName);
                    }
                }
                
                if (type != null) {
                    SyntaxElement element = new SyntaxElement(type,
                            True.INSTANCE, True.INSTANCE);
                    element.setSourceFile(sourceFile);
                    
                    if (elements.empty()) {
                        topElement = element;
                    } else {
                        elements.peek().addNestedElement(element, this.relation);
                    }
                    this.relation = "";
                    elements.push(element);
                    
                    wantCharacters = elementWantsCharacters(qName);
                } else {
                    this.relation = relation;
                }
            }
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (null != qName) {
            if (qName.startsWith("cpp:")) {
                // TODO: handle preprocessor
            } else {
                wantCharacters = false;
                if (NAME_RELATION_MAPPING.get(qName) == null) {
                    // if we interpreted this tag as a relation, then don't pop the element (since we haven't created one) 
                    elements.pop();
                }
            }
        }
    }
    
    
    private static boolean elementWantsCharacters(String elementName) {
        return elementName.equals("name") | elementName.equals("literal");
    }
    
    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        if (wantCharacters) {
            String str = new String(ch, start, length);
            
            SyntaxElement element = new SyntaxElement(new LiteralSyntaxElement(str),
                    True.INSTANCE, True.INSTANCE);
            element.setSourceFile(sourceFile);
            
            elements.peek().addNestedElement(element);
        }
    }

    @Override
    public SyntaxElement getAst() {
        return topElement;
    }

}
