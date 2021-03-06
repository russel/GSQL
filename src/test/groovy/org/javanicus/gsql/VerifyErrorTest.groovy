package org.javanicus.gsql;

class VerifyErrorTest extends GroovyTestCase {
    void testBug() {
	// This bug was fixed in Groovy 1.5
	// In Groovy 1.0, you would get
        //java.lang.VerifyError: (class: org/javanicus/gsql/VerifyErrorDemo, method: setProperty signature: (Ljava/lang/String;Ljava/lang/Object;)V) Inconsistent stack height 1 != 0
	// after activating the next line:
        //def o = new VerifyErrorDemo ()
    }
}

class VerifyErrorDemo {
    Set propertySet

    void setProperty(String property, Object value) {
        if (propertySet.contains(property))
        {
            values[property] = value
//            return
        }
        else
        {
            /* This line seems to be the culprit */
            super.setProperty (property, value)
        }
    }
}
