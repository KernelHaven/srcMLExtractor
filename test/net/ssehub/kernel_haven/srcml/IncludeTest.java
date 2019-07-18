/*
 * Copyright 2019 University of Hildesheim, Software Systems Engineering
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.code_model.ast.CompoundStatement;
import net.ssehub.kernel_haven.code_model.ast.CppBlock;
import net.ssehub.kernel_haven.code_model.ast.CppStatement;
import net.ssehub.kernel_haven.code_model.ast.CppStatement.Type;
import net.ssehub.kernel_haven.code_model.ast.ErrorElement;
import net.ssehub.kernel_haven.code_model.ast.File;
import net.ssehub.kernel_haven.code_model.ast.Function;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.code_model.ast.SingleStatement;
import net.ssehub.kernel_haven.util.logic.Variable;

/**
 * Tests the header inclusion mechanism.
 *
 * @author Adam
 */
public class IncludeTest extends AbstractSrcMLExtractorTest {

    /**
     * Tests that no header is included if the setting isn't specified.
     */
    @Test
    public void testDisabledHeaderInclusion() {
        SourceFile<ISyntaxElement> ast = loadFile("simple.c", HeaderHandling.IGNORE);
        
        List<ISyntaxElement> elements = getElements(ast);
        
        CppStatement include = assertElement(CppStatement.class, "1", "1", elements.get(0));
        SingleStatement stmt = assertElement(SingleStatement.class, "1", "1", elements.get(1));
        assertThat(elements.size(), is(2));
        
        assertThat(include.getType(), is(Type.INCLUDE));
        assertCode("\"simple.h\"", include.getExpression());
        
        assertCode("int b ;", stmt.getCode());
    }
    
    /**
     * Tests that a simple header is included correctly.
     */
    @Test
    public void testSimpleHeaderInclusion() {
        SourceFile<ISyntaxElement> ast = loadFile("simple.c", HeaderHandling.INCLUDE);
        List<ISyntaxElement> elements = getElements(ast);
        
        File file = assertElement(File.class, "1", "1", elements.get(0));
        SingleStatement stmt2 = assertElement(SingleStatement.class, "1", "1", elements.get(1));
        assertThat(elements.size(), is(2));
        
        SingleStatement stmt1 = assertElement(SingleStatement.class, "1", "1", file.getNestedElement(0));
        assertThat(file.getNestedElementCount(), is(1));
        
        assertCode("int a ;", stmt1.getCode());
        assertCode("int b ;", stmt2.getCode());
        
        // TODO: should this be headers/simple.h instead?
        assertThat(stmt1.getSourceFile(), is(new java.io.File("testdata/headers/simple.h")));
        assertThat(stmt1.getLineStart(), is(4));
        
        assertThat(stmt2.getSourceFile(), is(new java.io.File("headers/simple.c")));
        assertThat(stmt2.getLineStart(), is(3));
    }
    
    /**
     * Tests the expansion of function presence conditions.
     */
    @Test
    public void testConditionExpansion() {
        SourceFile<ISyntaxElement> ast = loadFile("function.c", HeaderHandling.EXPAND_FUNCTION_CONDITION);
        List<ISyntaxElement> elements = getElements(ast);
        
        assertElement(File.class, "1", "1", elements.get(0));
        CppBlock ifdef = assertIf("C", "C", new Variable("C"), 1, CppBlock.Type.IFDEF, elements.get(1));
        assertThat(elements.size(), is(2));
        
        Function func = assertElement(Function.class, "C && (A || B)", "C && (A || B)", ifdef.getNestedElement(0));
        assertThat(func.getName(), is("func1"));
        assertThat(func.getNestedElementCount(), is(1));
        
        // check that PC for all nested elements was updated.
        
        CompoundStatement body = assertElement(CompoundStatement.class, "C", "C && (A || B)", func.getNestedElement(0));
        
        assertElement(SingleStatement.class, "C", "C && (A || B)", body.getNestedElement(0));
    }
    
    /**
     * Tests the case where a header file doesn't exist.
     */
    @Test
    public void testHeaderNotFound() {
        SourceFile<ISyntaxElement> ast = loadFile("missing_header.c", HeaderHandling.INCLUDE);
        List<ISyntaxElement> elements = getElements(ast);
        
        CppStatement incl1 = assertElement(CppStatement.class, "1", "1", elements.get(0));
        CppStatement incl2 = assertElement(CppStatement.class, "1", "1", elements.get(1));
        assertThat(elements.size(), is(2));
        
        assertCode("\"missing_header.h\"", incl1.getExpression());
        assertCode("< other_missing_header.h >", incl2.getExpression());
    }

    /**
     * Tests that including an unparseable header is handled correctly.
     */
    @Test
    public void testUnparseableHeader() {
        SourceFile<ISyntaxElement> ast = loadFile("unparseable_base.c", HeaderHandling.INCLUDE);
        List<ISyntaxElement> elements = getElements(ast);
        
        SingleStatement stmt1 = assertElement(SingleStatement.class, "1", "1", elements.get(0));
        ErrorElement error = assertElement(ErrorElement.class, "1", "1", elements.get(1));
        SingleStatement stmt2 = assertElement(SingleStatement.class, "1", "1", elements.get(2));
        
        assertCode(";", stmt1.getCode());
        assertCode(";", stmt2.getCode());
        
        assertThat(error.getErrorText(), is("Can't parse header testdata\\headers\\unparseable_included.java: "
                + "Unsupported language \"Java\""));
        assertThat(error.getNestedElementCount(), is(0));
    }
    
    @Override
    protected SourceFile<ISyntaxElement> loadFile(String file, HeaderHandling headerHandling) {
        return super.loadFile("headers/" + file, headerHandling);
    }
    
}
