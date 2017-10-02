package net.ssehub.kernel_haven.srcml.xml.element_parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import net.ssehub.kernel_haven.code_model.Block;

/**
 * Interface for the translation of a single XML element.
 * @author El-Sharkawy
 *
 */
public interface IElementVisitor {
    
    /**
     * Returns the translated AST element.
     * @return The element, which was currently processed.
     */
    Block getAstElement();
    
    /**
     * Receive notification of the start of an element.
     * 
     * @param qName The qualified name (with prefix), or the
     *        empty string if qualified names are not available.
     * @param attributes The attributes attached to the element.  If
     *        there are no attributes, it shall be an empty
     *        Attributes object.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#startElement
     */
    public void startElement(String qName, Attributes attributes) throws SAXException;
    
    /**
     * Receive notification of the end of an element.
     * 
     * @param qName The qualified name (with prefix), or the
     *        empty string if qualified names are not available..
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#startElement
     */
    public void endElement(String qName) throws SAXException;

    /**
     * Receive notification of character data inside an element.
     *
     * @param ch The characters.
     * @param start The start position in the character array.
     * @param length The number of characters to use from the
     *               character array.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#characters
     */
    public void characters(char ch[], int start, int length) throws SAXException;
}
