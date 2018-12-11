package net.ssehub.kernel_haven.srcml;

import java.io.File;
import java.util.Properties;

import org.junit.Test;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.test_utils.TestConfiguration;
import net.ssehub.kernel_haven.util.ExtractorException;

/**
 * Tests that invalid source files are correctly detected.
 * 
 * @author Adam
 */
public class InvalidFileTest extends AbstractSrcMLExtractorTest {

    /**
     * Tests that running on a C++ file correctly throws an exception.
     * 
     * @throws SetUpException unwanted.
     * @throws ExtractorException expected.
     */
    @Test(expected = ExtractorException.class)
    public void testCplusplusFile() throws SetUpException, ExtractorException {
        String file = "invalidFile.cpp";
        
        Properties props = new Properties();
        props.setProperty("resource_dir", RESOURCE_DIR.getAbsolutePath());
        props.setProperty("source_tree", "testdata/");
        props.setProperty("code.extractor.files", file);
        
        TestConfiguration config = new TestConfiguration(props);
        
        SrcMLExtractor extractor = new SrcMLExtractor();
        extractor.init(config);
        extractor.runOnFile(new File(file));
    }
    
    /**
     * Tests that running on a non-existing file correctly throws an exception.
     * 
     * @throws SetUpException unwanted.
     * @throws ExtractorException expected.
     */
    @Test(expected = ExtractorException.class)
    public void notExistingFile() throws SetUpException, ExtractorException {
        String file = "doesntExist.cpp";
        
        Properties props = new Properties();
        props.setProperty("resource_dir", RESOURCE_DIR.getAbsolutePath());
        props.setProperty("source_tree", "testdata/");
        props.setProperty("code.extractor.files", file);
        
        TestConfiguration config = new TestConfiguration(props);
        
        SrcMLExtractor extractor = new SrcMLExtractor();
        extractor.init(config);
        extractor.runOnFile(new File(file));
    }
    
}
