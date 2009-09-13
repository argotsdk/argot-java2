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
import com.argot.TypeLocationRelation;
import com.argot.TypeMap;
import com.argot.TypeOutputStream;
import com.argot.TypeReader;
import com.argot.TypeWriter;
import com.argot.auto.TypeBeanMarshaller;
import com.argot.common.UInt16;

public class DictionaryRelation 
extends DictionaryLocation
implements TypeLocationRelation
{
	public static final String TYPENAME = "dictionary.relation";
	
	private int _id;
	private String _tag;
	
	public DictionaryRelation()
	{
		super(TypeLocation.RELATION);
	}
	
	public DictionaryRelation(int targetId, String tag)
	{
		super(TypeLocation.RELATION);
		_id = targetId;
		_tag = tag;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public void setId(int id)
	{
		_id = id;
	}
	
	public String getTag()
	{
		return _tag;
	}
	
	public void setTag(String tag)
	{
		_tag = tag;
	}
	
	public static class DictionaryRelationTypeReader
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
			DictionaryRelation rel = (DictionaryRelation) reader.read( in );
			
			// Check that what its referencing exists and convert from
			// external mapping to internal mapping.
			ReferenceTypeMap mapCore = (ReferenceTypeMap) in.getTypeMap();
						
			if (  mapCore.referenceMap().isValid( rel.getId() ) )
				rel.setId( mapCore.referenceMap().getDefinitionId( rel.getId() ));
			else
				throw new TypeException( "DictionaryDefinition: invalid id " + rel.getId() );

			return rel;
	    }
	}
	
	public static class DicitonaryRelationTypeWriter
	implements TypeWriter, TypeBound, TypeLibraryWriter
	{
		TypeBeanMarshaller _writer = new TypeBeanMarshaller();
		
	    public void write(TypeOutputStream out, Object o) throws TypeException, IOException
	    {
			DictionaryRelation dd = (DictionaryRelation) o;
			ReferenceTypeMap mapCore = (ReferenceTypeMap) out.getTypeMap();
			int id = mapCore.referenceMap().getStreamId( dd.getId() );
			DictionaryRelation dr = new DictionaryRelation(id, dd._tag);
			_writer.getWriter(out.getTypeMap()).write(out, dr);
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