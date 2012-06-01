package org.javanicus.gsql

import java.sql.Types

class ColumnTest extends GroovyTestCase {
    def typeMap
    def column
    def idColumn
    def nameColumn
    def yearOfBirthColumn          
              
    void setUp() {
        TypeMap typeMap = new TypeMap()          
        column = new Column(typeMap,size:"10,2")
        idColumn = new Column(typeMap,name:"ghx_id",groovyName:"id",type:0,size:10,required:true,required:true,autoIncrement:true)     
        nameColumn = new Column(typeMap,name:"name",type:"VARCHAR",size:"250",required:true,defaultValue:"<no name>")
        yearOfBirthColumn = new Column(typeMap,name:"year of birth",groovyName:"yearOfBirth",type:Types.INTEGER,size:"4")
    }
    
    void testPrimaryKey() {
        assert false == column.isPrimaryKey()
    }
    
    void testDBName() {
        assert "ghx_id" == idColumn.name
        assert "id" == idColumn.groovyName
    }
    
    void testSizeAndScale() {
        assert 10 == column.size
        assert 2 == column.scale
        assert 250 == nameColumn.size          
        assert 0 == nameColumn.scale          
        assert 4 == yearOfBirthColumn.size          
        assert 0 == yearOfBirthColumn.scale          
    }
    
    void testTypeNameHasBeenInitialisedWhenOnlyColumnTypeHasBeenSet() {
        assertEquals ("NULL", idColumn.type)
    }
    
    void testNameTypes() {
        assert "VARCHAR" == nameColumn.type
        assert Types.VARCHAR == nameColumn.typeCode
    }
    
    void testYearTypes() {
        assertEquals("INTEGER", yearOfBirthColumn.type)
        assert Types.INTEGER == yearOfBirthColumn.typeCode
    }
}
