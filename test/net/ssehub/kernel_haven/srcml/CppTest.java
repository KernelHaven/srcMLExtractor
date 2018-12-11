package net.ssehub.kernel_haven.srcml;

import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.and;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.not;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.or;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.code_model.ast.CppBlock;
import net.ssehub.kernel_haven.code_model.ast.CppBlock.Type;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.code_model.ast.SingleStatement;
import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Variable;

/**
 * Tests the translation of C-preprocessor directives by the {@link SrcMLExtractor}.
 * 
 * @author Adam
 * @author El-Sharkawy
 */
public class CppTest extends AbstractSrcMLExtractorTest {
    
    /**
     * Test that a simple <tt>&#35ifdef</tt> statement with a single empty statement can be parsed.
     */
    @Test
    public void testSimpleIfDef() {
        SourceFile<ISyntaxElement> ast = loadFile("SimpleIfDef.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 1, elements.size());
        
        CppBlock ifElem = assertIf("A", "A", new Variable("A"), 1, Type.IFDEF, elements.get(0));
        assertElement(SingleStatement.class, "A", "A", ifElem.getNestedElement(0));
        
    }
    
    /**
     * Test translation of nested <tt>&#35ifdef</tt> statements.
     */
    @Test
    public void testNestedIfDef() {
        SourceFile<ISyntaxElement> ast = loadFile("NestedIfDef.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());
        
        assertElement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock outerIf = assertIf("A", "A", new Variable("A"), 3, Type.IFDEF, elements.get(1));
        assertElement(SingleStatement.class, "1", "1", elements.get(2));
        
        
        assertElement(SingleStatement.class, "A", "A", outerIf.getNestedElement(0));
        CppBlock innerIf = assertIf("B", "A && B", new Variable("B"), 1, Type.IFDEF, outerIf.getNestedElement(1));
        assertElement(SingleStatement.class, "A", "A", outerIf.getNestedElement(2));
        
        assertElement(SingleStatement.class, "B", "A && B", innerIf.getNestedElement(0));
    }
    
    /**
     * Tests whether an #else block for an #ifdef is translated correcltly. 
     */
    @Test
    public void testElseForIfDef() {
        SourceFile<ISyntaxElement> ast = loadFile("ElseForIfdef.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 4, elements.size());

        assertElement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock ifElem = assertIf("A", "A", new Variable("A"), 1, Type.IFDEF, elements.get(1));
        CppBlock elseElem = assertIf("!A", "!A", not("A"), 1, Type.ELSE, elements.get(2));
        assertElement(SingleStatement.class, "1", "1", elements.get(3));
        
        assertElement(SingleStatement.class, "A", "A", ifElem.getNestedElement(0));
        assertElement(SingleStatement.class, "!A", "!A", elseElem.getNestedElement(0));
    }
    
    /**
     * Test that a simple <tt>&#35ifndef</tt> statement with a single empty statement can be parsed.
     */
    @Test
    public void testSimpleIfNDef() {
        SourceFile<ISyntaxElement> ast = loadFile("SimpleIfNDef.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());

        assertElement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock ifElem = assertIf("!A", "!A", not("A"), 1, Type.IFNDEF, elements.get(1));
        assertElement(SingleStatement.class, "1", "1", elements.get(2));
        
        assertElement(SingleStatement.class, "!A", "!A", ifElem.getNestedElement(0));
    }
    
    /**
     * Test translation of nested <tt>&#35ifdef</tt> statements.
     */
    @Test
    public void testNestedIfNDef() {
        SourceFile<ISyntaxElement> ast = loadFile("NestedIfNDef.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 1, elements.size());
        
        CppBlock outerIf = assertIf("A", "A", new Variable("A"), 3, Type.IFDEF, elements.get(0));
        
        assertElement(SingleStatement.class, "A", "A", outerIf.getNestedElement(0));
        CppBlock innerIf = assertIf("!B", "A && !B", not("B"), 1, Type.IFNDEF, outerIf.getNestedElement(1));
        assertElement(SingleStatement.class, "A", "A", outerIf.getNestedElement(2));
        
        assertElement(SingleStatement.class, "!B", "A && !B", innerIf.getNestedElement(0));
    }
    
    
    /**
     * Tests whether an #else block for an #ifndef is translated correcltly. 
     */
    @Test
    public void testElseForIfNdef() {
        SourceFile<ISyntaxElement> ast = loadFile("ElseForIfNdef.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 4, elements.size());

        assertElement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock ifElem = assertIf("!A", "!A", not("A"), 1, Type.IFNDEF, elements.get(1));
        CppBlock elseElem = assertIf("!!A", "!!A", not(not("A")), 1, Type.ELSE, elements.get(2));
        assertElement(SingleStatement.class, "1", "1", elements.get(3));
        
        assertElement(SingleStatement.class, "!A", "!A", ifElem.getNestedElement(0));
        assertElement(SingleStatement.class, "!!A", "!!A", elseElem.getNestedElement(0));
    }

    /**
     * Test that a simple <tt>&#35if defined()</tt> statement with a single empty statement can be parsed.
     */
    @Test
    public void testSimpleIf() {
        SourceFile<ISyntaxElement> ast = loadFile("SimpleIf.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 1, elements.size());
        
        CppBlock ifElem = assertIf("A", "A", new Variable("A"), 1, Type.IF, elements.get(0));
        
        assertElement(SingleStatement.class, "A", "A", ifElem.getNestedElement(0));
    }
    
    /**
     * Test that a simple <tt>&#35if defined()</tt> with an <tt>&#35else</tt> statement can be parsed.
     */
    @Test
    public void testSimpleIfElse() {
        SourceFile<ISyntaxElement> ast = loadFile("SimpleIfElse.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 2, elements.size());

        CppBlock ifElem = assertIf("A", "A", new Variable("A"), 1, Type.IF, elements.get(0));
        CppBlock elseElem = assertIf("!A", "!A", not("A"), 1, Type.ELSE, elements.get(1));
        
        assertElement(SingleStatement.class, "A", "A", ifElem.getNestedElement(0));
        assertElement(SingleStatement.class, "!A", "!A", elseElem.getNestedElement(0));
    }
    
    /**
     * Test that a simple <tt>&#35if defined()</tt> with an <tt>&#35elif</tt> statement can be parsed.
     */
    @Test
    public void testSimpleIfElif() {
        SourceFile<ISyntaxElement> ast = loadFile("SimpleIfElif.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 2, elements.size());

        CppBlock ifElem = assertIf("A", "A", new Variable("A"), 1, Type.IF, elements.get(0));
        CppBlock elifElem = assertIf("!A && B", "!A && B", and(not("A"), "B"), 1, Type.ELSEIF, elements.get(1));
        
        assertElement(SingleStatement.class, "A", "A", ifElem.getNestedElement(0));
        assertElement(SingleStatement.class, "!A && B", "!A && B", elifElem.getNestedElement(0));
    }
    
    /**
     * Test that a simple <tt>&#35if defined()</tt> with an <tt>&#35elif</tt> and <tt>&#35else</tt> statements
     * can be parsed.
     */
    @Test
    public void testSimpleIfElifElse() {
        SourceFile<ISyntaxElement> ast = loadFile("SimpleIfElifElse.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());

        CppBlock ifElem = assertIf("A", "A", new Variable("A"), 1, Type.IF, elements.get(0));
        CppBlock elifElem = assertIf("!A && B", "!A && B", and(not("A"), "B"), 1, Type.ELSEIF, elements.get(1));
        CppBlock elseElem = assertIf("!A && !B", "!A && !B", and(not("A"), not("B")), 1, Type.ELSE, elements.get(2));
        
        assertElement(SingleStatement.class, "A", "A", ifElem.getNestedElement(0));
        assertElement(SingleStatement.class, "!A && B", "!A && B", elifElem.getNestedElement(0));
        assertElement(SingleStatement.class, "!A && !B", "!A && !B", elseElem.getNestedElement(0));
    }
    
    /**
     * Test translation of nested <tt>&#35if defined()</tt> statements.
     */
    @Test
    public void testNestedIf() {
        SourceFile<ISyntaxElement> ast = loadFile("NestedIf.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());
        
        assertElement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock outerIf = assertIf("A", "A", new Variable("A"), 3, Type.IF, elements.get(1));
        assertElement(SingleStatement.class, "1", "1", elements.get(2));
        
        
        assertElement(SingleStatement.class, "A", "A", outerIf.getNestedElement(0));
        CppBlock innerIf = assertIf("B", "A && B", new Variable("B"), 1, Type.IF, outerIf.getNestedElement(1));
        assertElement(SingleStatement.class, "A", "A", outerIf.getNestedElement(2));
        
        assertElement(SingleStatement.class, "B", "A && B", innerIf.getNestedElement(0));
    }
    
    /**
     * Test that a <tt>&#35if defined()</tt> statement with a compound expression can be parsed.
     */
    @Test
    public void testCompoundIf() {
        SourceFile<ISyntaxElement> ast = loadFile("CompoundIf.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());
        
        assertElement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock ifElem = assertIf("A && B", "A && B", and("A", "B"), 1, Type.IF, elements.get(1));
        assertElement(SingleStatement.class, "1", "1", elements.get(2));
        
        assertElement(SingleStatement.class, "A && B", "A && B", ifElem.getNestedElement(0));
    }
    
    /**
     * Test that <tt>&#35if defined()</tt> statement with compound expression, <tt>elif</tt> and <tt>else</tt>
     * can be parsed.
     */
    @Test
    public void testCompoundIfElifElse() {
        SourceFile<ISyntaxElement> ast = loadFile("CompoundIfElifElse.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 5, elements.size());
        
        Formula notAAndBAndNotC = and(not(and("A", "B")), not("C"));
        Formula notAAndBAndNotNotC = and(not(and("A", "B")), not(not("C")));
        
        assertElement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock ifElem = assertIf("A && B", "A && B", and("A", "B"), 1, Type.IF, elements.get(1));
        CppBlock elifElem = assertIf("!(A && B) && !C", "!(A && B) && !C", notAAndBAndNotC, 1,
                Type.ELSEIF, elements.get(2));
        CppBlock elseElem = assertIf("!(A && B) && !!C", "!(A && B) && !!C", notAAndBAndNotNotC, 1,
                Type.ELSE, elements.get(3));
        assertElement(SingleStatement.class, "1", "1", elements.get(4));
        
        assertElement(SingleStatement.class, "A && B", "A && B", ifElem.getNestedElement(0));
        assertElement(SingleStatement.class, "!(A && B) && !C", "!(A && B) && !C", elifElem.getNestedElement(0));
        assertElement(SingleStatement.class, "!(A && B) && !!C", "!(A && B) && !!C",
                elseElem.getNestedElement(0));
    }
    
    /**
     * Test that nested <tt>&#35if defined()</tt> statements with compound expressions can be parsed.
     */
    @Test
    public void testNestedCompoundIf() {
        SourceFile<ISyntaxElement> ast = loadFile("NestedCompoundIf.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());
        
        Formula aAndB = and("A", "B");
        Formula cAndNotD = and("C", not("D"));
        
        assertElement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock outerIf = assertIf("A && B", "A && B", aAndB, 3, Type.IF, elements.get(1));
        assertElement(SingleStatement.class, "1", "1", elements.get(2));
        
        assertElement(SingleStatement.class, "A && B", "A && B", outerIf.getNestedElement(0));
        CppBlock innerIf = assertIf("C && !D", "A && B && C && !D", cAndNotD, 1, Type.IF, outerIf.getNestedElement(1));
        assertElement(SingleStatement.class, "A && B", "A && B", outerIf.getNestedElement(2));
        
        assertElement(SingleStatement.class, "C && !D", "A && B && C && !D", innerIf.getNestedElement(0));
    }
    
    /**
     * Tests an if with a complicated structure of !, || and && operators and brackets.
     */
    @Test
    public void testComplicatedIf() {
        SourceFile<ISyntaxElement> ast = loadFile("ComplicatedIf.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());
        
        Formula f = and(not(and(or("A", not(not("B"))), "C")), not("D"));

        assertElement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock ifElem = assertIf("!((A || !!B) && C) && !D", "!((A || !!B) && C) && !D", f, 1,
                Type.IF, elements.get(1));
        assertElement(SingleStatement.class, "1", "1", elements.get(2));
        
        assertElement(SingleStatement.class, "!((A || !!B) && C) && !D", "!((A || !!B) && C) && !D",
                ifElem.getNestedElement(0));
    }
    
    /**
     * Tests an whether an if with a variable outside of a defined() call is handled correctly.
     */
    @Test
    public void testMissingDefined() {
        SourceFile<ISyntaxElement> ast = loadFile("MissingDefined.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());

        assertElement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock ifElem = assertIf("0", "0", False.INSTANCE, 1, Type.IF, elements.get(1));
        assertElement(SingleStatement.class, "1", "1", elements.get(2));
        
        assertElement(SingleStatement.class, "0", "0", ifElem.getNestedElement(0));
    }
    
    /**
     * Tests whether an #if 0 correctly ignores it contents.
     */
    @Test
    public void testIf0() {
        SourceFile<ISyntaxElement> ast = loadFile("If0.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());

        assertElement(SingleStatement.class, "1", "1", elements.get(0));
        assertIf("0", "0", False.INSTANCE, 0, Type.IF, elements.get(1));
        assertElement(SingleStatement.class, "1", "1", elements.get(2));
    }
    
    @Override
    protected SourceFile<ISyntaxElement> loadFile(String file) {
        return super.loadFile("cpp/" + file);
    }
    
}
