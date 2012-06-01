package org.javanicus.gsql

public class Table extends GroovyObjectSupport implements Cloneable {
    /** DB schema of the table */
    String schema
    /** Table name */
    String name
    /** Groovy name of the table if the table name is not groovy :-) */
    String groovyName
    /** Additional remarks */
    String remarks
    // ???
    def type
    /** An object which represents this table. This is used in the Select class. */
    Class rowType
    /** List of all columns in the table */
    List columns
    /** List of foreign keys of the table */
    List foreignKeys
    /** List of indexes on the table */
    List indexes
    
    public Table(aName) {
        schema = null
        name = aName
        groovyName = null
        remarks = null
        type = null
        columns = []
        foreignKeys = []
        indexes = []
    }

    public Object clone() { // throws CloneNotSupportedException {
        def result = new Table(name)

        result.schema      = schema
        result.name        = name
        result.groovyName  = groovyName
        result.remarks     = remarks
        result.type        = type
        result.rowType     = rowType
        result.columns     = columns.clone()
        result.foreignKeys = foreignKeys.clone()
        result.indexes     = indexes.clone()
        
        return result
    }

// @todo - cannot override property getter succesfully
//    public String getType() {
//        return (type == null) ? "(null)" : type
//    }

    public List getUniques() {
        indexes.findAll() {it.isUnique()}
    }
    
    public boolean hasPrimaryKey() {
        def aPrimaryKeyColumn = getColumns().find() {it.isPrimaryKey()}
        return aPrimaryKeyColumn != null
    }
    
    public Object getProperty(String propertyName) {
        try {
            return super.getProperty(propertyName);
        } catch (Exception e) {
            return findColumn(propertyName);
        }
    }

    /** Search a column by DB or Groovy name. The DB name is not case sensitive. */
    public Column findColumn(aName) {
        getColumns().find() {it.name.equalsIgnoreCase(aName) || it.groovyName.equals(aName)}
    }

    /** Search an index by DB or Groovy name. The DB name is not case sensitive. */
    public Index findIndex(aName) {
        getIndexes().find() {it.name.equalsIgnoreCase(aName) || it.groovyName.equals(aName)}
    }

    /** Get a list of all primary keys */
    public List getPrimaryKeyColumns() {
        getColumns().findAll() {it.isPrimaryKey()}
    }

    /** Get a list of all autoIncrement columns */
    public Column getAutoIncrementColumn() {
        getColumns().find() {it.isAutoIncrement()}
    }
    
    public String toString() {
        "Table[schema=${schema};name=${name};columnCount=${columns.size()}]"
    }

    public Table addColumn(Column col) {
        columns.add (col);
        col.table = this
        return this
    }
    
    void init () {
        if (!groovyName)
            groovyName = name
    }
}
