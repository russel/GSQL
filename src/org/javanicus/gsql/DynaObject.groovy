package org.javanicus.gsql

/** This is like Expando but it keeps track of the sequence of properties/fields
 *  and it knows if a property has been set.
 *  
 *  Keeping track of the order of properties is essential when toString() is used
 *  in test cases with different versions of Java or different options to the
 *  optimizer.
 */
class DynaObject {
    /** Ordered set for quick lookup if a propery belongs to this class. */
    LinkedHashSet propertySet
    /** The real values */
    Map values

    DynaObject (Collection properties) {
        this.propertySet = new HashSet ()
        this.propertySet.addAll (properties)
        this.values = [:]
    }
    
    void setProperty(String property, Object value) {
        if (propertySet.contains(property)) {
            values[property] = value
        } else {
            getMetaClass().setProperty(getClass(), this, property, value, true, true)
        }
    }
    
    Object getProperty(String property) {
        if (propertySet.contains(property)) {
            return values[property]
        }
        
        return getMetaClass().getProperty(getClass(), this, property, true, true)
    }
    
    /** Dump this object into a String. The result will contain all values of all properties in the order
     *  in which they have been defined in the constructor.
     */
    String toString () {
        def buffer = new StringBuffer ()
        buffer << getClass().name
        buffer << '@'
        buffer << hashCode()
        def delim = ' ('
        for (property in propertySet) {
            buffer << delim
            delim = ', '
            buffer << property
            buffer << '='
            def value = toString (getProperties().get(property))
            buffer << value
        }
        buffer << ')'
        return buffer.toString()
    }
    
    String toString (value) {
        if (value == null)
            return 'null'
        
        if (value instanceof String)
            return "\"${value}\""
        
        return value.toString()
    }
    
    /** Copy all properties of another object into this one. */
    void copy (other) {
        for (propName in propertySet) {
            // TODO Ignore missing properties?
            setProperty (propName, other.getProperty (propName))
        }
    }
    
    /** Getter for the properties because it's not possible to use "obj.propertySet" */
    Set getPropertySequence () {
        return propertySet
    }
    
    Map getProperties() {
        return values
    }
    
    /** Clone this instance */
    def clone () {
        Class c = this.getClass()
        DynaObject o = c.newInstance (propertySet)
        o.getProperties ().putAll (values)
        return o
    }
}
    
