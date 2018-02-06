package net.ssehub.kernel_haven.srcml;

import org.junit.Test;

import net.ssehub.kernel_haven.code_model.SourceFile;

/**
 * Tests the srcML extractor.
 * 
 * @author El-Sharkawy
 */
public class SrcMLExtractorTest extends AbstractSrcMLExtractorTest {
    
    
    /**
     * Dummy "test" that simply logs the result to console.
     */
    @Test
    public void test() {
//        SourceFile ast = loadFile("FunctionsAndCPP.c");
//        SourceFile ast = loadFile("NestedCppIfs.c");
//        SourceFile ast = loadFile("test.c");
//        SourceFile ast = loadFile("FunctionWithIfdefHeader.c");
        SourceFile ast = loadFile("test2.c");
        System.out.println(ast.iterator().next());
    }
    
}
