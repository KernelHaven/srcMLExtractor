package net.ssehub.kernel_haven.srcml.transformation.testing;

public class PreprocessorElse extends PreprocessorBlock {

    private PreprocessorIf startElement = null;
    
    public PreprocessorElse(Type type, String condition, PreprocessorIf startElement) {
        super(type, condition);
        this.startElement = startElement;
    }

}