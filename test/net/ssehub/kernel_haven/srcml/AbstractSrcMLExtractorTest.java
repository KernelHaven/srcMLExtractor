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
    
    /**
     * Creates the temporary resource directory.
     */
    @BeforeClass
    public static void setup() {
        RESOURCE_DIR.mkdir();
    }
    
    /**
     * Clears the temporary resource directory.
     * 
     * @throws IOException If clearing fails.
     */
    @AfterClass
    public static void teardown() throws IOException {
        Util.deleteFolder(RESOURCE_DIR);
    }
    
    /**
     * Returns the top level elements of the translation unit of the parsed {@link SourceFile} as a list.
     * @param file A file, which was translated with {@link SrcMLExtractor}.
     * @return The top level elements.
     */
    protected List<ISyntaxElement> getElements(SourceFile<ISyntaxElement> file) {
        List<ISyntaxElement> result = new ArrayList<>();
        Assert.assertEquals("The SourceFile has more than only one translation unit: "
            + file.getPath().getAbsolutePath(), 1, file.getTopElementCount());
        
        // Extract translation unit
        for (ISyntaxElement element : file) {
            // Extract the relevant, top level elements
            for (int i = 0; i < element.getNestedElementCount(); i++) {
                result.add(element.getNestedElement(i));                
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
    protected SourceFile<ISyntaxElement> loadFile(String file) {
        return loadFile(file, HeaderHandling.IGNORE);
    }
    
    /**
     * Helper method which runs the {@link SrcMLExtractor} on the specified source file.
     *  
     * @param file The source file to parse.
     * @param headerHandling The header handling that should be used.
     * 
     * @return The parsed code model, ready for testing the result.
     */
    protected SourceFile<ISyntaxElement> loadFile(String file, HeaderHandling headerHandling) {
        File srcFile = new File(AllTests.TESTDATA, file);
        Assert.assertTrue("Specified test file does not exist: " + srcFile, srcFile.isFile());
        
        SourceFile<ISyntaxElement> result = null;
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
     * 
     * @param <T> The type of element that is expected.
     * 
     * @return The element to test, cast to the correct type.
     */
    @SuppressWarnings({ "null", "unchecked" })
    protected <T extends ISyntaxElement> @NonNull T assertElement(Class<T> type, String condition,
            String presenceCondition, ISyntaxElement element) {
        
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
    
    /**
     * Tests that the given element is a {@link CppBlock}.
     * 
     * @param condition The expected condition of the element.
     * @param presenceCondition The expected presence condition of the element.
     * @param ifCondition The expected CPP condition of the CPP block. May be <code>null</code>.
     * @param numNested The expected number of nested elements.
     * @param type The expected type of CPP block.
     * @param element The element that should be a CPP block.
     * 
     * @return The element cast to a {@link CppBlock}.
     */
    // CHECKSTYLE:OFF // too many arguments
    protected CppBlock assertIf(String condition, String presenceCondition, Formula ifCondition, int numNested,
            Type type, ISyntaxElement element) {
    // CHECKSTYLE:ON
        
        assertElement(CppBlock.class, condition, presenceCondition, element);
        
        CppBlock cppIf = (CppBlock) element;
        
        assertEquals("Wrong type", type, cppIf.getType());
        
        assertEquals("Wrong number of nested statements", numNested, cppIf.getNestedElementCount());
        
        if (ifCondition == null) {
            assertNull("Wrong CPP-If condition", cppIf.getCondition());
        } else {
            assertEquals("Wrong CPP-If condition", ifCondition, cppIf.getCondition());
        }
        
        return cppIf;
    }
    
    /**
     * Asserts that the given element is a {@link Code}.
     * 
     * @param text The expected literal code text.
     * @param element The element that should be a {@link Code}.
     */
    protected void assertCode(String text, ISyntaxElement element) {
        // Class check
        Assert.assertTrue("Wrong syntax element type: expected " + Code.class.getSimpleName() + "; actual: "
                + element.getClass().getSimpleName(), element.getClass().equals(Code.class));
        
        Code code = (Code) element;
        
        assertEquals("Wrong code String", text, code.getText());
    }

}
