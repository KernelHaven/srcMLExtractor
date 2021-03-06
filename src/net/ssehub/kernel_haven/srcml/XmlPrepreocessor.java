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

import static net.ssehub.kernel_haven.srcml.XmlUserData.CPP_BLOCK_END;
import static net.ssehub.kernel_haven.srcml.XmlUserData.LINE_END;
import static net.ssehub.kernel_haven.srcml.XmlUserData.LINE_START;
import static net.ssehub.kernel_haven.srcml.XmlUserData.NODE_REFERENCE;
import static net.ssehub.kernel_haven.srcml.XmlUserData.PREVIOUS_CPP_BLOCK;
import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.io.File;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * A pre-processing step before converting the XML to AST.
 * <ul>
 *  <li>Removes all {@code extern "C"} headers</li>
 *  <li>Nests all nodes between {@code <cpp:if>} and {@code <cpp:endif>} in the <cpp:if> node.</li>
 * </ul>
 *
 * @author Adam
 */
class XmlPrepreocessor {
    
    private static final boolean DEBUG_LOGGING = SrcMLExtractor.DEBUG_LOGGING;
    
    /**
     * Contains all node names that will be converted to {@link ISyntaxElement}s without nested elements.
     * This is used to determine when to move {@code <cpp:if>} elements into their next sibling.
     */
    private static final @NonNull Set<@NonNull String> NODES_WITHOUT_NESTING;
    
    static {
        NODES_WITHOUT_NESTING = new HashSet<>(Arrays.asList(
            "expr_stmt",
            "continue",
            "break",
            "goto",
            "return",
            "empty_stmt",
            "function_decl",
            "struct_decl",
            "union_decl",
            "decl_stmt",
            "macro",
            "comment",
            "label"
        ));
    }
    
    private @NonNull File baseFile;
    
    private @NonNull Document doc;
    
    /**
     * Used in {@link #matchCppIfs(Node)}.
     */
    private @NonNull Deque<@NonNull Node> cppIfStack;

    /**
     * A list of all C-preprocessor block nodes. Populated by {@link #findRelevantNodes(Node)}, used by
     * {@link #convertNesting()}.
     */
    private @NonNull List<@NonNull Node> cppBlockNodes;
    
    /**
     * A list of all {@code <case>} and {@code <default>} nodes. Populated by {@link #findRelevantNodes(Node)}, used by
     * {@link #convertNesting()}.
     */
    private @NonNull List<@NonNull Node> caseNodes;
    
    /**
     * Helper variable for {@link #moveNodesAfterEndif(Node, Node, Node, boolean)}. Reset by
     * {@link #convertIfNesting(Node, Node)} before caling {@link #moveNodesAfterEndif(Node, Node, Node, boolean)}.
     */
    private int numMoved = 0;
    
    /**
     * Creates a preprocessor.
     * 
     * @param baseFile The current file that is parsed. Used for error messages.
     * @param doc The XML document. Used for creating new nodes.
     */
    public XmlPrepreocessor(@NonNull File baseFile, @NonNull Document doc) {
        this.baseFile = baseFile;
        this.doc = doc;
        this.cppIfStack = new LinkedList<>();
        
        this.cppBlockNodes = new LinkedList<>();
        this.caseNodes = new LinkedList<>();
    }

    /**
     * Does pre-processing on the given top-level node.
     * 
     * @param node The top-level node of the document. Must be a {@code <unit language="C">}.
     * 
     * @throws FormatException If pre-processing detects an invalid structure.
     */
    public void preprocess(@NonNull Node node) throws FormatException {
        if (DEBUG_LOGGING) {
            System.out.println();
            System.out.println();
        }
        
        matchCppIfs(node);

        if (DEBUG_LOGGING) {
            System.out.println();
            System.out.println();
        }
        
        findRelevantNodes(node);
        convertNesting();
        
        if (DEBUG_LOGGING) {
            System.out.println();
            System.out.println();
        }
    }
    
    /**
     * Recurses through the complete XMl structure and matches all if, ifdef, ifndef, else, elif, and endif nodes.
     * Uses {@link #cppIfStack}. After this,
     * {@link XmlUserData#CPP_BLOCK_END} is set for all if, ifdef, ifndef, else, and elif nodes; and
     * {@link XmlUserData#PREVIOUS_CPP_BLOCK} is set for all else and elif nodes.
     * 
     * @param node The XML (root) node to match all C-preprocessor blocks for.
     *  
     * @throws FormatException If the closing and opening (nesting) structure of the C-preprocessor blocks is invalid.
     */
    private void matchCppIfs(@NonNull Node node) throws FormatException {
        if (isStartingNode(node)) {
            cppIfStack.push(node);
            
        } else if (isContinue(node)) {
            if (cppIfStack.isEmpty()) {
                throw makeException(node, "Found <" + node.getNodeName() + "> without starting <cpp:if>");
            }
            
            Node start = notNull(cppIfStack.pop());
            start.setUserData(CPP_BLOCK_END, node, null);
            start.setUserData(LINE_END, (int) node.getUserData(LINE_START) - 1, null);
            node.setUserData(PREVIOUS_CPP_BLOCK, start, null);
            
            if (DEBUG_LOGGING) {
                System.out.println("<" + start.getNodeName() + " kh_lineStart="
                            + start.getUserData(LINE_START) + "> matches with <" + node.getNodeName()
                            + " kh:lineStart=" + node.getUserData(LINE_START) + ">");
            }
            
            cppIfStack.push(node);
            
        } else if (isEnd(node)) {
            if (cppIfStack.isEmpty()) {
                throw makeException(node, "Found <cpp:endif> without starting <cpp:if>");
            }
            
            Node start = notNull(cppIfStack.pop());
            start.setUserData(CPP_BLOCK_END, node, null);
            start.setUserData(LINE_END, node.getUserData(LINE_START), null);
            
            if (DEBUG_LOGGING) {
                System.out.println("<" + start.getNodeName() + " kh_lineStart="
                        + start.getUserData(LINE_START) + "> matches with <" + node.getNodeName()
                        + " kh:lineStart=" + node.getUserData(LINE_START) + ">");
            }
        }
        
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            matchCppIfs(notNull(children.item(i)));
        }
    }
    
    /**
     * Populates {@link #cppBlockNodes} and {@link #caseNodes}. Recurses through all child nodes of the given nodes
     * and searches for the relevant nodes.
     * 
     * @param node The parent node to search in.
     */
    private void findRelevantNodes(@NonNull Node node) {
        // order is important here: first search all child nodes, then recruse into them
        
        // search all child nodes for the node names we expect
        Node child = node.getFirstChild();
        while (child != null) {
            if (isCase(child)) {
                caseNodes.add(child);
                
            } else if (isStartingNode(child) || isContinue(child)) {
                cppBlockNodes.add(child);
            }
            
            child = child.getNextSibling();
        }
        
        // then recurse into all child nodes
        child = node.getFirstChild();
        while (child != null) {
            findRelevantNodes(child);
            
            child = child.getNextSibling();
        }
    }
    
    /**
     * Goes through the nodes found by {@link #findRelevantNodes(Node)} and converts the nesting structure for them.
     * 
     * @throws FormatException If the nesting structure can not be converted (e.g. because it is malformed).
     */
    private void convertNesting() throws FormatException {
        for (Node caseNode : caseNodes) {
            convertCaseNesting(caseNode);
        }
        
        for (Node cppNode : cppBlockNodes) {
            Node end = (Node) cppNode.getUserData(CPP_BLOCK_END);
            if (end == null) {
                throw makeException(cppNode, "Didn't find an <cpp:endif> for <" + cppNode.getNodeName() + ">");
            }
          
            convertIfNesting(cppNode, end);
        }
    }
    
    /**
     * Converts the nesting structure of case and default statements. The elements following a case label are moved
     * into that case node.
     * 
     * @param caseNode The case or default node.
     */
    private void convertCaseNesting(@NonNull Node caseNode) {
        Node sibling = caseNode.getNextSibling();
        while (sibling != null && !isCase(sibling)) {
            caseNode.appendChild(sibling);
            
            // update end line number for case
            caseNode.setUserData(LINE_END, sibling.getUserData(LINE_END), null);
            
            sibling = caseNode.getNextSibling();
        }
    }
    
    /**
     * Converts the nesting structure for the given C-preprocessor block. The elements contained in a CPP-block are
     * nested inside that block.
     * 
     * @param start The starting node of the block. Is one of: if, ifdef, ifndef, else, elif.
     * @param end The end node of the block. Is one of: else, elif, endif.
     * 
     * @throws FormatException If converting the nesting structure fails.
     */
    private void convertIfNesting(@NonNull Node start, @NonNull Node end) throws FormatException {
        fixStartingIfAtEnd(start);
        fixStartingIfBefore(start, end);
        
        Node parent = notNull(start.getParentNode());
        
        Node sibling;
        do {
            sibling = start.getNextSibling();
            
            if (sibling != null && sibling != end) {
                if (DEBUG_LOGGING) {
                    System.out.println("Moving <" + sibling.getNodeName() + "> into <" + start.getNodeName() + ">");
                }
                start.appendChild(sibling);
            }
        } while (sibling != null && !containsEndNode(sibling, end));
        
        if (sibling == null) {
            // we did not find the <cpp:endif> at this or any nested level
            // check if it follows immediately after any parent (or parent parent) closing tag
            boolean found = fixEndifNesting(parent, end);
            
            // if we found the <cpp:endif>, it is treated as if it was the last element in the current nesting
            // if not, then we throw an exception :-(
            if (!found) {
                throw makeException(start, "Did not find closing <" + end.getNodeName() + ">");
            }
        }
        
        // move all elements after the <cpp:endif> after the <cpp:if>
        if (DEBUG_LOGGING) {
            System.out.println("--- Start Move");
        }
        if (sibling != null && sibling != end) {
            numMoved = 0;
            moveNodesAfterEndif(start, end, sibling, false);
        }
        if (DEBUG_LOGGING) {
            System.out.println("--- End Move");
        }
        
        // remove the endif
        if (isEnd(end)) {
            end.getParentNode().removeChild(end);
        }
    }
    
    /**
     * Fixes a special case where a {@code <cpp:if*>} is the last child of a parent.
     * <p>
     * <b>Example:</b>
     * <code>
     * <pre>
     * &lt;a>
     *     &lt;b>&lt;/b>
     *     &lt;cpp:ifdef>&lt;/cpp:ifdef>
     *     &lt;cpp:ifdef>&lt;/cpp:ifdef>
     * &lt;/a>
     * </pre>
     * </code>
     * will be converted to:
     * <code>
     * <pre>
     * &lt;a>
     *     &lt;b>&lt;/b>
     * &lt;/a>
     * &lt;cpp:ifdef>&lt;/cpp:ifdef>
     * &lt;cpp:ifdef>&lt;/cpp:ifdef>
     * </pre>
     * </code>
     * 
     * @param cppstart The starting C-preprocessor XML node.
     */
    private void fixStartingIfAtEnd(@NonNull Node cppstart) {
        Function<@NonNull Node, @NonNull Boolean> isLastNode = new Function<@NonNull Node, @NonNull Boolean>() {
            
            @Override
            public @NonNull Boolean apply(@NonNull Node node) {
                return (isStartingNode(node) || isContinue(node))
                        && (node.getNextSibling() == null || apply(notNull(node.getNextSibling())));
            }
        };
        
        // if the starting ifdef is the last element of a block, move it to the parent
        while (isLastNode.apply(cppstart)) {
            Node p = cppstart.getParentNode();
            Node pp = p.getParentNode();
            
            Node child;
            do {
                child = p.getLastChild();
                pp.insertBefore(child, p.getNextSibling());
            } while (child != cppstart);
        }
    }
    
    /**
     * Fixes a special case where a {@code <cpp:if*>} is directly in front of a single statement, and the
     * corresponding {@code <cpp:endif>} is in that single statement.
     * <p>
     * <b>Example:</b>
     * <code>
     * <pre>
     * &lt;cpp:ifdef>&lt;/cpp:ifdef>
     * &lt;expr_stmt>
     *     &lt;b>&lt;/b>
     *     &lt;cpp:endif>&lt;/cpp:endif>
     * &lt;/expr_stmt>
     * </pre>
     * </code>
     * will be converted to:
     * <code>
     * <pre>
     * &lt;expr_stmt>
     *     &lt;cpp:ifdef>&lt;/cpp:ifdef>
     *     &lt;b>&lt;/b>
     *     &lt;cpp:endif>&lt;/cpp:endif>
     * &lt;/expr_stmt>
     * </pre>
     * </code>
     * 
     * @param cppStart The starting C-preprocessor node.
     * @param cppEnd The ending C-preprocessor node.
     */
    private void fixStartingIfBefore(@NonNull Node cppStart, @NonNull Node cppEnd) {
        Node sibling = cppStart.getNextSibling();
        if (NODES_WITHOUT_NESTING.contains(sibling.getNodeName()) && containsEndNode(sibling, cppEnd)) {
            do {
                sibling.insertBefore(cppStart, sibling.getFirstChild());
                
                // repeat this until we moved in deep enough; we don't need to check NODES_WITHOUT_NESTING anymore,
                // since we know we are already inside one
                sibling = cppStart.getNextSibling();
            } while (sibling != null && containsEndNode(sibling, cppEnd));
        }
    }
    
    /**
     * Fixes a special case where the {@code <cpp:endif>} has a lower nesting depth than its corresponding
     * {@code <cpp:if*>}.
     * 
     * <p>
     * <b>Example:</b>
     * <code>
     * <pre>
     * &lt;a>
     *     &lt;cpp:ifdef>&lt;/cpp:ifdef>
     *     &lt;b>&lt;/b>
     *     &lt;cpp:ifdef>&lt;/cpp:ifdef>
     * &lt;/a>
     * &lt;cpp:endif>&lt;/cpp:endif>
     * &lt;cpp:endif>&lt;/cpp:endif>
     * </pre>
     * </code>
     * will be converted to:
     * <code>
     * <pre>
     * &lt;a>
     *     &lt;cpp:ifdef>&lt;/cpp:ifdef>
     *     &lt;b>&lt;/b>
     *     &lt;cpp:ifdef>&lt;/cpp:ifdef>
     *     &lt;cpp:endif>&lt;/cpp:endif>
     *     &lt;cpp:endif>&lt;/cpp:endif>
     * &lt;/a>
     * </pre>
     * </code>
     * 
     * @param parent The parent node of the {@code <cpp:if*>} that does not contain the end node.
     * @param end The {@code <cpp:endif>} node.
     * 
     * @return <code>true</code> if this special case was detected and fixed.
     */
    private boolean fixEndifNesting(@NonNull Node parent, @NonNull Node end) {
        boolean found = false;
        Node p = parent;
        while (!found && p != null) {
            Node pSibling = p.getNextSibling();
            
            while (!found && pSibling != null && (isContinue(pSibling) || isEnd(pSibling))) {
                if (pSibling == end) {
                    found = true;
                    
                    // move all <cpp:endif>s between p and end
                    pSibling = p.getNextSibling();
                    while (pSibling != end) {
                        if (DEBUG_LOGGING) {
                            System.out.println("Moving <" + pSibling.getNodeName()
                                + "> into <" + parent.getNodeName() + ">");
                        }
                        parent.appendChild(pSibling);
                        
                        pSibling = p.getNextSibling();
                    }
                }
                
                pSibling = pSibling.getNextSibling();
            }
            
            p = p.getParentNode();
        }
        return found;
    }
    
    /**
     * Recursively checks if the given node contains the given end node.
     * 
     * @param node The node to check in (deep).
     * @param end The node to search for.
     * 
     * @return <code>true</code> If the end node is found anywhere inside the given node.
     */
    private boolean containsEndNode(@NonNull Node node, @NonNull Node end) {
        boolean result = false;
        if (node == end) {
            result = true;
        } else {
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                result = containsEndNode(notNull(children.item(i)), end);
                if (result) {
                    break;
                }
            }
        }
        return result;
    }
    
    /**
     * Moves all nodes that appear after the given end node (endif) outside of the ifdef node.
     * 
     * @param ifdef The node starting the C-preprocessor block.
     * @param endif The node ending the C-preprocessor block.
     * @param containingEndif The sibling of the ifdef node that contains the endif node.
     * @param foundEndif Whether the endif was found (yet). <code>false</code> for the initial call from
     *      {@link #convertIfNesting(Node, Node)}.
     *      
     * @return Whether the endif node was found (yet).
     */
    private boolean moveNodesAfterEndif(@NonNull Node ifdef, @NonNull Node endif, @NonNull Node containingEndif,
            boolean foundEndif) {
        
        NodeList children = containingEndif.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = notNull(children.item(i));
            if (child == endif) {
                foundEndif = true;
            }
            
            if (foundEndif) {
                if (DEBUG_LOGGING) {
                    System.out.println("Moving <" + child.getNodeName() + "> into after <" + ifdef.getNodeName() + ">");
                }
                
                // move this child to after the ifdef
                if (isStartingNode(child) || isContinue(child) || isEnd(child)) {
                    // do not leave a reference for <cpp:if*> elements
                    i--;
                    
                } else {
                    // create a reference node
                    Node reference = doc.createElement("kh:reference");
                    reference.setUserData(NODE_REFERENCE, child, null);
                    reference.setUserData(LINE_START, child.getUserData(LINE_START), null);
                    reference.setUserData(LINE_END, child.getUserData(LINE_END), null);
                    containingEndif.replaceChild(reference, child);
                }
                Node ifdefParent = ifdef.getParentNode();
                Node ifdefSibling = ifdef.getNextSibling();
                for (int j = 0; j < numMoved; j++) {
                    if (ifdefSibling == null) {
                        break;
                    }
                    ifdefSibling = ifdefSibling.getNextSibling();
                }
                ifdefParent.insertBefore(child, ifdefSibling);
                numMoved++;
                
            } else {
                foundEndif = moveNodesAfterEndif(ifdef, endif, child, foundEndif);
            }
        }
        
        return foundEndif;
    }
    
    /**
     * Checks whether this is if, ifdef or ifndef.
     * 
     * @param node The XML node.
     * 
     * @return Whether this node starts a new block level.
     */
    private static boolean isStartingNode(@NonNull Node node) {
        String name = node.getNodeName();
        return name.equals("cpp:if") || name.equals("cpp:ifdef") || name.equals("cpp:ifndef");
    }
    
    /**
     * Checks whether this is elif or else.
     * 
     * @param node The XML node.
     * 
     * @return Whether this node is a new block on the same level.
     */
    private static boolean isContinue(@NonNull Node node) {
        String name = node.getNodeName();
        return name.equals("cpp:elif") || name.equals("cpp:else");
    }
    
    /**
     * Checks whether this is endif.
     * 
     * @param node The XML node.
     * 
     * @return Whether this node ends a level.
     */
    private static boolean isEnd(@NonNull Node node) {
        String name = node.getNodeName();
        return name.equals("cpp:endif");
    }
    
    /**
     * Checks whether this is a case or default node.
     * 
     * @param node The XML node.
     * 
     * @return Whether this is a case or default node.
     */
    private static boolean isCase(@NonNull Node node) {
        String name = node.getNodeName();
        return name.equals("case") || name.equals("default");
    }
    
    /**
     * Creates an exception with properly points to the offending code.
     * 
     * @param cause The node that caused the exception. Used for locating the error.
     * @param message The message to print into the exception.
     * 
     * @return The created exception.
     */
    private @NonNull FormatException makeException(@NonNull Node cause, @NonNull String message) {
        Object line = cause.getUserData(LINE_START);
        String prefix;
        if (line != null && line instanceof Integer) {
            prefix = baseFile.getPath() + ":" + line;
        } else {
            prefix = baseFile.getPath();
        }
        return new FormatException(prefix + " " + message);
    }
    
}
