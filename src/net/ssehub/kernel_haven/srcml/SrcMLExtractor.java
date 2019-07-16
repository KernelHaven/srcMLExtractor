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

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UncheckedIOException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.code_model.AbstractCodeModelExtractor;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.DefaultSettings;
import net.ssehub.kernel_haven.config.EnumSetting;
import net.ssehub.kernel_haven.config.Setting;
import net.ssehub.kernel_haven.srcml_old.transformation.FunctionConditionExpander;
import net.ssehub.kernel_haven.util.CodeExtractorException;
import net.ssehub.kernel_haven.util.ExtractorException;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Util;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * An extractor which uses <a href="https://www.srcml.org/">srcML</a> to create an AST, still containing variability
 * information.
 * <p>
 * The AST has the following properties:
 * <ul>
 *   <li>C-AST</li>
 *   <li>Contains preprocessor directives (variation points / presence conditions)</li>
 *   <li>Won't include headers or expand macros</li>
 * </ul>
 * This extractor supports the following platforms:
 * <ul>
 *   <li>Windows 64 Bit</li>
 *   <li>Linux 64 Bit</li>
 *   <li>macOS: El Capitan (not tested)</li>
 * </ul>
 * @author El-Sharkawy
 *
 */
public class SrcMLExtractor extends AbstractCodeModelExtractor {
    
    public static final boolean DEBUG_LOGGING = false;
    
    private static final Logger LOGGER = Logger.get();
    
    private static final @NonNull Setting<@NonNull HeaderHandling> HEADER_HANDLING_SETTING = new EnumSetting<>(
            "code.extractor.header_handling", HeaderHandling.class, true, HeaderHandling.IGNORE,
            "How #include directives should be handled.\n\n- IGNORE: Does nothing; leaves the #include directives as"
            + " preprocessor statements in the AST.\n- INCLUDE: Parses the headers and includes their AST instead of"
            + " the #include directive.\n- EXPAND_FUNCTION_CONDITION: Includes headers like INCLUDE. Searches for"
            + " declarations of functions in the headers."
            + " If declarations for the functions that are implemented in the C file are found, then their conditions"
            + " are expanded by the condition of the declaration.\n\nCurrently only quote include directives"
            + " (#include \"file.h\") relative to the source file being parsed are supported.");
    // TODO AK: update "currently only supports" when applicable
    
    private @NonNull HeaderHandling headerHandling = HeaderHandling.IGNORE; // will be overriden in init()
    
    private @NonNull File sourceTree = new File("will be initialized"); // will be overriden in init()
    
    private File srcExec;

    @Override
    protected void init(@NonNull Configuration config) throws SetUpException {
        sourceTree = config.getValue(DefaultSettings.SOURCE_TREE);
        config.registerSetting(HEADER_HANDLING_SETTING);
        headerHandling = config.getValue(HEADER_HANDLING_SETTING);
        
        Preparation preparator = new Preparation(config);
        srcExec = preparator.prepareExec();
    }
    
    @Override
    protected @NonNull SourceFile<ISyntaxElement> runOnFile(@NonNull File target) throws ExtractorException {
        File absoulteTarget = new File(sourceTree, target.getPath());
        if (!absoulteTarget.exists()) {
            throw new ExtractorException("srcML could not parse specified file, which does not exist: "
                    + absoulteTarget.getAbsolutePath());
        }
        
        return parseFile(absoulteTarget, target);
    }

    /**
     * Parses the given source file.
     * 
     * @param absoluteTarget The absolute path to the file to parse.
     * @param relativeTarget The path to the file to parse, relative to the source tree. This is used in exceptions
     *      and as the path in the result {@link SourceFile}.
     *      
     * @return The parsed {@link SourceFile}.
     * 
     * @throws CodeExtractorException If parsing the file fails.
     */
    public @NonNull SourceFile<ISyntaxElement> parseFile(@NonNull File absoluteTarget, @NonNull File relativeTarget)
            throws CodeExtractorException {
        
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        
        Thread worker = null;
        try {
            PipedInputStream stdout = new PipedInputStream();
            PipedOutputStream stdoutIn = new PipedOutputStream(stdout);
            
            ProcessBuilder builder = new ProcessBuilder(srcExec.getAbsolutePath(), absoluteTarget.getAbsolutePath());
            
//            builder.directory(srcExec.getParentFile());
            /*
             * LD_LIBRARY_PATH = ../lib needed on Linux/Mac
             * DYLD_LIBRARY_PATH = ../lib needed on Mac
             * Both settings do no harm on Windows.
             */
            String libFolder = new File(srcExec.getParentFile().getParentFile(), "lib").getAbsolutePath();
            builder.environment().put("LD_LIBRARY_PATH", libFolder);
            builder.environment().put("DYLD_LIBRARY_PATH", libFolder);
            
            worker = new Thread(() -> {
                boolean success;
                try {
                    success = Util.executeProcess(builder, "srcML", stdoutIn, stderr, 0);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                if (!success) {
                    LOGGER.logWarning("srcML exe did not execute succesfully");
                }
            }, "SrcMLExtractor-Worker");
            worker.start();
            
            SourceFile<ISyntaxElement> result = new SourceFile<>(relativeTarget);
            result.addElement(parse(absoluteTarget, relativeTarget, stdout));
            
            return result;
            
        } catch (IOException | UncheckedIOException | SAXException | FormatException e) {
            throw new CodeExtractorException(relativeTarget, e);
        } finally {
            if (worker != null) {
                try {
                    worker.join();
                } catch (InterruptedException e) {
                    // ignore
                }
            }
            
            if (stderr.size() > 0) {
                System.out.println("-------");
                System.out.println("stderr:");
                System.out.println(stderr.toString());
                System.out.println("-------");
            }
        }
    }
    
    /**
     * Parses the given XML stream to an AST.
     * 
     * @param absoluteTarget The absolute path to the file to parse.
     * @param relativeTarget The path to the file to parse, relative to the source tree. This is used in exceptions
     *      and as the path in the result {@link SourceFile}.
     * @param xml The XML stream to parse.
     * 
     * @return The parsed AST.
     * 
     * @throws FormatException If converting the XML to an AST structure fails.
     * @throws SAXException If parsing the XML fails.
     * @throws IOException If reading the XML stream fails.
     */
    private @NonNull ISyntaxElement parse(@NonNull File absoluteTarget, @NonNull File relativeTarget,
            @NonNull InputStream xml) throws FormatException, SAXException, IOException {
        
        Document doc = XmlParser.parse(xml);
        Node root = notNull(doc.getDocumentElement());
        
        if (!root.getNodeName().equals("unit")) {
            throw new FormatException("Expected <unit> but got <" + root.getNodeName() + ">");
        }
        
        Node languageAttr = root.getAttributes().getNamedItem("language");
        if (languageAttr == null) {
            throw new FormatException("Language attribute not specified in <unit>");
        }
        if (!languageAttr.getTextContent().equals("C")) {
            throw new FormatException("Unsupported language \"" + languageAttr.getTextContent() + "\"");
        }
        
        if (DEBUG_LOGGING) {
            System.out.println("==============");
            System.out.println("   Parsed");
            System.out.println("==============");
            XmlUserData.debugPrintXml(root);
        }
        
        new XmlPrepreocessor(relativeTarget, doc).preprocess(root);

        if (DEBUG_LOGGING) {
            System.out.println("==============");
            System.out.println("   Pre-Processed");
            System.out.println("==============");
            XmlUserData.debugPrintXml(root);
        }
        
        XmlToAstConverter converter = new XmlToAstConverter(relativeTarget);
        net.ssehub.kernel_haven.code_model.ast.File file = converter.convertFile(root);
        
        if (DEBUG_LOGGING) {
            System.out.println("==============");
            System.out.println("   Result");
            System.out.println("==============");
            System.out.println(file);
        }
        
        switch (headerHandling) {
        case IGNORE:
            // do nothing
            break;
            
        case INCLUDE:
            new IncludeExpander(absoluteTarget, this).expand(file);
            break;
            
        case EXPAND_FUNCTION_CONDITION:
            new IncludeExpander(absoluteTarget, this).expand(file);
            new FunctionConditionExpander().expand(file);
            break;
        
        default:
            throw new FormatException("Header handling " + headerHandling + " not implemented");
        }
        
        return file;
    }
    
    @Override
    protected @NonNull String getName() {
        return "SrcMLExtractor";
    }

}
