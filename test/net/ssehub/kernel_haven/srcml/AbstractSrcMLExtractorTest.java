package net.ssehub.kernel_haven.srcml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.code_model.CodeElement;
import net.ssehub.kernel_haven.code_model.ISyntaxElementType;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.code_model.SyntaxElement;
import net.ssehub.kernel_haven.test_utils.TestConfiguration;
import net.ssehub.kernel_haven.util.ExtractorException;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Util;

/**
 * Abstract tests to test the {@link SrcMLExtractor}.
 * 
 * @author El-Sharkawy
 */
public class AbstractSrcMLExtractorTest {
    
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
     * Returns the top level elements of the translation unit of the parsed {@link SourceFile} as a list.
     * @param file A file, which was translated with {@link SrcMLExtractor}.
     * @return The top level elements.
     */
    List<SyntaxElement> getElements(SourceFile file) {
        List<SyntaxElement> result = new ArrayList<>();
        Assert.assertEquals("The SourceFile has more than only one translation unit: "
            + file.getPath().getAbsolutePath(), 1, file.getTopElementCount());
        
        // Extract translation unit
        for (CodeElement element : file) {
            if (!(element instanceof SyntaxElement)) {
                Assert.fail("SourceFile \"" + file.getPath().getAbsolutePath() + "\" contains a non SyntaxElement: "
                    + element);
            }
            
            // Extract the relevant, top level elements
            SyntaxElement translationUnit = (SyntaxElement) element;
            for (int i = 0; i < translationUnit.getNestedElementCount(); i++) {
                result.add(translationUnit.getNestedElement(i));                
            }
        }
        
        return result;
    }
    
    /**
     * Tests the statement.
     * @param type The expected syntax type
     * @param condition The expected condition in c-style.
     * @param element The element to test.
     */
    protected void assertStatement(ISyntaxElementType type, String condition, SyntaxElement element) {
        Assert.assertSame("Wrong syntax element", type, element.getType());
        if (null != condition) {
            Assert.assertEquals("Wrong condition", condition, element.getCondition().toString());
        } else {
            Assert.assertNull("Element has a condition, but wasn't expected.", element.getCondition());
        }
    }
    
    /**
     * Helper method which uses the {@link EaseeExtractor} to load the build model from the
     * specified file.
     * @param file The model file to parse.
     * @return The parsed build model, ready for testing the result.
     */
    protected SourceFile loadFile(String file) {
        File srcFile = new File(AllTests.TESTDATA, file);
        Assert.assertTrue("Specified test file does not exist: " + file, srcFile.isFile());
        
        SourceFile result = null;
        try {
            Properties props = new Properties();
            props.setProperty("resource_dir", RESOURCE_DIR.getAbsolutePath());
            props.setProperty("source_tree", "testdata/");
            props.setProperty("code.extractor.files", file);
            
            TestConfiguration config = new TestConfiguration(props);
            
            SrcMLExtractor extractor = new SrcMLExtractor();
            extractor.init(config);
            result = extractor.runOnFile(srcFile);
        } catch (SetUpException | ExtractorException exc) {
            Assert.fail("Failed to initialize SrcMLExtractor: " + exc.getMessage());
        }
        
        Assert.assertNotNull("Test file wasn't translated: " + file, result);
        return result;
    }

}
