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

import com.argot.TypeLocation;

public class DictionaryLocation 
implements TypeLocation
{
	public static final String TYPENAME = "dictionary.location";
	
	private int _type;
	
	public DictionaryLocation( int type )
	{
		_type = type;
	}
	
	public int getType()
	{
		return _type;
	}
}