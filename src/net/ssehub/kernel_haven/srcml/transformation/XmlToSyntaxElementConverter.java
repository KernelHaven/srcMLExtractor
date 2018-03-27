package net.ssehub.kernel_haven.srcml.transformation;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import net.ssehub.kernel_haven.code_model.CodeElement;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.srcml.transformation.rules.Preprocessing;
import net.ssehub.kernel_haven.srcml.xml.AbstractAstConverter;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Translates the XML output of <a href="https://www.srcml.org">srcML</a> into {@link ISyntaxElement}. This is done
 * in 3 steps:
 * <ol>
 *     <li>The XML output is parsed by this class to create {@link ITranslationUnit}s.</li>
 *     <li>The {@link ITranslationUnit}s are refined by the {@link Preprocessing} to be closer to the target AST structure,
 *     to simplify the final parsing.</li>
 *     <li>The {@link ITranslationUnit}s are parsed into {@link ISyntaxElement} by the
 *     {@link TranslationUnitToAstConverter}</li>
 * </ol>
 * @author El-Sharkawy
 *
 */
public class XmlToSyntaxElementConverter extends AbstractAstConverter {
    
    private static final boolean DEBUG_LOGGING = false;
    
    /**
     * White list of supported XML tags, which will be processed by this converter. These are the top level elements of
     * the <a href="https://www.srcml.org/doc/c_srcML.html">srcML C and CPP grammar</a>.
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
        tmpSet.add("macro"); // usage of a CPP macro
        
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
    
    private @NonNull HeaderHandling headerHandling;
    
    private @NonNull Deque<@NonNull TranslationUnit> elements = new ArrayDeque<>();
    
    /**
     * Whether we are currently inside the &lt;name&gt; inside a &lt;function&gt;.
     */
    private boolean inNameInsideFunction = false;
    
    /**
     * The actual nesting of qNames of the XML structure.
     */
    private @NonNull Deque<@NonNull String> xmlNesting = new ArrayDeque<>();
    
    private @NonNull String debugLoggingIndentation = "";
    
    /**
     * Sole constructor for this classes.
     * @param path The relative path to the source file in the source tree. Must not be <code>null</code>.
     */
    public XmlToSyntaxElementConverter(@NonNull File path, @NonNull HeaderHandling headerHandling) {
        super(path);
        this.headerHandling = headerHandling;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (DEBUG_LOGGING) {
            System.out.println(debugLoggingIndentation + "<" + qName + ">");
            debugLoggingIndentation += '\t';
        }
        
        // check the language attribute of <unit>
        if (qName.equals("unit")) {
            String language = attributes.getValue("language");
            if (language == null || !language.equals("C")) {
                throw new SAXException("Invalid language: \"" + language + "\"; expected: \"C\"");
            }
        }
        
        if (SUPPORTED_ELEMENTS.contains(qName)) {
            TranslationUnit newElement = new TranslationUnit(notNull(qName));
            newElement.setStartLine(getLineNumber());
            if (!elements.isEmpty()) {
                notNull(elements.peekFirst()).add(newElement);
            }
            elements.addFirst(newElement);
        }
        
        // special handling for <name> inside <function>
        if (qName.equals("name") && !xmlNesting.isEmpty() && notNull(xmlNesting.peek()).equals("function")) {
            inNameInsideFunction = true;
        }
        
        xmlNesting.push(qName);
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (DEBUG_LOGGING) {
            debugLoggingIndentation = notNull(debugLoggingIndentation.substring(1));
            System.out.println(debugLoggingIndentation + "</" + qName + ">");
        }
        
        if (SUPPORTED_ELEMENTS.contains(qName)) {
            if (elements.size() > 1) {
                notNull(elements.removeFirst()).setEndLine(getLineNumber());
            } else if (elements.size() == 1) {
                // Do not remove top-level unit (= complete file), but update end line
                notNull(elements.peekFirst()).setEndLine(getLineNumber());
            }
        }
        
        xmlNesting.pop();
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String str = new String(ch, start, length).trim();
        if (!str.isEmpty()) {
            if (DEBUG_LOGGING) {
                System.out.println(debugLoggingIndentation + str);
            }
            
            CodeUnit unit = new CodeUnit(str);
            unit.setStartLine(getLineNumber());
            unit.setEndLine(getLineNumber());
            notNull(elements.peekFirst()).add(unit);
            
            
            // special handling for <name> inside <function>
            if (inNameInsideFunction) {
                inNameInsideFunction = false;
                notNull(elements.peekFirst()).setFunctionName(str);
            }
        }
    }
    
    @Override
    protected @NonNull CodeElement getAst() throws FormatException {
        ITranslationUnit unit = notNull(elements.removeFirst());
        if (DEBUG_LOGGING) {
            System.out.println("XML -> Translation Units");
            System.out.println("========================");
            System.out.println(unit);
        }
        
        try {
            Preprocessing converter = new Preprocessing();
            converter.convert(unit);
            if (DEBUG_LOGGING) {
                System.out.println("Translation Unit Preprocessing");
                System.out.println("==============================");
                System.out.println(unit);
            }
        } catch (AssertionError err) {
            /*
             * Continuation followed by white line lead in parsing bugs in srcML, which result here in an unexpected
             * structure and, thus, in AssertionErrors...
             */
            throw ExceptionUtil.makeException("Unexpected result produced by srcML", err, unit);
        }
        
        TranslationUnitToAstConverter converter2 = new TranslationUnitToAstConverter(getFile().getPath());
        CodeElement astResult = converter2.convert(unit);
        if (DEBUG_LOGGING) {
            System.out.println("Translation Units -> AST");
            System.out.println("========================");
            System.out.println(astResult);
        }
        
        switch (headerHandling) {
        case IGNORE:
            // do nothing
            break;
        
        default:
            throw new FormatException("Header handling " + headerHandling + " not implemented");
        }
        
        return astResult;
    }
}
