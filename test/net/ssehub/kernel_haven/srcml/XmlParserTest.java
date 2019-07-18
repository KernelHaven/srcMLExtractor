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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Tests the {@link XmlParser}.
 *
 * @author Adam
 */
public class XmlParserTest {

    /**
     * Tests simple opening and closing tags.
     * 
     * @throws IOException unwanted.
     * @throws SAXException unwanted. 
     */
    @Test
    public void testSimpleTags() throws SAXException, IOException {
        String xml = "<abc><def></def><c><some:tag /></c></abc>";
        
        Document doc = XmlParser.parse(new ByteArrayInputStream(xml.getBytes()));
        
        Node root = doc.getDocumentElement();
        assertThat(root.getNodeName(), is("abc"));
        assertThat(root.getNodeType(), is(Node.ELEMENT_NODE));
        
        NodeList rootChildren = root.getChildNodes();
        assertThat(rootChildren.getLength(), is(2));
        
        assertThat(rootChildren.item(0).getNodeName(), is("def"));
        assertThat(rootChildren.item(0).getNodeType(), is(Node.ELEMENT_NODE));
        assertThat(rootChildren.item(0).getChildNodes().getLength(), is(0));
        
        Node c = rootChildren.item(1);
        assertThat(c.getNodeName(), is("c"));
        assertThat(c.getNodeType(), is(Node.ELEMENT_NODE));
        assertThat(c.getChildNodes().getLength(), is(1));
        
        assertThat(c.getFirstChild().getNodeName(), is("some:tag"));
    }
    
    /**
     * Tests attributes on tags.
     * 
     * @throws IOException unwanted.
     * @throws SAXException unwanted. 
     */
    @Test
    public void testAttributes() throws SAXException, IOException {
        String xml = "<abc a=\"b\" kh:def=\"Hello World!\"></abc>";
        
        Document doc = XmlParser.parse(new ByteArrayInputStream(xml.getBytes()));
        
        Node root = doc.getDocumentElement();
        assertThat(root.getNodeName(), is("abc"));
        assertThat(root.getChildNodes().getLength(), is(0));
        
        NamedNodeMap attributes = root.getAttributes();
        assertThat(attributes.getLength(), is(2));
        
        assertThat(attributes.getNamedItem("a").getTextContent(), is("b"));
        assertThat(attributes.getNamedItem("kh:def").getTextContent(), is("Hello World!"));
    }
    
    /**
     * Tests text content nodes.
     * 
     * @throws SAXException unwanted.
     * @throws IOException unwanted.
     */
    @Test
    public void testTextContent() throws SAXException, IOException {
        String xml = "<a><b> </b><c> txt </c><d>\ns\tq\nu</d><e>\t\r\n</e></a>";
        
        Document doc = XmlParser.parse(new ByteArrayInputStream(xml.getBytes()));
        
        Node root = doc.getDocumentElement();
        assertThat(root.getNodeName(), is("a"));
        assertThat(root.getNodeType(), is(Node.ELEMENT_NODE));
        assertThat(root.getChildNodes().getLength(), is(4));
        
        Node b = root.getChildNodes().item(0);
        Node c = root.getChildNodes().item(1);
        Node d = root.getChildNodes().item(2);
        Node e = root.getChildNodes().item(3);
        
        assertThat(b.getNodeType(), is(Node.ELEMENT_NODE));
        assertThat(c.getNodeType(), is(Node.ELEMENT_NODE));
        assertThat(d.getNodeType(), is(Node.ELEMENT_NODE));
        assertThat(e.getNodeType(), is(Node.ELEMENT_NODE));
        
        
        assertThat(b.getChildNodes().getLength(), is(0)); // only whitespace -> no text node
        
        assertThat(c.getChildNodes().getLength(), is(1));
        Node cChild = c.getFirstChild();
        assertThat(cChild.getNodeType(), is(Node.TEXT_NODE));
        assertThat(cChild.getTextContent(), is("txt")); // whitespaces removed
        
        assertThat(d.getChildNodes().getLength(), is(2)); // two lines of text content
        Node dChild1 = d.getChildNodes().item(0);
        Node dChild2 = d.getChildNodes().item(1);
        
        assertThat(dChild1.getNodeType(), is(Node.TEXT_NODE));
        assertThat(dChild1.getTextContent(), is("s q")); // all whitespaces replaced by space and trimmed
        assertThat(dChild2.getNodeType(), is(Node.TEXT_NODE));
        assertThat(dChild2.getTextContent(), is("u")); // all whitespaces replaced by space and trimmed
        
        assertThat(e.getChildNodes().getLength(), is(0)); // only whitespace -> no text node
    }
    
    /**
     * Tests that an invalid XML structure throws an exception.
     * 
     * @throws SAXException wanted.
     * @throws IOException unwanted.
     */
    @Test(expected = SAXException.class)
    public void invalidXml() throws SAXException, IOException {
        String xml = "<a><b><c></b></c></a>";
        
        XmlParser.parse(new ByteArrayInputStream(xml.getBytes()));
    }
    
    /**
     * Tests that line numbers are set correctly.
     * 
     * @throws SAXException unwanted.
     * @throws IOException unwanted.
     */
    @Test
    public void testLineNumbers() throws SAXException, IOException {
        String xml =
                /* 1 */    "<a>\n"
                /* 2 */  + "<b></b>\n"
                /* 3 */  + "<c>\n"
                /* 4 */  + "txt\n"
                /* 5 */  + "<d>\n"
                /* 6 */  + "</d>\n"
                /* 7 */  + "</c>\n"
                /* 8 */  + "</a>\n";
        
        /*
         * Add an offset of -1; this is due a workaround in the XmlParser needed for the header that srcML prepends
         */
        final int offset = -1;
        
        Document doc = XmlParser.parse(new ByteArrayInputStream(xml.getBytes()));
        
        Node root = doc.getDocumentElement();
        assertThat(root.getNodeName(), is("a"));
        assertThat(root.getUserData(XmlUserData.LINE_START), is(1 + offset));
        assertThat(root.getUserData(XmlUserData.LINE_END), is(8 + offset));
        assertThat(root.getChildNodes().getLength(), is(2));
        
        Node b = root.getFirstChild();
        assertThat(b.getNodeName(), is("b"));
        assertThat(b.getUserData(XmlUserData.LINE_START), is(2 + offset));
        assertThat(b.getUserData(XmlUserData.LINE_END), is(2 + offset));
        assertThat(b.getChildNodes().getLength(), is(0));
        
        
        Node c = root.getLastChild();
        assertThat(c.getNodeName(), is("c"));
        assertThat(c.getUserData(XmlUserData.LINE_START), is(3 + offset));
        assertThat(c.getUserData(XmlUserData.LINE_END), is(7 + offset));
        assertThat(c.getChildNodes().getLength(), is(2));
        
        Node txt = c.getFirstChild();
        assertThat(txt.getNodeType(), is(Node.TEXT_NODE));
        assertThat(txt.getUserData(XmlUserData.LINE_START), is(4 + offset));
        assertThat(txt.getUserData(XmlUserData.LINE_END), is(4 + offset));
        
        Node d = c.getLastChild();
        assertThat(d.getNodeName(), is("d"));
        assertThat(d.getUserData(XmlUserData.LINE_START), is(5 + offset));
        assertThat(d.getUserData(XmlUserData.LINE_END), is(6 + offset));
        assertThat(d.getChildNodes().getLength(), is(0));
    }
    
}
