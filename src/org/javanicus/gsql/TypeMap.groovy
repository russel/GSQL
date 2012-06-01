package org.javanicus.gsql

import java.sql.Types

public class TypeMap {
    def nameToCode
    def codeToName
    def decimalTypes
    def textTypes
    def otherTypes
    
    /** Overwrite this to change the default typemap */
    static TypeMap DEFAULT = new TypeMap ()
    
    public TypeMap() {
        nameToCode = [:]
        codeToName = [:]
                  
        decimalTypes = [
            "NUMERIC" : Types.NUMERIC,
            "DECIMAL" : Types.DECIMAL,
            "FLOAT" : Types.FLOAT,
            "REAL" : Types.REAL,
            "DOUBLE" : Types.DOUBLE
        ]
        textTypes = [
            "CHAR" : Types.CHAR,
            "VARCHAR" : Types.VARCHAR,
            "LONGVARCHAR" : Types.LONGVARCHAR,
            "CLOB" : Types.CLOB,
            "DATE" : Types.DATE,
            "TIME" : Types.TIME,
            "TIMESTAMP" : Types.TIMESTAMP
        ]
        otherTypes = [
            "BIT" : Types.BIT,
            "TINYINT" : Types.TINYINT,
            "SMALLINT" : Types.SMALLINT,
            "INTEGER" : Types.INTEGER,
            "BIGINT" : Types.BIGINT,
            "BINARY" : Types.BINARY,
            "VARBINARY" : Types.VARBINARY,
            "LONGVARBINARY" : Types.LONGVARBINARY,
            "NULL" : Types.NULL,
            "OTHER" : Types.OTHER,
            "JAVA_OBJECT" : Types.JAVA_OBJECT,
            "DISTINCT" : Types.DISTINCT,
            "STRUCT" : Types.STRUCT,
            "ARRAY" : Types.ARRAY,
            "BLOB" : Types.BLOB,
            "REF" : Types.REF,
            "DATALINK" : Types.DATALINK,
            "BOOLEAN" : Types.BOOLEAN
        ]
        for (entries in decimalTypes.entrySet()) {
            crossRef(entries.key,entries.value)          
        }          
        for (entries in textTypes.entrySet()) {
            crossRef(entries.key,entries.value)          
        }          
        for (entries in otherTypes.entrySet()) {
            crossRef(entries.key,entries.value)          
        }          
  
    }
    
    private crossRef(name,code) {
        nameToCode.put(name,code)
        codeToName.put(code,name)
    }
    
    public int getJdbcTypeCode(String name) {
        return nameToCode.get(name.toUpperCase(),Types.OTHER)
    }

    public String getJdbcTypeName(int code) {
        return codeToName.get(code,"unknown")
    }
    
    /**
      * Returns true if values for the type need have size and scale measurements
      *
      * @param type The type to check.
      */
    public boolean isDecimalType(int type) {
        return isDecimalType(getJdbcTypeName(type));
    }
    
    /**
      * Returns true if values for the type need have size and scale measurements
      *
      * @param type The type to check.
      */
    public boolean isDecimalType(String type) {
        return decimalTypes.keySet().any {
            type.equalsIgnoreCase(it)
        }
    }
    
    public boolean isTextType(int type) {
        return isTextType(getJdbcTypeName(type))
    }
    
    public boolean isTextType(String type) {
        return textTypes.keySet().any {
            type.equalsIgnoreCase(it)
        }
    }
}
