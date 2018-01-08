package net.ssehub.kernel_haven.srcml.model;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.ssehub.kernel_haven.util.logic.Formula;

/**
 * A syntax element to represent an unknown element. This class is used so we can represent the parts of a source file
 * that we don't have any specialized class for.
 * 
 * @author Adam
 */
public class OtherSyntaxElement extends SrcMlSyntaxElement {

    private String name;
    
    private List<SrcMlSyntaxElement> nested;
    
    private Map<String, String> attributes; // TODO is this needed?
    
    public OtherSyntaxElement(String name, Formula presenceCondition) {
        super(presenceCondition);
        
        this.name = name;
        nested = new LinkedList<>();
        attributes = new HashMap<>();
    }
    
    public OtherSyntaxElement(String name, int lineStart, int lineEnd, File sourceFile, Formula condition,
            Formula presenceCondition) {
        
        super(lineStart, lineEnd, sourceFile, condition, presenceCondition);
        
        this.name = name;
        nested = new LinkedList<>();
        attributes = new HashMap<>();
    }

    @Override
    public int getNestedElementCount() {
        return nested.size();
    }

    @Override
    public SrcMlSyntaxElement getNestedElement(int index) throws IndexOutOfBoundsException {
        return nested.get(index);
    }

    @Override
    protected void addNestedElement(SrcMlSyntaxElement element) {
        nested.add(element);
    }
    
    @Override
    public void setNestedElement(int index, SrcMlSyntaxElement element) {
        nested.set(index, element);
    }

    /**
     * Adds an attribute to this element. The attributes are a direct representation of the XML attributes.
     * 
     * @param key The attribute name. Not <code>null</code>.
     * @param value The value of the attribute. Not <code>null</code>, may be empty.
     */
    public void addAttribute(String key, String value) {
        attributes.put(key, value);
    }
    
    /**
     * Retrieves an attribute of this element. The attributes are a direct representation of the XMl attributes.
     * 
     * @param key The name of the attribute. Not <code>null</code>.
     * @return The value of the attribute. <code>null</code> if this element does not contain the given attribute.
     */
    public String getAttribute(String key) {
        return attributes.get(key);
    }
    
    /**
     * Returns the name of this element. This is most likely the name of the XML node.
     * 
     * @return The name of this element. Not <code>null</code>.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Iterates over the elements nested inside this element. Not recursively.
     * 
     * @return An iterable over the nested elements
     */
    public Iterable<SrcMlSyntaxElement> iterateNestedOtherSyntaxElements() {
        return new Iterable<SrcMlSyntaxElement>() {
            
            @Override
            public Iterator<SrcMlSyntaxElement> iterator() {
                return new Iterator<SrcMlSyntaxElement>() {

                    private int index = 0;
                    
                    @Override
                    public boolean hasNext() {
                        return index < getNestedElementCount();
                    }

                    @Override
                    public SrcMlSyntaxElement next() {
                        return getNestedElement(index++);
                    }
                };
            }
        };
    }

    @Override
    protected String elementToString() {
        return name;
    }

}
