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
    
    @Override
    protected SourceFile loadFile(String file) {
        return super.loadFile("cpp/" + file);
    }

}
