package net.ssehub.kernel_haven.srcml.transformation.testing;

public interface ITranslationUnit extends Iterable<ITranslationUnit> {
    
    public String getType();
    
    public void replaceNested(ITranslationUnit oldUnit, ITranslationUnit newUnit);

}
