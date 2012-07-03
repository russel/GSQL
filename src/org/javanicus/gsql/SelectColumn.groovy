package org.javanicus.gsql;


class SelectColumn {
    private Column column
    def TableAlias tableAlias
    def alias
    // For ORDER BY
    def boolean ascending = true
    def specialCode
    
    void setTableAlias (tableAlias)
    {
        if (tableAlias instanceof Join)
            tableAlias = tableAlias.alias
        
        this.tableAlias = tableAlias
    }
    
    public void setColumn (Column column)
    {
	if (!column)
	    throw new IllegalArgumentException ('column is null')
	
        this.column = column
        if (alias == null)
            alias = column.name
    }
    
    public Column getColumn ()
    {
        return column
    }
    
    public String toString ()
    {
        if (specialCode)
        {
            def s = specialCode
            if (s.contains ('${column}'))
                s = s.replaceAll (/\$\{column\}/, "${tableAlias.alias}.${column.name}")
            return "${s} AS ${alias}"
        }
        
        return "${tableAlias.alias}.${column.name} AS ${alias}"
    }
    
    public String toWhere ()
    {
        return "${tableAlias.alias}.${column.name}"
    }
}
