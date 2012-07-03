package org.javanicus.gsql;

import java.sql.Types

class SelectTest extends GroovyTestCase {

    void testSelect() {
        def s = new Select (database.event)
        s.all(database.event)
        
        s.where = new LIKE (s.whereColumn(database.event.description), '?')
        s.param << 'a%'
        
        assertEquals('SELECT t0.event_id AS event_id, t0.description AS description FROM event t0 WHERE (t0.description LIKE ?)', s.getSQL())
    }

    void testOR_LIKE_ISNULL() {
        def s = new Select (database.event)
        def sc = s.add(database.event.id)
        
        s.where = new OR (
            new LIKE (s.whereColumn(database.event.description), '?'),
            new IS_NULL (sc)
        )
        s.param << 'a%'
        
        assertEquals('SELECT t0.event_id AS event_id FROM event t0 WHERE ((t0.description LIKE ?) OR (t0.event_id IS NULL))', s.getSQL())
    }

    void testAND_NOTLIKE_ISNOTNULL() {
        def s = new Select (database.event)
        def sc = s.add(database.event.id)
        
        s.where = new AND (
            new NOT (new LIKE (s.whereColumn(database.event.description), '?')),
            new IS_NOT_NULL (sc)
        )
        s.param << 'a%'
        
        assertEquals('SELECT t0.event_id AS event_id FROM event t0 WHERE (NOT ((t0.description LIKE ?)) AND (t0.event_id IS NOT NULL))', s.getSQL())
    }

    void testINT() {
        def s = new Select (database.event)
        def sc = s.add(database.event.id)
        
        s.where = new WhereCondition (new INT(sc), '=', '?')
        s.param << 1
        
        assertEquals('SELECT t0.event_id AS event_id FROM event t0 WHERE (INT (t0.event_id) = ?)', s.getSQL())
    }

    void testRTRIM_LENGTH() {
        def s = new Select (database.event)
        s.add(database.event.id)
        def sc = s.add(database.event.description)
        sc.specialCode = 'RTRIM(${column})'
        
        s.where = new WhereCondition (new LENGTH(new RTRIM(sc)), '>', '?')
        s.param << 5
        
        assertEquals('SELECT t0.event_id AS event_id, RTRIM(t0.description) AS description FROM event t0 WHERE (LENGTH (RTRIM (t0.description)) > ?)', s.getSQL())
    }

    void testSUM() {
        def s = new Select (database.event)
        def sc = s.add(database.event.description)
        sc.specialCode = 'SUM(LENGTH(${column}))'
        
        assertEquals('SELECT SUM(LENGTH(t0.description)) AS description FROM event t0', s.getSQL())
    }
    
    void testIN() {
        def s = new Select (database.event)
        def sc = s.add(database.event.id)
        s.add(database.event.description)
        
        def values = [1,2,3,4,5]
        s.where = new IN (sc, ['?'] * values.size)
        s.param.addAll (values)
        
        assertEquals('SELECT t0.event_id AS event_id, t0.description AS description FROM event t0 WHERE (t0.event_id IN (?, ?, ?, ?, ?))', s.getSQL())
        assertEquals('[1, 2, 3, 4, 5]', s.param.toString())
    }
    
    void testNOT_IN() {
        def s = new Select (database.event)
        def sc = s.add(database.event.id)
        s.add(database.event.description)
        
        def values = [1,2,3,4,5]
        s.where = new NOT_IN (sc, ['?'] * values.size)
        s.param.addAll (values)
        
        assertEquals('SELECT t0.event_id AS event_id, t0.description AS description FROM event t0 WHERE (t0.event_id NOT IN (?, ?, ?, ?, ?))', s.getSQL())
        assertEquals('[1, 2, 3, 4, 5]', s.param.toString())
    }
    
    void testJoin() {
        // Get all descriptions of events with individuals which have a price > 5.0
        def s = new Select (database.event)
        def sc = s.add(database.event.description)
        
        // Join two tables
        def join = s.join(database.individual) { join ->
            // Define the condition which joins the two tables.
            // Select.whereColumn() will return the correct names for each column
            join.condition = new WhereCondition (s.whereColumn(database.event.id), '=', join.whereColumn(database.individual.event_id))
        }
        
        s.where = new WhereCondition (s.whereColumn(database.individual.price), '>', '?')
        s.param << 5.0
        
        assertEquals('SELECT t0.description AS description FROM event t0 JOIN xxx.individual t1 ON (t0.event_id = t1.event_id) WHERE (t1.price > ?)', s.getSQL())
        assertEquals('[5.0]', s.param.toString())
    }
    
    def typeMap
    def build
    def database
    def sqlGenerator
    
    void setUp() {
        typeMap = new TypeMap()          
        build = new RelationalBuilder(typeMap)
        sqlGenerator = new SqlGenerator(typeMap,System.getProperty( "line.separator", "\n" ))
                  
        database = build.database(name:'genealogy') {
          table(name:'event') {
              column(name:'event_id', groovyName:'id', type:Types.INTEGER, size:10, primaryKey:true, required:true)
              column(name:'description', type:Types.VARCHAR, size:30)          
          }
          table(schema:'xxx', name:'individual') {
            column(name:'individual_id', type:Types.INTEGER, size:10, required:true, primaryKey:true, autoIncrement:true)
            column(name:'surname', type:Types.VARCHAR, size:15, required:true)
            column(name:'event_id', type:Types.INTEGER, size:10)
            column(name:'size', type:Types.INTEGER, size:5)
            column(name:'price', type:Types.DECIMAL, size:'7,2')
            foreignKey(foreignTable:'event') {
                reference(local:'event_id',foreign:'event_id')
            }
            index(name:'surname_index') {
                indexColumn(name:'surname')
            }
          }
        }
    }
    
    def tearDown() {
        super.tearDown()
        
        typeMap = null
        build = null
        database = null
        sqlGenerator = null
    }
}
