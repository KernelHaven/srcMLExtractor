package net.ssehub.kernel_haven.srcml;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.code_model.ast.SingleStatement;

/**
 * Tests the translation of C-statements.
 * 
 * @author El-Sharkawy
 * @author Adam
 */
public class CTest extends AbstractSrcMLExtractorTest {
    
    @Test
    @SuppressWarnings("null")
    public void returnStatement() {
        SourceFile ast = loadFile("Statement_SingleReturn.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 1, elements.size());
        
        SingleStatement ret = assertElement(SingleStatement.class, "1", "1", elements.get(0));
        assertCode("return true ;", ret.getCode());
    }
    
    @Override
    protected SourceFile loadFile(String file) {
        return super.loadFile("c/" + file);
    }

}
