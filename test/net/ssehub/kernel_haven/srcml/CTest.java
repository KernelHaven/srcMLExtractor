/*
 * Copyright 2017-2019 University of Hildesheim, Software Systems Engineering
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.ssehub.kernel_haven.srcml;

import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.not;
import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kernel_haven.code_model.JsonCodeModelCache;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.code_model.ast.BranchStatement;
import net.ssehub.kernel_haven.code_model.ast.Code;
import net.ssehub.kernel_haven.code_model.ast.CodeList;
import net.ssehub.kernel_haven.code_model.ast.CompoundStatement;
import net.ssehub.kernel_haven.code_model.ast.CppBlock;
import net.ssehub.kernel_haven.code_model.ast.CppBlock.Type;
import net.ssehub.kernel_haven.code_model.ast.CppStatement;
import net.ssehub.kernel_haven.code_model.ast.ErrorElement;
import net.ssehub.kernel_haven.code_model.ast.File;
import net.ssehub.kernel_haven.code_model.ast.Function;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.code_model.ast.ReferenceElement;
import net.ssehub.kernel_haven.code_model.ast.SingleStatement;
import net.ssehub.kernel_haven.code_model.ast.TypeDefinition;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.logic.Variable;

/**
 * Tests the translation of C-statements.
 * 
 * @author El-Sharkawy
 * @author Adam
 */
public class CTest extends AbstractSrcMLExtractorTest {
    
    /**
     * Tests parsing of a file that contains only a single return statement.
     */
    @Test
    public void returnStatement() {
        SourceFile<ISyntaxElement> ast = loadFile("Statement_SingleReturn.c");
        List<ISyntaxElement> elements = getElements(ast);
        
        assertEquals("Got unexpected number of elements", 1, elements.size());
        
        SingleStatement ret = assertElement(SingleStatement.class, "1", "1", elements.get(0));
        assertCode("return true ;", ret.getCode());
    }
    
    /**
     * Tests that the names of global variables can be found in the parsed AST.
     */
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
    
    /**
     * Checks the AST created for testdata/c/LineNUmbers.c. Especially checks that that the line numbers of the
     * elements are set properly.
     */
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
        assertThat(block.getLineEnd(), is(6));
        
        SingleStatement printStmt = assertElement(SingleStatement.class, "CONFIG_DEBUG", "CONFIG_DEBUG",
                block.getNestedElement(0));
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
    
    /**
     * Tests a C-if where the header is surroundend by an ifdef, but not the body.
     */
    @Test
    public void testIfWithIfdef() {
        SourceFile<ISyntaxElement> ast = loadFile("IfWithIfdef.c");
        assertThat(ast.getTopElementCount(), is(1));
        
        File file = assertElement(File.class, "1", "1", ast.getElement(0));
        assertThat(file.getPath(), is(new java.io.File("c/IfWithIfdef.c")));
        assertThat(file.getLineStart(), is(1));
        assertThat(file.getLineEnd(), is(7));
        assertThat(file.getNestedElementCount(), is(2));
        
        CppBlock ifdef = assertIf("A", "A", new Variable("A"), 1, Type.IFDEF, file.getNestedElement(0));
        assertThat(ifdef.getLineStart(), is(1));
        assertThat(ifdef.getLineEnd(), is(3));
        assertThat(ifdef.getNestedElementCount(), is(1));
        
        CompoundStatement block = assertElement(CompoundStatement.class, "1", "1", file.getNestedElement(1));
        assertThat(block.getLineStart(), is(4));
        assertThat(block.getLineEnd(), is(6));
        assertThat(block.getNestedElementCount(), is(1));
        
        SingleStatement stmt = assertElement(SingleStatement.class, "1", "1", block.getNestedElement(0));
        assertThat(stmt.getLineStart(), is(5));
        assertThat(stmt.getLineEnd(), is(5));
        assertThat(stmt.getNestedElementCount(), is(0));
        assertCode(";", stmt.getCode());
        
        BranchStatement ifStmt = assertElement(BranchStatement.class, "A", "A", ifdef.getNestedElement(0));
        assertThat(ifStmt.getLineStart(), is(2));
        assertThat(ifStmt.getLineEnd(), is(6));
        assertThat(ifStmt.getNestedElementCount(), is(1));
        assertCode("if ( abc == 1 )", ifStmt.getIfCondition());
        
        ReferenceElement ref = assertElement(ReferenceElement.class, "A", "A", ifStmt.getNestedElement(0));
        assertThat(ref.getLineStart(), is(4));
        assertThat(ref.getLineEnd(), is(6));
        assertThat(ref.getNestedElementCount(), is(0));
        
        assertSame(block, ref.getReferenced());
    }
    
    /**
     * Tests that the AST from "testdata/c/Komplex2.c" is parsed correctly. This test does a full equality check on
     * the AST.
     * 
     * @throws FormatException unwanted. 
     * @throws IOException unwanted.
     */
    @SuppressWarnings("null")
    @Test
    public void testComplexAst() throws IOException, FormatException {
        SourceFile<ISyntaxElement> ast = loadFile("Komplex2.c");
        
        JsonCodeModelCache cache = new JsonCodeModelCache(new java.io.File("testdata/c"));
//        cache.write(ast);
        
        // load manually verified, cached version of the AST; this is known to be correct
        SourceFile<ISyntaxElement> expected = cache.read(new java.io.File("Komplex2.c")).castTo(ISyntaxElement.class);
        
        assertThat(ast, is(expected));
    }
    
    /**
     * Tests that {@link ISyntaxElement#containsErrorElement()} is correclty set.
     */
    @Test
    public void testContainsErrorElement() {
        SourceFile<ISyntaxElement> ast = loadFile("ErrorElement.c");
        assertThat(ast.getTopElementCount(), is(1));
        
        File file = assertElement(File.class, "1", "1", ast.getElement(0));
        assertThat(file.getPath(), is(new java.io.File("c/ErrorElement.c")));
        assertThat(file.getLineStart(), is(1));
        assertThat(file.getLineEnd(), is(7));
        assertThat(file.containsErrorElement(), is(true));
        assertThat(file.getNestedElementCount(), is(3));
        
        SingleStatement stmt1 = assertElement(SingleStatement.class, "1", "1", file.getNestedElement(0));
        assertThat(stmt1.getLineStart(), is(1));
        assertThat(stmt1.getLineEnd(), is(1));
        assertThat(stmt1.containsErrorElement(), is(false));
        
        CompoundStatement block = assertElement(CompoundStatement.class, "1", "1", file.getNestedElement(1));
        assertThat(block.getLineStart(), is(2));
        assertThat(block.getLineEnd(), is(5));
        assertThat(block.containsErrorElement(), is(true));
        assertThat(block.getNestedElementCount(), is(2));
        
        SingleStatement stmt3 = assertElement(SingleStatement.class, "1", "1", file.getNestedElement(2));
        assertThat(stmt3.getLineStart(), is(6));
        assertThat(stmt3.getLineEnd(), is(6));
        assertThat(stmt3.containsErrorElement(), is(false));
        
        
        ErrorElement error = assertElement(ErrorElement.class, "1", "1", block.getNestedElement(0));
        assertThat(error.getLineStart(), is(3));
        assertThat(error.getLineEnd(), is(3));
        assertThat(error.containsErrorElement(), is(true));
        
        SingleStatement stmt2 = assertElement(SingleStatement.class, "1", "1", block.getNestedElement(1));
        assertThat(stmt2.getLineStart(), is(4));
        assertThat(stmt2.getLineEnd(), is(4));
        assertThat(stmt2.containsErrorElement(), is(false));
    }
    
    /**
     * Tests parsing a file where the ifdef is the first element of a {@link SingleStatement}.
     */
    @Test
    public void testDeclStartingWithIfdef() {
        SourceFile<ISyntaxElement> ast = loadFile("DeclStartingWithIfdef.c");
        assertThat(ast.getTopElementCount(), is(1));
        
        File file = assertElement(File.class, "1", "1", ast.getElement(0));
        assertThat(file.getPath(), is(new java.io.File("c/DeclStartingWithIfdef.c")));
        assertThat(file.getLineStart(), is(1));
        assertThat(file.getLineEnd(), is(7));
        assertThat(file.containsErrorElement(), is(false));
        assertThat(file.getNestedElementCount(), is(1));
        
        SingleStatement stmt = assertElement(SingleStatement.class, "1", "1", file.getNestedElement(0));
        assertThat(stmt.getLineStart(), is(2));
        assertThat(stmt.getLineEnd(), is(6));
        assertThat(stmt.getNestedElementCount(), is(0));
        
        CodeList code = (CodeList) stmt.getCode();
        assertThat(code.getNestedElementCount(), is(3));

        CppBlock ifdef = assertIf("A", "A", new Variable("A"), 1, Type.IFDEF, code.getNestedElement(0));
        CppBlock elsedef = assertIf("!A", "!A", not("A"), 1, Type.ELSE, code.getNestedElement(1));
        assertCode("a ;", code.getNestedElement(2));
        
        assertCode("int", ifdef.getNestedElement(0));
        assertCode("float", elsedef.getNestedElement(0));
        
        assertThat(ifdef.getNestedElementCount(), is(1));
        assertThat(elsedef.getNestedElementCount(), is(1));
        
        assertThat(ifdef.getLineStart(), is(1));
        assertThat(ifdef.getLineEnd(), is(2));
        assertThat(elsedef.getLineStart(), is(3));
        assertThat(elsedef.getLineEnd(), is(5));
    }
    
    /**
     * Tests a C-if where the condition contains an ifdef.
     */
    @Test
    public void testIfConditionWithIfdef() {
        SourceFile<ISyntaxElement> ast = loadFile("IfConditionWithIfdef.c");
        assertThat(ast.getTopElementCount(), is(1));
        
        File file = assertElement(File.class, "1", "1", ast.getElement(0));
        assertThat(file.getPath(), is(new java.io.File("c/IfConditionWithIfdef.c")));
        assertThat(file.getLineStart(), is(1));
        assertThat(file.getLineEnd(), is(8));
        assertThat(file.getNestedElementCount(), is(1));
        
        BranchStatement ifStmt = assertElement(BranchStatement.class, "1", "1", file.getNestedElement(0));
        assertThat(ifStmt.getLineStart(), is(1));
        assertThat(ifStmt.getLineEnd(), is(7));
        assertThat(ifStmt.getNestedElementCount(), is(1));
        
        CodeList outerList = (CodeList) notNull(ifStmt.getIfCondition());
        assertThat(outerList.getNestedElementCount(), is(3));
        assertCode("if ( a", outerList.getNestedElement(0));
        assertCode(")", outerList.getNestedElement(2));

        CppBlock ifdef = assertIf("A", "A", new Variable("A"), 1, Type.IFDEF, outerList.getNestedElement(1));
        assertThat(ifdef.getLineStart(), is(2));
        assertThat(ifdef.getLineEnd(), is(4));
        assertThat(ifdef.getNestedElementCount(), is(1));
        assertCode("== 0", ifdef.getNestedElement(0));
        
        CompoundStatement block = assertElement(CompoundStatement.class, "1", "1", ifStmt.getNestedElement(0));
        assertThat(block.getLineStart(), is(5));
        assertThat(block.getLineEnd(), is(7));
        assertThat(block.getNestedElementCount(), is(1));
        
        SingleStatement stmt = assertElement(SingleStatement.class, "1", "1", block.getNestedElement(0));
        assertThat(stmt.getLineStart(), is(6));
        assertThat(stmt.getLineEnd(), is(6));
        assertThat(stmt.getNestedElementCount(), is(0));
        assertCode(";", stmt.getCode());
    }
    
    @Override
    protected SourceFile<ISyntaxElement> loadFile(String file) {
        return super.loadFile("c/" + file);
    }

}
