package net.ssehub.kernel_haven.srcml;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.code_model.CodeElement;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.test_utils.TestConfiguration;
import net.ssehub.kernel_haven.util.ExtractorException;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Util;

/**
 * Tests the translation of C-statements.
 * @author El-Sharkawy
 *
 */
public class NewConverterCTest {
    
    private static final File RESOURCE_DIR = new File(AllTests.TESTDATA, "tmpRes");
    
    /**
     * Initializes the logger.
     */
    @BeforeClass
    public static void beforeClass() {
        if (null == Logger.get()) {
            Logger.init();
        }
    }
    
    @Before
    public void enableNewConverter() {
        SrcMLExtractor.USE_NEW_CONVERTER = true;
        
        RESOURCE_DIR.mkdir();
    }
    
    @After
    public void disableNewConverter() throws IOException {
        SrcMLExtractor.USE_NEW_CONVERTER = false;
        
        Util.deleteFolder(RESOURCE_DIR);
    }
    
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
    
    /**
     * Returns the top level elements of the translation unit of the parsed {@link SourceFile} as a list.
     * @param file A file, which was translated with {@link SrcMLExtractor}.
     * @return The top level elements.
     */
    private List<ISyntaxElement> getElements(SourceFile file) {
        List<ISyntaxElement> result = new ArrayList<>();
        Assert.assertEquals("The SourceFile has more than only one translation unit: "
            + file.getPath().getAbsolutePath(), 1, file.getTopElementCount());
        
        // Extract translation unit
        for (CodeElement element : file) {
            if (!(element instanceof ISyntaxElement)) {
                Assert.fail("SourceFile \"" + file.getPath().getAbsolutePath()
                    + "\" contains a non ISyntaxElement: " + element);
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
    private SourceFile loadFile(String file) {
        file = "c/" + file;
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
            result = extractor.runOnFile(new File(file));
        } catch (SetUpException | ExtractorException exc) {
            Assert.fail("Failed to initialize SrcMLExtractor: " + exc.getMessage());
        }
        
        Assert.assertNotNull("Test file wasn't translated: " + file, result);
        return result;
    }

}
