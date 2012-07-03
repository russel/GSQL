package org.javanicus.gsql

public class ForeignKey implements Cloneable {
    def foreignTable
    def List references
    
    public ForeignKey() {
        references = []
    }
    
    public Object clone() { // @todo throws CloneNotSupportedException {
        def result = new ForeignKey()
        
        result.foreignTable = foreignTable
        result.references   = references.clone()
        return result
    }
}

