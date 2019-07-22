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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Tests the {@link XmlPrepreocessor}.
 *
 * @author Adam
 */
@RunWith(Parameterized.class)
public class XmlPreprocessorTest {

    private static final @NonNull File TESTDATA = new File("testdata/XmlPreprocessorTest");
    
    private static final @NonNull File INPUT = new File(TESTDATA, "input");
    
    private static final @NonNull File EXPECTED = new File(TESTDATA, "expected");

    private @NonNull File input;
    
    private @NonNull File expected;
    
    /**
     * Creates a test instance.
     * 
     * @param input The input XML to pass through the {@link XmlPrepreocessor}.
     * @param expected The expected XML outcome. 
     */
    public XmlPreprocessorTest(@NonNull File input, @NonNull File expected) {
        this.input = input;
        this.expected = expected;
    }
    
    /**
     * Creates the test data.
     * 
     * @return The test data.
     * 
     * @throws IOException If reading file names fails.
     */
    @Parameters(name = "{0}")
    public static Object[][] data() throws IOException {
        List<Path> sourceFiles = Files.walk(INPUT.toPath())
                .filter(p ->p.getFileName().toString().endsWith(".xml"))
                .collect(Collectors.toList());
        
        Object[][] result = new Object[sourceFiles.size()][];
        
        for (int i = 0; i < sourceFiles.size(); i++) {
            result[i] = new Object[2];
            result[i][0] = sourceFiles.get(i).toFile();
            result[i][1] = new File(EXPECTED, sourceFiles.get(i).toFile().getName());
        }
        
        return result;
    }
    
    /**
     * Tests the transformation on {@link #input}.
     * 
     * @throws SAXException unwanted.
     * @throws IOException unwanted.
     * @throws FormatException unwanted.
     */
    @Test
    public void testTransformation() throws SAXException, IOException, FormatException {
        assertThat("Input file doesn't exist", this.input.isFile(), is(true));
        assertThat("Expected output file doesn't exist", this.expected.isFile(), is(true));
        
        Document input = XmlParser.parse(new FileInputStream(this.input));
        Document expected = XmlParser.parse(new FileInputStream(this.expected));
        
        Node root = notNull(input.getDocumentElement());
        new XmlPrepreocessor(this.input, input).preprocess(root);
        
        assertNodeEquals(root, notNull(expected.getDocumentElement()));
    }
    
    /**
     * Checks that two XML nodes equal. Includes {@link XmlUserData#LINE_START} etc.
     * 
     * @param actual The actual node produced by the test.
     * @param expected The expected outcome.
     */
    private void assertNodeEquals(@NonNull Node actual, @NonNull Node expected) {
        assertThat(actual.getNodeName(), is(expected.getNodeName()));
        assertThat("Got wrong node type for <" + actual.getNodeName() + ">",
                actual.getNodeType(), is(expected.getNodeType()));
        
        if (expected.getNodeType() == Node.TEXT_NODE) {
            assertThat("Got wrong text content in text node", actual.getTextContent(), is(expected.getTextContent()));
        }
        
        int actualLineStart = (int) actual.getUserData(XmlUserData.LINE_START);
        int actualLineEnd = (int) actual.getUserData(XmlUserData.LINE_END);
        
        int expectedLineStart;
        int expectedLineEnd;
        if (expected.hasAttributes() && expected.getAttributes().getNamedItem("lineStart") != null) {
            expectedLineStart = Integer.parseInt(expected.getAttributes().getNamedItem("lineStart").getTextContent());
            expectedLineEnd = Integer.parseInt(expected.getAttributes().getNamedItem("lineEnd").getTextContent());
        } else {
            expectedLineStart = (int) expected.getUserData(XmlUserData.LINE_START);
            expectedLineEnd = (int) expected.getUserData(XmlUserData.LINE_END);
        }
        
        if (expected.getNodeType() != Node.TEXT_NODE) {
            assertThat("Got wrong start line for <" + actual.getNodeName() + ">",
                    actualLineStart, is(expectedLineStart));
            assertThat("Got wrong end line for <" + actual.getNodeName() + ">",
                    actualLineEnd, is(expectedLineEnd));
        }
        
        assertThat("Got wrong number of child nodes in <" + actual.getNodeName() + ">",
                actual.getChildNodes().getLength(), is(expected.getChildNodes().getLength()));
        
        for (int i = 0; i < expected.getChildNodes().getLength(); i++) {
            assertNodeEquals(notNull(actual.getChildNodes().item(i)), notNull(expected.getChildNodes().item(i)));
        }
    }
    
}
