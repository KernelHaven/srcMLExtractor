package net.ssehub.kernel_haven.srcml;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.code_model.ast.Code;
import net.ssehub.kernel_haven.code_model.ast.CompoundStatement;
import net.ssehub.kernel_haven.code_model.ast.CppBlock;
import net.ssehub.kernel_haven.code_model.ast.CppStatement;
import net.ssehub.kernel_haven.code_model.ast.File;
import net.ssehub.kernel_haven.code_model.ast.Function;
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
        SourceFile<ISyntaxElement> ast = loadFile("Statement_SingleReturn.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 1, elements.size());
        
        SingleStatement ret = assertElement(SingleStatement.class, "1", "1", elements.get(0));
        assertCode("return true ;", ret.getCode());
    }
    
    
    @Test
    public void globalVariables() {
        SourceFile<ISyntaxElement> ast = loadFile("GlobalVariables.c");
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
    
    @Test
    @SuppressWarnings("null")
    public void testLineNumbers() {
        SourceFile<ISyntaxElement> ast = loadFile("LineNumbers.c");
        assertThat(ast.getTopElementCount(), is(1));
        
        File file = assertElement(File.class, "1", "1", ast.getElement(0));
        assertThat(file.getPath(), is(new java.io.File("c/LineNumbers.c")));
        assertThat(file.getLineStart(), is(1));
        assertThat(file.getLineEnd(), is(9));
        assertThat(file.getNestedElementCount(), is(2));
        
        CppStatement stmt = assertElement(CppStatement.class, "1", "1", file.getNestedElement(0));
        assertThat(stmt.getType(), is(CppStatement.Type.INCLUDE));
        assertThat(((Code) stmt.getExpression()).getText(), is("< stdio.h >"));
        assertThat(stmt.getLineStart(), is(1));
        assertThat(stmt.getLineEnd(), is(1));
        assertThat(stmt.getNestedElementCount(), is(0));
        
        Function func = assertElement(Function.class, "1", "1", file.getNestedElement(1));
        assertThat(func.getName(), is("main"));
        assertThat(((Code) func.getHeader()).getText(), is("int main ( int argc , char * * argv )"));
        assertThat(func.getLineStart(), is(3));
        assertThat(func.getLineEnd(), is(8));
        assertThat(func.getNestedElementCount(), is(1));
        
        CompoundStatement funcBlock = assertElement(CompoundStatement.class, "1", "1", func.getNestedElement(0));
        assertThat(funcBlock.getNestedElementCount(), is(2));
        assertThat(funcBlock.getLineStart(), is(3));
        assertThat(funcBlock.getLineEnd(), is(8));
        
        CppBlock block = assertElement(CppBlock.class, "CONFIG_DEBUG", "CONFIG_DEBUG", funcBlock.getNestedElement(0));
        assertThat(block.getNestedElementCount(), is(1));
        assertThat(block.getLineStart(), is(4));
        assertThat(block.getLineEnd(), is(5));
        
        SingleStatement printStmt = assertElement(SingleStatement.class, "CONFIG_DEBUG", "CONFIG_DEBUG", block.getNestedElement(0));
        assertThat(((Code) printStmt.getCode()).getText(), is("printf ( \"Debugging\" ) ;"));
        assertThat(printStmt.getType(), is(SingleStatement.Type.INSTRUCTION));
        assertThat(printStmt.getNestedElementCount(), is(0));
        assertThat(printStmt.getLineStart(), is(5));
        assertThat(printStmt.getLineEnd(), is(5));
        
        SingleStatement retStmt = assertElement(SingleStatement.class, "1", "1", funcBlock.getNestedElement(1));
        assertThat(((Code) retStmt.getCode()).getText(), is("return 0 ;"));
        assertThat(retStmt.getType(), is(SingleStatement.Type.INSTRUCTION));
        assertThat(retStmt.getNestedElementCount(), is(0));
        assertThat(retStmt.getLineStart(), is(7));
        assertThat(retStmt.getLineEnd(), is(7));
    }
    
    @Override
    protected SourceFile<ISyntaxElement> loadFile(String file) {
        return super.loadFile("c/" + file);
    }

}
