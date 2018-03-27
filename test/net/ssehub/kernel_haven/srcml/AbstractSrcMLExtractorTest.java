package net.ssehub.kernel_haven.srcml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.code_model.ast.Code;
import net.ssehub.kernel_haven.code_model.ast.CppBlock;
import net.ssehub.kernel_haven.code_model.ast.CppBlock.Type;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.srcml.transformation.HeaderHandling;
import net.ssehub.kernel_haven.test_utils.TestConfiguration;
import net.ssehub.kernel_haven.util.ExtractorException;
import net.ssehub.kernel_haven.util.Util;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Abstract tests to test the {@link SrcMLExtractor}.
 * 
 * @author El-Sharkawy
 */
public class AbstractSrcMLExtractorTest {
    
    protected static final @NonNull File RESOURCE_DIR = new File(AllTests.TESTDATA, "tmpRes");
    
    @BeforeClass
    public static void setup() {
        RESOURCE_DIR.mkdir();
    }
    
    @AfterClass
    public static void teardown() throws IOException {
        Util.deleteFolder(RESOURCE_DIR);
    }
    
    /**
     * Returns the top level elements of the translation unit of the parsed {@link SourceFile} as a list.
     * @param file A file, which was translated with {@link SrcMLExtractor}.
     * @return The top level elements.
     */
    protected List<ISyntaxElement> getElements(SourceFile file) {
        List<ISyntaxElement> result = new ArrayList<>();
        Assert.assertEquals("The SourceFile has more than only one translation unit: "
            + file.getPath().getAbsolutePath(), 1, file.getTopElementCount());
        
        // Extract translation unit
        for (CodeElement element : file) {
            if (!(element instanceof ISyntaxElement)) {
                Assert.fail("SourceFile \"" + file.getPath().getAbsolutePath() + "\" contains a non SrcMlSyntaxElement: "
                    + element);
            }
            
            // Extract the relevant, top level elements
            ISyntaxElement translationUnit = (ISyntaxElement) element;
            for (int i = 0; i < translationUnit.getNestedElementCount(); i++) {
                result.add(translationUnit.getNestedElement(i));                
            }
        }
        
        return result;
    }
    
    /**
     * Helper method which runs the {@link SrcMLExtractor} on the specified source file.
     *  
     * @param file The source file to parse.
     * @return The parsed code model, ready for testing the result.
     */
    protected SourceFile loadFile(String file) {
        return loadFile(file, HeaderHandling.IGNORE);
    }
    
    /**
     * Helper method which runs the {@link SrcMLExtractor} on the specified source file.
     *  
     * @param file The source file to parse.
     * @return The parsed code model, ready for testing the result.
     */
    protected SourceFile loadFile(String file, HeaderHandling headerHandling) {
        File srcFile = new File(AllTests.TESTDATA, file);
        Assert.assertTrue("Specified test file does not exist: " + srcFile, srcFile.isFile());
        
        SourceFile result = null;
        try {
            Properties props = new Properties();
            props.setProperty("resource_dir", RESOURCE_DIR.getAbsolutePath());
            props.setProperty("source_tree", "testdata/");
            props.setProperty("code.extractor.files", file);
            props.setProperty("code.extractor.header_handling", headerHandling.name());
            
            TestConfiguration config = new TestConfiguration(props);
            
            SrcMLExtractor extractor = new SrcMLExtractor();
            extractor.init(config);
            result = extractor.runOnFile(new File(file));
        } catch (SetUpException exc) {
            Assert.fail("Failed to initialize SrcMLExtractor: " + exc.getMessage());
        } catch (ExtractorException exc) {
            exc.printStackTrace(System.out);
            Assert.fail("Extractor failed to run on file " + file + ": " + exc.toString());
        }
        
        Assert.assertNotNull("Test file wasn't translated: " + file, result);
        return result;
    }
    
    /**
     * Tests the statement.
     * @param type The expected syntax type
     * @param condition The expected condition in c-style.
     * @param presenceCondition The expected presence/compound in c-style. This contains also all surrounding
     *     conditions.
     * @param element The element to test.
     */
    @SuppressWarnings({ "null", "unchecked" })
    protected <T extends ISyntaxElement> @NonNull T assertElement(Class<T> type, String condition,
            String presenceCondition, CodeElement element) {
        
        // Class check
        Assert.assertTrue("Wrong syntax element type: expected " + type.getSimpleName() + "; actual: "
                + element.getClass().getSimpleName(), element.getClass().equals(type));
        
        // Check of current condition
        if (null != condition) {
            Assert.assertEquals("Wrong condition", condition, element.getCondition().toString());
        } else {
            Assert.assertNull("Element has a condition, but wasn't expected.", element.getCondition());
        }
        
        // Check of presence condition
        if (null != presenceCondition) {
            Assert.assertEquals("Wrong presence/compound condition", presenceCondition,
                element.getPresenceCondition().toString());
        }
        
        return (T) element;
    }
    
    protected CppBlock assertIf(String condition, String presenceCondition, Formula ifCondition, int numNested,
            Type type, CodeElement element) {
        
        assertElement(CppBlock.class, condition, presenceCondition, element);
        
        CppBlock cppIf = (CppBlock) element;
        
        assertEquals("Wrong type", type, cppIf.getType());
        
        assertEquals("Wrong number of nested statements", numNested, cppIf.getNestedElementCount());
        
        if (ifCondition == null) {
            assertNull("Wrong CPP-If condition",cppIf.getCondition());
        } else {
            assertEquals("Wrong CPP-If condition", ifCondition, cppIf.getCondition());
        }
        
        return cppIf;
    }
    
    protected void assertCode(String text, CodeElement element) {
        // Class check
        Assert.assertTrue("Wrong syntax element type: expected " + Code.class.getSimpleName() + "; actual: "
                + element.getClass().getSimpleName(), element.getClass().equals(Code.class));
        
        Code code = (Code) element;
        
        assertEquals("Wrong code String", text, code.getText());
    }

}
