/**
 *  Thaumcraft Fix
 *  Copyright (c) 2024 TheCodex6824.
 *
 *  This file is part of Thaumcraft Fix.
 *
 *  Thaumcraft Fix is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Thaumcraft Fix is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Thaumcraft Fix.  If not, see <https://www.gnu.org/licenses/>.
 */

package thecodex6824.thaumcraftfix.common.json;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;

public class JsonPatch {

    public static enum PatchOp {

	ADD("add"),
	REMOVE("remove"),
	REPLACE("replace"),
	COPY("copy"),
	MOVE("move"),
	TEST("test");

	private String internalName;

	private PatchOp(String name) {
	    internalName = name;
	}

	@Nullable
	public static PatchOp fromString(String s) {
	    for (PatchOp o : values()) {
		if (o.internalName.equals(s))
		    return o;
	    }

	    return null;
	}

    }

    public final PatchOp op;
    public final String path;
    public final JsonElement meta;

    public JsonPatch(PatchOp o, String p, JsonElement m) {
	op = o;
	path = p;
	meta = m;
    }

}
