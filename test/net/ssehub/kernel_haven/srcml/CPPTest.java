package net.ssehub.kernel_haven.srcml;

import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.code_model.SyntaxElement;
import net.ssehub.kernel_haven.code_model.SyntaxElementTypes;

/**
 * Tests the translation of C-preprocessor directives by the {@link SrcMLExtractor}.
 * @author El-Sharkawy
 *
 */
public class CPPTest extends AbstractSrcMLExtractorTest {
    
    /**
     * Test that a simple <tt>&#35ifdef</tt> statement with a single empty statement can be parsed.
     */
    @Ignore("Translation of ifdef not implemented yet")
    @Test
    public void testSimpleIfDef() {
        SourceFile ast = loadFile("SimpleIfDef.c");
        List<SyntaxElement> elements = super.getElements(ast);
        
        Assert.assertEquals(1, elements.size());
        assertStatement(SyntaxElementTypes.EMPTY_STATEMENT, "A", elements.get(0));
    }

    /**
     * Test that a simple <tt>&#35if defined()</tt> statement with a single empty statement can be parsed.
     */
    @Test
    public void testSimpleIf() {
        SourceFile ast = loadFile("SimpleIf.c");
        List<SyntaxElement> elements = super.getElements(ast);
        
        Assert.assertEquals(1, elements.size());
        assertStatement(SyntaxElementTypes.EMPTY_STATEMENT, "A", elements.get(0));
    }
    
    /**
     * Test that a simple <tt>&#35if defined()</tt> with an <tt>&#35else</tt> statement can be parsed.
     */
    @Test
    public void testSimpleIfElse() {
        SourceFile ast = loadFile("SimpleIfElse.c");
        List<SyntaxElement> elements = super.getElements(ast);
        
        Assert.assertEquals(2, elements.size());
        assertStatement(SyntaxElementTypes.EMPTY_STATEMENT, "A", elements.get(0));
        assertStatement(SyntaxElementTypes.EMPTY_STATEMENT, "!A", elements.get(1));
    }
    
    /**
     * Test that a simple <tt>&#35if defined()</tt> with an <tt>&#35elif</tt> statement can be parsed.
     */
    @Test
    public void testSimpleIfElif() {
        SourceFile ast = loadFile("SimpleIfElif.c");
        List<SyntaxElement> elements = super.getElements(ast);
        
        Assert.assertEquals(2, elements.size());
        // if defined(A)
        assertStatement(SyntaxElementTypes.EMPTY_STATEMENT, "A", elements.get(0));
        // elif defined(B)
        assertStatement(SyntaxElementTypes.EMPTY_STATEMENT, "!A && B", elements.get(1));
    }
    
    /**
     * Test that a simple <tt>&#35if defined()</tt> with an <tt>&#35elif</tt> and <tt>&#35else</tt> statements
     * can be parsed.
     */
    @Test
    public void testSimpleIfElifElse() {
        SourceFile ast = loadFile("SimpleIfElifElse.c");
        List<SyntaxElement> elements = super.getElements(ast);
        
        Assert.assertEquals(3, elements.size());
        // if defined(A)
        assertStatement(SyntaxElementTypes.EMPTY_STATEMENT, "A", elements.get(0));
        // elif defined(B)
        assertStatement(SyntaxElementTypes.EMPTY_STATEMENT, "!A && B", elements.get(1));
        // else
        assertStatement(SyntaxElementTypes.EMPTY_STATEMENT, "!(!A && B)", elements.get(2));
    }
    
    /**
     * Test translation of nested <tt>&#35if defined()</tt> statements.
     */
    @Test
    public void testNestedIf() {
        SourceFile ast = loadFile("NestedIf.c");
        List<SyntaxElement> elements = super.getElements(ast);
        
        Assert.assertEquals(3, elements.size());
        // if defined(A)
        assertStatement(SyntaxElementTypes.EMPTY_STATEMENT, "A", elements.get(0));
        // nested if defined(B)
        assertStatement(SyntaxElementTypes.EMPTY_STATEMENT, "B", "A && B", elements.get(1));
        // if defined(A)
        assertStatement(SyntaxElementTypes.EMPTY_STATEMENT, "A", elements.get(2));
    }
    
    /**
     * Test that a <tt>&#35if defined()</tt> statement with a compound expression can be parsed.
     */
    @Test
    public void testCompoundIf() {
        SourceFile ast = loadFile("CompoundIf.c");
        List<SyntaxElement> elements = super.getElements(ast);
        
        Assert.assertEquals(1, elements.size());
        assertStatement(SyntaxElementTypes.EMPTY_STATEMENT, "A && B", elements.get(0));
    }
    
    /**
     * Test that <tt>&#35if defined()</tt> statement with compound expression, <tt>elif</tt> and <tt>else</tt>
     * can be parsed.
     */
    @Test
    public void testCompoundIfElifElse() {
        SourceFile ast = loadFile("CompoundIfElifElse.c");
        List<SyntaxElement> elements = super.getElements(ast);
        
        Assert.assertEquals(3, elements.size());
        // if defined(A) && defined(B)
        assertStatement(SyntaxElementTypes.EMPTY_STATEMENT, "A && B", elements.get(0));
        // elif !defined(C)
        assertStatement(SyntaxElementTypes.EMPTY_STATEMENT, "!(A && B) && !C", elements.get(1));
        // else
        assertStatement(SyntaxElementTypes.EMPTY_STATEMENT, "!(!(A && B) && !C)", "!(!(A && B) && !C)",
            elements.get(2));
    }
    
    /**
     * Test that nested <tt>&#35if defined()</tt> statements with compound expressions can be parsed.
     */
    @Test
    public void testNestedCompoundIf() {
        SourceFile ast = loadFile("NestedCompoundIf.c");
        List<SyntaxElement> elements = super.getElements(ast);
        
        Assert.assertEquals(3, elements.size());
        // if defined(A) && defined(B)
        assertStatement(SyntaxElementTypes.EMPTY_STATEMENT, "A && B", "A && B", elements.get(0));
        // nested if defined(C) && !defined(D)
        assertStatement(SyntaxElementTypes.EMPTY_STATEMENT, "C && !D", "A && B && C && !D" , elements.get(1));
        // first endif
        assertStatement(SyntaxElementTypes.EMPTY_STATEMENT, "A && B", "A && B", elements.get(2));
        // second endif
    }
    
    /**
     * Tests an if with a complicated structure of !, || and && operators and brackets.
     */
    @Test
    public void testComplicatedIf() {
        SourceFile ast = loadFile("ComplicatedIf.c");
        List<SyntaxElement> elements = super.getElements(ast);
        
        Assert.assertEquals(1, elements.size());
        
        assertStatement(SyntaxElementTypes.EMPTY_STATEMENT, "!((A || !!B) && C) && !D",
                "!((A || !!B) && C) && !D", elements.get(0));
    }
    
    /**
     * Tests an whether an if with a variable outside of a defined() call is handled correctly.
     */
    @Test
    public void testMissingDefined() {
        SourceFile ast = loadFile("MissingDefined.c");
        List<SyntaxElement> elements = super.getElements(ast);
        
        Assert.assertEquals(1, elements.size());
        
        assertStatement(SyntaxElementTypes.EMPTY_STATEMENT, "0", "0", elements.get(0));
    }
    
    @Override
    protected SourceFile loadFile(String file) {
        return super.loadFile("cpp/" + file);
    }

}
