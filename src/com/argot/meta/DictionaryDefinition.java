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
package com.argot.meta;

import java.io.IOException;

import com.argot.ReferenceTypeMap;
import com.argot.TypeBound;
import com.argot.TypeElement;
import com.argot.TypeException;
import com.argot.TypeInputStream;
import com.argot.TypeLibrary;
import com.argot.TypeLibraryReader;
import com.argot.TypeLibraryWriter;
import com.argot.TypeLocation;
import com.argot.TypeLocationDefinition;
import com.argot.TypeMap;
import com.argot.TypeOutputStream;
import com.argot.TypeReader;
import com.argot.TypeWriter;
import com.argot.auto.TypeBeanMarshaller;
import com.argot.common.UInt16;

public class DictionaryDefinition 
extends DictionaryLocation
implements TypeLocationDefinition
{
	public static final String TYPENAME = "dictionary.definition";
	
	private int _id;
	private MetaName _name;
	private MetaVersion _version;

	public DictionaryDefinition()
	{
		super(TypeLocation.DEFINITION);
		_id = -1;
	}

	public DictionaryDefinition(int nameId, MetaName name, MetaVersion version) 
	throws TypeException
	{
		super(TypeLocation.DEFINITION);
		_id = nameId;
		_name = name;
		_version = version;
	}

	public DictionaryDefinition(TypeLibrary library, int id, String name, String version)
	throws TypeException
	{
		this( id, MetaName.parseName(library, name), MetaVersion.parseVersion(version) );
	}


	public MetaName getName() 
	{
		return _name;
	}
	
	public void setName(MetaName name)
	{
		_name = name;
	}

	public MetaVersion getVersion() 
	{
		return _version;
	}
	
	public void setVersion(MetaVersion version)
	{
		_version = version;
	}
	
	public void setId(int id)
	{
		_id = id;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public static class DictionaryDefinitionTypeReader
	implements TypeReader,TypeBound,TypeLibraryReader
	{
		TypeBeanMarshaller _reader = new TypeBeanMarshaller();
		
		public void bind(TypeLibrary library, int definitionId, TypeElement definition) 
		throws TypeException 
		{
			_reader.bind(library, definitionId, definition);
		}
		
		public TypeReader getReader(TypeMap map) 
		throws TypeException 
		{
			return this;
		}
		
	    public Object read(TypeInputStream in) throws TypeException, IOException
	    {
	    	
			// Use the Automatic reader to read and create this object.
			TypeReader reader = _reader.getReader(in.getTypeMap());
			DictionaryDefinition def = (DictionaryDefinition) reader.read( in );
			
			// Check that what its referencing exists and convert from
			// external mapping to internal mapping.
			ReferenceTypeMap mapCore = (ReferenceTypeMap) in.getTypeMap();
			
			def._id = mapCore.getLibrary().getTypeId(def.getName());

			return def;
	    }
	}
	
	public static class DictionaryDefinitionTypeWriter
	implements TypeWriter, TypeBound, TypeLibraryWriter
	{
		TypeBeanMarshaller _writer = new TypeBeanMarshaller();
		
	    public void write(TypeOutputStream out, Object o) throws TypeException, IOException
	    {
			DictionaryDefinition dd = (DictionaryDefinition) o;
			_writer.getWriter(out.getTypeMap()).write(out, dd);
	    }

		public TypeWriter getWriter(TypeMap map) 
		throws TypeException 
		{
			return this;
		} 

		public void bind(TypeLibrary library, int definitionId, TypeElement definition) 
		throws TypeException 
		{
			_writer.bind(library, definitionId, definition);	
		} 

	}
	
}
