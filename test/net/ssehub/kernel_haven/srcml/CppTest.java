package net.ssehub.kernel_haven.srcml;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.code_model.ast.CppBlock;
import net.ssehub.kernel_haven.code_model.ast.CppBlock.Type;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.code_model.ast.SingleStatement;
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
 */
public class CppTest extends AbstractSrcMLExtractorTest {
    
    /**
     * Test that a simple <tt>&#35ifdef</tt> statement with a single empty statement can be parsed.
     */
    @Test
    public void testSimpleIfDef() {
        SourceFile ast = loadFile("SimpleIfDef.c");
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
        SourceFile ast = loadFile("NestedIfDef.c");
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
        SourceFile ast = loadFile("ElseForIfdef.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 4, elements.size());

        assertElement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock ifElem = assertIf("A", "A", new Variable("A"), 1, Type.IFDEF, elements.get(1));
        CppBlock elseElem = assertIf("!A", "!A", new Negation(new Variable("A")), 1, Type.ELSE, elements.get(2));
        assertElement(SingleStatement.class, "1", "1", elements.get(3));
        
        assertElement(SingleStatement.class, "A", "A", ifElem.getNestedElement(0));
        assertElement(SingleStatement.class, "!A", "!A", elseElem.getNestedElement(0));
    }
    
    /**
     * Test that a simple <tt>&#35ifndef</tt> statement with a single empty statement can be parsed.
     */
    @Test
    public void testSimpleIfNDef() {
        SourceFile ast = loadFile("SimpleIfNDef.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());

        assertElement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock ifElem = assertIf("!A", "!A", new Negation(new Variable("A")), 1, Type.IFNDEF, elements.get(1));
        assertElement(SingleStatement.class, "1", "1", elements.get(2));
        
        assertElement(SingleStatement.class, "!A", "!A", ifElem.getNestedElement(0));
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
        
        assertElement(SingleStatement.class, "A", "A", outerIf.getNestedElement(0));
        CppBlock innerIf = assertIf("!B", "A && !B", new Negation(new Variable("B")), 1, Type.IFNDEF, outerIf.getNestedElement(1));
        assertElement(SingleStatement.class, "A", "A", outerIf.getNestedElement(2));
        
        assertElement(SingleStatement.class, "!B", "A && !B", innerIf.getNestedElement(0));
    }
    
    
    /**
     * Tests whether an #else block for an #ifndef is translated correcltly. 
     */
    @Test
    public void testElseForIfNdef() {
        SourceFile ast = loadFile("ElseForIfNdef.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 4, elements.size());

        assertElement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock ifElem = assertIf("!A", "!A", new Negation(new Variable("A")), 1, Type.IFNDEF, elements.get(1));
        CppBlock elseElem = assertIf("!!A", "!!A", new Negation(new Negation(new Variable("A"))), 1, Type.ELSE, elements.get(2));
        assertElement(SingleStatement.class, "1", "1", elements.get(3));
        
        assertElement(SingleStatement.class, "!A", "!A", ifElem.getNestedElement(0));
        assertElement(SingleStatement.class, "!!A", "!!A", elseElem.getNestedElement(0));
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
        
        assertElement(SingleStatement.class, "A", "A", ifElem.getNestedElement(0));
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
        
        assertElement(SingleStatement.class, "A", "A", ifElem.getNestedElement(0));
        assertElement(SingleStatement.class, "!A", "!A", elseElem.getNestedElement(0));
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
        
        assertElement(SingleStatement.class, "A", "A", ifElem.getNestedElement(0));
        assertElement(SingleStatement.class, "!A && B", "!A && B", elifElem.getNestedElement(0));
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
                Type.ELSE, elements.get(2));
        
        assertElement(SingleStatement.class, "A", "A", ifElem.getNestedElement(0));
        assertElement(SingleStatement.class, "!A && B", "!A && B", elifElem.getNestedElement(0));
        assertElement(SingleStatement.class, "!A && !B", "!A && !B", elseElem.getNestedElement(0));
    }
    
    /**
     * Test translation of nested <tt>&#35if defined()</tt> statements.
     */
    @Test
    public void testNestedIf() {
        SourceFile ast = loadFile("NestedIf.c");
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
        SourceFile ast = loadFile("CompoundIf.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());
        
        assertElement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock ifElem = assertIf("A && B", "A && B", new Conjunction(new Variable("A"), new Variable("B")),
                1, Type.IF, elements.get(1));
        assertElement(SingleStatement.class, "1", "1", elements.get(2));
        
        assertElement(SingleStatement.class, "A && B", "A && B", ifElem.getNestedElement(0));
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
        
        assertElement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock ifElem = assertIf("A && B", "A && B", aAndB, 1, Type.IF, elements.get(1));
        CppBlock elifElem = assertIf("!(A && B) && !C", "!(A && B) && !C", notAAndBAndNotC, 1, Type.ELSEIF, elements.get(2));
        CppBlock elseElem = assertIf("!(A && B) && !!C", "!(A && B) && !!C", notAAndBAndNotNotC, 1, Type.ELSE, elements.get(3));
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
        SourceFile ast = loadFile("NestedCompoundIf.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());
        
        Formula aAndB = new Conjunction(new Variable("A"), new Variable("B"));
        Formula cAndNotD = new Conjunction(new Variable("C"), new Negation(new Variable("D")));
        
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
        SourceFile ast = loadFile("ComplicatedIf.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 3, elements.size());
        
        Formula f = new Conjunction(new Negation(new Conjunction(
                new Disjunction(new Variable("A"), new Negation(new Negation(new Variable("B")))), new Variable("C"))),
                new Negation(new Variable("D")));

        assertElement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock ifElem = assertIf("!((A || !!B) && C) && !D", "!((A || !!B) && C) && !D", f, 1, Type.IF, elements.get(1));
        assertElement(SingleStatement.class, "1", "1", elements.get(2));
        
        assertElement(SingleStatement.class, "!((A || !!B) && C) && !D", "!((A || !!B) && C) && !D",
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

        assertElement(SingleStatement.class, "1", "1", elements.get(0));
        CppBlock ifElem = assertIf("0", "0", False.INSTANCE, 1, Type.IF, elements.get(1));
        assertElement(SingleStatement.class, "1", "1", elements.get(2));
        
        assertElement(SingleStatement.class, "0", "0", ifElem.getNestedElement(0));
    }
    
    @Override
    protected SourceFile loadFile(String file) {
        return super.loadFile("cpp/" + file);
    }
    
}
