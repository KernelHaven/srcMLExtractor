package net.ssehub.kernel_haven.srcml;

import org.junit.Test;

import net.ssehub.kernel_haven.code_model.SourceFile;

/**
 * Helper class for manual testing of the extractor.
 * 
 * @author Adam
 */
public class ManualTest extends AbstractSrcMLExtractorTest {
    
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
//        SourceFile ast = loadFile("../real/Linux4.15/pci_stub.c");
        System.out.println(ast.getElement(0));
    }
    
    @Override
    protected SourceFile loadFile(String file) {
        return super.loadFile("manual/" + file);
    }
    
}
