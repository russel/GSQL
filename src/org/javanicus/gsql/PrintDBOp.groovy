package org.javanicus.gsql;

import java.sql.Timestamp
import java.sql.Time
import java.sql.Date

import groovy.sql.Sql
import groovy.sql.GroovyResultSet

class PrintDBOp extends DBOp
{
	int maxRows = Integer.MAX_VALUE
	boolean dumpMetaData = false
	boolean trim = false
	int count
	int numColumns
	String delimiter = ","

	public void prepare(Sql sql, GroovyResultSet firstRow)
	{
		numColumns = firstRow.getMetaData().getColumnCount();
		
		if (dumpMetaData)
			println DBUtil.toString (firstRow.getMetaData());
	}
	
	public boolean processRow(GroovyResultSet row)
	{
		StringBuffer buffer = new StringBuffer (1024);
		addRowData(row, buffer);
		
		printRow (buffer.toString());
		
		count ++;
		return count < maxRows;
	}

	public void printRow (String row)
	{
		println row
	}

	/**
	 * Insert the columns in the row to the buffer.
	 * 
	 * @param row
	 * @param buffer
	 * @throws SQLException
	 */
	protected void addRowData(GroovyResultSet row, StringBuffer buffer)
	{
		for (int i in 0..<numColumns)
		{
			if (!columnFilter (row, i))
				continue;
			
			if (i != 0)
				buffer.append (delimiter);
			
			Object o = row[i];
			buffer.append(convert (i, o));
		}
	}

	/**
	 * This method is called for every column. It is used to convert the object
	 * in the column into a string which is appended to the buffer. 
	 * 
	 * @param column The number of the column (starting with 0)
	 * @param o The object in the column
	 */
	protected String convert (int column, Object o)
	{
		if (o == null)
			return ("null");
		else if (o instanceof String
				|| o instanceof Timestamp
				|| o instanceof Time
				|| o instanceof Date)
		{
			o = o.toString()
			if (trim)
				o = o.trim()
			return "'${o}'";
		}

		return o.toString();
	}

	/**
	 * Simple column filter. Return false for every column you don't want to see in the result.
	 * 
	 * @param row
	 * @param i
	 * @return
	 */
	protected boolean columnFilter(GroovyResultSet row, int i)
	{
		return true;
	}
  
}