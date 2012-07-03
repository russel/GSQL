package org.javanicus.gsql;

import java.sql.Connection
import java.sql.DriverManager
import java.sql.Types
import javax.sql.DataSource
import org.hsqldb.jdbc.jdbcDataSource

class SelectResultSetTest extends GroovyTestCase {

    void testA_All() {
        def select = new Select(db)
        select.all(db.a)
        
        assertEquals('SELECT t0.A_ID AS A_ID, t0.b AS b, t0.l AS l, t0.c AS c, t0.BIG_INT AS BIG_INT, t0.BIG_DEC AS BIG_DEC, t0.s AS s FROM xxx.a t0', select.getSQL())
        
        def results = []
        select.execute() {
            String s = it.toString()
            s = SQLResultObjectTest.fixDynaObject(s)
            results << s
        }
        assertEquals ('''\
org.javanicus.gsql.SelectResultSetTest_A@cafe (id=1, b=true, l=2147483648, c="x", bigInt=2147483648, bigDec=123456789.1234, s="a1")
org.javanicus.gsql.SelectResultSetTest_A@cafe (id=2, b=false, l=4294967296, c="y", bigInt=4294967296, bigDec=0.0001, s="a2")
org.javanicus.gsql.SelectResultSetTest_A@cafe (id=3, b=true, l=-9223372036854775808, c="z", bigInt=-9223372036854775808, bigDec=999999999.9999, s="a3  ")\
'''.replaceAll('\r\n', '\n'), results.join('\n'))
    }

    void testA_ById() {
        def select = new Select(db)
        select.all(db.a)
        select.where = new WhereCondition (select.whereColumn(db.a.id), '=', '?')
        select.param = [2]
        
        assertEquals('SELECT t0.A_ID AS A_ID, t0.b AS b, t0.l AS l, t0.c AS c, t0.BIG_INT AS BIG_INT, t0.BIG_DEC AS BIG_DEC, t0.s AS s FROM xxx.a t0 WHERE (t0.A_ID = ?)', select.getSQL())
        assertEquals('[2]', select.param.toString())
        
        def results = []
        select.execute() {
            String s = it.toString()
            s = SQLResultObjectTest.fixDynaObject(s)
            results << s
        }
        assertEquals ('''\
org.javanicus.gsql.SelectResultSetTest_A@cafe (id=2, b=false, l=4294967296, c="y", bigInt=4294967296, bigDec=0.0001, s="a2")\
'''.replaceAll('\r\n', '\n'), results.join('\n'))
    }

    void testToString() {
        def select = new Select(db)
        select.all(db.a)
        select.where = new WhereCondition (select.whereColumn(db.a.id), '=', '?')
        select.param = [2]

        String s;
        select.rawExecute() {
            s = DBUtil.toString(it)
	}

        assertEquals ('''\
groovy.sql.GroovyResultSet@unknown (A_ID=2, B="No", L=4294967296, C="y", BIG_INT=4294967296, BIG_DEC=0.0001, S="a2")\
'''.replaceAll('\r\n', '\n'),
                SQLResultObjectTest.fixDynaObject(s))
    }

    void testToString2() {
        def select = new Select(db)
        select.all(db.a)
        select.where = new WhereCondition (select.whereColumn(db.a.id), '=', '?')
        select.param = [2]

	String s;
        select.rawExecute() {
            // BUG: it.toString() throws groovy.lang.MissingMethodException: No signature of method: groovy.sql.GroovyResultSet.toString() is applicable for argument types: () values: {}
            s = "${it}"
	}

        assertEquals ('''\
[A_ID:2, B:No, L:4294967296, C:y, BIG_INT:4294967296, BIG_DEC:0.0001, S:a2]\
'''.replaceAll('\r\n', '\n'),
		SQLResultObjectTest.fixDynaObject(s))
    }
    
    // Test whether the tool can distinguish A.S and B.S
    void testAB() {
        def select = new Select(db)
        select.add(db.a.s)
        select.add(db.b.s)
        // db.b.name is the name String of the table 'b'
        select.add(db.b.findColumn('name'))
        
        select.join(db.b) { join ->
            join.condition = new WhereCondition(select.whereColumn(db.a.id), '=', select.whereColumn(db.b.id))
        }
        
        select.where = new WhereCondition (select.whereColumn(db.a.id), '>', '?')
        select.param = [1]
        
        assertEquals('SELECT t0.s AS s, t1.s AS s, t1.name AS name FROM xxx.a t0 JOIN yyy.b t1 ON (t0.A_ID = t1.id) WHERE (t0.A_ID > ?)', select.getSQL())
        assertEquals('[1]', select.param.toString())
        
        def results = []
        select.execute() {
            String s = it[0].toString()
            s = SQLResultObjectTest.fixDynaObject(s)
            results << s
            
            s = it[1].toString()
            s = SQLResultObjectTest.fixDynaObject(s)
            results << s
        }
        assertEquals ('''\
org.javanicus.gsql.SelectResultSetTest_A@cafe (id=null, b=null, l=null, c=null, bigInt=null, bigDec=null, s="a2")
org.javanicus.gsql.SelectResultSetTest_B@cafe (id=null, name="b2", s="b2 text")\
'''.replaceAll('\r\n', '\n'), results.join('\n'))
    }
    
    Connection c
    Database db
    
    void setUp () {
        DataSourceProvider.DEFAULT = new HSLDBDataSourceProvider ()
//        Class.forName("org.hsqldb.jdbcDriver")
//        c = DriverManager.getConnection("jdbc:hsqldb:mem:aname", "sa", "")
        
        def build = new RelationalBuilder(new TypeMap())
        
        db = build.database(name:'fred') {
            table(schema:'xxx', name:'a', rowType:SelectResultSetTest_A.class) {
                column(name:'A_ID', groovyName:'id', type:Types.INTEGER, required:true, primaryKey:true, autoIncrement:true)
                column(name:'b', type:Types.VARCHAR, size:3, mapper:new BooleanMapper('Yes', 'No'))
                column(name:'l', type:Types.BIGINT)
                column(name:'c', type:Types.CHAR)
                column(name:'BIG_INT', groovyName:'bigInt', type:Types.BIGINT)
                column(name:'BIG_DEC', groovyName:'bigDec', type:Types.DECIMAL, size:'13,4')
                column(name:'s', type:Types.VARCHAR, size:250)
            }
            
            table(schema:'yyy', name:'b', rowType:SelectResultSetTest_B.class) {
                column(name:'id', type:Types.BIGINT)
                column(name:'name', type:Types.VARCHAR, size:250)
                column(name:'s', type:Types.VARCHAR, size:250)
            }
        }
        
        c = DataSourceProvider.DEFAULT.getDataSource(db).getConnection()
        
        def sqlGenerator = new SqlGenerator(build.typeMap, System.getProperty( "line.separator", "\n" ))
        def sql = new StringWriter()
        sqlGenerator.writer = sql
        sqlGenerator.createDatabase(db, false)
        
        c.createStatement().execute('CREATE SCHEMA xxx AUTHORIZATION dba')
        c.createStatement().execute('CREATE SCHEMA yyy AUTHORIZATION dba')
        sql = sql.toString()
//        println sql
        c.createStatement().execute(sql)
        
        def l = 1L << 31
        sql = "INSERT INTO xxx.a VALUES (1, \'Yes\', ${l}, 'x', ${l}, 123456789.1234, 'a1')"
        c.createStatement().execute(sql)
        l = 1L << 32
        sql = "INSERT INTO xxx.a VALUES (2, \'No\', ${l}, 'y', ${l}, 0.0001, 'a2')"
        c.createStatement().execute(sql)
        l = 1L << 63
        sql = "INSERT INTO xxx.a VALUES (3, \'Yes\', ${l}, 'z', ${l}, 999999999.9999, 'a3  ')"
        c.createStatement().execute(sql)
        
        sql = "INSERT INTO yyy.b VALUES (1, 'b1', 'b1 text')"
        c.createStatement().execute(sql)
        sql = "INSERT INTO yyy.b VALUES (2, 'b2', 'b2 text')"
        c.createStatement().execute(sql)
    }
    
    void tearDown () {
        if (c != null) {
            c.createStatement().execute("shutdown")
            c.close ()
        }
    }
}

class HSLDBDataSourceProvider extends DataSourceProvider {
    DataSource getDataSource (String dbName) {
        DataSource ds = new jdbcDataSource()
        ds.database = 'jdbc:hsqldb:mem:aname'
        ds.user = 'sa'
        ds.password = ''
        return ds
    }
}

class SelectResultSetTest_A extends SQLResultObject {
    int id;
    boolean b;
    long l;
    char c;
    BigInteger bigInt;
    BigDecimal bigDec;
    String s;
    
    SelectResultSetTest_A (Collection p) {
        super (p)
    }
}

class SelectResultSetTest_B extends SQLResultObject {
    long id;
    String name;
    String s;

    SelectResultSetTest_B (Collection p) {
        super (p)
    }
}
