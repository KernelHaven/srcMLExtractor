package net.ssehub.kernel_haven.srcml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.code_model.AbstractCodeModelExtractor;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.config.CodeExtractorConfiguration;
import net.ssehub.kernel_haven.srcml.xml.CXmlHandler;
import net.ssehub.kernel_haven.srcml.xml.XmlToAstConverter;
import net.ssehub.kernel_haven.util.CodeExtractorException;
import net.ssehub.kernel_haven.util.CommandExecutor;
import net.ssehub.kernel_haven.util.CommandExecutor.ExecutionResult;
import net.ssehub.kernel_haven.util.ExtractorException;
import net.ssehub.kernel_haven.util.FormatException;

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
    private File srcExec;

    @Override
    protected void init(CodeExtractorConfiguration config) throws SetUpException {
        Preparation preparator = new Preparation(config);
        srcExec = preparator.prepareExec();
    }

    @Override
    protected SourceFile runOnFile(File target) throws ExtractorException {
        CommandExecutor cmdExec;
        try {
            System.out.println(srcExec.getParentFile().getAbsoluteFile());
            cmdExec = new CommandExecutor(srcExec.getParentFile().getAbsoluteFile(),
                srcExec.getAbsolutePath(), target.getAbsolutePath());
        } catch (IOException e) {
            throw new CodeExtractorException(target, "srcML initialization not successful for file: "
                + target.getAbsolutePath() + ", cause: " + e.getMessage());
        }
        
        ExecutionResult result;
        try {
            result = cmdExec.execute();
        } catch (InterruptedException e) {
            throw new CodeExtractorException(target, "srcML execution interuppted on file: "
                + target.getAbsolutePath() + ", cause: " + e.getMessage());
        } catch (IOException e) {
            throw new CodeExtractorException(target, "srcML execution not successful on file: "
                + target.getAbsolutePath() + ", cause: " + e.getMessage());
        }
        
        if (null != result.getError()) {
            throw new CodeExtractorException(target, "srcML execution not successful on file: "
                + target.getAbsolutePath() + ", cause: " + result.getError());
        } else if (result.getCode() != 0) {
            throw new CodeExtractorException(target, "srcML execution not successful on file: "
                + target.getAbsolutePath());
        } else {
            String astAsXML = result.getOutput();
            System.out.println(astAsXML);
            try {
                XmlToAstConverter converter = new XmlToAstConverter(astAsXML, new CXmlHandler(target));
                converter.parseToAst();
            } catch (ParserConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SAXException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (FormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        return null;
    }

    @Override
    protected String getName() {
        return this.getClass().getSimpleName();
    }

}
