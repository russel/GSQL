package org.javanicus.gsql;

public interface DBMapper
{
    void init (IColumn column);
    Object fromDB (Object dbValue);
    Object toDB (Object value);
}
