package net.ssehub.kernel_haven.srcml;

import java.io.File;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.config.CodeExtractorConfiguration;
import net.ssehub.kernel_haven.util.PreparationTool;

/**
 * Prepares the srcML binary and checks if an update is required.
 * 
 * @author El-Sharkawy
 */
class Preparation extends PreparationTool {

    private File exec;

    /**
     * Creates a preparation wrapper for the srcML binary. 
     * 
     * @param config The extractor configuration.
     * 
     * @throws SetUpException If the current OS is not supported or the executable cannot be found.
     */
    public Preparation(CodeExtractorConfiguration config) throws SetUpException {
        File resourceFolder = config.getExtractorResourceDir(SrcMLExtractor.class);
        resourceFolder = new File(resourceFolder, "srcML");

        OSType os = super.determineOS();
        String execPath = null;
        if (null != os) {
            switch (os) {
            case WIN64:
                execPath = "win64/bin/srcml.exe";
                break;
            case LINUX32:
                // falls through
                break;
            case LINUX64:
                // falls through
                break;
            case MACOS64:
                // falls through
                break;
            case WIN32:
                // falls through
                break;
            default:
                throw new SetUpException("Operating system not supported by srcML extractor: " + os.name());
            }
        } else {
            throw new SetUpException("Could not determine appropriate executable for srcML");
        }
        
        if (null != execPath) {
            init(resourceFolder, execPath, "srcML.zip");
            exec = new File(resourceFolder, execPath);
        }
    }

    /**
     * Prepares and checks whether the srcML binary is up to date.
     * 
     * @return The prepared srcML binary.
     * 
     * @throws SetUpException If preparation fails.
     */
    public File prepareExec() throws SetUpException {
        super.prepare();
        return exec;
    }
}
