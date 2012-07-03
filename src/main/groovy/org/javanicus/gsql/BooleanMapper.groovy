package org.javanicus.gsql;


class BooleanMapper implements DBMapper {
    String trueValue
    String falseValue
    
    BooleanMapper(String trueValue, String falseValue) {
        this.trueValue = trueValue
        this.falseValue = falseValue
    }
    
    void init (IColumn column)
    {
        // NOP
    }
    
    def fromDB (dbValue)
    {
        if (dbValue instanceof String)
            dbValue = dbValue.trim()
        
        return trueValue == dbValue
    }
    
    def toDB (javaValue)
    {
        return javaValue ? trueValue : falseValue
    }
}
