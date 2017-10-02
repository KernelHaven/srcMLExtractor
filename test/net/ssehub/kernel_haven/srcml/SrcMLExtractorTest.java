package net.ssehub.kernel_haven.srcml;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.ssehub.kernel_haven.PipelineConfigurator;
import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.TestConfiguration;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.todo.NonBooleanConditionConverter;
import net.ssehub.kernel_haven.util.ExtractorException;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Util;

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
    
    @AfterClass
    public static void afterClass() {
        File resFolder = new File(AllTests.TESTDATA, "net.ssehub.kernel_haven.srcml.SrcMLExtractor");
        try {
            Util.deleteFolder(resFolder);
        } catch (IOException e) {
            Assert.fail("Tmp folder could not be cleaned up: " + e.getMessage());
        }
    }
    
    @Test
    public void test() {
        SourceFile ast = loadFile("FunctionsAndCPP.c");
        System.out.println(ast.iterator().next());
    }
    
    /**
     * Helper method which uses the {@link EaseeExtractor} to load the build model from the
     * specified file.
     * @param file The model file to parse.
     * @param variables Should be <tt>null</tt> or empty if the preparation should be tested without a variability
     *     model or the complete list of relevant variables.
     * @return The parsed build model, ready for testing the result.
     */
    private SourceFile loadFile(String file) {
        SourceFile result = null;
        try {
            Properties props = new Properties();
            props.setProperty("resource_dir", RESOURCE_DIR.getAbsolutePath());
            props.setProperty("source_tree", "testdata/");
            props.setProperty(NonBooleanConditionConverter.PROPERTY_VARIABLE_PATTERN, "\\p{Alpha}+(\\p{Alnum}|_)*");
            props.setProperty("code.extractor.files", file);
            
            TestConfiguration config = new TestConfiguration(props);
            PipelineConfigurator.instance().init(config);
            PipelineConfigurator.instance().instantiateExtractors();
            PipelineConfigurator.instance().createProviders();
            
            SrcMLExtractor extractor = new SrcMLExtractor();
            extractor.init(config.getCodeConfiguration());
            result = extractor.runOnFile(new File(AllTests.TESTDATA, file));
        } catch (SetUpException | ExtractorException exc) {
            Assert.fail("Failed to initialize SrcMLExtractor: " + exc.getMessage());
        }
        
        return result;
    }

}
