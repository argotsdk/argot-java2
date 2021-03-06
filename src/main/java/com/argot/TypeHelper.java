/*
 * Copyright (c) 2003-2019, Live Media Pty. Ltd.
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
package com.argot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import com.argot.meta.MetaDefinition;

public final class TypeHelper {
    public static byte[] toByteArray(TypeMap core, TypeElement definition) throws TypeException {
        return resolveStructure(core, definition);
    }

    public static boolean structureMatches(TypeMap core, TypeElement definition1, byte[] definition2) {
        try {
            byte[] struct1 = resolveStructure(core, definition1);
            return Arrays.equals(struct1, definition2);
        } catch (TypeException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean structureMatches(TypeMap core, TypeElement definition1, TypeElement definition2) {
        try {
            byte[] struct1 = resolveStructure(core, definition1);
            byte[] struct2 = resolveStructure(core, definition2);

            return Arrays.equals(struct1, struct2);
        } catch (TypeException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static byte[] resolveStructure(TypeMap refMap, TypeElement definition) throws TypeException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TypeOutputStream tmos = new TypeOutputStream(out, refMap);
        try {
            tmos.writeObject("meta.definition", definition);
        } catch (IOException e) {
            e.printStackTrace();
            throw new TypeException("failed to write the definition", e);
        }
        byte b[] = out.toByteArray();

        return b;
    }

    /**
     * This checks if the id, name &amp; structure are the same as used in the map. The byte[] must follow the
     * TypeMapCore type id's. Any function or references must be valid in the context of this TypeMap.
     * 
     * This is like the register version below, however in some cases like a protocol you just need to check if the id's
     * are the same.
     */
    public static void isSame(int id, TypeLocation location, byte[] structure, TypeMap coreMap) throws TypeException {
        TypeLibrary library = coreMap.getLibrary();

        // First check if we can find the same identifier.
        int i = library.getTypeId(location);

        // Are the identifiers the same.
        if (id != i) {
            throw new TypeException("Type Mismatch: Type identifiers different");
        }

        // Are the definitions the same.
        // read the definition.

        TypeElement definition = readStructure(coreMap, structure);

        // check what we've read with the local version.
        TypeElement localStruct = library.getStructure(i);
        if (!TypeHelper.structureMatches(coreMap, definition, localStruct)) {
            throw new TypeException("Type mismatch: structures do not match: ");
        }
    }

    public static TypeElement readStructure(TypeMap core, byte[] structure) throws TypeException {
        ByteArrayInputStream bais = new ByteArrayInputStream(structure);
        TypeInputStream tmis = new TypeInputStream(bais, core);

        try {
            Object definition = tmis.readObject(MetaDefinition.TYPENAME);
            return (TypeElement) definition;
        } catch (IOException e) {
            throw new TypeException("failed reading structure:" + e.getMessage());
        }

    }

}
