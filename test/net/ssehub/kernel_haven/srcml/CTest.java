package net.ssehub.kernel_haven.srcml;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;

/**
 * Tests the translation of C-statements.
 * 
 * TODO: no useful tests yet.
 * 
 * @author El-Sharkawy
 *
 */
public class CTest extends AbstractSrcMLExtractorTest {
    
    @Test
    public void test() {
//        SourceFile ast = loadFile("Komplex2.c");
        SourceFile ast = loadFile("../test.c");
//        SourceFile ast = loadFile("../blubb.c");
//        SourceFile ast = loadFile("../NestedCppIfs.c");
//        List<SyntaxElement> elements = getElements(ast);
        System.out.println(ast.iterator().next());
        
        //assertEquals("Got unexpected number of elements", 1, elements.size());
    }
    
    @Test
    public void returnStatement() {
        SourceFile ast = loadFile("Statement_SingleReturn.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 1, elements.size());
//        SyntaxElement statement = elements.get(0);
//        System.out.println(statement);
    }
    
    @Override
    protected SourceFile loadFile(String file) {
        return super.loadFile("c/" + file);
    }

}
