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

package com.argot.meta;

import com.argot.ResourceDictionaryLoader;
import com.argot.TypeException;
import com.argot.TypeLibrary;
import com.argot.auto.TypeBeanMarshaller;
import com.argot.auto.TypeReaderAuto;

public class MetaExtensionLoader
extends ResourceDictionaryLoader
{
	public static final String DICTIONARY = "meta_extensions.dictionary";	
	
	public MetaExtensionLoader()
	{
		super(DICTIONARY);
	}
		
	public String getName()
	{
		return DICTIONARY;
	}

	public void bind(TypeLibrary library) throws TypeException
	{
	
		
	    library.bind( 
	    	library.getTypeId(MetaAtomAttributeSigned.TYPENAME, "1.3"),
	    	new TypeReaderAuto(MetaAtomAttributeUnsigned.class), 
	    	new TypeBeanMarshaller(), 
	    	MetaAtomAttributeSigned.class );	    

	    library.bind( 
    		library.getTypeId(MetaAtomAttributeIEEE756.TYPENAME, "1.3"),
    		new TypeReaderAuto(MetaAtomAttributeIEEE756.class), 
    		new TypeBeanMarshaller(), 
    		MetaAtomAttributeIEEE756.class );
	    
	}

}
