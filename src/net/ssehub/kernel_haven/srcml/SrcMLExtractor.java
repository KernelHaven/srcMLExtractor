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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.block_extractor.CodeBlockExtractor;
import net.ssehub.kernel_haven.block_extractor.InvalidConditionHandling;
import net.ssehub.kernel_haven.code_model.AbstractCodeModelExtractor;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.DefaultSettings;
import net.ssehub.kernel_haven.config.EnumSetting;
import net.ssehub.kernel_haven.config.Setting;
import net.ssehub.kernel_haven.util.CodeExtractorException;
import net.ssehub.kernel_haven.util.ExtractorException;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.PerformanceProbe;
import net.ssehub.kernel_haven.util.Util;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.util.null_checks.Nullable;

/**
 * An extractor which uses <a href="https://www.srcml.org/">srcML</a> to create an AST, still containing variability
 * information.
 * <p>
 * The AST has the following properties:
 * <ul>
 *   <li>C-AST</li>
 *   <li>Contains preprocessor directives (variation points / presence conditions)</li>
 *   <li>Optionally includes headers</li>
 *   <li>Won't expand macros</li>
 * </ul>
 * This extractor supports the following platforms:
 * <ul>
 *   <li>Windows 64 Bit</li>
 *   <li>Linux 64 Bit</li>
 *   <li>macOS: El Capitan (not tested)</li>
 *   <li>Any where srcml is installed and available as "srcml" on the PATH</li>
 * </ul>
 * 
 * @author Adam
 * @author El-Sharkawy
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
    
    /**
     * <b>Do not use this variable directly, use {@link #hasSrcmlInstalled()} instead.</b>
     * Caches the result of {@link #hasSrcmlInstalled()}.
     */
    private static @Nullable Boolean hasSrcmlInstalled;
    
    private @NonNull HeaderHandling headerHandling = HeaderHandling.IGNORE; // will be overridden in init()
    
    private @NonNull File sourceTree = new File("will be initialized"); // will be overridden in init()
    
    private boolean handleLinuxMacro = false; // will be overridden in init()
    
    private boolean fuzzyParsing = false; // will be overridden in init()
    
    // will be overridden in init()
    private @NonNull InvalidConditionHandling invalidConditionHandling = InvalidConditionHandling.EXCEPTION;
    
    private File srcExec;

    @Override
    protected void init(@NonNull Configuration config) throws SetUpException {
        this.sourceTree = config.getValue(DefaultSettings.SOURCE_TREE);
        this.fuzzyParsing = config.getValue(DefaultSettings.FUZZY_PARSING);
        
        config.registerSetting(HEADER_HANDLING_SETTING);
        this.headerHandling = config.getValue(HEADER_HANDLING_SETTING);
        
        // TODO: these settings are CodeBlockExtractor-specific; the user may not know that they apply here
        config.registerSetting(CodeBlockExtractor.HANDLE_LINUX_MACROS);
        this.handleLinuxMacro = config.getValue(CodeBlockExtractor.HANDLE_LINUX_MACROS);
        config.registerSetting(CodeBlockExtractor.INVALID_CONDITION_SETTING);
        this.invalidConditionHandling = config.getValue(CodeBlockExtractor.INVALID_CONDITION_SETTING);
        
        if (!hasSrcmlInstalled()) {
            Preparation preparator = new Preparation(config);
            srcExec = preparator.prepareExec();
        }
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
     * Holds the information associated with a srcml process.
     */
    private class SrcMlProcess {
        
        private @NonNull Process process;
        
        private @NonNull String stderr = "";

        /**
         * Creates and starts a srcml process.
         * 
         * @param absoluteTarget The file to run the process on.
         * 
         * @throws IOException If starting the process fails.
         */
        public SrcMlProcess(@NonNull File absoluteTarget) throws IOException {
            ProcessBuilder builder;
            if (hasSrcmlInstalled()) {
                builder = new ProcessBuilder("srcml", absoluteTarget.getAbsolutePath());
                
            } else {
                builder = new ProcessBuilder(srcExec.getAbsolutePath(), absoluteTarget.getAbsolutePath());
                /*
                 * LD_LIBRARY_PATH = ../lib needed on Linux & Mac
                 * DYLD_LIBRARY_PATH = ../lib needed on Mac
                 * Both settings do no harm on Windows.
                 */
                String libFolder = new File(srcExec.getParentFile().getParentFile(), "lib").getAbsolutePath();
                builder.environment().put("LD_LIBRARY_PATH", libFolder);
                builder.environment().put("DYLD_LIBRARY_PATH", libFolder);
            }

            this.process = notNull(builder.start());
            
            Thread errorReader = new Thread(() -> {
                try {
                    this.stderr = Util.readStream(notNull(this.process.getErrorStream()));
                } catch (IOException e) {
                }
            }, "SrcMLStderrReader");
            errorReader.setDaemon(true);
            errorReader.start();
        }
        
        /**
         * Returns the standard output stream of this process.
         * 
         * @return The standard output stream of this process.
         */
        public @NonNull InputStream getStdout() {
            return notNull(this.process.getInputStream());
        }
        
        /**
         * After {@link #waitFor()} is called, this returns the error output of this process.
         * 
         * @return The error output of this process.
         */
        public @NonNull String getStderr() {
            return stderr;
        }
        
        /**
         * Waits until this process is finished and returns the exit code. If this process is already finished, only
         * the exit code is returned.
         * 
         * @param timeout The maximum time to wait for the process to finish, in milliseconds. If this is elapsed,
         *      the process is killed and <code>null</code> is returned.
         * 
         * @return The exit code of this process or <code>null</code> if the process was killed due to a timeout.
         */
        public @Nullable Integer waitFor(long timeout) {
            return Util.waitForProcess(this.process, timeout);
        }
        
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

        SourceFile<ISyntaxElement> result = null;
        int iteration = 1;
        
        /*
         * this is set to true if either
         * a) we have a successful parsing result
         * b) the srcML process exited normally 
         */
        boolean success = false;
        
        do {
            if (iteration > 1) {
                LOGGER.logInfo("Trying again");
            }
            
            SrcMlProcess process = null;
            
            try {
                process = new SrcMlProcess(absoluteTarget);
                
                result = new SourceFile<>(relativeTarget);
                result.addElement(parse(absoluteTarget, relativeTarget, process.getStdout()));
                // if we have a successfully parsed result, we don't need to try again if the srcML exe hangs
                success = true;
                
            } catch (IOException | SAXException | FormatException e) {
                throw new CodeExtractorException(relativeTarget, e);
                
            } finally {
                if (process != null) {
                    try {
                        // close stdout in the case that an exception aborted our parsing early
                        process.getStdout().close();
                    } catch (IOException e) {
                        // ignore
                    }
                    // wait only a bit for the srcml exe to stop, since parsing is already finished
                    Integer exitCode = process.waitFor(100);
                    
                    if (exitCode != null) {
                        if (exitCode != 0) {
                            LOGGER.logWarning("srcML exe did not execute succesfully: " + exitCode);
                        } else {
                            // if the srcML exe didn't hang, we don't need to try again
                            success = true;
                        }
                    } else {
                        LOGGER.logWarning("srcML was killed due to the kill-timeout being reached");
                    }
                    if (process.getStderr().length() > 0) {
                        LOGGER.logDebug("srcML stderr:", process.getStderr());
                    }
                }
            }
            
        } while (!success && (++iteration) < 3);
        
        return result;
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
        PerformanceProbe p = new PerformanceProbe("SrcMLExtractor parse()");
        PerformanceProbe p1 = new PerformanceProbe("SrcMLExtractor 1) XML parsing");
        
        Document doc = XmlParser.parse(xml);
        @NonNull Node root = notNull(doc.getDocumentElement());
        
        if (!root.getNodeName().equals("unit")) {
            p1.close();
            p.close();
            throw new FormatException("Expected <unit> but got <" + root.getNodeName() + ">");
        }
        
        Node languageAttr = root.getAttributes().getNamedItem("language");
        if (languageAttr == null) {
            p1.close();
            p.close();
            throw new FormatException("Language attribute not specified in <unit>");
        }
        if (!languageAttr.getTextContent().equals("C")) {
            p1.close();
            p.close();
            throw new FormatException("Unsupported language \"" + languageAttr.getTextContent() + "\"");
        }    
        debugXmlOutput("Parsed", root);
        
        p1.close();
        p1 = new PerformanceProbe("SrcMLExtractor 2) Preprocessing");
        
        new XmlPrepreocessor(relativeTarget, doc).preprocess(root);
        debugXmlOutput("Pre-Processed", root);
        
        p1.close();
        p1 = new PerformanceProbe("SrcMLExtractor 3) Conversion");
        
        XmlToAstConverter converter = new XmlToAstConverter(relativeTarget, this.handleLinuxMacro, this.fuzzyParsing,
                this.invalidConditionHandling);
        net.ssehub.kernel_haven.code_model.ast.File file = converter.convertFile(root);   
        debugFileOutput(file);
        
        p1.close();
        p1 = new PerformanceProbe("SrcMLExtractor 4) Header Handling");
        
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
            p1.close();
            p.close();
            throw new FormatException("Header handling " + headerHandling + " not implemented");
        }
        
        p1.close();
        p.close();
        return file;
    }

    /**
     * Prints the parsed {@link net.ssehub.kernel_haven.code_model.ast.File}, if {@link #DEBUG_LOGGING}.
     * @param file The parsed file to print
     */
    private void debugFileOutput(net.ssehub.kernel_haven.code_model.ast.File file) {
        if (DEBUG_LOGGING) {
            System.out.println("==============");
            System.out.println("   Result");
            System.out.println("==============");
            System.out.println(file);
        }
    }

    /**
     * Prints the parsed XML document, if {@link #DEBUG_LOGGING}.
     * @param type The processing type for printing the log message, e.g., parsed, pre-processed, ...
     * @param root The root of the XML to print.
     */
    private void debugXmlOutput(String type, @NonNull Node root) {
        if (DEBUG_LOGGING) {
            System.out.println("==============");
            System.out.print("   ");
            System.out.println(type);
            System.out.println("==============");
            XmlUserData.debugPrintXml(root);
        }
    }
    
    /**
     * Checks whether the srcml executable is installed on the current system.
     * 
     * @return Whether the srcml executable is available on the system PATH.
     */
    private static synchronized boolean hasSrcmlInstalled() {
        boolean result;
        if (hasSrcmlInstalled != null) {
            result = hasSrcmlInstalled;
        } else {
            int ret = -1;
            try {
                ProcessBuilder pb = new ProcessBuilder("srcml", "--version");
                Process p = pb.start();
                ret = p.waitFor();
            } catch (IOException | InterruptedException e) {
                // ignore
            }
            
            result = (ret == 0);
            hasSrcmlInstalled = result;
        }
        return result;
    }
    
    @Override
    protected @NonNull String getName() {
        return "SrcMLExtractor";
    }

}
