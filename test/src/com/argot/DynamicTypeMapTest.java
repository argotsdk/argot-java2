/*
 * Copyright 2003-2009 (c) Live Media Pty Ltd. <argot@einet.com.au> 
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

import com.argot.common.CommonLoader;
import com.argot.dictionary.DictionaryLoader;
import com.argot.meta.MetaLoader;
import com.argot.meta.MetaSequence;

import junit.framework.TestCase;

public class DynamicTypeMapTest
extends TestCase
{
	TypeLibrary _library;
	TypeMap _typeMap;
	
	TypeLibraryLoader libraryLoaders[] = {
		new MetaLoader(),
		new DictionaryLoader(),
		new CommonLoader()
	};
	
	protected void setUp() 
	throws Exception
	{
		super.setUp();
		
		_library = new TypeLibrary( libraryLoaders );
		_typeMap = new TypeMap( _library, new TypeMapperDynamic( new TypeMapperLibrary() ) );
	}
	
	public void testMap()
	throws Exception
	{
		_typeMap.map( 1, _library.getDefinitionId( "uint8", "1.3" ));
	}
	
	public void testGetIdName()
	throws Exception
	{
		int id = _typeMap.getStreamId( "uint8" );
		assertTrue( id != TypeMap.NOTYPE );
	}
	
	public void testGetIdSystemId()
	throws Exception
	{
		int id = _typeMap.getStreamId( _library.getDefinitionId("uint8","1.3") );
		assertTrue( id != TypeMap.NOTYPE );
	}
	
	public void testGetIdClass()
	throws Exception
	{
		int id = _typeMap.getStreamId( MetaSequence.class );
		assertTrue( id != TypeMap.NOTYPE );
	}
	
	public void testGetReader()
	throws Exception
	{
		TypeReader reader = _typeMap.getReader( _typeMap.getStreamId("uint8"));
		assertNotNull( reader );
	}
	
	public void testGetWriter()
	throws Exception
	{
		TypeWriter writer = _typeMap.getWriter( _typeMap.getStreamId("uint8"));
		assertNotNull(writer);
	}
/*
	public void testIsValidName()
	throws Exception
	{
		boolean valid = _typeMap.isValid( "u8" );
		assertTrue( valid );
	}
*/
}
