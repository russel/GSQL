package org.javanicus.gsql;

import groovy.sql.GroovyResultSet

class SelectResultSet {
    /** GroovyResultSet from the DB */
    private GroovyResultSet it
    /** The Select which returned the GroovyResultSet */
    Select select
    /** The instances to fill with the data from the GroovyResultSet */
    List objects
    /** A list with the property to set for each column of the GroovyResultSet */
    List propertyPerColumn
    /** The property list for each instance (constructor argument for DynaObject). */
    Map  propertiesPerType
    /** The mapper to call for each column. null, if there is no mapper for a column. */
    List mapper
    /** Which column maps to which object */
    List objectIndexPerColumn
    /** Key: index in the list objects, Value: class of the instance to create */
    Map objectTypes

    public SelectResultSet (Select select)
    {
        this.select = select
        this.objects = []
    }
    
    public void setIt (GroovyResultSet it)
    {
        if (this.it != it)
        {
            this.it = it
            remap ()
        }
        copyData ()
    }
    
    /**
     * Update the caches.
     * 
     * This will fill the many cache lists and maps to the copying of the data from the GroovyResultSet
     * into the instances is fast.
     */
    public void remap ()
    {
//        println 'remap'
        propertyPerColumn = []
         mapper = []
        propertiesPerType = [:]
        objectIndexPerColumn = []
        objectTypes = [:]

        Map objectIndex = [:]
        for (sc in select.columns)
        {
            // Collect the property names
            propertyPerColumn += sc.column.groovyName
            // and mappers
            mapper += sc.column.mapper
            
            // Find the type for this column
            Class type = sc.tableAlias.table.rowType
            if (type == null) {
                throw new IllegalArgumentException ("rowType ist für ${sc.tableAlias.table.name} nicht definiert")
            }
            
            // Check if we already encountered this type
            Integer index = objectIndex[type]
            if (index == null)
            {
                // If not, add it to the list objectIndex
//                println "type=${type}"
                index = objectIndex.size()
//                println "index=${index}"
                objectIndex[type] = index
                objectTypes[index] = type
                
                // Remember the table. We'll figure out the list of properties later
                propertiesPerType[type] = sc.tableAlias.table
            }
            
            // Append the index to the "column index->object" map, so we can look up the destination of the data later
            objectIndexPerColumn << index
        }

        // propertiesPerType contains the tables. Convert this to a list of Groovy properties.
        for (e in propertiesPerType)
        {
            Table table = e.value
            List list = []
            for (col in table.columns)
            {
                list << col.groovyName
            }
            e.value = list
        }
//        println "objectIndex=${objectIndex}"
//        println "objectTypes=${objectTypes}"
//        println "objectIndexPerColumn=${objectIndexPerColumn}"
//        println "propertiesPerType=${propertiesPerType}"
    }
    
    public void copyData ()
    {
        // First, create a new set of objects where we can store the data
        objects = []
        def properties = []
        for (type in objectTypes)
        {
            List pl = propertiesPerType[type.value]
//            println properties
            def obj = type.value.newInstance (pl)
            objects[type.key] = obj
            properties[type.key] = obj.getProperties ()
        }
//        println "objects=${objects}"
        
        // Copy the data for each column into the right object
        for (col in 0..<objectIndexPerColumn.size)
        {
            Object value = it[col]
            if (mapper[col])
                value = mapper[col].fromDB (value)

            def idx = objectIndexPerColumn[col]
            properties[idx].put (propertyPerColumn[col], value)
        }
//        println objects
    }
    
    /**
     * Helper method: Get the list of objects we just filled with data from the DB.
     * If the list contains just a single object, return that.
     */
    def getObjects ()
    {
        if (objects.size == 1)
            return objects[0]
        
        return objects
    }
    
    /**
     * This allows to use this instance like GroovyResultSet: You can access any column data as a property.
     */
    public Object getProperty(String propertyName) {
        try {
            return super.getProperty(propertyName);
        } catch (MissingPropertyException e) {
            SelectColumn sc = findColumn(propertyName);
            if (sc == null)
                throw e
            return it.getProperty (sc.column.name)
        }
    }
    
    public SelectColumn findColumn(String aName)
    {
        return select.columns.find() {
            it.column.groovyName.equals (aName) || it.alias.equalsIgnoreCase (aName)
        }
    }

    /** Convert the data of the current row into a string. */
    public String toString()
    {
        def buffer = new StringBuffer ();
        buffer << super.toString()
        buffer << " ("
        
        String delim = ""
        for (sc in select.columns)
        {
            buffer << delim
            delim = ", "
            
            SelectColumn col = sc.column
            buffer << col.groovyName
            buffer << "="
            
            def value = it.getProperty(col.name)
            boolean isText = col.typeMap.isTextType(col.type) && value != null
            if (isText)
                buffer << "'"
            
            buffer << value
            
            if (isText)
                buffer << "'"
        }
        buffer << ")"
        
        return buffer.toString()
    }
}
