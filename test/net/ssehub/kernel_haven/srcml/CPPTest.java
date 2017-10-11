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
     * Test that a simple <tt>ifdef</tt> statement with a single empty statement can be parsed.
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
     * Test that a simple <tt>if defined()</tt> statement with a single empty statement can be parsed.
     */
    @Ignore("Statement is not translated correctly")
    @Test
    public void testSimpleIf() {
        SourceFile ast = loadFile("SimpleIf.c");
        List<SyntaxElement> elements = super.getElements(ast);
        
        Assert.assertEquals(1, elements.size());
        assertStatement(SyntaxElementTypes.EMPTY_STATEMENT, "A", elements.get(0));
    }
    
    @Override
    protected SourceFile loadFile(String file) {
        return super.loadFile("cpp/" + file);
    }

}
