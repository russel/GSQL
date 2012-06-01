package org.javanicus.gsql;


class MethodSelectorTest extends GroovyTestCase {

    void testSetters() {
        def o = new OverloadDemo ()
        o.i = 1
        assertEquals(1, o.i)
        o.setI('10')
        assertEquals(10, o.i) // This works
        o.i = '2'
        //assertEquals(5, o.i) // o.i is 50!?!?
        //o.i = "30" // GroovyClassCastException: Cannot cast object '10' with class 'java.lang.String' to class 'java.lang.Integer' 
        //assertEquals(30, o.i)
    }

}

class OverloadDemo {
    int i
    
    void setI(int i) {
        //println "(int)${i}"
        this.i = i
    }
    
    // This is never called
    void setI(String i) {
        //println "(String)${i}"
        this.i = Integer.parseInt(i)
        //println this.i
    }
}
