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
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;
import net.ssehub.kernel_haven.util.logic.parser.Parser;
import net.ssehub.kernel_haven.util.logic.parser.VariableCache;

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
    
    /**
     * The stack of conditions. A new element has the immediate condition of peek(). A new element
     * (that is not a CPP directive) push()es true. A closing element pop()s.
     * A CPP directive push()es its condition, an endif pops()s the pushed if condition.
     * This starts with a single True push()ed.
     */
    private Stack<Formula> conditions;
    
    /**
     * Not <code>null</code> if we are currently in a CPP directive. In this case, this equals the qname.
     */
    private String inCpp;
    
    /**
     * The expression that was created from a &lt;expr&gt; inside a CPP directive.
     */
    private Formula cppExpr;
    
    /**
     * Whether we are currently inside an &lt;expr&gt; inside a CPP directive.
     */
    private boolean inCppExpr;
    
    /**
     * A stack with the qNames of the current hierarchy when walking through an &lt;expr&gt; inside a CPP directive.
     * inCppExpr may only turn back to false, if this is empty.
     */
    private Stack<String> inCppExprNodes;
    
    /**
     * The expression string that we build while we are in an &lt;expr&gt; inside a CPP directive. This will be parsed
     * at the end into a Formula.
     */
    private StringBuilder inCppExprString;
    
    private boolean inCppExprCall;
    
    /**
     * Creates an XML handler for the given source file.
     * 
     * @param path The path of the source file inside the source tree.
     */
    public CXmlHandler(File path) {
        super(path);
        this.sourceFile = path;
        elements = new Stack<>();
        conditions = new Stack<>();
        conditions.push(True.INSTANCE);
        inCppExprNodes = new Stack<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (null != qName) {
            if (inCppExpr) {
                inCppExprStart(qName, attributes);
                
            } else if (qName.startsWith("cpp:")) {
                if (inCpp == null) {
                    inCpp = qName;
                }
            } else if (inCpp != null) {
                if (qName.equals("expr")) {
                    // start parsing the expression of the cpp directive
                    inCppExprString = new StringBuilder();
                    inCppExpr = true;
                }
                
            } else { // we are not in a CPP directive
                ISyntaxElementType type = NAME_TYPE_MAPPING.get(qName);
                if (type == null) {
//                    System.out.println("Unknown xml name: " + qName);
                    type = new ErrorSyntaxElement(qName);
                }
                
                SyntaxElement element = new SyntaxElement(type,
                        conditions.peek(), getPc());
                element.setSourceFile(sourceFile);
                
                if (elements.empty()) {
                    topElement = element;
                } else {
                    elements.peek().addNestedElement(element);
                }
                elements.push(element);
                
                wantCharacters = ELEMENT_WANTS_CHARACTER_LITERAL.contains(qName);
                
                // condition for nested elements is true
                conditions.push(True.INSTANCE);
            }
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (null != qName) {
            if (qName.startsWith("cpp:")) {
                if (qName.equals(inCpp)) {
                    inCpp = null;
                    
                    // we reached the end of the CPP directive. modify the current condition accordingly
                    switch (qName) {
                    case "cpp:if":
                        // a normal if just replaces the current condition
                        conditions.push(cppExpr);
                        break;
                    case "cpp:else":
                        // an else negates the previous condition
                        conditions.push(new Negation(conditions.peek()));
                        break;
                    case "cpp:elif":
                        // an elif negates the previous and appends the parsed condition
                        conditions.push(new Conjunction(new Negation(conditions.peek()), cppExpr));
                        break;
                    case "cpp:endif":
                        // an endif clears the condition
                        conditions.pop();
                        break;
                    // TODO: ifdef, ifndef
                        
                    default:
                        Logger.get().logError("Unknown CPP directive: " + qName);
                        break;
                    }
                    
                }
            } else if (inCpp != null) {
                if (inCppExpr && inCppExprNodes.isEmpty() && qName.equals("expr")) { 
                    // we reached the end of the CPP expression (</expr>);
                    // we should have a formula now
                    inCppExpr = false;
                    
                    // TODO: temporary debug try
                    System.out.println("-------------");
                    System.out.println(inCppExprString);
                    VariableCache cache = new VariableCache();
                    Parser<Formula> parser = new Parser<>(new SrcMlConditionGrammar(cache));
                    
                    try {
                        cppExpr = parser.parse(inCppExprString.toString());
                    } catch (ExpressionFormatException e) {
                        Logger.get().logException("Unable to parser condition; using True instead", e);
                        cppExpr = True.INSTANCE;
                    }
                } else if (inCppExpr) {
                    inCppExprEnd(qName);
                }
                
            } else { // we are not in a CPP directive
                wantCharacters = false;
                elements.pop();
                conditions.pop();
            }
        }
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String str = new String(ch, start, length);
        
        if (inCppExpr) {
            if (!inCppExprNodes.empty()) {
                
                if (inCppExprNodes.peek().equals("name")) {
                    System.out.println("Name: " + inCppExprNodes + " -> " + str);
                    
                    if (inCppExprCall) {
                        inCppExprCall = false;
                        inCppExprString.append(str + "(");
                    } else {
                        inCppExprString.append(str);
                    }
                } else if (inCppExprNodes.peek().equals("operator")) {
                    System.out.println("Op: " + inCppExprNodes + " -> " + str);
                    
                    inCppExprString.append(str);
                }
            }
            
        } else if (wantCharacters) {
            
            SyntaxElement element = new SyntaxElement(new LiteralSyntaxElement(str),
                    True.INSTANCE, True.INSTANCE);
            element.setSourceFile(sourceFile);
            
            elements.peek().addNestedElement(element);
        }
    }
    
    /**
     * Handles an opening XML node found inside an &lt;expr&gt; inside a CPP directive.
     * 
     * @param qName The XML node.
     * @param attributes The attributes of the XML node.
     */
    private void inCppExprStart(String qName, Attributes attributes) {
        inCppExprNodes.push(qName);
        
        if (qName.equals("call")) {
            inCppExprCall = true;
        }
        
        System.out.println("Start: " + inCppExprNodes);
    }
    
    /**
     * Handles a closing XML node found inside an &lt;expr&gt; inside a CPP directive.
     * 
     * @param qName The XML node.
     */
    private void inCppExprEnd(String qName) {
        inCppExprNodes.pop();
        System.out.println("End: " + inCppExprNodes);
        
        if (qName.equals("call")) {
            inCppExprString.append(")");
        }
    }
    
    /**
     * Calculates the presence condition from {@link #conditions}.
     * 
     * @return The presence condition;
     */
    private Formula getPc() {
        Formula pc = True.INSTANCE;
        
        for (Formula f : conditions) {
            if (!(f instanceof True)) {
                if (pc instanceof True) {
                    pc = f;
                } else {
                    pc = new Conjunction(pc, f);
                }
            }
        }
        
        return pc;
    }

    @Override
    public SyntaxElement getAst() {
        return topElement;
    }

}
