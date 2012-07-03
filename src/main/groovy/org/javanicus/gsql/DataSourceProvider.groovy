package org.javanicus.gsql

import javax.sql.DataSource

/** Simple helper class to supply Select with a DataSource to work with. */
public abstract class DataSourceProvider
{
    public static DataSourceProvider DEFAULT
    
    public DataSource getDataSource (Database db)
    {
        return getDataSource (db.name)
    }
    
    public abstract DataSource getDataSource (String dbName)
}
