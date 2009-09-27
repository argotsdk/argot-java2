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
package com.argot.remote;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.argot.ReferenceTypeMap;
import com.argot.TypeElement;
import com.argot.TypeException;
import com.argot.TypeInputStream;
import com.argot.TypeLibrary;
import com.argot.TypeLocation;
import com.argot.TypeOutputStream;
import com.argot.TypeReader;
import com.argot.TypeRelation;
import com.argot.TypeWriter;
import com.argot.common.UInt8;
import com.argot.common.UInt16;
import com.argot.meta.MetaDefinition;
import com.argot.meta.MetaExpression;
import com.argot.meta.MetaName;



public class MetaInterface 
extends MetaExpression
implements MetaDefinition, TypeRelation
{
	public static final String TYPENAME = "remote.interface";
	public static final String VERSION = "1.3";
	
	// TODO Need a two way map here.
	private HashMap _methodToMetaMethod;
	private HashMap _metaMethodToMethod;
	
	private HashMap _nameToMetaMethod;
	
	private HashMap _relations;

	private int[] _parentInterfaceTypes;
	private MetaInterface[] _parentInterfaces;
	
	public MetaInterface()
	{
		_parentInterfaceTypes = new int[0];
		_parentInterfaces = new MetaInterface[0];
		_nameToMetaMethod = new HashMap();
		_methodToMetaMethod = new HashMap();
		_metaMethodToMethod = new HashMap();
		_relations = new HashMap();
	}	
	
	public MetaInterface( Object[] interfaces )
	{
		this();
		if ( interfaces != null )
		{
			_parentInterfaceTypes = new int[ interfaces.length ];
			for ( int x=0 ; x< interfaces.length; x++ )
			{
				_parentInterfaceTypes[x] = ((Integer) interfaces[x]).intValue();
			}
			_parentInterfaces = new MetaInterface[ interfaces.length ];
		}
	}

	public MetaInterface( List interfaces )
	{
		this( interfaces.toArray() );
	}
	
	public MetaInterface( int[] interfaces )
	{
		this();
		_parentInterfaceTypes = interfaces;
		_parentInterfaces = new MetaInterface[interfaces.length];
	}
	
	public String getTypeName() 
	{
		return TYPENAME;
	}
	
	public void bind(TypeLibrary library, int memberTypeId, TypeLocation location, TypeElement definition) 
	throws TypeException 
	{
		super.bind(library, memberTypeId, location, definition);

		// bind and check the parent interfaces.
		for ( int x=0; x < _parentInterfaceTypes.length; x++ )
		{
			TypeElement element = library.getStructure( _parentInterfaceTypes[x]);
			if ( !(element instanceof MetaInterface))
				throw new TypeException( "parent not Interface type" );
			
			_parentInterfaces[x] = (MetaInterface) element;
		}
		
		// bind the java methods to MetaMethods.
		// If no class is bound then do nothing.
		Class clss;
		try 
		{
			clss = library.getClass(memberTypeId);
		} 
		catch (TypeException e) 
		{
			// DO NOTHING.
			return;
		}
		bindMethods( library, clss );
	}

	public int getRelation(String tag) 
	{
		Integer i = (Integer) _relations.get(tag);
		if (i==null)
		{
			return TypeLibrary.NOTYPE;
		}
		return i.intValue();
	}

	public void setRelation(String tag, int id) 
	{
		_relations.put(tag, new Integer(id));
	}

	public MetaMethod getMetaMethod(Method method) 
	{
		MetaMethod metaMethod = (MetaMethod) _methodToMetaMethod.get(method);
		if (metaMethod == null)
		{
			for (int x=0; x<_parentInterfaces.length;x++)
			{
				metaMethod = _parentInterfaces[x].getMetaMethod(method);
				if (method!=null) return metaMethod;
			}
			return null;
		} 
		return metaMethod;
	}
	
	public Method getMethod( MetaMethod metaMethod )
	{
		return (Method) _metaMethodToMethod.get( metaMethod );
	}
	
	public void addMethod( MetaMethod method )
	{
		_nameToMetaMethod.put( method.getMethodName(), method );
	}
	
	public Iterator getMetaMethodIterator()
	{
		return _nameToMetaMethod.values().iterator();
	}
	
	public int[] getInterfaces()
	{
		return _parentInterfaceTypes;
	}

	private void bindMethods( TypeLibrary library, Class clss ) 
	throws TypeException
	{
		Iterator iter = _nameToMetaMethod.values().iterator();
		while ( iter.hasNext() )
		{
			Object o = iter.next();
			MetaMethod metaMethod = (MetaMethod) o;
			Method method = findMethod( library, clss, metaMethod );
			_methodToMetaMethod.put( method, metaMethod );
			_metaMethodToMethod.put( metaMethod, method );
		}
	}
	
	/**
	 * A helper class used to proces a request and invoke a proxy
	 * object.
	 * 
	 * @param request
	 * @throws SomException
	 */
	private Method findMethod( TypeLibrary library, Class proxyClass, MetaMethod metaMethod )
	throws TypeException
	{
		String name = metaMethod.getMethodName();
		MetaParameter[] requestTypes = metaMethod.getRequestTypes();
		
		// Setup the objects used to invoke the request.
		Class[] args = new Class[ metaMethod.getRequestTypes().length ];
		
		// Pop the args back into the Arrays from the Request.
		for ( int x = 0 ; x < metaMethod.getRequestTypes().length ; x++ )
		{
			try
			{
				args[x] = library.getClass( requestTypes[x].getParamType() );
			}
			catch( TypeException e)
			{
				MetaName filedType = library.getName(requestTypes[x].getParamType());
				throw new TypeException("Failed to bind method: " + metaMethod.getMethodName() + " arg: " + (x+1), e);
			}
		}
		
		Method method = null;		
		try
		{
			// Using the method name and classes, get the method.
			//System.out.println( "Invoke " + request.method() + " argCount:" + cargs.length );
			
			// Can't use getMethod because it does not handle conversion of
			// basic types correctly.  We need to filter out the basic types
			// and match primitives with java object types we're supplied.
			//method = _proxyClass.getMethod( request.method(), cargs );
			
			
			// Search through each of the methods on the object.
			Method[] methods = proxyClass.getMethods();
			for ( int y = 0 ; y < methods.length ; y++ )
			{
				
				// First find the right name.
				if ( !methods[y].getName().equals( name ))
					continue;
					
				// Now check we have the same parameter length
				Class[] paramTypes = methods[y].getParameterTypes();
				if ( paramTypes.length != requestTypes.length )
					continue;

				method = methods[y];
				
				
				// Now check the param types are the same.
				boolean found = true;
				for ( int z = 0; z< paramTypes.length; z++ )
				{
					// If this fails do some further checking.
					//if ( !paramTypes[z].isInstance( args[z] ) )
					if (!paramTypes[z].equals(args[z]))
					{
						if ( args[z] == null )
							continue;
						
						// First check if we have any basic types.
						if ( paramTypes[z].getName().equals( "short") && args[z].getName().equals( "java.lang.Short") )
							continue;
											
						if ( paramTypes[z].getName().equals( "byte" ) && args[z].getName().equals( "java.lang.Byte"))
							continue;
					
						if ( paramTypes[z].getName().equals( "int" ) && args[z].getName().equals( "java.lang.Integer" ))
							continue;
						
						if ( paramTypes[z].getName().equals( "long" ) && args[z].getName().equals( "java.lang.Long" ))
							continue;
						
						if ( paramTypes[z].getName().equals( "boolean") && args[z].getName().equals( "java.lang.Boolean" ))
							continue;
											
						found = false;
						break;
						
					}
				}
				
				// If we got through all the params break.  We got it.
				if ( found )
					break;
				
				method = null;
			}
		}
		catch (SecurityException e)
		{
			throw new TypeException( "SecurityException", e );
		}

		if ( method == null )
		{
			StringBuffer error = new StringBuffer();
			error.append(proxyClass.getName());
			error.append(".");
			error.append(name);
			error.append("(");
			for ( int x=0; x < args.length; x++ )
			{
				if ( args[x] != null)
					error.append( args[x].getName() );
				else
					error.append( "null");
				
				if ( x < args.length-1 )
					error.append( "," );
			}
			error.append(")");
						
			throw new TypeException( "NoSuchMethod '" + error + "'" );
		}	
		return method;
	}
	
	public static class MetaInterfaceReader
	implements TypeReader
	{
		public Object read(TypeInputStream in) 
		throws TypeException, IOException 
		{		
			ReferenceTypeMap mapCore = (ReferenceTypeMap) in.getTypeMap();
			
			Short size = (Short) in.readObject( UInt8.TYPENAME );
			int interfaces[] = new int[size.intValue()];
			for ( int x=0; x<size.intValue(); x++ )
			{
				Integer id = (Integer) in.readObject( UInt16.TYPENAME );
				interfaces[x] = mapCore.referenceMap().getDefinitionId( id.intValue() );
			}
			
			return new MetaInterface( interfaces );
		}
	}
	
	public static class MetaInterfaceWriter
	implements TypeWriter
	{
		public void write(TypeOutputStream out, Object o) 
		throws TypeException, IOException 
		{
			MetaInterface mc = (MetaInterface) o;
			ReferenceTypeMap mapCore = (ReferenceTypeMap) out.getTypeMap();
			
			if ( mc.getInterfaces() != null )
			{
				out.writeObject( UInt8.TYPENAME, new Integer( mc.getInterfaces().length ));
				for( int x=0 ;x < mc.getInterfaces().length; x++ )
				{
					int id = mapCore.referenceMap().getStreamId( mc.getInterfaces()[x]);
					out.writeObject( UInt16.TYPENAME, new Integer(id) );
				}
			}
			else
			{
				out.writeObject( UInt8.TYPENAME, new Integer(0));
			}
		}
	}

}
