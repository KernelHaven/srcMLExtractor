package net.ssehub.kernel_haven.srcml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.PreparationTool;
import net.ssehub.kernel_haven.util.Util;
import net.ssehub.kernel_haven.util.Util.OSType;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Prepares the srcML binary and checks if an update is required.
 * 
 * @author El-Sharkawy
 */
class Preparation extends PreparationTool {

    private static final Logger LOGGER = Logger.get();
    
    private @NonNull File exec;

    /**
     * Creates a preparation wrapper for the srcML binary. 
     * 
     * @param config The extractor configuration.
     * 
     * @throws SetUpException If the current OS is not supported or the executable cannot be found.
     */
    public Preparation(@NonNull Configuration config) throws SetUpException {
        File resourceFolder = Util.getExtractorResourceDir(config, SrcMLExtractor.class);
        resourceFolder = new File(resourceFolder, "srcML");

        OSType os = Util.determineOS();
        String execPath = null;
        if (null != os) {
            switch (os) {
            case WIN64:
                execPath = "win64/bin/srcml.exe";
                break;
            case LINUX64:
                execPath = "linux64/bin/srcml";
                break;
            case MACOS64:
                execPath = "mac_elcapitan/bin/srcml";
                break;
            case LINUX32: // falls through
            case WIN32:   // falls through
            default:
                throw new SetUpException("Operating system not supported by srcML extractor: " + os.name());
            }
        } else {
            throw new SetUpException("Could not determine appropriate executable for srcML");
        }
        
        init(resourceFolder, execPath, "net/ssehub/kernel_haven/srcml/res/srcML.zip");
        exec = new File(resourceFolder, execPath);
    }

    /**
     * Prepares and checks whether the srcML binary is up to date.
     * 
     * @return The prepared srcML binary.
     * 
     * @throws SetUpException If preparation fails.
     */
    public @NonNull File prepareExec() throws SetUpException {
        super.prepare();
        
        try {
            exec.setExecutable(true);
        } catch (SecurityException exc) {
            LOGGER.logDebug("Could not make \"" + exec.getAbsolutePath() + "\" executable: " + exc.getMessage());
        }
        
        OSType os = Util.determineOS();
        if (os == OSType.LINUX64 || os == OSType.MACOS64) {
            try {
                Set<PosixFilePermission> perms = new HashSet<>();
                perms.addAll(Files.getPosixFilePermissions(exec.toPath()));
                perms.add(PosixFilePermission.OWNER_EXECUTE);
                perms.add(PosixFilePermission.GROUP_EXECUTE);
                perms.add(PosixFilePermission.OTHERS_EXECUTE);
                Files.setPosixFilePermissions(exec.toPath(), perms);
            } catch (IOException exc) {
                LOGGER.logDebug("Could set execution bit for \"" + exec.getAbsolutePath() + "\": "
                        + exc.getMessage());
            }
        }
        
        return exec;
    }
}
