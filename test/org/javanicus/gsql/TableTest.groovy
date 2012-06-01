package org.javanicus.gsql

class TableTest extends GroovyTestCase {
    def typeMap
    def table
    def idColumn
    def nameColumn
              
    void setUp() {
        typeMap = new TypeMap()          
        idColumn = new Column(typeMap,name:"id",type:0,size:10,required:true,primaryKey:true,autoIncrement:true)     
        nameColumn = new Column(typeMap,name:"name",type:0,size:"250",required:true,defaultValue:"<no name>")
        table = new Table("wheelbarrow")
        table.schema = 'test'
        table.addColumn(idColumn)          
        table.addColumn(nameColumn)     
    }
    
    void testPrimaryKey()
    {
        // @todo wouldn't it be groovy to have the inverse of
        // the contains() method, on Object, such that you
        // could express "if idColumn.isIn(table.primaryKeyColumns) {"
        // i.e. add to DefaultGroovyMethods something along the
        // lines of public static boolean isIn(Object obj,Collection cltn) ...
        
        assert table.primaryKeyColumns.contains(idColumn)
    }
    
    void testFindColumnUsingCaseInsensitiveName() {
        assert nameColumn == table.findColumn("NaMe")
    }
}
