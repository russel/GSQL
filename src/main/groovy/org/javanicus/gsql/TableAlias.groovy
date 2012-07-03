package org.javanicus.gsql;

/** Connects a table to an alias. This is used in the Select class to distinguish several tables in the same select */
class TableAlias
{
    Table table
    String alias
    boolean join
    
    public String toString ()
    {
        def buffer = new StringBuffer ()
        if (table.schema)
        {
            buffer << table.schema
            buffer << "."
        }
        buffer << table.name
        if (alias)
        {
            buffer << " "
            buffer << alias
        }
        return buffer.toString()
    }
}
