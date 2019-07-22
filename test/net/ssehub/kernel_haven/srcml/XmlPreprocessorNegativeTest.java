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
 * Negative tests for the {@link XmlPrepreocessor}.
 *
 * @author Adam
 */
@RunWith(Parameterized.class)
public class XmlPreprocessorNegativeTest {

    private static final @NonNull File TESTDATA = new File("testdata/XmlPreprocessorTest/negative");
    
    private @NonNull File input;
    
    /**
     * Creates a test instance.
     * 
     * @param input The input XML to pass through the {@link XmlPrepreocessor}.
     */
    public XmlPreprocessorNegativeTest(@NonNull File input) {
        this.input = input;
    }
    
    /**
     * Creates the test data.
     * 
     * @return The test data.
     * 
     * @throws IOException If reading file names fails.
     */
    @Parameters(name = "{0}")
    public static Object[] data() throws IOException {
        List<File> sourceFiles = Files.walk(TESTDATA.toPath())
                .filter(p ->p.getFileName().toString().endsWith(".xml"))
                .map((path) -> path.toFile())
                .collect(Collectors.toList());
        
        return sourceFiles.toArray();
    }
    
    /**
     * Tests that the transformation on {@link #input} fails.
     * 
     * @throws SAXException unwanted.
     * @throws IOException unwanted.
     * @throws FormatException wanted.
     */
    @Test(expected = FormatException.class)
    public void testTransformationFails() throws SAXException, IOException, FormatException {
        assertThat("Input file doesn't exist", this.input.isFile(), is(true));
        
        Document input = XmlParser.parse(new FileInputStream(this.input));
        
        Node root = notNull(input.getDocumentElement());
        new XmlPrepreocessor(this.input, input).preprocess(root);
    }
    
}
