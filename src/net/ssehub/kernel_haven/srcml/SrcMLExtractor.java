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
import net.ssehub.kernel_haven.srcml.transformation.XmlToSyntaxElementConverter;
import net.ssehub.kernel_haven.srcml.xml.AbstractAstConverter;
import net.ssehub.kernel_haven.srcml.xml.CXmlHandler;
import net.ssehub.kernel_haven.srcml.xml.XmlToAstConverter;
import net.ssehub.kernel_haven.util.ExtractorException;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Util;

/**
 * An extractor which uses <a href="http://www.srcml.org/">srcML</a> to create an AST, still containing variability
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
 * </ul>
 * @author El-Sharkawy
 *
 */
public class SrcMLExtractor extends AbstractCodeModelExtractor {
    
    private static final Logger LOGGER = Logger.get();
    
    public static boolean USE_NEW_CONVERTER = false;
    
    private File srcExec;

    @Override
    protected void init(Configuration config) throws SetUpException {
        Preparation preparator = new Preparation(config);
        srcExec = preparator.prepareExec();
    }

    @Override
    protected SourceFile runOnFile(File target) throws ExtractorException {
        try {
            PipedOutputStream out = new PipedOutputStream();
            PipedInputStream stdout = new PipedInputStream(out);
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();
            
            ProcessBuilder builder = new ProcessBuilder(srcExec.getAbsolutePath(), target.getAbsolutePath());
//            builder.directory(srcExec.getParentFile());
            // set LD_LIBRARY_PATH to ../lib
            // only needed for linux, but does no harm on windows.
            builder.environment().put("LD_LIBRARY_PATH",
                    new File(srcExec.getParentFile().getParentFile(), "lib").getAbsolutePath());
            
            AbstractAstConverter xmlConverter;
            if (USE_NEW_CONVERTER) {
                xmlConverter = new XmlToSyntaxElementConverter(target);
            } else {
                xmlConverter = new CXmlHandler(target);
            }
            
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
            
            LOGGER.logDebug(stderr.toString().split("\n"));
            
            return resultFile;
            
        } catch (IOException | UncheckedIOException | ParserConfigurationException | SAXException | FormatException e) {
            throw new ExtractorException(e);
        }
    }

    @Override
    protected String getName() {
        return this.getClass().getSimpleName();
    }

}
