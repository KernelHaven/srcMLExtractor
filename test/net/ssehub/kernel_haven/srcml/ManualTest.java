package net.ssehub.kernel_haven.srcml;

import org.junit.Test;

import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.srcml.transformation.HeaderHandling;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Logger.Level;

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
        Logger.get().setLevel(Level.DEBUG);
        
//        SourceFile ast = loadFile("FunctionsAndCPP.c");
//        SourceFile ast = loadFile("NestedCppIfs.c");
//        SourceFile ast = loadFile("test.c");
//        SourceFile ast = loadFile("FunctionWithIfdefHeader.c");
        SourceFile<ISyntaxElement> ast = loadFile("test2.c", HeaderHandling.EXPAND_FUNCTION_CONDITION);
//        SourceFile ast = loadFile("../real/Linux4.15/pci_stub.c");
        System.out.println(ast.getElement(0));
    }
    
    @Override
    protected SourceFile<ISyntaxElement> loadFile(String file, HeaderHandling headerHandling) {
        return super.loadFile("manual/" + file, headerHandling);
    }
    
}
