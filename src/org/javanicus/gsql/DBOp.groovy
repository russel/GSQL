package org.javanicus.gsql;

import javax.sql.DataSource
import java.sql.Connection

import groovy.sql.Sql
import groovy.sql.GroovyResultSet

class DBOp
{
	public void run (db, query)
	{
		run (db, query, [])
	}
	
	public void run (db, query, args)
	{
		boolean first = true
		
		if (db instanceof DataSource)
			db = new Sql (db)
		if (db instanceof Connection)
			db = new Sql (db)
		if (!(db instanceof Sql) )
			throw new RuntimeException ("Expected DataSource, Connection or Sql as first parameter but got "+db.dump())
		
		db.eachRow(query, args) {
			if (first)
			{
				first = false
				prepare (db, it)
			}
			
			processRow (it)
		}
		
		post (db)
	}
	
	/**
	 * Verarbeitung vorbereiten. Wird aufgerufen, sobald das erste ResultSet eintrifft.<p>
	 * 
	 * Wenn das ResultSet leer ist, wird diese Methode nicht aufgerufen.
	 * 
	 * @param conn
	 * @param firstRow
	 */
	public void prepare (Sql sql, GroovyResultSet firstRow)
	{
		
	}
	
	/**
	 * Ein Ergebnis aus der DB verarbeiten.<p>
	 * 
	 * Diese Methode wird auch für das erste ResultSet aufgerufen.
	 * 
	 * @param row
	 * @return false, wenn die Verarbeitung abgebrochen werden soll.
	 */
	public boolean processRow (GroovyResultSet row)
	{
		return true;
	}
	
	/**
	 * Diese Methode wird aufgerufen, nachdem alle ResultSets gelesen wurden oder
	 * processRow() false zurückgegeben hat.
	 * 
	 * Wenn das ResultSet leer ist, wird diese Methode nicht aufgerufen.
	 * 
	 * @param conn
	 */
	public void post (Sql sql)
	{
		
	}
}