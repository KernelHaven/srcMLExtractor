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
    
    private File sourceTree;
    
    private File srcExec;

    @Override
    protected void init(@NonNull Configuration config) throws SetUpException {
        Preparation preparator = new Preparation(config);
        srcExec = preparator.prepareExec();
        sourceTree = config.getValue(DefaultSettings.SOURCE_TREE);
    }

    @Override
    protected @NonNull SourceFile runOnFile(@NonNull File target) throws ExtractorException {
        File absoulteTarget = new File(sourceTree, target.getPath());
        if (!absoulteTarget.exists()) {
            throw new ExtractorException("srcML could not parse specified file, which does not exist: "
                    + absoulteTarget.getAbsolutePath());
        }
        
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
            
            AbstractAstConverter xmlConverter = new XmlToSyntaxElementConverter(target);
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
            throw new CodeExtractorException(target, e);
            
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
