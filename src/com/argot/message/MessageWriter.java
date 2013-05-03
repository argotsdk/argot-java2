/*
 * Copyright (c) 2003-2010, Live Media Pty. Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this list of
 *     conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice, this list of
 *     conditions and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *  3. Neither the name of Live Media nor the names of its contributors may be used to endorse
 *     or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.argot.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import com.argot.TypeException;
import com.argot.TypeLocation;
import com.argot.TypeMap;
import com.argot.TypeMapperCore;
import com.argot.TypeMapperDynamic;
import com.argot.TypeMapperError;
import com.argot.TypeOutputStream;
import com.argot.TypeLibrary;
import com.argot.common.UInt16;
import com.argot.common.UInt8;
import com.argot.dictionary.Dictionary;
import com.argot.meta.DictionaryLocation;
import com.argot.meta.MetaDefinition;

public class MessageWriter
{	
    private TypeLibrary _library;
    
    public MessageWriter( TypeLibrary library )
    {
        _library = library;
    }
    
    public void writeMessage( OutputStream out, int id, Object object) throws TypeException, IOException
    {
		
		// write out the dictionary used to write the content.
		TypeMap refCore = new TypeMap( _library, new TypeMapperDynamic(new TypeMapperCore(new TypeMapperError())));

		// write out the dictionary used to write the content.
		TypeMap core = new TypeMap( _library, new TypeMapperDynamic(new TypeMapperCore(new TypeMapperError())));
		core.setReference(TypeMap.REFERENCE_MAP, refCore);
	
		// create a dynamic type map.
		TypeMap dtm = new TypeMap( _library, new TypeMapperDynamic( new TypeMapperError() ));
		
		// get the id of the object on the stream.
		int streamId = dtm.getStreamId(id);
		
		// write out the message content.  Definition of the core type map.
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		TypeOutputStream tmos = new TypeOutputStream( baos , dtm );
		tmos.writeObject( UInt16.TYPENAME, new Integer( streamId));
		tmos.writeObject( streamId, object );
		baos.close();
		
		// problem is that in writing the dtm, new types might
		// need to be dynamically added.  Simple solution is to 
		// write it twice.

		core.setReference( TypeMap.REFERENCE_MAP, dtm );
		ByteArrayOutputStream dictionaryStream = new ByteArrayOutputStream(); 
		TypeOutputStream dictionaryObjectStream = new TypeOutputStream( dictionaryStream , core );
		int count = dtm.size();
		int lastCount = 0;
		
		dtm.setReference( TypeMap.REFERENCE_MAP, dtm);
		while( count != lastCount )
		{
			lastCount = count;
			
			dictionaryStream = new ByteArrayOutputStream(); 
			dictionaryObjectStream = new TypeOutputStream( dictionaryStream , dtm );
			dictionaryObjectStream.writeObject( UInt8.TYPENAME, new Integer( 1 ));
			dictionaryObjectStream.writeObject( Dictionary.DICTIONARY_ENTRY_LIST, dtm );
			dictionaryObjectStream.getStream().close();
			dictionaryStream.close();
			
			count = dtm.size();		
		}
		
		dictionaryStream = new ByteArrayOutputStream(); 
		dictionaryObjectStream = new TypeOutputStream( dictionaryStream , core );
		dictionaryObjectStream.writeObject( UInt8.TYPENAME, new Integer( 1 ));
		dictionaryObjectStream.writeObject( Dictionary.DICTIONARY_ENTRY_LIST, dtm );
		dictionaryObjectStream.getStream().close();
		dictionaryStream.close();
		
		
		
		// write out the core used to write the message dictionary.
		core.setReference( TypeMap.REFERENCE_MAP, refCore );
		ByteArrayOutputStream baos3 = new ByteArrayOutputStream(); 
		TypeOutputStream tmos3 = new TypeOutputStream( baos3 , core );
		writeCoreMap( tmos3, core );
		baos3.close();		
		
		// write out the file.
		out.write( baos3.toByteArray() );
		out.write( dictionaryStream.toByteArray() );
		out.write( baos.toByteArray() );      
    }
    
	private static void writeCoreMap( TypeOutputStream out, TypeMap map ) 
	throws TypeException, IOException
	{
		// writing out the core and then the extensions.
		out.writeObject( UInt8.TYPENAME , new Integer( 2 ));
		
		byte[] coreBuffer = writeCore( map );
		out.writeObject( UInt16.TYPENAME, new Integer( coreBuffer.length ));
		out.getStream().write( coreBuffer );

		int count = map.size();
		int lastCount = -1;
		ByteArrayOutputStream baos2 = new ByteArrayOutputStream();

		while ( count != lastCount )
		{
			lastCount = count;

			// count the number of extensions
			List<Integer> coreIds = TypeMapperCore.getCoreIdentifiers();
			Iterator<Integer> i = coreIds.iterator();	
			int extensionCount = 0;
			i = map.getIdList().iterator();
			while (i.hasNext() )
			{
				Integer id = (Integer) i.next();
				if ( coreIds.contains(id))
				{
					// already written this out in the core.
					continue;	
				}
				extensionCount++;
			}
	
			// write out extensions.
			baos2 = new ByteArrayOutputStream();
	
			
			TypeOutputStream out2 = new TypeOutputStream( baos2, map );
			
			out2.writeObject(UInt16.TYPENAME, new Integer( extensionCount ));
			
			// write out the extensions
			i = map.getIdList().iterator();
			while (i.hasNext() )
			{
				Integer id = (Integer) i.next();
				if ( coreIds.contains(id))
				{
					// already written this out in the core.
					continue;	
				}
				TypeLocation location = map.getLocation(id.intValue());
				MetaDefinition definition = (MetaDefinition) map.getStructure(id.intValue());
				out2.writeObject( UInt16.TYPENAME, new Integer(id.intValue()));
				out2.writeObject( DictionaryLocation.TYPENAME, location);
				out2.writeObject( "dictionary.definition_envelope", definition );
			}
			
			out2.getStream().close();
			baos2.close();
			
			count = map.size();
		}
		
		byte[] extBuffer = baos2.toByteArray();
		out.writeObject( UInt16.TYPENAME, new Integer( extBuffer.length ));
		out.getStream().write( extBuffer );
	}

	public static byte[] writeCore( TypeMap map ) throws TypeException, IOException
	{
		TypeMap refCore = new TypeMap(map.getLibrary(), new TypeMapperCore(new TypeMapperError()));
		ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
		TypeOutputStream coreObjectStream = new TypeOutputStream( baos1, map );
		coreObjectStream.writeObject( Dictionary.DICTIONARY_ENTRY_LIST, refCore );
		baos1.close();		
		return baos1.toByteArray();
	}

	
}
