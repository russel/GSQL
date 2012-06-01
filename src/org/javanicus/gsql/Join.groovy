package org.javanicus.gsql;


class Join
{
    /** Join type */
    String type = 'JOIN'
    /** Table to be joined */
    TableAlias alias
    /** The join columns/condition */
    def condition
    
    String toString () {
        return " ${type} ${alias} ON ${condition}"
    }
}
