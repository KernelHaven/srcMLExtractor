package net.ssehub.kernel_haven.srcml.transformation;

/**
 * A data structure for parsing the output of <a href="http://www.srcml.org/">srcML</a>, which is used to generate the
 * AST. A translation unit may be a single token or already a complex structure, which can be directly mapped to an
 * AST element.
 * @author El-Sharkawy
 * 
 */
public interface ITranslationUnit  {
    
    /**
     * Returns the type (syntax element) of the ITranslation unit, may a
     * <a href="http://www.srcml.org/doc/c_srcML.html">srcML-Tag</a> if this was not further processed.
     * @return A type denoting which kind of AST element is represented by the {@link ITranslationUnit}.
     */
    public String getType();
    
    /**
     * Exchanges the old element by the new element at the same position.
     * @param oldUnit An {@link ITranslationUnit} which should be replaced by a more processed element.
     * @param newUnit The &#34;more processed/parsed&#34; element.
     */
    public void replaceNested(ITranslationUnit oldUnit, ITranslationUnit newUnit);
    
    /**
     * Removes a nested element. In most cases not needed, probably you want to use the
     * {@link #replaceNested(ITranslationUnit, ITranslationUnit)} method.
     * @param oldUnit A {@link ITranslationUnit} which is no longer needed (be careful with this function).
     */
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
    
    /**
     * Specifies the line number where the element starts.
     * @param rowIndex A 1-based index.
     */
    public void setStartLine(int rowIndex);
}
