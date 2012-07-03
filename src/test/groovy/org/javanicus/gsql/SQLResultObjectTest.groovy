package org.javanicus.gsql;


class SQLResultObjectTest extends GroovyTestCase {

    void testOrder() {
        def o = new SQLResultObjectTest_Dummy ()
        assertEquals('org.javanicus.gsql.SQLResultObjectTest_Dummy@cafe (a=null, x=null, b=null, y=1)', fixDynaObject(o.toString()))
    }
    
    void testDefined() {
        def o = new SQLResultObjectTest_Dummy ()
        o.a = null
        o.b = ''
        assert true == o.isDefined('a')
        assert false == o.isDefined('x')
        assert true == o.isDefined('b')
        assertEquals('org.javanicus.gsql.SQLResultObjectTest_Dummy@cafe (a=null, x=null, b="", y=1)', fixDynaObject(o.toString()))
    }

    void testAccessors() {
        def o = new SQLResultObjectTest_Dummy ()
//        println o.getPropertySequence()
//        println o.getProperties()
        assert 3 == o.getPropertySequence().size()
        assert 0 == o.getProperties().size()
        assert 1 == o.y
        o.y = 2
        assert 2 == o.y
        assert 2 == o.getter()
        assert 0 == o.getProperties().size()
    }
    
    void testExpandoExtension() {
        def o = new ExpandoTest ()
        o.a = 'test'
        assert 'test' == o.a
        assert 1 == o.getter()
        assert 1 == o.y
        //o.y = 2
        o.metaClass.setProperty(ExpandoTest.class, o, 'y', 2, false, true)
        //o.metaClass.setProperty(ExpandoTest.class, o, 'a', 'xxx', true, true)
        o.a = 'xxx'
        assert 2 == o.y
        assert 2 == o.getter(), "2 == ${o.getter()}"
        assert 'xxx' == o.a
    }

    void testPrecedence() {
        def o = new PrecedenceTest()
        o.x = 'a'
        // Field precedence/shadowing is broken
        assertTrue(o.isDefined('x'))
        assertEquals('a', o.x)
        assertEquals('a', o.getProperties()['x'])
        assertNull(o.getter())
    }
    
    void testUndefined() {
	def o = new SQLResultObjectTest_Dummy ()
	try {
	    def dummy = o.dummy
	    fail("Access didn't throw an exception")
	} catch (MissingPropertyException e) {
	    assert 'No such property: dummy for class: org.javanicus.gsql.SQLResultObjectTest_Dummy' == e.message
	}
    }
    
    /** Fixate the String returned by DynaObject.toString(), i.e. replace the
     *  hashCode after the classname with the constant "cafe"
     */
    static String fixDynaObject (String s) {
        return s.replaceAll('@[0-9a-f]+ [(]', '@cafe (')
    }
}

class SQLResultObjectTest_Dummy extends SQLResultObject {
    int y = 1
    
    SQLResultObjectTest_Dummy () {
        super (['a', 'x', 'b'])
    }
    
    int getter() {
        return this.y
    }

    void additionalPropertiesToString (StringBuffer buffer) {
	super.additionalPropertiesToString (buffer)

	buffer << ', y='
	buffer << y
    }

}

class PrecedenceTest extends SQLResultObject {
    def x
    PrecedenceTest() {
        super (['x'])
    }
    
    def getter() {
        return this.x
    }
}

class ExpandoTest extends Expando {
    def y = 1

    int getter() {
        return this.y
    }
}
