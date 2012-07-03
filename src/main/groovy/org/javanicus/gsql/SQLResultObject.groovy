package org.javanicus.gsql;

/** Use this as the base class if you need to read data from the database and you don't already a POJO */
class SQLResultObject extends DynaObject
{
    SQLResultObject (Collection properties)
    {
        super (properties)
    }
    
    /** Is a property NULL? */
    boolean isDefined (String property)
    {
        return getProperties ().containsKey (property)
    }
}
