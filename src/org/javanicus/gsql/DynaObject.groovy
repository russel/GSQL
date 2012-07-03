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
    LinkedHashSet __do_propertySet
    /** The real values */
    Map __do_values

    DynaObject (Collection properties) {
        this.__do_propertySet = new HashSet ()
        this.__do_propertySet.addAll (properties)
        this.__do_values = [:]
    }
    
    void setProperty(String property, Object value) {
        if (__do_propertySet.contains(property)) {
            __do_values[property] = value
        } else {
            getMetaClass().setProperty(getClass(), this, property, value, false, true)
        }
    }
    
    Object getProperty(String property) {
        if (__do_propertySet.contains(property)) {
            return __do_values[property]
        }
        
        return getMetaClass().getProperty(getClass(), this, property, false, true)
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
        for (property in __do_propertySet) {
            buffer << delim
            delim = ', '
            buffer << property
            buffer << '='
            def value = toString (getProperties().get(property))
            buffer << value
        }
	additionalPropertiesToString (buffer)
        buffer << ')'
        return buffer.toString()
    }

    /**
     * Derived classes can override this method to append additional properties
     * to the result of toString()
     */
    void additionalPropertiesToString (StringBuffer buffer) {
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
        for (propName in __do_propertySet) {
            // TODO Ignore missing properties?
            setProperty (propName, other.getProperty (propName))
        }
    }
    
    /** Getter for the properties because it's not possible to use "obj.__do_propertySet" */
    Set getPropertySequence () {
        return __do_propertySet
    }
    
    Map getProperties() {
        return __do_values
    }
    
    /** Clone this instance */
    def clone () {
        Class c = this.getClass()
        DynaObject o = c.newInstance (__do_propertySet)
        o.getProperties ().putAll (__do_values)
        return o
    }
}
    
