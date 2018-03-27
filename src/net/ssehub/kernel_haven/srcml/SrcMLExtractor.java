package net.ssehub.kernel_haven.srcml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UncheckedIOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.code_model.AbstractCodeModelExtractor;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.DefaultSettings;
import net.ssehub.kernel_haven.config.EnumSetting;
import net.ssehub.kernel_haven.config.Setting;
import net.ssehub.kernel_haven.srcml.transformation.HeaderHandling;
import net.ssehub.kernel_haven.srcml.transformation.XmlToSyntaxElementConverter;
import net.ssehub.kernel_haven.srcml.xml.AbstractAstConverter;
import net.ssehub.kernel_haven.srcml.xml.XmlToAstConverter;
import net.ssehub.kernel_haven.util.CodeExtractorException;
import net.ssehub.kernel_haven.util.ExtractorException;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Util;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * An extractor which uses <a href="https://www.srcml.org/">srcML</a> to create an AST, still containing variability
 * information. <br/>
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
    
    private static final Logger LOGGER = Logger.get();
    
    private static final @NonNull Setting<@NonNull HeaderHandling> HEADER_HANDLING_SETTING = new EnumSetting<>(
            "code.extractor.header_handling", HeaderHandling.class, true, HeaderHandling.IGNORE,
            "How #include directives should be handled.\n\n- IGNORE: Does nothing; leaves the #include directives as"
            + " preprocessor statements in the AST.\n- INCLUDE: Parses the headers and includes their AST instead of the"
            + " #include directive.\n- EXPAND_FUNCTION_CONDITION: Searches for declarations of functions in the headers."
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
    protected @NonNull SourceFile runOnFile(@NonNull File target) throws ExtractorException {
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
     * @param absoulteTarget The absolute path to the file to parse.
     * @param relativeTarget The path to the file to parse, relative to the source tree. This is used in exceptions
     *      and as the path in the result {@link SourceFile}.
     *      
     * @return The parsed {@link SourceFile}.
     * 
     * @throws CodeExtractorException If parsing the file fails.
     */
    public @NonNull SourceFile parseFile(@NonNull File absoulteTarget, @NonNull File relativeTarget)
            throws CodeExtractorException {
        
        
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        
        try {
            PipedOutputStream out = new PipedOutputStream();
            PipedInputStream stdout = new PipedInputStream(out);
            
            ProcessBuilder builder = new ProcessBuilder(srcExec.getAbsolutePath(), absoulteTarget.getAbsolutePath());
            
//            builder.directory(srcExec.getParentFile());
            /*
             * LD_LIBRARY_PATH = ../lib needed on Linux/Mac
             * DYLD_LIBRARY_PATH = ../lib needed on Mac
             * Both settings do no harm on Windows.
             */
            String libFolder = new File(srcExec.getParentFile().getParentFile(), "lib").getAbsolutePath();
            builder.environment().put("LD_LIBRARY_PATH", libFolder);
            builder.environment().put("DYLD_LIBRARY_PATH", libFolder);
            
            AbstractAstConverter xmlConverter = new XmlToSyntaxElementConverter(absoulteTarget, relativeTarget,
                    headerHandling, this);
            XmlToAstConverter converter = new XmlToAstConverter(stdout, xmlConverter);

            // CHECKSTYLE:OFF
            new Thread(() -> {
                boolean success;
                try {
                    success = Util.executeProcess(builder, "srcML", out, stderr, 0);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                } finally {
                    try {
                        out.close();
                    } catch (IOException e) {
                        LOGGER.logException("Exception while closing piped stream for stdout", e);
                    }
                    try {
                        stderr.close();
                    } catch (IOException e) {
                        LOGGER.logException("Exception while closing piped stream for stderr", e);
                    }
                }
                if (!success) {
                    LOGGER.logWarning("srcML exe did not execute succesfully.");
                }
            }, "SrcMLExtractor-Worker").start();
            // CHECKSTYLE:ON
            
            SourceFile resultFile = converter.parseToAst();
            
            
            return resultFile;
            
        } catch (IOException | UncheckedIOException | ParserConfigurationException | SAXException | FormatException e) {
            throw new CodeExtractorException(relativeTarget, e);
            
        } finally {
            if (!stderr.toString().isEmpty()) {
                LOGGER.logDebug(stderr.toString().split("\n"));
            }
        }
    }

    @Override
    protected @NonNull String getName() {
        return "SrcMLExtractor";
    }

}
