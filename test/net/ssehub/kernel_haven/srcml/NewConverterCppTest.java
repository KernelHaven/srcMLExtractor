package net.ssehub.kernel_haven.srcml;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
import net.ssehub.kernel_haven.srcml.model.CppElif;
import net.ssehub.kernel_haven.srcml.model.CppElse;
import net.ssehub.kernel_haven.srcml.model.CppEndif;
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
        
        Iterator<SrcMlSyntaxElement> it = elements.iterator();
        
        assertStatement(CppIf.class, "1", it.next());
        assertStatement(EmptyStatement.class, "A", it.next());
        assertStatement(CppEndif.class, "1", it.next());
        
        assertFalse("Got more elements than expected", it.hasNext());
    }
    
    /**
     * Test translation of nested <tt>&#35ifdef</tt> statements.
     */
    @Test
    public void testNestedIfDef() {
        SourceFile ast = loadFile("NestedIfDef.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        Iterator<SrcMlSyntaxElement> it = elements.iterator();
        
        assertStatement(EmptyStatement.class, "1", "1", it.next());
        // ifdef A
        assertStatement(CppIf.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "A", it.next());
        // nested ifdef B
        assertStatement(CppIf.class, "A", "A", it.next());
        assertStatement(EmptyStatement.class, "B", "A && B", it.next());
        assertStatement(CppEndif.class, "A", "A", it.next());
        // nested endif B
        assertStatement(EmptyStatement.class, "A", it.next());
        // endif a
        assertStatement(CppEndif.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "1", "1", it.next());
        
        assertFalse("Got more elements than expected", it.hasNext());
    }
    
    /**
     * Tests whether an #else block for an #ifdef is translated correcltly. 
     */
    @Test
    public void testElseForIfDef() {
        SourceFile ast = loadFile("ElseForIfdef.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        Iterator<SrcMlSyntaxElement> it = elements.iterator();
        
        assertStatement(EmptyStatement.class, "1", "1", it.next());
        // ifdef A
        assertStatement(CppIf.class, "1", it.next());
        assertStatement(EmptyStatement.class, "A", it.next());
        // else
        assertStatement(CppElse.class, "1", it.next());
        assertStatement(EmptyStatement.class, "!A", it.next());
        assertStatement(CppEndif.class, "1", it.next());
        assertStatement(EmptyStatement.class, "1", "1", it.next());
        
        assertFalse("Got more elements than expected", it.hasNext());
    }
    
    /**
     * Test that a simple <tt>&#35ifndef</tt> statement with a single empty statement can be parsed.
     */
    @Test
    public void testSimpleIfNDef() {
        SourceFile ast = loadFile("SimpleIfNDef.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        Iterator<SrcMlSyntaxElement> it = elements.iterator();
        
        assertStatement(EmptyStatement.class, "1", "1", it.next());
        assertStatement(CppIf.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "!A", it.next());
        assertStatement(CppEndif.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "1", "1", it.next());
        
        assertFalse("Got more elements than expected", it.hasNext());
    }
    
    /**
     * Test translation of nested <tt>&#35ifdef</tt> statements.
     */
    @Test
    public void testNestedIfNDef() {
        SourceFile ast = loadFile("NestedIfNDef.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        Iterator<SrcMlSyntaxElement> it = elements.iterator();
        
        // ifdef A
        assertStatement(CppIf.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "A", it.next());
        // nested ifndef B
        assertStatement(CppIf.class, "A", "A", it.next());
        assertStatement(EmptyStatement.class, "!B", "A && !B", it.next());
        // nested endif b
        assertStatement(CppEndif.class, "A", "A", it.next());
        assertStatement(EmptyStatement.class, "A", it.next());
        
        assertStatement(CppEndif.class, "1", "1", it.next());
        
        assertFalse("Got more elements than expected", it.hasNext());
    }
    
    /**
     * Tests whether an #else block for an #ifndef is translated correcltly. 
     */
    @Test
    public void testElseForIfNdef() {
        SourceFile ast = loadFile("ElseForIfNdef.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        Iterator<SrcMlSyntaxElement> it = elements.iterator();
        
        assertStatement(EmptyStatement.class, "1", "1", it.next());
        // ifdef A
        assertStatement(CppIf.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "!A", it.next());
        // else
        assertStatement(CppElse.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "!!A", it.next());
        assertStatement(CppEndif.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "1", "1", it.next());
        
        assertFalse("Got more elements than expected", it.hasNext());
    }

    /**
     * Test that a simple <tt>&#35if defined()</tt> statement with a single empty statement can be parsed.
     */
    @Test
    public void testSimpleIf() {
        SourceFile ast = loadFile("SimpleIf.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        Iterator<SrcMlSyntaxElement> it = elements.iterator();
        
        assertStatement(CppIf.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "A", "A", it.next());
        assertStatement(CppEndif.class, "1", "1", it.next());
        
        assertFalse("Got more elements than expected", it.hasNext());
    }
    
    /**
     * Test that a simple <tt>&#35if defined()</tt> with an <tt>&#35else</tt> statement can be parsed.
     */
    @Test
    public void testSimpleIfElse() {
        SourceFile ast = loadFile("SimpleIfElse.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        Iterator<SrcMlSyntaxElement> it = elements.iterator();

        assertStatement(CppIf.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "A", "A", it.next());
        assertStatement(CppElse.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "!A", "!A", it.next());
        assertStatement(CppEndif.class, "1", "1", it.next());
        
        assertFalse("Got more elements than expected", it.hasNext());
    }
    
    /**
     * Test that a simple <tt>&#35if defined()</tt> with an <tt>&#35elif</tt> statement can be parsed.
     */
    @Test
    public void testSimpleIfElif() {
        SourceFile ast = loadFile("SimpleIfElif.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        Iterator<SrcMlSyntaxElement> it = elements.iterator();
        
        // if defined(A)
        assertStatement(CppIf.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "A", "A", it.next());
        // elif defined(B)
        assertStatement(CppElif.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "!A && B", "!A && B", it.next());
        assertStatement(CppEndif.class, "1", "1", it.next());
        
        assertFalse("Got more elements than expected", it.hasNext());
    }
    
    /**
     * Test that a simple <tt>&#35if defined()</tt> with an <tt>&#35elif</tt> and <tt>&#35else</tt> statements
     * can be parsed.
     */
    @Test
    public void testSimpleIfElifElse() {
        SourceFile ast = loadFile("SimpleIfElifElse.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        Iterator<SrcMlSyntaxElement> it = elements.iterator();
        
        // if defined(A)
        assertStatement(CppIf.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "A", "A", it.next());
        // elif defined(B)
        assertStatement(CppElif.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "!A && B", "!A && B", it.next());
        // else
        assertStatement(CppElse.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "!(!A && B)", "!(!A && B)", it.next());
        assertStatement(CppEndif.class, "1", "1", it.next());
        
        assertFalse("Got more elements than expected", it.hasNext());
    }
    
    /**
     * Test translation of nested <tt>&#35if defined()</tt> statements.
     */
    @Test
    public void testNestedIf() {
        SourceFile ast = loadFile("NestedIf.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        Iterator<SrcMlSyntaxElement> it = elements.iterator();
        
        assertStatement(EmptyStatement.class, "1", "1", it.next());
        // if defined(A)
        assertStatement(CppIf.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "A", "A", it.next());
        // nested if defined(B)
        assertStatement(CppIf.class, "A", "A", it.next());
        assertStatement(EmptyStatement.class, "B", "A && B", it.next());
        // if defined(A)
        assertStatement(CppEndif.class, "A", "A", it.next());
        assertStatement(EmptyStatement.class, "A", "A", it.next());
        assertStatement(CppEndif.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "1", "1", it.next());
        
        assertFalse("Got more elements than expected", it.hasNext());
    }
    
    /**
     * Test that a <tt>&#35if defined()</tt> statement with a compound expression can be parsed.
     */
    @Test
    public void testCompoundIf() {
        SourceFile ast = loadFile("CompoundIf.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        Iterator<SrcMlSyntaxElement> it = elements.iterator();
        
        assertStatement(EmptyStatement.class, "1", "1", it.next());
        assertStatement(CppIf.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "A && B", "A && B", it.next());
        assertStatement(CppEndif.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "1", "1", it.next());
        
        assertFalse("Got more elements than expected", it.hasNext());
    }
    
    /**
     * Test that <tt>&#35if defined()</tt> statement with compound expression, <tt>elif</tt> and <tt>else</tt>
     * can be parsed.
     */
    @Test
    public void testCompoundIfElifElse() {
        SourceFile ast = loadFile("CompoundIfElifElse.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        Iterator<SrcMlSyntaxElement> it = elements.iterator();
        
        assertStatement(EmptyStatement.class, "1", "1", it.next());
        // if defined(A) && defined(B)
        assertStatement(CppIf.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "A && B", it.next());
        // elif !defined(C)
        assertStatement(CppElif.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "!(A && B) && !C", it.next());
        // else
        assertStatement(CppElse.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "!(!(A && B) && !C)", "!(!(A && B) && !C)",
            it.next());
        assertStatement(CppEndif.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "1", "1", it.next());
        
        assertFalse("Got more elements than expected", it.hasNext());
    }
    
    /**
     * Test that nested <tt>&#35if defined()</tt> statements with compound expressions can be parsed.
     */
    @Test
    public void testNestedCompoundIf() {
        SourceFile ast = loadFile("NestedCompoundIf.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        Iterator<SrcMlSyntaxElement> it = elements.iterator();
        
        assertStatement(EmptyStatement.class, "1", "1", it.next());
        // if defined(A) && defined(B)
        assertStatement(CppIf.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "A && B", "A && B", it.next());
        // nested if defined(C) && !defined(D)
        assertStatement(CppIf.class, "A && B", "A && B", it.next());
        assertStatement(EmptyStatement.class, "C && !D", "A && B && C && !D" , it.next());
        // first endif
        assertStatement(CppEndif.class, "A && B", "A && B", it.next());
        assertStatement(EmptyStatement.class, "A && B", "A && B", it.next());
        // second endif
        assertStatement(CppEndif.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "1", "1", it.next());
        
        assertFalse("Got more elements than expected", it.hasNext());
    }
    
    /**
     * Tests an if with a complicated structure of !, || and && operators and brackets.
     */
    @Test
    public void testComplicatedIf() {
        SourceFile ast = loadFile("ComplicatedIf.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        Iterator<SrcMlSyntaxElement> it = elements.iterator();
        
        assertStatement(EmptyStatement.class, "1", "1", it.next());
        assertStatement(CppIf.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "!((A || !!B) && C) && !D",
                "!((A || !!B) && C) && !D", it.next());
        assertStatement(CppEndif.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "1", "1", it.next());
        
        assertFalse("Got more elements than expected", it.hasNext());
    }
    
    /**
     * Tests an whether an if with a variable outside of a defined() call is handled correctly.
     */
    @Test
    public void testMissingDefined() {
        SourceFile ast = loadFile("MissingDefined.c");
        List<SrcMlSyntaxElement> elements = getElements(ast);
        
        Iterator<SrcMlSyntaxElement> it = elements.iterator();
        
        assertStatement(EmptyStatement.class, "1", "1", it.next());
        assertStatement(CppIf.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "0", "0", it.next());
        assertStatement(CppEndif.class, "1", "1", it.next());
        assertStatement(EmptyStatement.class, "1", "1", it.next());
        
        assertFalse("Got more elements than expected", it.hasNext());
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
     * @param element The element to test.
     */
    private void assertStatement(Class<? extends SrcMlSyntaxElement> type, String condition, SrcMlSyntaxElement element) {
        assertStatement(type, condition, null, element);
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
            SrcMlSyntaxElement element) {
        
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
    
}
