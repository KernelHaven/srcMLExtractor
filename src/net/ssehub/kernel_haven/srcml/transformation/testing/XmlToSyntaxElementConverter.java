package net.ssehub.kernel_haven.srcml.transformation.testing;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import net.ssehub.kernel_haven.code_model.CodeElement;
import net.ssehub.kernel_haven.code_model.ast.SyntaxElement;
import net.ssehub.kernel_haven.srcml.model.SrcMlSyntaxElement;
import net.ssehub.kernel_haven.srcml.transformation.testing.rules.Preprocessing;
import net.ssehub.kernel_haven.srcml.xml.AbstractAstConverter;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Translates the XML output of <a href="http://www.srcml.org">srcML</a> into {@link SrcMlSyntaxElement}. This is done
 * in 3 steps:
 * <ol>
 *     <li>The XML output is parsed by this class to create {@link ITranslationUnit}s.</li>
 *     <li>The {@link ITranslationUnit}s are refined by the {@link Preprocessing} to be closer to the target AST structure,
 *     to simplify the final parsing.</li>
 *     <li>The {@link ITranslationUnit}s are parsed into {@link SyntaxElement} by the
 *     {@link TranslationUnitToAstConverter}</li>
 * </ol>
 * @author El-Sharkawy
 *
 */
public class XmlToSyntaxElementConverter extends AbstractAstConverter {
    
    /**
     * White list of supported XML tags, which will be processed by this converter. These are the top level elements of
     * the <a href="http://www.srcml.org/doc/c_srcML.html">srcML C and CPP grammar</a>.
     */
    private static final Set<String> SUPPORTED_ELEMENTS;
    
    static {
        Set<String> tmpSet = new HashSet<>();
        // C-Preprocessor
        tmpSet.add("cpp:include");
        tmpSet.add("cpp:if");
        tmpSet.add("cpp:ifdef");
        tmpSet.add("cpp:ifndef");
        tmpSet.add("cpp:else");
        tmpSet.add("cpp:elif");
        tmpSet.add("cpp:endif");
        tmpSet.add("cpp:define");
        tmpSet.add("cpp:undef");
        tmpSet.add("cpp:pragma");
        tmpSet.add("cpp:error");
        tmpSet.add("cpp:warning");
        tmpSet.add("cpp:line");
        tmpSet.add("cpp:empty");
        
        // C
        tmpSet.add("unit");
        // Structs & other TypeDefs
        tmpSet.add("struct_decl");
        tmpSet.add("struct");
        tmpSet.add("union_decl");
        tmpSet.add("union");
        tmpSet.add("enum");
        tmpSet.add("typedef");
        // Functions
        tmpSet.add("function_decl");
        tmpSet.add("function");
        // Control structures
        tmpSet.add("if");
        tmpSet.add("else");
        tmpSet.add("elseif");
        tmpSet.add("while");
        tmpSet.add("for");
        tmpSet.add("do");
        tmpSet.add("switch");
        tmpSet.add("case");
        tmpSet.add("default");
        tmpSet.add("break");
        tmpSet.add("continue");
        tmpSet.add("goto");
        tmpSet.add("label");
        // Statements
        tmpSet.add("block");
        tmpSet.add("decl_stmt");
        tmpSet.add("expr_stmt");
        tmpSet.add("empty_stmt");
        tmpSet.add("return");
        // Comments
        tmpSet.add("comment");
        
        SUPPORTED_ELEMENTS = Collections.unmodifiableSet(tmpSet);
    }
    
    private Deque<TranslationUnit> elements = new ArrayDeque<TranslationUnit>();
    
    /**
     * Whether we are currently inside the &lt;name&gt; inside a &lt;function&gt;.
     */
    private boolean inNameInsideFunction = false;
    
    /**
     * The actual nesting of qNames of the XML structure.
     */
    private Deque<String> xmlNesting = new ArrayDeque<>();
    
    /**
     * Sole constructor for this classes.
     * @param path The relative path to the source file in the source tree. Must not be <code>null</code>.
     */
    public XmlToSyntaxElementConverter(@NonNull File path) {
        super(path);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (SUPPORTED_ELEMENTS.contains(qName)) {
            TranslationUnit newElement = new TranslationUnit(qName);
            if (!elements.isEmpty()) {
                elements.peekFirst().add(newElement);
            }
            elements.addFirst(newElement);
        }
        
        // special handling for <name> inside <function>
        if (qName.equals("name") && !xmlNesting.isEmpty() && xmlNesting.peek().equals("function")) {
            inNameInsideFunction = true;
        }
        
        xmlNesting.push(qName);
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (SUPPORTED_ELEMENTS.contains(qName)) {
            if (elements.size() > 1) {
                elements.removeFirst();
            }
        }
        
        xmlNesting.pop();
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String str = new String(ch, start, length).trim();
        if (!str.isEmpty()) {
            CodeUnit unit = new CodeUnit(str);
            elements.peekFirst().add(unit);
        }
        
        // special handling for <name> inside <function>
        if (inNameInsideFunction) {
            inNameInsideFunction = false;
            elements.peekFirst().setFunctionName(str);
        }
    }
    
    @Override
    protected CodeElement getAst() {
        ITranslationUnit unit = elements.removeFirst();
        System.out.println("1: ");
        System.out.println(unit);
        Preprocessing converter = new Preprocessing();
        converter.convert(unit);
        System.out.println("2: ");
        System.out.println(unit);
        System.out.println("3: ");
        TranslationUnitToAstConverter converter2 = new TranslationUnitToAstConverter(getFile().getPath());
        CodeElement astResult = converter2.convert(unit);
        System.out.println(astResult);
        
        System.out.println();
        System.out.println();
        System.out.println();
        return astResult;
    }
}
