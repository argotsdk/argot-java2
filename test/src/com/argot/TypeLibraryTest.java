/*
 * Copyright 2003-2005 (c) Live Media Pty Ltd. <argot@einet.com.au> 
 *
 * This software is licensed under the Argot Public License 
 * which may be found in the file LICENSE distributed 
 * with this software.
 *
 * More information about this license can be found at
 * http://www.einet.com.au/License
 * 
 * The Developer of this software is Live Media Pty Ltd,
 * PO Box 4591, Melbourne 3001, Australia.  The license is subject 
 * to the law of Victoria, Australia, and subject to exclusive 
 * jurisdiction of the Victorian courts.
 */
package com.argot;

import junit.framework.TestCase;

public class TypeLibraryTest
extends TestCase
{
    private TypeLibrary _library;
    
    protected void setUp() throws Exception
    {
        super.setUp();
        _library = new TypeLibrary();
    }
    
    public void testCreateTypeLibrary() throws Exception
    {
        TypeLibrary library = new TypeLibrary();
        assertNotNull( library );
    }
    
    /**
     * Test reserve of a type.
     * @throws Exception
     */
    public void testReserveType() throws Exception
    {
        _library.reserve("test");
        
        assertEquals( _library.getTypeState("test"), TypeLibrary.TYPE_RESERVED );
    }
    
    public void testNotDefinedTypeState() throws Exception
    {
        int id = _library.getTypeState("blah");
        assertEquals( id, TypeLibrary.TYPE_NOT_DEFINED );
    }
    
    public void testRegisterStructure() throws Exception
    {
        _library.register( "test", new TestTypeElement() );
        assertEquals( _library.getTypeState("test"), TypeLibrary.TYPE_REGISTERED );
    }
    
    public void testRegisterComplete() throws Exception
    {
        _library.register( "test", new TestTypeElement(), new TestReader(), new TestWriter(), null );
        assertEquals( _library.getTypeState("test"), TypeLibrary.TYPE_COMPLETE );
    }
    
    public void testRegisterAfterReserve() throws Exception
    {
        _library.reserve( "test" );
        _library.register( "test", new TestTypeElement(), new TestReader(), new TestWriter(), null );
        assertEquals( _library.getTypeState("test"), TypeLibrary.TYPE_COMPLETE );
        
        TypeElement structure = _library.getStructure( _library.getId("test"));
        assertNotNull( structure );
    }
    
    public void testBind() throws Exception
    {
        _library.register( "test", new TestTypeElement() );
        _library.bind( "test", new TestReader(), new TestWriter(), null );
    }
    
    public void testBindException() throws Exception
    {
        try
        {
            _library.bind( "test", new TestReader(), new TestWriter(), null );
            fail("expected exception");
        }
        catch( TypeException ex )
        {
            // ignore.
        }
    }
    
    public void testGetStructure() throws Exception
    {
        TestTypeElement element = new TestTypeElement();
        _library.register( "test", element, new TestReader(), new TestWriter(), null );
        
        TypeElement elem = _library.getStructure( _library.getId("test"));
        assertEquals( element, elem );
    }
     
    public void testGetReader() throws Exception
    {
        TestReader reader = new TestReader();
        _library.register( "test", new TestTypeElement(), reader, new TestWriter(), null );
        
        TypeReader read = _library.getReader( _library.getId("test"));
        assertEquals( reader, read );
    }
    
    public void testGetReaderFail() throws Exception
    {
        _library.register( "test", new TestTypeElement() );
        
        try
        {
            TypeReader read = _library.getReader( _library.getId("test"));
            assertNotNull(read);
            fail();
        } 
        catch ( TypeException ex )
        {
            // ignore.
        }
        
        try
        {
            TypeReader read = _library.getReader( _library.getId("badtype"));
            assertNotNull( read );
            fail();
        } 
        catch ( TypeException ex )
        {
            // ignore.
        }
                
    }
    
    public void testGetWriter() throws Exception
    {
        TestWriter writer = new TestWriter();
        _library.register( "test", new TestTypeElement(), new TestReader(), writer, null );
        
        TypeWriter write = _library.getWriter( _library.getId("test"));
        assertEquals( writer, write );
    }
    
    public void testGetWriterFail() throws Exception
    {
        _library.register( "test", new TestTypeElement() );
        
        try
        {
            TypeWriter read = _library.getWriter( _library.getId("test"));
            assertNotNull(read);
            fail();
        } 
        catch ( TypeException ex )
        {
            // ignore.
        }
        
        try
        {
            TypeWriter read = _library.getWriter( _library.getId("badtype"));
            assertNotNull(read);
            fail();
        } 
        catch ( TypeException ex )
        {
            // ignore.
        }
                
    }
    
    public void testGetClassId() throws Exception
    {
        TestWriter writer = new TestWriter();
        int id = _library.register( "test", new TestTypeElement(), new TestReader(), writer, writer.getClass() );
        
        int cid = _library.getId( writer.getClass() );
        assertEquals( id, cid );      
    }
    
    public void testGetClassIdAfterReserve() throws Exception
    {
        TestWriter writer = new TestWriter();
        _library.reserve("test");
        int id = _library.register( "test", new TestTypeElement(), new TestReader(), writer, writer.getClass() );
        
        int cid = _library.getId( writer.getClass() );
        assertEquals( id, cid );      
    }  
    
    public void testRegisterAfterReserveCheckId() throws Exception
    {
        int idres = _library.reserve("test");
        int idreg = _library.register( "test", new TestTypeElement() );

        assertEquals( idres, idreg );
    }
    
    public void testMixedCaseNames() throws Exception
    {
        int id1 = _library.reserve( "TeSt" );
        assertEquals( "test", _library.getName( id1 ));
        
        int id2 = _library.register( "TeSt2", new TestTypeElement() );
        assertEquals( "test2", _library.getName( id2 ));
        
        TestWriter writer = new TestWriter();
        int id3 = _library.register( "teSt3", new TestTypeElement(), new TestReader(), writer, writer.getClass() );
        assertEquals( "test3", _library.getName( id3 ));
        
        assertEquals( id1, _library.getId( "tEst"));
    }
    
    public void testNullNameForGetId() throws Exception
    {
        try
        {
            _library.getId( (String) null );
            fail("expected TypeException");
        } 
        catch (TypeException e)
        {
            // ignore.  correct.
        }
    }
}
