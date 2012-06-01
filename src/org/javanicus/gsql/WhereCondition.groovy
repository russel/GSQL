package org.javanicus.gsql;


class WhereCondition
{
    def v1
    def op
    def v2
    def value
    
    public WhereCondition(v1, op, v2)
    {
        this.v1 = v1
        this.op = op
        this.v2 = v2
    }
    
    public WhereCondition(op, value)
    {
        this.op = op
        this.value = value
    }
    
    WhereCondition ()
    {
    }
    
    public String toString ()
    {
        if (value != null)
        {
            if (!value)
                return ''
                
            return "${op} (${toString (value)})"
        }
        else
        {
            def s1 = toString (v1)
            def s2 = toString (v2)
            
            if (v2 instanceof List)
                return "(${s1} ${op} (${s2}))"
            
            return "(${s1} ${op} ${s2})"
        }
    }
    
    public String toString (v)
    {
        if (v == null)
            return "NULL"

        if (v instanceof SelectColumn)
            return v.toWhere()
        if (v instanceof Select)
            return v.getSQL ()
        
        if (v instanceof List)
        {
            def result = new StringBuffer ()
            def delim = ''
            for (value in v)
            {
                result << delim
                delim = ', '
                result << toString (value)
            }
            return result.toString ()
        }
            
        return v.toString()
    }
}

class INT extends WhereCondition
{
    INT (value)
    {
        super ('INT', value)
    }
}

class NOT extends WhereCondition
{
    NOT (value)
    {
        super ('NOT', value)
    }
}

class LTRIM extends WhereCondition
{
    LTRIM (value)
    {
        super ('LTRIM', value)
    }
}

class RTRIM extends WhereCondition
{
    RTRIM (value)
    {
        super ('RTRIM', value)
    }
}

class SUM extends WhereCondition
{
    SUM (value)
    {
        super ('SUM', value)
    }
}

class LENGTH extends WhereCondition
{
    LENGTH (value)
    {
        super ('LENGTH', value)
    }
}

class IN extends WhereCondition
{
    IN (col, list)
    {
        super (col, 'IN', list)
    }
}

class NOT_IN extends WhereCondition
{
    NOT_IN (col, list)
    {
        super (col, 'NOT IN', list)
    }
}

class IS_NULL extends WhereCondition
{
    IS_NULL (value)
    {
        super (value, 'IS', 'NULL')
    }
}

class IS_NOT_NULL extends WhereCondition
{
    IS_NOT_NULL (value)
    {
        super (value, 'IS NOT', 'NULL')
    }
}

class LIKE extends WhereCondition
{
    public LIKE (v1, v2)
    {
        super (v1, "LIKE", v2)
    }
}

class WhereCondChain extends WhereCondition
{
    WhereCondChain (v1, op, v2)
    {
        super (v1, op, v2)
    }
    
    WhereCondChain (op, list)
    {
        super (op, list)
    }
    
    
    public String toString ()
    {
        if (value != null)
        {
            if (!value)
                return ''
            else if (value.size() == 1)
                return toString (value[0])
            
            def result = new StringBuffer ()
            result << '('
            def delim = ''
            def delim2 = " ${op} "
            for (v in value)
            {
                result << delim
                delim = delim2
                result << toString (v)
            }
            result << ')'
            return result.toString ()
        }
        else
            return super.toString ()
    }
}

class AND extends WhereCondChain
{
    public AND (v1, v2)
    {
        super (v1, "AND", v2)
    }
    
    public AND (List values)
    {
        super ('AND', values)
    }
}

class OR extends WhereCondChain
{
    public OR (v1, v2)
    {
        super (v1, "OR", v2)
    }
    
    public OR (List values)
    {
        super ('OR', values)
    }
}
