package org.javanicus.gsql.db2;

import org.javanicus.gsql.Select
import org.javanicus.gsql.DataSourceProvider

class DB2Select extends Select {
	boolean withUR 

	DB2Select (db) {
	    super (db)
	}
	
	void appendEnd (StringBuffer buffer) {
		if (isDB2 ()) {
			if (maxRows > 0 && maxRows != Integer.MAX_VALUE) {
				buffer << " FETCH FIRST ${maxRows} ROWS ONLY"
			}

			if (withUR) {
				buffer << " WITH UR"
			}
		}
	}

	final static int IS_DB2_NOT_SET = 0
	final static int IS_DB2 = 1
	final static int IS_NOT_DB2 = 2
	int isDB2
	
	boolean isDB2 () {
		if (isDB2 == IS_DB2_NOT_SET) {
			def ds = DataSourceProvider.DEFAULT.getDataSource (db)
			isDB2 = (ds.getClass().getName().contains(".ibm.") ? IS_DB2 : IS_NOT_DB2);
		}
		return isDB2 == IS_DB2
	}

}