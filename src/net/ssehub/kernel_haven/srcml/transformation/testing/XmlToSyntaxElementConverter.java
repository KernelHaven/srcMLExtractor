package net.ssehub.kernel_haven.srcml.transformation.testing;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import net.ssehub.kernel_haven.srcml.model.SrcMlSyntaxElement;
import net.ssehub.kernel_haven.srcml.xml.AbstractAstConverter;

public class XmlToSyntaxElementConverter extends AbstractAstConverter {
    
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
        tmpSet.add("while");
        tmpSet.add("for");
        tmpSet.add("do");
        tmpSet.add("switch");
        tmpSet.add("case");
        tmpSet.add("break");
        tmpSet.add("continue");
        tmpSet.add("goto");
        tmpSet.add("label");
        // Statements
        tmpSet.add("block");
        tmpSet.add("decl_stmt");
        tmpSet.add("expr_stmt");
        tmpSet.add("empty_stmt");
        
        SUPPORTED_ELEMENTS = Collections.unmodifiableSet(tmpSet);
    }
    
    private Stack<TranslationUnit> elements;
    
    
    public XmlToSyntaxElementConverter(File path) {
        super(path);
        
        elements = new Stack<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (SUPPORTED_ELEMENTS.contains(qName)) {
            TranslationUnit newElement = new TranslationUnit(qName);
            if (!elements.isEmpty()) {
                elements.peek().addTranslationUnit(newElement);
            }
            elements.add(newElement);
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (SUPPORTED_ELEMENTS.contains(qName)) {
            if (elements.size() > 1) {
                elements.pop();
            }
        }
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String str = new String(ch, start, length).trim();
        if (!str.isEmpty()) {
            CodeUnit unit = new CodeUnit(str);
            elements.peek().addToken(unit);
        }
    }
    
    @Override
    protected SrcMlSyntaxElement getAst() {
        ITranslationUnit unit = elements.pop();
        System.out.println("Before: ");
        System.out.println(unit);
        Converter converter = new Converter();
        converter.convert(unit);
        System.out.println("After: ");
        System.out.println(unit);
        return null;
    }

}
