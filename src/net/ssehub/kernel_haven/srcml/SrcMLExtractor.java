package net.ssehub.kernel_haven.srcml;

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
import net.ssehub.kernel_haven.config.CodeExtractorConfiguration;
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
    
    private File srcExec;

    @Override
    protected void init(CodeExtractorConfiguration config) throws SetUpException {
        Preparation preparator = new Preparation(config);
        srcExec = preparator.prepareExec();
    }

    @Override
    protected SourceFile runOnFile(File target) throws ExtractorException {
        try {
            PipedOutputStream out = new PipedOutputStream();
            PipedInputStream stdout = new PipedInputStream(out);
            
            ProcessBuilder builder = new ProcessBuilder(srcExec.getAbsolutePath(), target.getAbsolutePath());
//            builder.directory(srcExec.getParentFile());
            
            XmlToAstConverter converter = new XmlToAstConverter(stdout, new CXmlHandler(target));

            new Thread(() -> {
                boolean success;
                try {
                    success = Util.executeProcess(builder, "srcML", out, null, 0);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                if (!success) {
                    LOGGER.logWarning("srcML exe did not execute succesfully.");
                }
            }, "SrcMLExtractor-Worker").start();
            
            SourceFile resultFile = converter.parseToAst();
            
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