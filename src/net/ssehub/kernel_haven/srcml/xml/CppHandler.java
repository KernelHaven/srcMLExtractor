package net.ssehub.kernel_haven.srcml.xml;

import java.util.Stack;

import org.xml.sax.Attributes;

import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;
import net.ssehub.kernel_haven.util.logic.parser.Parser;
import net.ssehub.kernel_haven.util.logic.parser.VariableCache;

/**
 * A separate helper class for parsing the XML structure of C preprocessor elements.
 * 
 * @author Adam
 */
public class CppHandler {
    
    private static final Logger LOGGER = Logger.get();
    
    private static final boolean LOG_IN_EXPR_STRUCUTRE = false;
    
    private static final boolean LOG_EXPR_STRING = false;
    
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
     * Creates a new {@link CppHandler}.
     */
    public CppHandler() {
        conditions = new Stack<>();
        conditions.push(True.INSTANCE);
        inCppExprNodes = new Stack<>();
    }
    
    /**
     * Whether we are currently inside a &lt;cpp:&gt; directive. When this is <code>true</code>, this handler wants
     * all the elements that start and end ({@link #startElement(String, Attributes)} and {@link #endElement(String)}).
     * 
     * @return Whether we are currently inside a CPP expression.
     */
    public boolean inCpp() {
        return inCpp != null;
    }
    
    /**
     * Called for opening XML nodes. This must be called if we are {@link #inCpp()}, or a &lt;cpp:&gt; was found.
     * 
     * @param qName The name of the XML node.
     * @param attributes The attributes of the XML node.
     */
    public void startElement(String qName, Attributes attributes) {
        if (inCpp != null) {

            if (inCppExpr) {
                inCppExprStart(qName, attributes);
                
            } else if (qName.equals("expr")) {
                // start parsing the expression of the cpp directive
                inCppExprString = new StringBuilder();
                inCppExpr = true;
                
//            } else {
//                LOGGER.logWarning("Don't know what to do with opening " + qName + " inside " + inCpp);
            }
            
        } else if (qName.startsWith("cpp:")) {
            inCpp = qName;
            
        } else {
            throw new IllegalStateException("Got a starting element, but we are not inside a <cpp:> element");
        }
    }
    
    /**
     * Called for a closing XML node. This must be called if we are {@link #inCpp()}.
     * 
     * @param qName The name of the closed XML node.
     */
    public void endElement(String qName) {
        if (inCpp == null) {
            throw new IllegalStateException("Closing element passed, but we are not in a CPP expression");
        }
        
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
                    conditions.push(new Negation(conditions.pop()));
                    break;
                case "cpp:elif":
                    // an elif negates the previous and appends the parsed condition
                    conditions.push(new Conjunction(new Negation(conditions.pop()), cppExpr));
                    break;
                case "cpp:endif":
                    // an endif clears the condition
                    conditions.pop();
                    break;
                // TODO: ifdef, ifndef
                    
                default:
                    throw new IllegalArgumentException("Unknown CPP directive: " + qName);
                }
            }
            
        } else {
            if (inCppExpr && inCppExprNodes.isEmpty() && qName.equals("expr")) { 
                // we reached the end of the CPP expression (</expr>);
                // we should have a formula now
                inCppExpr = false;
                
                if (LOG_EXPR_STRING) {
                    System.out.println("\nExprString: " + inCppExprString + "\n");
                }
                VariableCache cache = new VariableCache();
                Parser<Formula> parser = new Parser<>(new SrcMlConditionGrammar(cache));
                
                try {
                    cppExpr = parser.parse(inCppExprString.toString());
                } catch (ExpressionFormatException e) {
                    throw new RuntimeException(e);
                }
                
            } else if (inCppExpr) {
                inCppExprEnd(qName);
                
//            } else {
//                LOGGER.logWarning("Don't know what to do with closing " + qName + " inside " + inCpp);
            }
        }
    }
    
    /**
     * Called for characters nested inside XML nodes. Must be called if we are {@link #inCpp()}.
     * 
     * @param str The nested characters.
     */
    public void characters(String str) {
        if (inCppExpr) {
            if (!inCppExprNodes.empty()) {
                
                if (inCppExprNodes.peek().equals("name")) {
                    if (LOG_IN_EXPR_STRUCUTRE) {
                        System.out.println("Name: " + inCppExprNodes + " -> " + str);
                    }
                    
                    if (inCppExprCall) {
                        inCppExprCall = false;
                        inCppExprString.append(str).append("(");
                        
                    } else {
                        if (inCppExprNodes.size() < 5
                                || !inCppExprNodes.get(inCppExprNodes.size() - 5).equals("call")) {
                            // if a variable name is used outside of a call
                            LOGGER.logWarning("Variable \"" + str + "\" used in preprocessor condition outside of a "
                                    + "function call; assuming false");
                            inCppExprString.append("0");
                            
                        } else {
                            inCppExprString.append(str);
                        }
                    }
                    
                } else if (inCppExprNodes.peek().equals("operator")) {
                    if (LOG_IN_EXPR_STRUCUTRE) {
                        System.out.println("Op: " + inCppExprNodes + " -> " + str);
                    }
                    
                    inCppExprString.append(str);
                }
            }
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
        
        if (LOG_IN_EXPR_STRUCUTRE) {
            System.out.print("Start: " + inCppExprNodes);
            if (attributes.getLength() > 0) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    if (i != 0) {
                        System.out.println(", ");
                    }
                    System.out.print(attributes.getQName(i) + "=" + attributes.getValue(i));
                }
            }
            System.out.println();
        }
    }

    /**
     * Handles a closing XML node found inside an &lt;expr&gt; inside a CPP directive.
     * 
     * @param qName The XML node.
     */
    private void inCppExprEnd(String qName) {
        if (LOG_IN_EXPR_STRUCUTRE) {
            System.out.println("End: " + inCppExprNodes);
        }
        inCppExprNodes.pop();
        
        if (qName.equals("call")) {
            inCppExprString.append(")");
        }
    }
    
    /**
     * The condition that a new element should have.
     * 
     * @return The condition that a newly constructed element should have.
     */
    public Formula getCondition() {
        return conditions.peek();
    }
    
    /**
     * Calculates the presence condition that a new element should have.
     * 
     * @return The presence condition for a newly constructed element.
     */
    public Formula getPc() {
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
    
    /**
     * Called whenever a normal element is added to the stack. This is used to make sure that the conditions for new
     * elements correctly represent the nesting hierarchy.
     */
    public void onNormalElementAdded() {
        conditions.push(True.INSTANCE);
    }

    /**
     * Called whenever a normal element is removed from the stack. This is used to make sure that the conditions for
     * new elements correctly represent the nesting hierarchy.
     */
    public void onNormalElementRemoved() {
        conditions.pop();
    }
    
}
