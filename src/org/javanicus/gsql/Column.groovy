/**
 * ported from commons-sql
 * @author Jeremy Rayner
 * @author Dierk Koenig, adapted to jsr-04
 */
package org.javanicus.gsql

public class Column implements Cloneable, IColumn {
    /** Table in which this column belongs */
    Table table
    /** Name of the column in the database */
    String name
    /** Groovy name of the column. If not specified, then "name" above is used.
        This name can be used to lookup the column. */
    String groovyName
    /** Is this a primary key? */
    boolean primaryKey
    /** Is this required (NOT NULL)? */
    boolean required
    /** Should the values in this column be incremented automatically? Usually true for primaryKey columns. */
    boolean autoIncrement
    /** See java.sql.Types */
    int typeCode
    /** SQL type as string */
    String type
    /** Size of the column */
    Integer size // wegen "null", 0, ...
    /** Scale of the column */
    int scale
    /* Defaul value */
    def defaultValue
    // ???
    int precisionRadix = 10
    // ???
    int ordinalPosition = 0
    /** TypeMap to use to convert SQL types into int values and vice versa */
    TypeMap typeMap
    /** Mapper to convert DB values into Java values and vice versa */
    DBMapper mapper

    Column (TypeMap typeMap) {
        this.typeMap = typeMap
    }
    
    Column (Map args, TypeMap typeMap) {
        this.typeMap = typeMap
        
        args.each { entry ->
            this."${entry.key}" = entry.value
        }
    }
    
    Object clone() { //todo: throws CloneNotSupportedException {
        def result = new Column(typeMap)
        
        result.table           = table
        result.name            = name
        result.groovyName      = groovyName
        result.primaryKey      = primaryKey
        result.required        = required
        result.autoIncrement   = autoIncrement
        result.typeCode        = typeCode
        result.type            = type
        result.size            = size
        result.scale           = scale
        result.defaultValue    = defaultValue
        result.precisionRadix  = precisionRadix
        result.ordinalPosition = ordinalPosition
        result.typeMap         = typeMap
        result.mapper          = mapper
                  
        return result
    }

    String toString() {
        return "Column ${name} ${type}(${size},${scale})"
    }
    
    //  Groovy 1.0 bug: Can't use two setters here (one for int, one for String) because the String setter will never be found/used
    void setType(type) {
        if (type instanceof Integer) {
            this.type = typeMap.getJdbcTypeName(type)
            this.typeCode = type
        } else if (type instanceof String) {
            this.type = type
            this.typeCode = typeMap.getJdbcTypeCode(type)
        } else {
            throw new IllegalArgumentException ("Unsupported type for type: ${type.class.name}")
        }
    }
    
    // Groovy 1.0 bug: Can't use two setters here (one for int, one for String) because the String setter will never be found/used
    void setSize(size) {
        if (size instanceof Integer) {
            this.size = size
        } else if (size instanceof String) {
            int pos = size.indexOf(",")

            if (pos < 0) {
                this.size = Integer.parseInt(size)
                this.scale = 0
            } else {
                this.size = Integer.parseInt(size.substring(0, pos))
                scale     = Integer.parseInt(size.substring(pos + 1))
            }
        } else {
            throw new IllegalArgumentException ("Unsupported type for size: ${size.class.name}")
        }
    }
    
    /** Define a mapper which converts Java values to DB values and vice versa. See TrimDBMapper for an example. */
    void setMapper (DBMapper mapper)
    {
        this.mapper = mapper
        mapper.init (this)
    }
    
    /** This is called after all the attributes have been set */
    void init ()
    {
        if (!groovyName)
            groovyName = name.toLowerCase ()
    }
}
