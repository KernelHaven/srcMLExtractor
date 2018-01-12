package net.ssehub.kernel_haven.srcml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.code_model.CodeElement;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.srcml.model.CppIf;
import net.ssehub.kernel_haven.srcml.model.EmptyStatement;
import net.ssehub.kernel_haven.srcml.model.SrcMlSyntaxElement;
import net.ssehub.kernel_haven.test_utils.TestConfiguration;
import net.ssehub.kernel_haven.util.ExtractorException;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Util;

/**
 * Tests the translation of C-preprocessor directives by the {@link SrcMLExtractor}.
 * 
 * @author Adam
 * @author El-Sharkawy
 *
 */
public class NewConverterCppTest {
    
    private static final File RESOURCE_DIR = new File(AllTests.TESTDATA, "tmpRes");
    
    /**
     * Initializes the logger.
     */
    @BeforeClass
    public static void beforeClass() {
        if (null == Logger.get()) {
            Logger.init();
        }
    }
    
    @Before
    public void enableNewConverter() {
        SrcMLExtractor.USE_NEW_CONVERTER = true;
        
        RESOURCE_DIR.mkdir();
    }
    
    @After
    public void disableNewConverter() throws IOException {
        SrcMLExtractor.USE_NEW_CONVERTER = false;
        
        Util.deleteFolder(RESOURCE_DIR);
    }
    
    /**
     * Test that a simple <tt>&#35ifdef</tt> statement with a single empty statement can be parsed.
     */
    @Test
    public void testSimpleIfDef() {
        SourceFile ast = loadFile("SimpleIfDef.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 1, elements.size());
        
        CppIf ifElem = assertIf("1", "1", 1, new int[0], 0, elements.get(0));
        assertStatement(EmptyStatement.class, "A", "A", ifElem.getThenElement(0));
        
    }
    
    /**
     * Test translation of nested <tt>&#35ifdef</tt> statements.
     */
    @Test
    public void testNestedIfDef() {
        SourceFile ast = loadFile("NestedIfDef.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());
        
        assertStatement(EmptyStatement.class, "1", "1", elements.get(0));
        CppIf outerIf = assertIf("1", "1", 3, new int[0], 0, elements.get(1));
        assertStatement(EmptyStatement.class, "1", "1", elements.get(2));
        
        
        assertStatement(EmptyStatement.class, "A", "A", outerIf.getThenElement(0));
        CppIf innerIf = assertIf("A", "A", 1, new int[0], 0, outerIf.getThenElement(1));
        assertStatement(EmptyStatement.class, "A", "A", outerIf.getThenElement(2));
        
        assertStatement(EmptyStatement.class, "B", "A && B", innerIf.getNestedElement(0));
    }
    
    /**
     * Tests whether an #else block for an #ifdef is translated correcltly. 
     */
    @Test
    public void testElseForIfDef() {
        SourceFile ast = loadFile("ElseForIfdef.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());

        assertStatement(EmptyStatement.class, "1", "1", elements.get(0));
        CppIf ifElem = assertIf("1", "1", 1, new int[0], 1, elements.get(1));
        assertStatement(EmptyStatement.class, "1", "1", elements.get(2));
        
        assertStatement(EmptyStatement.class, "A", "A", ifElem.getThenElement(0));
        assertStatement(EmptyStatement.class, "!A", "!A", ifElem.getElseElement(0));
    }
    
    /**
     * Test that a simple <tt>&#35ifndef</tt> statement with a single empty statement can be parsed.
     */
    @Test
    public void testSimpleIfNDef() {
        SourceFile ast = loadFile("SimpleIfNDef.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());

        assertStatement(EmptyStatement.class, "1", "1", elements.get(0));
        CppIf ifElem = assertIf("1", "1", 1, new int[0], 0, elements.get(1));
        assertStatement(EmptyStatement.class, "1", "1", elements.get(2));
        
        assertStatement(EmptyStatement.class, "!A", "!A", ifElem.getThenElement(0));
    }
    
    /**
     * Test translation of nested <tt>&#35ifdef</tt> statements.
     */
    @Test
    public void testNestedIfNDef() {
        SourceFile ast = loadFile("NestedIfNDef.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 1, elements.size());
        
        CppIf outerIf = assertIf("1", "1", 3, new int[0], 0, elements.get(0));
        
        assertStatement(EmptyStatement.class, "A", "A", outerIf.getThenElement(0));
        CppIf innerIf = assertIf("A", "A", 1, new int[0], 0, outerIf.getThenElement(1));
        assertStatement(EmptyStatement.class, "A", "A", outerIf.getThenElement(2));
        
        assertStatement(EmptyStatement.class, "!B", "A && !B", innerIf.getNestedElement(0));
    }
    
    
    /**
     * Tests whether an #else block for an #ifndef is translated correcltly. 
     */
    @Test
    public void testElseForIfNdef() {
        SourceFile ast = loadFile("ElseForIfNdef.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());

        assertStatement(EmptyStatement.class, "1", "1", elements.get(0));
        CppIf ifElem = assertIf("1", "1", 1, new int[0], 1, elements.get(1));
        assertStatement(EmptyStatement.class, "1", "1", elements.get(2));
        
        assertStatement(EmptyStatement.class, "!A", "!A", ifElem.getThenElement(0));
        assertStatement(EmptyStatement.class, "!!A", "!!A", ifElem.getElseElement(0));
    }

    /**
     * Test that a simple <tt>&#35if defined()</tt> statement with a single empty statement can be parsed.
     */
    @Test
    public void testSimpleIf() {
        SourceFile ast = loadFile("SimpleIf.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 1, elements.size());
        
        CppIf ifElem = assertIf("1", "1", 1, new int[0], 0, elements.get(0));
        
        assertStatement(EmptyStatement.class, "A", "A", ifElem.getThenElement(0));
    }
    
    /**
     * Test that a simple <tt>&#35if defined()</tt> with an <tt>&#35else</tt> statement can be parsed.
     */
    @Test
    public void testSimpleIfElse() {
        SourceFile ast = loadFile("SimpleIfElse.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 1, elements.size());

        CppIf ifElem = assertIf("1", "1", 1, new int[0], 1, elements.get(0));
        
        assertStatement(EmptyStatement.class, "A", "A", ifElem.getThenElement(0));
        assertStatement(EmptyStatement.class, "!A", "!A", ifElem.getElseElement(0));
    }
    
    /**
     * Test that a simple <tt>&#35if defined()</tt> with an <tt>&#35elif</tt> statement can be parsed.
     */
    @Test
    public void testSimpleIfElif() {
        SourceFile ast = loadFile("SimpleIfElif.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 1, elements.size());

        CppIf ifElem = assertIf("1", "1", 1, new int[] { 1 }, 0, elements.get(0));
        
        assertStatement(EmptyStatement.class, "A", "A", ifElem.getThenElement(0));
        assertStatement(EmptyStatement.class, "!A && B", "!A && B", ifElem.getElifElement(0, 0));
    }
    
    /**
     * Test that a simple <tt>&#35if defined()</tt> with an <tt>&#35elif</tt> and <tt>&#35else</tt> statements
     * can be parsed.
     */
    @Test
    public void testSimpleIfElifElse() {
        SourceFile ast = loadFile("SimpleIfElifElse.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 1, elements.size());

        CppIf ifElem = assertIf("1", "1", 1, new int[] { 1 }, 1, elements.get(0));
        
        assertStatement(EmptyStatement.class, "A", "A", ifElem.getThenElement(0));
        assertStatement(EmptyStatement.class, "!A && B", "!A && B", ifElem.getElifElement(0, 0));
        assertStatement(EmptyStatement.class, "!A && !B", "!A && !B", ifElem.getElseElement(0));
    }
    
    /**
     * Test translation of nested <tt>&#35if defined()</tt> statements.
     */
    @Test
    public void testNestedIf() {
        SourceFile ast = loadFile("NestedIf.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());
        
        assertStatement(EmptyStatement.class, "1", "1", elements.get(0));
        CppIf outerIf = assertIf("1", "1", 3, new int[0], 0, elements.get(1));
        assertStatement(EmptyStatement.class, "1", "1", elements.get(2));
        
        
        assertStatement(EmptyStatement.class, "A", "A", outerIf.getThenElement(0));
        CppIf innerIf = assertIf("A", "A", 1, new int[0], 0, outerIf.getThenElement(1));
        assertStatement(EmptyStatement.class, "A", "A", outerIf.getThenElement(2));
        
        assertStatement(EmptyStatement.class, "B", "A && B", innerIf.getNestedElement(0));
    }
    
    /**
     * Test that a <tt>&#35if defined()</tt> statement with a compound expression can be parsed.
     */
    @Test
    public void testCompoundIf() {
        SourceFile ast = loadFile("CompoundIf.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());
        
        assertStatement(EmptyStatement.class, "1", "1", elements.get(0));
        CppIf ifElem = assertIf("1", "1", 1, new int[0], 0, elements.get(1));
        assertStatement(EmptyStatement.class, "1", "1", elements.get(2));
        
        assertStatement(EmptyStatement.class, "A && B", "A && B", ifElem.getThenElement(0));
    }
    
    /**
     * Test that <tt>&#35if defined()</tt> statement with compound expression, <tt>elif</tt> and <tt>else</tt>
     * can be parsed.
     */
    @Test
    public void testCompoundIfElifElse() {
        SourceFile ast = loadFile("CompoundIfElifElse.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());
        
        assertStatement(EmptyStatement.class, "1", "1", elements.get(0));
        CppIf ifElem = assertIf("1", "1", 1, new int[] {1}, 1, elements.get(1));
        assertStatement(EmptyStatement.class, "1", "1", elements.get(2));
        
        assertStatement(EmptyStatement.class, "A && B", "A && B", ifElem.getThenElement(0));
        assertStatement(EmptyStatement.class, "!(A && B) && !C", "!(A && B) && !C", ifElem.getElifElement(0, 0));
        assertStatement(EmptyStatement.class, "!(A && B) && !!C", "!(A && B) && !!C",
                ifElem.getElseElement(0));
    }
    
    /**
     * Test that nested <tt>&#35if defined()</tt> statements with compound expressions can be parsed.
     */
    @Test
    public void testNestedCompoundIf() {
        SourceFile ast = loadFile("NestedCompoundIf.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());
        
        assertStatement(EmptyStatement.class, "1", "1", elements.get(0));
        CppIf outerIf = assertIf("1", "1", 3, new int[0], 0, elements.get(1));
        assertStatement(EmptyStatement.class, "1", "1", elements.get(2));
        
        assertStatement(EmptyStatement.class, "A && B", "A && B", outerIf.getThenElement(0));
        CppIf innerIf = assertIf("A && B", "A && B", 1, new int[0], 0, outerIf.getThenElement(1));
        assertStatement(EmptyStatement.class, "A && B", "A && B", outerIf.getThenElement(2));
        
        assertStatement(EmptyStatement.class, "C && !D", "A && B && C && !D", innerIf.getThenElement(0));
    }
    
    /**
     * Tests an if with a complicated structure of !, || and && operators and brackets.
     */
    @Test
    public void testComplicatedIf() {
        SourceFile ast = loadFile("ComplicatedIf.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());

        assertStatement(EmptyStatement.class, "1", "1", elements.get(0));
        CppIf ifElem = assertIf("1", "1", 1, new int[0], 0, elements.get(1));
        assertStatement(EmptyStatement.class, "1", "1", elements.get(2));
        
        assertStatement(EmptyStatement.class, "!((A || !!B) && C) && !D", "!((A || !!B) && C) && !D",
                ifElem.getThenElement(0));
    }
    
    /**
     * Tests an whether an if with a variable outside of a defined() call is handled correctly.
     */
    @Test
    public void testMissingDefined() {
        SourceFile ast = loadFile("MissingDefined.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());

        assertStatement(EmptyStatement.class, "1", "1", elements.get(0));
        CppIf ifElem = assertIf("1", "1", 1, new int[0], 0, elements.get(1));
        assertStatement(EmptyStatement.class, "1", "1", elements.get(2));
        
        assertStatement(EmptyStatement.class, "0", "0", ifElem.getThenElement(0));
    }
    
    /**
     * Helper method which runs the {@link SrcMLExtractor} on the specified source file.
     *  
     * @param file The source file to parse.
     * @return The parsed code model, ready for testing the result.
     */
    private SourceFile loadFile(String file) {
        file = "cpp/" + file;
        File srcFile = new File(AllTests.TESTDATA, file);
        Assert.assertTrue("Specified test file does not exist: " + file, srcFile.isFile());
        
        SourceFile result = null;
        try {
            Properties props = new Properties();
            props.setProperty("resource_dir", RESOURCE_DIR.getAbsolutePath());
            props.setProperty("source_tree", "testdata/");
            props.setProperty("code.extractor.files", file);
            
            TestConfiguration config = new TestConfiguration(props);
            
            SrcMLExtractor extractor = new SrcMLExtractor();
            extractor.init(config);
            result = extractor.runOnFile(srcFile);
        } catch (SetUpException | ExtractorException exc) {
            Assert.fail("Failed to initialize SrcMLExtractor: " + exc.getMessage());
        }
        
        Assert.assertNotNull("Test file wasn't translated: " + file, result);
        return result;
    }
    
    /**
     * Returns the top level elements of the translation unit of the parsed {@link SourceFile} as a list.
     * @param file A file, which was translated with {@link SrcMLExtractor}.
     * @return The top level elements.
     */
    private List<SrcMlSyntaxElement> getElements(SourceFile file) {
        List<SrcMlSyntaxElement> result = new ArrayList<>();
        Assert.assertEquals("The SourceFile has more than only one translation unit: "
            + file.getPath().getAbsolutePath(), 1, file.getTopElementCount());
        
        // Extract translation unit
        for (CodeElement element : file) {
            if (!(element instanceof SrcMlSyntaxElement)) {
                Assert.fail("SourceFile \"" + file.getPath().getAbsolutePath() + "\" contains a non SrcMlSyntaxElement: "
                    + element);
            }
            
            // Extract the relevant, top level elements
            SrcMlSyntaxElement translationUnit = (SrcMlSyntaxElement) element;
            for (int i = 0; i < translationUnit.getNestedElementCount(); i++) {
                result.add((SrcMlSyntaxElement) translationUnit.getNestedElement(i));                
            }
        }
        
        return result;
    }

    /**
     * Tests the statement.
     * @param type The expected syntax type
     * @param condition The expected condition in c-style.
     * @param presenceCondition The expected presence/compound in c-style. This contains also all surrounding
     *     conditions.
     * @param element The element to test.
     */
    private void assertStatement(Class<? extends SrcMlSyntaxElement> type, String condition, String presenceCondition,
            CodeElement element) {
        
        // Syntax check
        Assert.assertTrue("Wrong syntax element type: expected " + type.getSimpleName() + "; actual: "
                + element.getClass().getSimpleName(), element.getClass().equals(type));
        
        // Check of current condition
        if (null != condition) {
            Assert.assertEquals("Wrong condition", condition, element.getCondition().toString());
        } else {
            Assert.assertNull("Element has a condition, but wasn't expected.", element.getCondition());
        }
        
        // Check of presence condition
        if (null != presenceCondition) {
            Assert.assertEquals("Wrong presence/compound condition", presenceCondition,
                element.getPresenceCondition().toString());
        }
    }
    
    private CppIf assertIf(String condition, String presenceCondition, int numThen, int[] numElif, int numElse,
            CodeElement element) {
        
        assertStatement(CppIf.class, condition, presenceCondition, element);
        
        CppIf cppIf = (CppIf) element;
        
        assertEquals("Wrong number of statements in then block", numThen, cppIf.getNumThenElements());
        
        if (numElse == 0) {
            assertFalse("Got else block but did not expect one", cppIf.hasElseBlock());
        } else {
            assertTrue("Expected else block but got none", cppIf.hasElseBlock());
            assertEquals("Wrong number of statements in else block", numElse, cppIf.getNumElseElements());
        }
        
        assertEquals("Wrong number of elif blocks", numElif.length, cppIf.getNumElifBlocks());
        for (int i = 0; i < numElif.length; i++) {
            assertEquals("Elif block " + i + " has wrong number of statements", numElif[i], cppIf.getNumElifElements(i));
        }
        
        return cppIf;
    }
    
}
