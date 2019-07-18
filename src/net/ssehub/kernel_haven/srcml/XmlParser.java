/*
 * Copyright 2019 University of Hildesheim, Software Systems Engineering
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.ssehub.kernel_haven.srcml;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Deque;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Parses an XML stream to a {@link Document}. While parsing, this also does the following:
 * <ul>
 *  <li>Annotates {@link Node}s with their start and end line number (see {@link Node#getUserData(String)},
 *  {@link XmlUserData#LINE_START},  {@link XmlUserData#LINE_END}</li> 
 *  <li>Removes leading and trailing whitespaces in text nodes, and ignores whitespace-only text nodes</li>
 *  <li>Converts all whitespace in text nodes to a single ' '</li>
 * </ul>
 *
 * @author Adam
 */
public class XmlParser {
    
    /**
     * A SAX handler that stores line numbers of {@link Node}s and omits whitespace-only text nodes.
     */
    private static class LineNumberHandler extends DefaultHandler {

        private Locator locator;
        
        private @NonNull Document doc;
        
        private @NonNull Deque<@NonNull Node> elements;
        
        /**
         * Creates a {@link LineNumberHandler}.
         * 
         * @param doc The {@link Document} to add the read nodes to.
         */
        public LineNumberHandler(@NonNull Document doc) {
            this.doc = doc;
            this.elements = new LinkedList<>();
        }
        
        @Override
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }
        
        /**
         * Returns the current line number.
         * 
         * @return The current line number.
         */
        private int getLineNumber() {
            return locator.getLineNumber() - 1;
        }
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            Element elem = doc.createElement(qName);
            for (int i = 0; i < attributes.getLength(); i++) {
                elem.setAttribute(attributes.getQName(i), attributes.getValue(i));
            }
            
            elem.setUserData(XmlUserData.LINE_START, getLineNumber(), null);
            
            elements.push(elem);
        }
        
        @Override
        public void endElement(String uri, String localName, String qName) {
            Node elem = notNull(elements.pop());
            
            elem.setUserData(XmlUserData.LINE_END, getLineNumber(), null);
            
            if (elements.isEmpty()) {
                // top-level element
                doc.appendChild(elem);
            } else {
                notNull(elements.peek()).appendChild(elem);
            }
        }
        
        @Override
        public void characters(char[] ch, int start, int length) {
            String str = new String(ch, start, length);
            str = str.trim();
            if (!str.isEmpty()) {
                Node textNode = doc.createTextNode(str.replaceAll("\\s+", " "));
                
                /*
                 * Calculate the start and end line number:
                 * - getLineNumber() is at the end of the text buffer
                 * - for the start line, we need to subtract how many newlines appeared before that (ignoring the
                 *   trimmed-off whitespace at the start of the buffer)
                 * - for the end line, we need to subtract how many newlines appeared between end of text node and
                 *   the end of the buffer (trimmed-off whitespace) 
                 */
                
                // count number of newlines starting with the text content (skip the whitespace trimmed at the start)
                int numNewlines = 0;
                boolean foundFirstNonWhitespace = false;
                for (int i = start; i < start + length; i++) {
                    if (!Character.isWhitespace(ch[i])) {
                        foundFirstNonWhitespace = true;
                    }
                    
                    if (foundFirstNonWhitespace && ch[i] == '\n') {
                        numNewlines++;
                    }
                }
                
                // count the number of newlines after the text content
                int numNewLinesEnd = 0;
                for (int i = start + length - 1; i >= start; i--) {
                    if (!Character.isWhitespace(ch[i])) {
                        break;
                    }
                    
                    if (ch[i] == '\n') {
                        numNewLinesEnd++;
                    }
                }
                
                int lineNumber = getLineNumber();
                int lineStart = lineNumber - numNewlines;
                int lineEnd = lineNumber - numNewLinesEnd;
                
                textNode.setUserData(XmlUserData.LINE_START, lineStart, null);
                textNode.setUserData(XmlUserData.LINE_END, lineEnd, null);
                
                notNull(elements.peek()).appendChild(textNode);
            }
        }
        
    }
    
    /**
     * Don't allow any instances. 
     */
    private XmlParser() {
    }
    
    /**
     * Parses the given input stream.
     * 
     * @param in The input stream to parse.
     * 
     * @return The {@link Document} that was read.
     * 
     * @throws SAXException If the XML is malformed.
     * @throws IOException If reading the stream fails.
     */
    public static @NonNull Document parse(@NonNull InputStream in) throws SAXException, IOException {
        SAXParser parser;
        Document doc;
        try {
            parser = SAXParserFactory.newInstance().newSAXParser();
            doc = notNull(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        } catch (ParserConfigurationException e) {
            throw new SAXException("Can't create document or parser", e);
        }
        
        parser.parse(in, new LineNumberHandler(doc));
        
        return doc;
    }
    
}
