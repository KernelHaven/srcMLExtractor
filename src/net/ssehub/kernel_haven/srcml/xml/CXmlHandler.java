package net.ssehub.kernel_haven.srcml.xml;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
    
    /**
     * Global (i.e. "always fits") mapping of XML name -> SyntaxElemenType.
     */
    private static final Map<String, ISyntaxElementType> NAME_TYPE_MAPPING = new HashMap<>();
    
    /**
     * Global (i.e. "always fits") set of XML names, that expect a literal value as a nested child.
     */
    private static final Set<String> ELEMENT_WANTS_CHARACTER_LITERAL = new HashSet<>();
    
    static {
        // statements
        NAME_TYPE_MAPPING.put("block", SyntaxElementTypes.COMPOUND_STATEMENT); // this only fits roughly...
        NAME_TYPE_MAPPING.put("break", SyntaxElementTypes.BREAK_STATEMENT);
        NAME_TYPE_MAPPING.put("case", SyntaxElementTypes.CASE_STATEMENT);
        NAME_TYPE_MAPPING.put("continue", SyntaxElementTypes.CONTINUE_STATEMENT);
        NAME_TYPE_MAPPING.put("decl_stmt", SyntaxElementTypes.DECLARATION_STATEMENT);
        NAME_TYPE_MAPPING.put("default", SyntaxElementTypes.DEFAULT_STATEMENT);
        NAME_TYPE_MAPPING.put("do", SyntaxElementTypes.DO_STATEMENT);
        NAME_TYPE_MAPPING.put("elseif", SyntaxElementTypes.ELIF_STATEMENT);
        NAME_TYPE_MAPPING.put("empty_stmt", SyntaxElementTypes.EMPTY_STATEMENT);
        NAME_TYPE_MAPPING.put("expr_stmt", SyntaxElementTypes.EXPR_STATEMENT);
        NAME_TYPE_MAPPING.put("for", SyntaxElementTypes.FOR_STATEMENT);
        NAME_TYPE_MAPPING.put("goto", SyntaxElementTypes.GOTO_STATEMENT);
        NAME_TYPE_MAPPING.put("if", SyntaxElementTypes.IF_STATEMENT);
        NAME_TYPE_MAPPING.put("label", SyntaxElementTypes.LABEL_STATEMENT);
        NAME_TYPE_MAPPING.put("return", SyntaxElementTypes.RETURN_STATEMENT);
        NAME_TYPE_MAPPING.put("switch", SyntaxElementTypes.SWITCH_STATEMENT);
        NAME_TYPE_MAPPING.put("while", SyntaxElementTypes.WHILE_STATEMENT);
        
        // expressions
        NAME_TYPE_MAPPING.put("name", SyntaxElementTypes.ID);
        NAME_TYPE_MAPPING.put("call", SyntaxElementTypes.FUNCTION_CALL);
        NAME_TYPE_MAPPING.put("decl", SyntaxElementTypes.DECLARATION);
        NAME_TYPE_MAPPING.put("init", SyntaxElementTypes.INITIALIZER);
        
        // other
        NAME_TYPE_MAPPING.put("unit", SyntaxElementTypes.TRANSLATION_UNIT);
        NAME_TYPE_MAPPING.put("function", SyntaxElementTypes.FUNCTION_DEF);
        NAME_TYPE_MAPPING.put("parameter_list", SyntaxElementTypes.DECL_PARAMETER_DECL_LIST);
        NAME_TYPE_MAPPING.put("parameter", SyntaxElementTypes.PARAMETER_DECLARATION_D);
        
        ELEMENT_WANTS_CHARACTER_LITERAL.add("name");
        ELEMENT_WANTS_CHARACTER_LITERAL.add("literal");
        ELEMENT_WANTS_CHARACTER_LITERAL.add("operator");
        ELEMENT_WANTS_CHARACTER_LITERAL.add("specifier");
    }
    
    private File sourceFile;
    
    /**
     * The element to add a new element as nesting to when the elements stack is empty.
     */
    private SyntaxElement topElement;
    
    /**
     * A stack of elements representing our current position in the XML hierarchy. Starts empty. New elements are
     * added as nesting elements of peek() and are push()'d to the top. A closing element pop()s.
     */
    private Stack<SyntaxElement> elements;
    
    /**
     * Whether the characters in the current tag should be added as a nesting Literal to it.
     */
    private boolean wantCharacters;
    
    private CppHandler cppHandler;
    
    /**
     * Creates an XML handler for the given source file.
     * 
     * @param path The path of the source file inside the source tree.
     */
    public CXmlHandler(File path) {
        super(path);
        this.sourceFile = path;
        elements = new Stack<>();
        cppHandler = new CppHandler();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (null != qName) {
            if (qName.startsWith("cpp:") || cppHandler.inCpp()) {
                cppHandler.startElement(qName, attributes);
                
            } else { // we are not in a CPP directive
                ISyntaxElementType type = NAME_TYPE_MAPPING.get(qName);
                if (type == null) {
//                    System.out.println("Unknown xml name: " + qName);
                    type = new ErrorSyntaxElement(qName);
                }
                
                SyntaxElement element = new SyntaxElement(type,
                        cppHandler.getCondition(), cppHandler.getPc());
                element.setSourceFile(sourceFile);
                
                if (elements.empty()) {
                    topElement = element;
                } else {
                    elements.peek().addNestedElement(element);
                }
                elements.push(element);
                
                wantCharacters = ELEMENT_WANTS_CHARACTER_LITERAL.contains(qName);
                
                // condition for nested elements is true
                cppHandler.onNormalElementAdded();
            }
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (null != qName) {
            if (qName.startsWith("cpp:") || cppHandler.inCpp()) {
                cppHandler.endElement(qName);
                
            } else { // we are not in a CPP directive
                wantCharacters = false;
                elements.pop();
                cppHandler.onNormalElementRemoved();
            }
        }
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String str = new String(ch, start, length);
        
        if (cppHandler.inCpp()) {
            cppHandler.characters(str);
            
        } else if (wantCharacters) {
            
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
