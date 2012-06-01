package org.javanicus.gsql;

class MaxRowsReachedException extends RuntimeException
{
    public MaxRowsReachedException (int count)
    {
        super ("Maximum of "+count+" rows fetched from database.");
    }
}
