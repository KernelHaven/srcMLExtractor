package net.ssehub.kernel_haven.srcml;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.code_model.ast.Code;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.code_model.ast.SingleStatement;
import net.ssehub.kernel_haven.code_model.ast.TypeDefinition;

/**
 * Tests the translation of C-statements.
 * 
 * @author El-Sharkawy
 * @author Adam
 */
public class CTest extends AbstractSrcMLExtractorTest {
    
    @Test
    public void returnStatement() {
        SourceFile ast = loadFile("Statement_SingleReturn.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 1, elements.size());
        
        SingleStatement ret = assertElement(SingleStatement.class, "1", "1", elements.get(0));
        assertCode("return true ;", ret.getCode());
    }
    
    
    @Test
    public void globalVariables() {
        SourceFile ast = loadFile("GlobalVariables.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        // Variables + comments
        assertEquals("Got unexpected number of elements", 11, elements.size());
        int[] relevantElements;
        String[] expectedNames;
        
        // Test structs
        relevantElements = new int[] {1, 3};
        expectedNames = new String[] {"adr", "x"};
        for (int i = 0; i < relevantElements.length; i++) {
            ISyntaxElement element = elements.get(relevantElements[i]);
            Assert.assertTrue(element instanceof TypeDefinition);
            TypeDefinition structVar = (TypeDefinition) element;
            Assert.assertSame(TypeDefinition.TypeDefType.STRUCT, structVar.getType());
            
            // Approximation of name
            Code declaration = (Code) structVar.getDeclaration();
            String[] code = declaration.getText().split(" ");
            String name = code[code.length - 2];
            Assert.assertEquals(expectedNames[i], name);
        }
        
        // Test single line declarations with initialization
        relevantElements = new int[] {6, 7, 8};
        expectedNames = new String[] {"shared", "overShared", "overSharedToo"};
        for (int i = 0; i < relevantElements.length; i++) {
            ISyntaxElement element = elements.get(relevantElements[i]);
            Assert.assertTrue(element instanceof SingleStatement);
            SingleStatement var = (SingleStatement) element;
            
            // Approximation of name
            Code declaration = (Code) var.getCode();
            String[] code = declaration.getText().split(" ");
            String name = code[code.length - 4];
            Assert.assertEquals(expectedNames[i], name);
        }
        
        // Test single line declarations without initialization
        relevantElements = new int[] {10};
        expectedNames = new String[] {"shared2"};
        for (int i = 0; i < relevantElements.length; i++) {
            ISyntaxElement element = elements.get(relevantElements[i]);
            Assert.assertTrue(element instanceof SingleStatement);
            SingleStatement var = (SingleStatement) element;
            
            // Approximation of name
            Code declaration = (Code) var.getCode();
            String[] code = declaration.getText().split(" ");
            String name = code[code.length - 2];
            Assert.assertEquals(expectedNames[i], name);
        }
    }
    
    @Override
    protected SourceFile loadFile(String file) {
        return super.loadFile("c/" + file);
    }

}
