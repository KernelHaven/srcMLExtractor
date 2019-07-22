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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Collection XML userdata keys used in this parser.
 * 
 * @see Node#getUserData(String)
 * @see Node#setUserData(String, Object, org.w3c.dom.UserDataHandler)
 *
 * @author Adam
 */
class XmlUserData {

    public static final @NonNull String LINE_START = "net.ssehub.kernel_haven:line_start";
    
    public static final @NonNull String LINE_END = "net.ssehub.kernel_haven:line_end";
    
    public static final @NonNull String NODE_REFERENCE = "net.ssehub.kernel_haven:reference";
    
    public static final @NonNull String PREVIOUS_CPP_BLOCK = "net.ssehub.kernel_haven:previous_cpp_block";
    
    public static final @NonNull String CPP_BLOCK_END = "net.ssehub.kernel_haven:cpp_block_end";
    
    public static final @NonNull String CONVERTED = "net.ssehub.kernel_haven:converted";

    /**
     * Don't allow any instances.
     */
    private XmlUserData() {
    }
    
    /**
     * Prints the given XML to {@link System#out} for debug purposes. Includes the userdata as attributes.
     * 
     * @param node The node to print.
     */
    public static void debugPrintXml(@NonNull Node node) {
        if (SrcMLExtractor.DEBUG_LOGGING) {
            debugPrintXmlImpl(node, "");
        }
    }
    
    /**
     * Implementation for {@link #debugPrintXml(Node)}.
     * 
     * @param element The element to print.
     * @param indentation The current indentation.
     */
    private static void debugPrintXmlImpl(@NonNull Node element, @NonNull String indentation) {
        if (SrcMLExtractor.DEBUG_LOGGING) {
            if (element.getNodeType() == Node.TEXT_NODE) {
                
                if (!element.getTextContent().trim().isEmpty()) {
                    System.out.println(indentation + element.getTextContent().trim());
                }
                
            } else {
                System.out.print(indentation + "<" + element.getNodeName());
                
                NamedNodeMap attributes = element.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node attribute = attributes.item(i);
                    System.out.print(" " + attribute.getNodeName() + "=\"" + attribute.getTextContent() + "\"");
                }
                
                printUserData(element, LINE_START, "lineStart");
                printUserData(element, LINE_END, "lineEnd");
                printUserData(element, NODE_REFERENCE, "nodeReference");
                printUserData(element, PREVIOUS_CPP_BLOCK, "previousCppBlock");
                printUserData(element, CPP_BLOCK_END, "cppBlockEnd");
                printUserData(element, CONVERTED, "converted");
                System.out.println(">");
                
                NodeList children = element.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    debugPrintXmlImpl(notNull(children.item(i)), indentation + "\t");
                }
                
                System.out.println(indentation + "</" + element.getNodeName() + ">");
            }
        }
    }
    
    /**
     * Prints a single userdata "attribute".
     * 
     * @param node The node to print the userdata of.
     * @param userData The userdata key.
     * @param name The name to print for the userdata.
     */
    private static void printUserData(@NonNull Node node, @NonNull String userData, @NonNull String name) {
        if (SrcMLExtractor.DEBUG_LOGGING) {
            Object data = node.getUserData(userData);
            if (data != null) {
                System.out.print(" ud:" + name + "=\"");
                
                if (data instanceof Node) {
                    Node n = (Node) data;
                    System.out.print("node: " + n.getNodeName());
                    Integer line = (Integer) n.getUserData(LINE_START);
                    if (line != null) {
                        System.out.print(" line " + line);
                    }
                } else if (data instanceof ISyntaxElement) {
                    System.out.print(data.getClass().getSimpleName()); // TODO
                } else {
                    System.out.print(data.toString());
                }
                
                System.out.print("\"");
            }
        }
    }
    
}
