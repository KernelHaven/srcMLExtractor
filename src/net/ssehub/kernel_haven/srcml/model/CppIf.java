package net.ssehub.kernel_haven.srcml.model;

import java.util.LinkedList;
import java.util.List;

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.True;

public class CppIf extends CppStatement {
    
    private Formula ifCondition = True.INSTANCE;
    
    private List<SrcMlSyntaxElement> thenBlock;
    
    private List<Formula> elifConditions;
    
    private List<List<SrcMlSyntaxElement>> elifBlocks;
    
    private List<SrcMlSyntaxElement> elseBlock;
    
    public CppIf(Formula presenceCondition) {
        super(presenceCondition);
        
        thenBlock = new LinkedList<>();
        elifBlocks = new LinkedList<>();
        elifConditions = new LinkedList<>();
    }
    
    public CppIf(int lineStart, int lineEnd, java.io.File sourceFile, Formula condition, Formula presenceCondition) {
        super(lineStart, lineEnd, sourceFile, condition, presenceCondition);

        thenBlock = new LinkedList<>();
        elifBlocks = new LinkedList<>();
        elifConditions = new LinkedList<>();
    }
    
    @Override
    public int getNestedElementCount() {
        int count = thenBlock.size();
        for (List<SrcMlSyntaxElement> elifBlock : elifBlocks) {
            count += elifBlock.size();
        }
        if (elseBlock != null) {
            count += elseBlock.size();
        }
        return count;
    }

    @Override
    public SrcMlSyntaxElement getNestedElement(int index) throws IndexOutOfBoundsException {
        if (index < thenBlock.size()) {
            return thenBlock.get(index);
        }
        index -= thenBlock.size();
        for (List<SrcMlSyntaxElement> elifBlock : elifBlocks) {
            if (index < elifBlock.size()) {
                return elifBlock.get(index);
            }
            index -= elifBlock.size();
        }
        
        if (elseBlock != null) {
            return elseBlock.get(index);
        }
        
        throw new IndexOutOfBoundsException();
    }

    @Override
    protected void addNestedElement(SrcMlSyntaxElement element) {
        throw new IndexOutOfBoundsException();
    }
    
    @Override
    public void setNestedElement(int index, SrcMlSyntaxElement element) {
        if (index < thenBlock.size()) {
            if (element == null) {
                thenBlock.remove(index);
            } else {
                thenBlock.set(index, element);
            }
            return;
        }
        index -= thenBlock.size();
        
        for (List<SrcMlSyntaxElement> elifBlock : elifBlocks) {
            if (index < elifBlock.size()) {
                if (element == null) {
                    elifBlock.remove(index);
                    elifConditions.remove(index);
                } else {
                    elifBlock.set(index, element);
                }
                return;
            }
            index -= elifBlock.size();
        }
        
        if (elseBlock != null) {
            if (element == null) {
                elseBlock.remove(index);
            } else {
                elseBlock.set(index, element);
            }
            return;
        }
        
        throw new IndexOutOfBoundsException();
    }
    
    public void setIfCondition(Formula ifCondition) {
        this.ifCondition = ifCondition;
    }
    
    public Formula getIfCondition() {
        return ifCondition;
    }
    
    public Formula getElifCondition(int index) {
        return elifConditions.get(index);
    }
    
    public void setElifCondition(int index, Formula condition) {
        elifConditions.set(index, condition);
    }
    
    public void addThenElement(SrcMlSyntaxElement element) {
        thenBlock.add(element);
    }
    
    public SrcMlSyntaxElement getThenElement(int index) {
        return thenBlock.get(index);
    }
    
    public int getNumThenElements() {
        return thenBlock.size();
    }
    
    public void addElseElement(SrcMlSyntaxElement element) {
        if (elseBlock == null) {
            elseBlock = new LinkedList<>();
        }
        elseBlock.add(element);
    }
    
    public boolean hasElseBlock() {
        return elseBlock != null;
    }
    
    public int getNumElseElements() {
        return elseBlock.size();
    }
    
    public SrcMlSyntaxElement getElseElement(int index) {
        return elseBlock.get(index);
    }
    
    public void setNumElifBlocks(int num) {
        for (int i = 0; i < num; i++) {
            elifBlocks.add(new LinkedList<>());
            elifConditions.add(True.INSTANCE);
        }
    }
    
    public void addElifElement(int index, SrcMlSyntaxElement element) {
        elifBlocks.get(index).add(element);
    }
    
    public SrcMlSyntaxElement getElifElement(int blockIndex, int index) {
        return elifBlocks.get(blockIndex).get(index);
    }
    
    public int getNumElifBlocks() {
        return elifBlocks.size();
    }
    
    public int getNumElifElements(int blockIndex) {
        return elifBlocks.get(blockIndex).size();
    }
    
    @Override
    protected String elementToString() {
        StringBuilder txt = new StringBuilder("#if ").append(ifCondition.toString())
                .append(" (").append(thenBlock.size()).append(" elements)");
        
        for (int i = 0; i < elifBlocks.size(); i++) {
            txt.append(" #elif ").append(elifConditions.get(i))
                .append(" (").append(elifBlocks.get(i).size()).append(" elements)");
        }
        
        if (elseBlock != null) {
            txt.append(" #else")
                .append(" (").append(elseBlock.size()).append(" elements)");
        }
        
        return txt.toString();
    }

}
