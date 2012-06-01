package org.javanicus.gsql;

/* DIA 6.7.2007 This doesn't work; the log output is just swallowed. No idea why and where. Switched back to log4j which just works.
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
*/
import org.apache.log4j.Logger;

import groovy.sql.Sql
import org.javanicus.gsql.DataSourceProvider

class Select
{
    public static final Logger log = Logger.getLogger(Select.class);
    
    /** The Database object for which to generate SQL */
    def db
    /** Add "DISTINCT"? */
    boolean distinct
    List columns = [] // The columns of the SELECT
    List tables = [] // The tables involved (instances of TableAlias)
    def where // The WHERE condition(s)
    List groupBy = [] // Any columns to GROUP BY
    List orderBy = [] // Dito for ORDER BY
    List param = [] // Parameters (when using prepared statements)
    /** Joins with other tables. If there are joins, this list contains
        the Join instances and there is a second matching instance
        in the tables list which contains the TableAlias for the join
        (ie. the name to use to refer to this join). */
    List joins = []
    /** Override the SQL generation and just run this query */
    String specialSQL
    /** How many rows to fetch. */
    int maxRows = Integer.MAX_VALUE
    /** true, if maxRows was reached. */
    boolean maxRowsReached

    Select (db)
    {
        if (db == null)
            throw new IllegalArgumentException ("db is null")
        
        this.db = db
    }
    
    /** Add all columns of a table (SELECT * ...) to the result */
    void all (table)
    {
        if (table == null)
            throw new IllegalArgumentException ("table is null")
        
        if (table instanceof Join)
            table = table.alias.table
        else if (table instanceof TableAlias)
            table = table.table
        
        for (col in table.columns)
        {
            add (col)
        }
    }
    
    /** Add a column to the SELECT part */
    SelectColumn add (Column column)
    {
        return add (column, null)
    }

    /** Add a column to the SELECT part with the specified alias. (SELECT ... AS alias) */
    SelectColumn add (Column column, String alias)
    {
        if (alias == null)
            alias = column.name

        def sc = whereColumn (column)
        sc.alias = alias
        columns.add (sc)
        return sc
    }
    
    /** Return an column object suitable for the WHERE part */
    SelectColumn whereColumn (Column column)
    {
        if (column == null)
            throw new IllegalArgumentException ("column is null")
        
        def ta = addTable (column.table)
        def sc = new SelectColumn (tableAlias:ta, column:column)
        return sc
    }
    
    /** Find an existing column in the SELECT */
    SelectColumn findColumn (Column col)
    {
        return columns.find() { it.column.equals (col) }
    }
    
    /** Include another table in the SELECT. Use join() if you want to do joins. */
    TableAlias addTable (Table table)
    {
        if (table == null)
            throw new IllegalArgumentException ("table is null")
        
        def ta = tables.find () { it.table == table }
        if (!ta)
        {
            def alias = "t${tables.size}"
            ta = new TableAlias (table:table, alias:alias)
            tables.add (ta)
        }
        return ta
    }
    
    /** Join two tables.
     * 
     * The closure will be called with the new Join object so it can add the condition
     * (which columns define the join) to the SELECT. See the Join class for details.
     */
    Join join (table, Closure c)
    {
        if (table == null)
            throw new IllegalArgumentException ("table is null")
        
        def ta = addTable (table)
        ta.join = true
        Join join = joins.find() { it.alias == ta }
        if (!join)
        {
            join = new Join (alias: ta)
            joins.add (join)
            
            c.call (join)
        }
        return join
    }
    
    /** Create the SQL query */
    String getSQL ()
    {
        if (specialSQL != null)
            return specialSQL;
        
        def buffer = new StringBuffer ();
        
        appendSelect (buffer)
        if (distinct)
            appendDistinct (buffer)
        
        appendColumns (buffer)
        appendFrom (buffer)
        
        if (joins)
            appendJoins (buffer)
            
        if (where)
            appendWhere (buffer)

        if (groupBy)
            appendGroupBy (buffer)

        if (orderBy)
            appendOrderBy (buffer)
        
        appendEnd (buffer)
        
        return buffer.toString()
    }

    void appendSelect (StringBuffer buffer)
    {
        buffer << 'SELECT '
    }
    
    void appendDistinct (StringBuffer buffer)
    {
        buffer << 'DISTINCT '
    }
    
    void appendColumns (StringBuffer buffer)
    {
        String delim = ''
        for (col in columns)
        {
            buffer << delim
            delim = ', '
            
            buffer << col
        }
    }
    
    void appendFrom (StringBuffer buffer)
    {
        buffer << ' FROM '
        String delim = ''
        for (tableAlias in tables)
        {
            if (tableAlias.join)
                continue
            
            buffer << delim
            delim = ', '
            
            buffer << tableAlias
        }
    }
    
    void appendJoins (StringBuffer buffer)
    {
        for (join in joins)
        {
            buffer << join
        }
    }
    
    void appendWhere (StringBuffer buffer)
    {
        buffer << ' WHERE '
        buffer << where
    }
    
    void appendGroupBy (StringBuffer buffer)
    {
        buffer << ' GROUP BY '
        String delim = ''
        for (col in groupBy)
        {
            buffer << delim
            delim = ', '
            
            buffer << col.toWhere()
        }
    }
    
    void appendOrderBy (StringBuffer buffer)
    {
        buffer << ' ORDER BY '
        String delim = ''
        for (col in orderBy)
        {
            buffer << delim
            delim = ', '
            
            buffer << col.toWhere()
            buffer << ' '
            buffer << (col.ascending ? 'ASC' : 'DESC')
        }
    }
    
    void appendEnd (StringBuffer buffer)
    {
        // NOP
    }
    
    /** Execute the SELECT. The closure will receive the data as a list of objects
      * as defined in the rowType of the tables involved.
      * 
      * If the SELECT returns only a single table, the closure will receive only the
      * first element of this list for convenience (so you don't have to say "it[0]"
      * all the time).
      * 
      * If the SELECT takes parameters, they must be stored in the param property.
      */
    void execute (Closure closure)
    {
        execute (param, closure)
    }
    
    /** Execute the SELECT. The closure will receive the data as a list of objects
      * as defined in the rowType of the tables involved.
      * 
      * If the SELECT returns only a single table, the closure will receive only the
      * first element of this list for convenience (so you don't have to say "it[0]"
      * all the time).
      * 
      * The list of arguments will be passed to groovy.sql.Sql as parameters for the
      * prepared statement which is created.
      */
    void execute (args, Closure closure)
    {
        if (closure == null)
            throw new IllegalArgumentException ("closure is null")
        
        Sql sql = new Sql (DataSourceProvider.DEFAULT.getDataSource (db))
        def mapper = new SelectResultSet (this)
        def query = getSQL ()
        
        if (log.isDebugEnabled ())
        {
            log.debug ("Executing: ${query}")
            log.debug ("Parameters: ${args}")
        }
        
        int count = 0;
        maxRowsReached = false;
        
        try
        {
            sql.eachRow (query, args) {
                count ++;
                if (count > maxRows)
                    throw new MaxRowsReachedException (maxRows);
                
                mapper.it = it
                closure.call (mapper.getObjects())
            }
        }
        catch (MaxRowsReachedException e)
        {
            maxRowsReached = true;
        }
    }
    
    /** Execute the SELECT and pass the GroovyResultSet to the closure.
      * 
      * If the SELECT takes parameters, they must be stored in the param property.
      */
    void rawExecute (closure)
    {
        rawExecute (param, closure)
    }
    
    /** Execute the SELECT and pass the GroovyResultSet to the closure */
    void rawExecute (args, closure)
    {
        if (closure == null)
            throw new IllegalArgumentException ("closure is null")
        
        Sql sql = new Sql (DataSourceProvider.DEFAULT.getDataSource (db))
        def query = getSQL ()

        if (log.isDebugEnabled ())
        {
            log.debug ("Executing: ${query}")
            log.debug ("Parameters: ${args}")
        }

        int count = 0;
        maxRowsReached = false;
        
        try
        {
            sql.eachRow (query, args) {
                count ++;
                if (count > maxRows)
                    throw new MaxRowsReachedException (maxRows);
                
                closure.call (it);
            }
        }
        catch (MaxRowsReachedException e)
        {
            maxRowsReached = true;
        }
    }
}
