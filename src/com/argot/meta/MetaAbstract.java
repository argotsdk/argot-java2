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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.argot.TypeBound;
import com.argot.TypeElement;
import com.argot.TypeException;
import com.argot.TypeInputStream;
import com.argot.TypeLibrary;
import com.argot.TypeLibraryReader;
import com.argot.TypeLibraryWriter;
import com.argot.TypeLocation;
import com.argot.TypeMap;
import com.argot.TypeOutputStream;
import com.argot.TypeReader;
import com.argot.TypeRelation;
import com.argot.TypeWriter;
import com.argot.auto.TypeArrayMarshaller;
import com.argot.common.UInt16;
import com.argot.common.UInt8;

public class MetaAbstract
extends MetaExpression
implements MetaDefinition, TypeRelation
{
    public static final String TYPENAME = "meta.abstract";
	public static final String VERSION = "1.3";

	private Map _concreteToMap;
	//private Map _mapToConcrete;
	private Map _relationMap;
	
	private MetaAbstractMap _defaultMappings[];
	
	// This keeps a list of any abstract types we've mapped.
	private List _abstractParentMap;
	
	private class MetaMap
	{
		boolean isHidden;
		int mapTypeId;
	}
	
	public MetaAbstract()
	{
		_concreteToMap = new HashMap();
		//_mapToConcrete = new HashMap();
		_relationMap = new HashMap();
		_abstractParentMap = new ArrayList();
		_defaultMappings = null;
	}
	
	public MetaAbstract( Object mappings[] )
	{
		this();
		_defaultMappings = new MetaAbstractMap[mappings.length];
		
		for (int x=0; x<mappings.length;x++)
		{
			_defaultMappings[x] = (MetaAbstractMap) mappings[x];
		}
	}
	
	public MetaAbstract( MetaAbstractMap defaultMappings[] )
	{
		this();
		_defaultMappings = defaultMappings;
	}
	
    public void bind(TypeLibrary library, int memberTypeId,	TypeLocation location, TypeElement definition) 
    throws TypeException 
    {
		super.bind(library, memberTypeId, location, definition);
		if (_defaultMappings != null)
		{
			for (int x=0; x<_defaultMappings.length;x++)
			{
				MetaAbstractMap mapping = _defaultMappings[x];
				
				
		        TypeElement concreteElement = library.getStructure( mapping.getConcreteType() ); 
		        if (concreteElement instanceof MetaAbstract)
		        {
		        	((MetaAbstract)concreteElement).addAbstractMap(this);
		        }
		        else if (!(concreteElement instanceof MetaIdentity))
		        {
		        	throw new TypeException("Can only map abstract types to identity or abstract definitions");
		        }
		        addMap( mapping.getConcreteType(), TypeLibrary.NOTYPE, true );
			}
		}
	}

	public String getTypeName()
    {
        return TYPENAME;
    }
    
    public Map getConcreteToMap()
    {
    	return _concreteToMap;
    }

    public void addAbstractMap( MetaAbstract parent )
    {
    	_abstractParentMap.add( parent );
    	
    	Iterator i = _concreteToMap.keySet().iterator();
    	while( i.hasNext())
    	{
    		Integer c = (Integer) i.next();
    		MetaMap m = (MetaMap) _concreteToMap.get(c);
    		
    		parent.addMap( c.intValue(), m.mapTypeId, m.isHidden);
    	}
    	
    }
    
	public void addMap( int concreteType, int mapType, boolean isHidden )
	{
		MetaMap metaMap = new MetaMap();
		metaMap.isHidden = isHidden;
		metaMap.mapTypeId = mapType;
		
		_concreteToMap.put( new Integer( concreteType ), metaMap);
		
    	Iterator i = _abstractParentMap.iterator();
    	while( i.hasNext())
    	{
    		MetaAbstract p = (MetaAbstract) i.next();
    		
    		p.addMap( concreteType, mapType, isHidden);
    	}		
	}
	
	public int getRelation(String tag) 
	{
		Integer i = (Integer) _relationMap.get(tag);
		if (i == null)
		{
			return TypeLibrary.NOTYPE;
		}
		return i.intValue();
	}

	public void setRelation(String tag, int id) 
	{
		_relationMap.put(tag, new Integer(id));
	}
	
	
	public static class MetaAbstractTypeReader
	implements TypeReader, TypeBound, TypeLibraryReader, MetaExpressionReader
	{
		private MetaMarshaller _reader;
		
		public MetaAbstractTypeReader()
		{
			_reader = new MetaMarshaller();
		}
		
		public void bind(TypeLibrary library, int definitionId, TypeElement definition) 
		throws TypeException 
		{
			_reader.bind(library, definitionId, definition);
		}
		
	    public Object read(TypeInputStream in) 
	    throws TypeException, IOException
	    {
			Object[] object = (Object[]) _reader.getReader(in.getTypeMap()).read(in);
			if (object.length != 1 && !(object[0] instanceof Object[])) throw new TypeException("Unexpected data");
			Object[] mapArray = (Object[]) object[0];
			MetaAbstractMap[] defaultMaps = new MetaAbstractMap[mapArray.length];
			for (int x=0;x<mapArray.length;x++)
			{
				defaultMaps[x] = (MetaAbstractMap) mapArray[x];
				//defaultMaps[x].setConcreteType(in.getTypeMap().getNameId(defaultMaps[x].getConcreteType()));
			}

			return new MetaAbstract(defaultMaps);
	    }

		public TypeReader getReader(TypeMap map) 
		throws TypeException 
		{
			return this;
		}

		public TypeReader getExpressionReader(TypeMap map, MetaExpressionResolver resolver, TypeElement element)
		throws TypeException 
		{
			MetaAbstract metaAbstract = (MetaAbstract) element;
			return new MetaAbstractReader(metaAbstract,map.getReader(map.getStreamId(UInt16.TYPENAME)));
		}
	}

	public static class MetaAbstractTypeWriter
	implements TypeWriter, TypeLibraryWriter, MetaExpressionWriter
	{
		
	    public void write(TypeOutputStream out, Object o) 
	    throws TypeException, IOException
	    {
	    	MetaAbstract ma = (MetaAbstract) o;
	    	
	    	out.writeObject(UInt8.TYPENAME, new Integer(ma._defaultMappings.length));
	    	
	    	MetaAbstractMap[] defaultMappings = ma._defaultMappings;
	    	for (int x=0; x<defaultMappings.length;x++)
	    	{
	    		out.writeObject(MetaAbstractMap.TYPENAME, defaultMappings[x]);
	    	}
	    }
	    
		public TypeWriter getWriter(TypeMap map) 
		throws TypeException 
		{
			return this;
		}

		public TypeWriter getExpressionWriter(TypeMap map, MetaExpressionResolver resolver, TypeElement element)
		throws TypeException 
		{
			MetaAbstract metaAbstract = (MetaAbstract) element;
			return new MetaAbstractWriter(metaAbstract,map.getWriter(map.getStreamId(UInt16.TYPENAME)));
		}
	}

	private static class MetaAbstractReader
	implements TypeReader
	{
		private MetaAbstract _metaAbstract;
		private TypeReader _uint16;
		private Map _mapCache;
		
		public MetaAbstractReader(MetaAbstract metaAbstract, TypeReader uint16)
		{
			_metaAbstract = metaAbstract;
			_uint16 = uint16;
			_mapCache = new HashMap();
		}
		
		public Object read(TypeInputStream in)
		throws TypeException, IOException 
		{
	        Integer type = (Integer) _uint16.read(in);
	        TypeReader mappedReader = (TypeReader) _mapCache.get(type);
	        if ( mappedReader == null )
	        {
	        	int id = mapType( in.getTypeMap(), type );
	        	mappedReader = in.getTypeMap().getReader(id);
	        	_mapCache.put(type, mappedReader);
	        }	        
			return mappedReader.read( in );
		}
		
		private int mapType(TypeMap map, Integer type)
		throws TypeException
		{
	        int definitionId = map.getDefinitionId( type.intValue() );
	        MetaName name = map.getLibrary().getName(definitionId);
	        int identityId = map.getLibrary().getTypeId(name);
			MetaMap concrete = (MetaMap) _metaAbstract._concreteToMap.get( new Integer( identityId ));
	        if ( concrete == null )
	        {
	        	throw new TypeException("type not mapped:" + type.intValue() + " " + map.getName( type.intValue() ) ); 
	        }
			return type.intValue();
		}
		
	}

	private static class CacheEntry
	{
		Integer mapId;
		TypeWriter idWriter;
	}
	
    private static class MetaAbstractWriter
    implements TypeWriter
    {
    	private MetaAbstract _metaAbstract;
    	private TypeWriter _uint16;
		private Map _mapCache;
		
    	public MetaAbstractWriter(MetaAbstract metaAbstract, TypeWriter uint16)
    	{
    		_metaAbstract = metaAbstract;
    		_uint16 = uint16;
    		_mapCache = new HashMap();
    	}
    	
		public void write(TypeOutputStream out, Object o)
		throws TypeException, IOException 
		{
			Class clss = o.getClass();
			CacheEntry entry = (CacheEntry) _mapCache.get(clss);
			if (entry == null )
			{
				entry = mapType(out.getTypeMap(), clss);
				_mapCache.put(clss, entry);
			}
			_uint16.write(out, entry.mapId);
			entry.idWriter.write(out, o);
		}
    	
		private CacheEntry mapType(TypeMap map, Class clss)
		throws TypeException
		{
			// assumes class is bound to definition, not identity.
			int[] ids = map.getLibrary().getId(clss);
			if (ids.length != 1)
			{
				if (ids.length>1)
					throw new TypeException("Class bound to multiple system types:" +clss.getName());
				if (ids.length==0)
					throw new TypeException("Class not bound to any mapped type:"+clss.getName());
			}
			int defId = ids[0];
			
			MetaName name = map.getLibrary().getName(defId);
			
			// get the identity id.
			int id = map.getLibrary().getTypeId(name);
			
			// Find the identifier for the abstract map definition using identity internal id.
			MetaMap metaMap = (MetaMap) _metaAbstract._concreteToMap.get( new Integer( id ));
	        if ( metaMap == null )
	        {
				throw new TypeException( "can't write abstract type directly.:" + clss.getName() );
			}
	        
	        CacheEntry entry = new CacheEntry();
	        
	        if (!metaMap.isHidden)
	        {
		        // This is very important.  When working as a remote client we must ensure
		        // that the server also has the mapping from concrete to map value.  By 
		        // getting the System Id of the MapId it will automatically resolve if the
		        // server has the correct value.
		        map.getStreamId(metaMap.mapTypeId);
		        map.getStreamId(id);
	        }

	        // The Id written to file is the mapped concrete id.
	        entry.mapId = new Integer(map.getStreamId(defId));
	        
	        // Cache the writer function.
	        entry.idWriter = map.getWriter(map.getStreamId( defId ));
			return entry;
		}
		
    }
 
	public boolean isMapped( int id )
	{
		return _concreteToMap.get( new Integer(id))==null?false:true;
	}


}
