package org.javanicus.gsql;


/** Simple mapper which will remove any extra blanks at the end of a DB value
 *  when reading data from the DB or pad the values to the full length when
 *  writing them back into DB.
 */
class TrimDBMapper implements DBMapper
{
    int length
    
    public TrimDBMapper ()
    {
    }
    
    void init (IColumn column)
    {
        this.length = column.size
    }
    
    def fromDB (dbValue)
    {
        return dbValue ? dbValue.toString().trim() : dbValue
    }
    
    def toDB (javaValue)
    {
        return javaValue == null ? javaValue : javaValue.padRight(length)
    }
}
