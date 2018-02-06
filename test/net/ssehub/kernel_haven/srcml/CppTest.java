package net.ssehub.kernel_haven.srcml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
import net.ssehub.kernel_haven.code_model.ast.CppBlock;
import net.ssehub.kernel_haven.code_model.ast.CppBlock.Type;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.code_model.ast.SingleStatement;
import net.ssehub.kernel_haven.test_utils.TestConfiguration;
import net.ssehub.kernel_haven.util.ExtractorException;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Util;
import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.Variable;

/**
 * Tests the translation of C-preprocessor directives by the {@link SrcMLExtractor}.
 * 
 * @author Adam
 * @author El-Sharkawy
 *
 */
public class CppTest {
    
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
    public void setup() {
        RESOURCE_DIR.mkdir();
    }
    
    @After
    public void teardown() throws IOException {
        Util.deleteFolder(RESOURCE_DIR);
    }
    
    /**
     * Test that a simple <tt>&#35ifdef</tt> statement with a single empty statement can be parsed.
     */
    @Test
    public void testSimpleIfDef() {
        SourceFile ast = loadFile("SimpleIfDef.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 1, elements.size());
        
        CppBlock ifElem = assertIf("A", "A", new Variable("A"), 1, Type.IFDEF, elements.get(0));
        assertStatement(SingleStatement.class, "A", "A", ifElem.getNestedElement(0));
        
    }
    
    /**
     * Test translation of nested <tt>&#35ifdef</tt> statements.
     */
    @Test
    public void testNestedIfDef() {
        SourceFile ast = loadFile("NestedIfDef.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());
        
        assertStatement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock outerIf = assertIf("A", "A", new Variable("A"), 3, Type.IFDEF, elements.get(1));
        assertStatement(SingleStatement.class, "1", "1", elements.get(2));
        
        
        assertStatement(SingleStatement.class, "A", "A", outerIf.getNestedElement(0));
        CppBlock innerIf = assertIf("B", "A && B", new Variable("B"), 1, Type.IFDEF, outerIf.getNestedElement(1));
        assertStatement(SingleStatement.class, "A", "A", outerIf.getNestedElement(2));
        
        assertStatement(SingleStatement.class, "B", "A && B", innerIf.getNestedElement(0));
    }
    
    /**
     * Tests whether an #else block for an #ifdef is translated correcltly. 
     */
    @Test
    public void testElseForIfDef() {
        SourceFile ast = loadFile("ElseForIfdef.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 4, elements.size());

        assertStatement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock ifElem = assertIf("A", "A", new Variable("A"), 1, Type.IFDEF, elements.get(1));
        CppBlock elseElem = assertIf("!A", "!A", new Negation(new Variable("A")), 1, Type.ELSE, elements.get(2));
        assertStatement(SingleStatement.class, "1", "1", elements.get(3));
        
        assertStatement(SingleStatement.class, "A", "A", ifElem.getNestedElement(0));
        assertStatement(SingleStatement.class, "!A", "!A", elseElem.getNestedElement(0));
    }
    
    /**
     * Test that a simple <tt>&#35ifndef</tt> statement with a single empty statement can be parsed.
     */
    @Test
    public void testSimpleIfNDef() {
        SourceFile ast = loadFile("SimpleIfNDef.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());

        assertStatement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock ifElem = assertIf("!A", "!A", new Negation(new Variable("A")), 1, Type.IFNDEF, elements.get(1));
        assertStatement(SingleStatement.class, "1", "1", elements.get(2));
        
        assertStatement(SingleStatement.class, "!A", "!A", ifElem.getNestedElement(0));
    }
    
    /**
     * Test translation of nested <tt>&#35ifdef</tt> statements.
     */
    @Test
    public void testNestedIfNDef() {
        SourceFile ast = loadFile("NestedIfNDef.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 1, elements.size());
        
        CppBlock outerIf = assertIf("A", "A", new Variable("A"), 3, Type.IFDEF, elements.get(0));
        
        assertStatement(SingleStatement.class, "A", "A", outerIf.getNestedElement(0));
        CppBlock innerIf = assertIf("!B", "A && !B", new Negation(new Variable("B")), 1, Type.IFNDEF, outerIf.getNestedElement(1));
        assertStatement(SingleStatement.class, "A", "A", outerIf.getNestedElement(2));
        
        assertStatement(SingleStatement.class, "!B", "A && !B", innerIf.getNestedElement(0));
    }
    
    
    /**
     * Tests whether an #else block for an #ifndef is translated correcltly. 
     */
    @Test
    public void testElseForIfNdef() {
        SourceFile ast = loadFile("ElseForIfNdef.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 4, elements.size());

        assertStatement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock ifElem = assertIf("!A", "!A", new Negation(new Variable("A")), 1, Type.IFNDEF, elements.get(1));
        CppBlock elseElem = assertIf("!!A", "!!A", new Negation(new Negation(new Variable("A"))), 1, Type.ELSE, elements.get(2));
        assertStatement(SingleStatement.class, "1", "1", elements.get(3));
        
        assertStatement(SingleStatement.class, "!A", "!A", ifElem.getNestedElement(0));
        assertStatement(SingleStatement.class, "!!A", "!!A", elseElem.getNestedElement(0));
    }

    /**
     * Test that a simple <tt>&#35if defined()</tt> statement with a single empty statement can be parsed.
     */
    @Test
    public void testSimpleIf() {
        SourceFile ast = loadFile("SimpleIf.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 1, elements.size());
        
        CppBlock ifElem = assertIf("A", "A", new Variable("A"), 1, Type.IF, elements.get(0));
        
        assertStatement(SingleStatement.class, "A", "A", ifElem.getNestedElement(0));
    }
    
    /**
     * Test that a simple <tt>&#35if defined()</tt> with an <tt>&#35else</tt> statement can be parsed.
     */
    @Test
    public void testSimpleIfElse() {
        SourceFile ast = loadFile("SimpleIfElse.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 2, elements.size());

        CppBlock ifElem = assertIf("A", "A", new Variable("A"), 1, Type.IF, elements.get(0));
        CppBlock elseElem = assertIf("!A", "!A", new Negation(new Variable("A")), 1, Type.ELSE, elements.get(1));
        
        assertStatement(SingleStatement.class, "A", "A", ifElem.getNestedElement(0));
        assertStatement(SingleStatement.class, "!A", "!A", elseElem.getNestedElement(0));
    }
    
    /**
     * Test that a simple <tt>&#35if defined()</tt> with an <tt>&#35elif</tt> statement can be parsed.
     */
    @Test
    public void testSimpleIfElif() {
        SourceFile ast = loadFile("SimpleIfElif.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 2, elements.size());

        CppBlock ifElem = assertIf("A", "A", new Variable("A"), 1, Type.IF, elements.get(0));
        CppBlock elifElem = assertIf("!A && B", "!A && B",
                new Conjunction(new Negation(new Variable("A")), new Variable("B")), 1, Type.ELSEIF, elements.get(1));
        
        assertStatement(SingleStatement.class, "A", "A", ifElem.getNestedElement(0));
        assertStatement(SingleStatement.class, "!A && B", "!A && B", elifElem.getNestedElement(0));
    }
    
    /**
     * Test that a simple <tt>&#35if defined()</tt> with an <tt>&#35elif</tt> and <tt>&#35else</tt> statements
     * can be parsed.
     */
    @Test
    public void testSimpleIfElifElse() {
        SourceFile ast = loadFile("SimpleIfElifElse.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());


        CppBlock ifElem = assertIf("A", "A", new Variable("A"), 1, Type.IF, elements.get(0));
        CppBlock elifElem = assertIf("!A && B", "!A && B",
                new Conjunction(new Negation(new Variable("A")), new Variable("B")), 1, Type.ELSEIF, elements.get(1));
        CppBlock elseElem = assertIf("!A && !B", "!A && !B",
                new Conjunction(new Negation(new Variable("A")), new Negation(new Variable("B"))), 1,
                Type.ELSE, elements.get(1));
        
        assertStatement(SingleStatement.class, "A", "A", ifElem.getNestedElement(0));
        assertStatement(SingleStatement.class, "!A && B", "!A && B", elifElem.getNestedElement(0));
        assertStatement(SingleStatement.class, "!A && !B", "!A && !B", elseElem.getNestedElement(0));
    }
    
    /**
     * Test translation of nested <tt>&#35if defined()</tt> statements.
     */
    @Test
    public void testNestedIf() {
        SourceFile ast = loadFile("NestedIf.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());
        
        assertStatement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock outerIf = assertIf("A", "A", new Variable("A"), 3, Type.IF, elements.get(1));
        assertStatement(SingleStatement.class, "1", "1", elements.get(2));
        
        
        assertStatement(SingleStatement.class, "A", "A", outerIf.getNestedElement(0));
        CppBlock innerIf = assertIf("B", "A && B", new Variable("B"), 1, Type.IF, outerIf.getNestedElement(1));
        assertStatement(SingleStatement.class, "A", "A", outerIf.getNestedElement(2));
        
        assertStatement(SingleStatement.class, "B", "A && B", innerIf.getNestedElement(0));
    }
    
    /**
     * Test that a <tt>&#35if defined()</tt> statement with a compound expression can be parsed.
     */
    @Test
    public void testCompoundIf() {
        SourceFile ast = loadFile("CompoundIf.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());
        
        assertStatement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock ifElem = assertIf("A && B", "A && B", new Conjunction(new Variable("A"), new Variable("B")),
                1, Type.IF, elements.get(1));
        assertStatement(SingleStatement.class, "1", "1", elements.get(2));
        
        assertStatement(SingleStatement.class, "A && B", "A && B", ifElem.getNestedElement(0));
    }
    
    /**
     * Test that <tt>&#35if defined()</tt> statement with compound expression, <tt>elif</tt> and <tt>else</tt>
     * can be parsed.
     */
    @Test
    public void testCompoundIfElifElse() {
        SourceFile ast = loadFile("CompoundIfElifElse.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 5, elements.size());
        
        Formula aAndB = new Conjunction(new Variable("A"), new Variable("B"));
        Formula notC = new Negation(new Variable("C"));
        Formula notAAndBAndNotC = new Conjunction(new Negation(aAndB), notC);
        Formula notAAndBAndNotNotC = new Conjunction(new Negation(aAndB), new Negation(notC));
        
        assertStatement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock ifElem = assertIf("A && B", "A && B", aAndB, 1, Type.IF, elements.get(1));
        CppBlock elifElem = assertIf("!(A && B) && !C", "!(A && B) && !C", notAAndBAndNotC, 1, Type.ELSEIF, elements.get(1));
        CppBlock elseElem = assertIf("!(A && B) && !!C", "!(A && B) && !!C", notAAndBAndNotNotC, 1, Type.ELSE, elements.get(1));
        assertStatement(SingleStatement.class, "1", "1", elements.get(2));
        
        assertStatement(SingleStatement.class, "A && B", "A && B", ifElem.getNestedElement(0));
        assertStatement(SingleStatement.class, "!(A && B) && !C", "!(A && B) && !C", elifElem.getNestedElement(0));
        assertStatement(SingleStatement.class, "!(A && B) && !!C", "!(A && B) && !!C",
                elseElem.getNestedElement(0));
    }
    
    /**
     * Test that nested <tt>&#35if defined()</tt> statements with compound expressions can be parsed.
     */
    @Test
    public void testNestedCompoundIf() {
        SourceFile ast = loadFile("NestedCompoundIf.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());
        
        Formula aAndB = new Conjunction(new Variable("A"), new Variable("B"));
        Formula cAndNotD = new Conjunction(new Variable("C"), new Negation(new Variable("D")));
        
        assertStatement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock outerIf = assertIf("A && B", "A && B", aAndB, 3, Type.IF, elements.get(1));
        assertStatement(SingleStatement.class, "1", "1", elements.get(2));
        
        assertStatement(SingleStatement.class, "A && B", "A && B", outerIf.getNestedElement(0));
        CppBlock innerIf = assertIf("C && !D", "A && B && C && !D", cAndNotD, 1, Type.IF, outerIf.getNestedElement(1));
        assertStatement(SingleStatement.class, "A && B", "A && B", outerIf.getNestedElement(2));
        
        assertStatement(SingleStatement.class, "C && !D", "A && B && C && !D", innerIf.getNestedElement(0));
    }
    
    /**
     * Tests an if with a complicated structure of !, || and && operators and brackets.
     */
    @Test
    public void testComplicatedIf() {
        SourceFile ast = loadFile("ComplicatedIf.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());
        
        Formula f = new Conjunction(new Negation(new Conjunction(
                new Disjunction(new Variable("A"), new Negation(new Negation(new Variable("B")))), new Variable("C"))),
                new Negation(new Variable("D")));

        assertStatement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock ifElem = assertIf("!((A || !!B) && C) && !D", "!((A || !!B) && C) && !D", f, 1, Type.IF, elements.get(1));
        assertStatement(SingleStatement.class, "1", "1", elements.get(2));
        
        assertStatement(SingleStatement.class, "!((A || !!B) && C) && !D", "!((A || !!B) && C) && !D",
                ifElem.getNestedElement(0));
    }
    
    /**
     * Tests an whether an if with a variable outside of a defined() call is handled correctly.
     */
    @Test
    public void testMissingDefined() {
        SourceFile ast = loadFile("MissingDefined.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());

        assertStatement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock ifElem = assertIf("0", "0", False.INSTANCE, 1, Type.IF, elements.get(1));
        assertStatement(SingleStatement.class, "1", "1", elements.get(2));
        
        assertStatement(SingleStatement.class, "0", "0", ifElem.getNestedElement(0));
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
            result = extractor.runOnFile(new File(file));
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
    private List<ISyntaxElement> getElements(SourceFile file) {
        List<ISyntaxElement> result = new ArrayList<>();
        Assert.assertEquals("The SourceFile has more than only one translation unit: "
            + file.getPath().getAbsolutePath(), 1, file.getTopElementCount());
        
        // Extract translation unit
        for (CodeElement element : file) {
            if (!(element instanceof ISyntaxElement)) {
                Assert.fail("SourceFile \"" + file.getPath().getAbsolutePath() + "\" contains a non SrcMlSyntaxElement: "
                    + element);
            }
            
            // Extract the relevant, top level elements
            ISyntaxElement translationUnit = (ISyntaxElement) element;
            for (int i = 0; i < translationUnit.getNestedElementCount(); i++) {
                result.add(translationUnit.getNestedElement(i));                
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
    private void assertStatement(Class<? extends ISyntaxElement> type, String condition, String presenceCondition,
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
    
    private CppBlock assertIf(String condition, String presenceCondition, Formula ifCondition, int numNested,
            Type type, CodeElement element) {
        
        assertStatement(CppBlock.class, condition, presenceCondition, element);
        
        CppBlock cppIf = (CppBlock) element;
        
        assertEquals("Wrong type", type, cppIf.getType());
        
        assertEquals("Wrong number of nested statements", numNested, cppIf.getNestedElementCount());
        
        if (ifCondition == null) {
            assertNull("Wrong CPP-If condition",cppIf.getCondition());
        } else {
            assertEquals("Wrong CPP-If condition", ifCondition, cppIf.getCondition());
        }
        
        return cppIf;
    }
    
}
