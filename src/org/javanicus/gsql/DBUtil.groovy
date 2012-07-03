package org.javanicus.gsql;

import java.sql.ResultSetMetaData
import java.sql.Connection
import java.sql.Statement
import java.sql.PreparedStatement
import java.sql.ResultSet

import org.apache.log4j.Logger;

import groovy.sql.GroovyResultSet
import groovy.sql.GroovyResultSetProxy

class DBUtil
{
	public static final Logger log = Logger.getLogger(DBUtil.class);

	static java.sql.Date toSQLDate (long date)
	{
		return new java.sql.Date (date)
	}
	
	static java.sql.Date toSQLDate (java.sql.Date date)
	{
		return date
	}
	
	static java.sql.Date toSQLDate (Date date)
	{
		if (date == null)
			return null
		
		return new java.sql.Date (date.getTime ())
	}
	
	static java.sql.Time toSQLTime (long date)
	{
		return new java.sql.Time (date)
	}
	
	static java.sql.Time toSQLTime (java.sql.Date date)
	{
		return date
	}
	
	static java.sql.Time toSQLTime (Date date)
	{
		if (date == null)
			return null
		
		return new java.sql.Time (date.getTime())
	}
	
	static java.sql.Timestamp toSQLTimestamp (long date)
	{
		return new java.sql.Time (date)
	}
	
	static java.sql.Timestamp toSQLTimestamp (java.sql.Timestamp date)
	{
		return date
	}
	
	static java.sql.Timestamp toSQLTimestamp (Date date)
	{
		if (date == null)
			return null
		
		return new java.sql.Timestamp (date.getTime())
	}
	
	static String toString (ResultSetMetaData md)
	{
		def l = []
        for (i in 1..md.columnCount)
        {
        	l += md.getColumnName (i)
        }
		return l.join (', ')
	}
	
	static String toString (GroovyResultSetProxy rs)
	{
	    return rs == null ? "null" : toString(rs.getImpl())
	}
	
	static String toString (GroovyResultSet rs)
	{
		def result = new StringBuffer ()
		// BUG result << rs.toString ()
		result << "groovy.sql.GroovyResultSet@unknown ("
		def delim = ''
		def md = rs.getMetaData()
        for (i in 1..md.columnCount)
        {
        	result << delim
        	delim = ', '
        	result << md.getColumnName (i)
        	result << "="
        	result << rs.getObject (i).inspect()
        }
		result << ")"
		return result.toString()
	}
	
	static ResultSet close (ResultSet x)
	{
		try
		{
			if (x != null)
				x.close();
		}
		catch (Exception e)
		{
			log.warn("Error closing ResultSet", e);
		}
		return null;
	}

	static Statement close (Statement x)
	{
		try
		{
			if (x != null)
				x.close();
		}
		catch (Exception e)
		{
			log.warn("Error closing Statement", e);
		}
		return null;
	}
	
	static Connection close(Connection x)
	{
		try
		{
			if (x != null)
				x.close();
		}
		catch (Exception e)
		{
			log.warn("Error closing Connection", e);
		}
		return null;
	}

	static void rollback (Connection x)
	{
		try
		{
			if (x != null)
				x.rollback ();
		}
		catch (Exception e)
		{
			log.warn("Error rolling back transaction", e);
		}
	}
	
	static void executeQuery (PreparedStatement statement, List param, Closure closure)
	{
		int i = 1
		for (value in param)
		{
			statement.setObject (i ++, value)
		}
		
		ResultSet results = null
		try
		{
	        results = statement.executeQuery();
	
	        GroovyResultSet groovyRS = new GroovyResultSetProxy(results).getImpl();
	        while (groovyRS.next()) {
	            closure.call(groovyRS);
	        }
		}
		finally
		{
			results = close (results)
		}
	}
	
	static boolean execute (PreparedStatement statement, List param)
	{
		int i = 1
		for (value in param)
		{
			statement.setObject (i ++, value)
		}
		
		return statement.execute ();
	}

	static int executeUpdate (PreparedStatement statement, List param)
	{
		int i = 1
		for (value in param)
		{
			statement.setObject (i ++, value)
		}
		
		return statement.executeUpdate ();
	}
}