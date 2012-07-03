package org.javanicus.gsql

public class Index implements Cloneable {
    def name
    def List indexColumns
        
    public Index() {
        indexColumns = []
    }
        
    public Object clone() { //@todo throws CloneNotSupportedException {
        def result = new Index()
            
        result.name         = name
        result.indexColumns = indexColumns.clone()
                  
        return result
    }
}
