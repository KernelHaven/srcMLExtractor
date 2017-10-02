package net.ssehub.kernel_haven.srcml.xml.element_parser;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import net.ssehub.kernel_haven.code_model.Block;

/**
 * Translates a <a href="http://www.srcml.org/doc/c_srcML.html#function-definition">function definition</a>.
 * @author El-Sharkawy
 *
 */
public class FunctionDefVisitor implements IElementVisitor {
    
    private static enum State {
        TYPE, FUNCTION_NAME, PARAMETER_LIST, BODY;
    }
    
    // Attributes, which shall be translated into AST
    private List<String> returnTypes = new ArrayList<>();
    private List<String> parameters = new ArrayList<>();
    private String functionName;
    
    // State of this visitor
    private State state = null;
    private boolean readNameValue = false;

    @Override
    public Block getAstElement() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void startElement(String qName, Attributes attributes) throws SAXException {
        switch (qName) {
        case "name":
            readNameValue = true;
            break;
        case "parameter":
            if (State.FUNCTION_NAME == state) {
                state = State.PARAMETER_LIST;
            }
            break;
        case "type":
            if (null == state) {
                state = State.TYPE;
            }
            break;
        }
        
    }

    @Override
    public void endElement(String qName) throws SAXException {
        switch (qName) {
        case "name":
            readNameValue = false;
            break;
        case "type":
            if (state == State.TYPE) {
                state = State.FUNCTION_NAME;
            }
            break;
        case "parameter_list":
            state = State.BODY;
            break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (null != state && readNameValue) {
            switch (state) {
            case FUNCTION_NAME:
                functionName = new String(ch, start, length);
                break;
            case PARAMETER_LIST:
                parameters.add(new String(ch, start, length));
                break;
            case TYPE:
                returnTypes.add(new String(ch, start, length));
                break;
            }
        }
    }
    
    @Override
    public String toString() {
        return returnTypes.toString() + " " + functionName + "(" + parameters.toString() + ")";
    }

}
