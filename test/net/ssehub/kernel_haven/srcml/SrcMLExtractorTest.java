package net.ssehub.kernel_haven.srcml;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.TestConfiguration;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.util.ExtractorException;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Util;

/**
 * Tests the srcML extractor.
 * 
 * @author El-Sharkawy
 */
public class SrcMLExtractorTest {
    
    private static final File RESOURCE_DIR = AllTests.TESTDATA;

    /**
     * Initializes the logger.
     */
    @BeforeClass
    public static void beforeClass() {
        if (null == Logger.get()) {
            Logger.init();
        }
    }
    
    /**
     * Removes the temporary res folder.
     */
    @AfterClass
    public static void afterClass() {
        File resFolder = new File(AllTests.TESTDATA, "net.ssehub.kernel_haven.srcml.SrcMLExtractor");
        try {
            Util.deleteFolder(resFolder);
        } catch (IOException e) {
            Assert.fail("Tmp folder could not be cleaned up: " + e.getMessage());
        }
    }
    
    /**
     * Dummy "test" that simply logs the result to console.
     */
    @Test
    public void test() {
        SourceFile ast = loadFile("FunctionsAndCPP.c");
//        SourceFile ast = loadFile("test.c");
        System.out.println(ast.iterator().next());
    }
    
    /**
     * Helper method which uses the {@link EaseeExtractor} to load the build model from the
     * specified file.
     * @param file The model file to parse.
     * @return The parsed build model, ready for testing the result.
     */
    private SourceFile loadFile(String file) {
        SourceFile result = null;
        try {
            Properties props = new Properties();
            props.setProperty("resource_dir", RESOURCE_DIR.getAbsolutePath());
            props.setProperty("source_tree", "testdata/");
            props.setProperty("code.extractor.files", file);
            
            TestConfiguration config = new TestConfiguration(props);
            
            SrcMLExtractor extractor = new SrcMLExtractor();
            extractor.init(config.getCodeConfiguration());
            result = extractor.runOnFile(new File(AllTests.TESTDATA, file));
        } catch (SetUpException | ExtractorException exc) {
            Assert.fail("Failed to initialize SrcMLExtractor: " + exc.getMessage());
        }
        
        return result;
    }

}
