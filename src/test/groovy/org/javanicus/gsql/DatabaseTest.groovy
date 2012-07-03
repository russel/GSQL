package org.javanicus.gsql

class DatabaseTest extends GroovyTestCase {
    def typeMap
    def table
    def idColumn
    def fred
    def barney
    def wilma
    def db1
    def db2
              
    void setUp() {
        typeMap = new TypeMap()          
        idColumn = new Column(typeMap,name:"id",type:0,size:10,required:true,autoIncrement:true)
                  
        fred = new Table("fred")
        fred.columns << idColumn
                  
        barney = new Table("barney")
        barney.columns << idColumn          
                  
        wilma = new Table("wilma")
        wilma.columns << idColumn
                  
        db1 = new Database("db1")
        db1.tables << fred
        db1.tables << barney
                  
        db2 = new Database("db2")
        db2.tables << wilma
        db2.tables << barney
    }
    
    void testMergeDatabasesTogether() {
        db1.mergeWith(db2);
        assert 3 == db1.tables.size()
        assert db1.tables.contains(fred)
        assert db1.tables.contains(barney)
        assert null != db1.tables.find(){it.name == "wilma"}
    }
}
