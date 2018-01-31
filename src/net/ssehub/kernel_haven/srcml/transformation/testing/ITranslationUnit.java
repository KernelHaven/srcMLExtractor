package net.ssehub.kernel_haven.srcml.transformation.testing;

public interface ITranslationUnit extends Iterable<ITranslationUnit> {
    
    public String getType();
    
    public void replaceNested(ITranslationUnit oldUnit, ITranslationUnit newUnit);
    
    public void removeNested(ITranslationUnit oldUnit);
    
    /**
     * Returns the number of nested elements.
     *
     * @return the number of nested elements (&ge; 0)
     */
    public int size();
    
    /**
     * Returns the nested element at the specified position (0-based index).
     *
     * @param index index of the nested element to return
     * @return the element at the specified position (0-based index)
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     * @see #size()
     */
    public ITranslationUnit getNestedElement(int index);

}
