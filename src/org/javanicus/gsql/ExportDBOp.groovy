package org.javanicus.gsql;

import groovy.sql.Sql
import org.javanicus.gsql.Table

class ExportDBOp extends PrintDBOp
{
	boolean append = false
	File    baseDir = new File ("test/test_db")
	
	PrintStream _printer;
	int count = 0;
	File	outputFile;
	String schema
	String table
	
	ExportDBOp(Table table)
	{
		this ()
		
		this.schema = table.schema
		this.table = table.name
	}
	
	ExportDBOp()
	{
		maxRows = 10000
		delimiter = ";"
	}
	
	File getFile (String schema, String table)
	{
		return new File (getDir (schema), table + ".dbe");
	}
	
	File getDir (String schema)
	{
		return new File (baseDir, schema);
	}
	
	PrintStream getPrinter ()
	{
		if (_printer == null)
		{
			outputFile = getFile (schema, table);
			outputFile.getParentFile().mkdirs();
			
			FileOutputStream out = new FileOutputStream (outputFile, append);
			_printer = new PrintStream (out);
		}
		
		return _printer
	}
	
	protected String convert (int column, Object o)
	{
		if (o == null)
			return "";
		
		String value = o.toString().trim();
		if (value.length() == 0)
			value = "\" \"";
		else if (value.indexOf(',') != -1)
			value = "\"" + value.replaceAll (/"/, '""') + "\"";
		
		return value;
	}

	void printRow (String row)
	{
		printer.println(row);
		count ++;
	}
	
	void post (Sql sql)
	{
		printer.close();
		System.out.println("Wrote "+count+" records to "+outputFile.getAbsolutePath());
	}
}
