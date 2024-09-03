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

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import thecodex6824.thaumcraftfix.ThaumcraftFix;

public final class PatchHelper {

    private PatchHelper() {}

    private static JsonElement getValue(JsonElement parent, String child) throws JsonSchemaException {
	if (parent.isJsonObject())
	    return parent.getAsJsonObject().get(child);
	else if (parent.isJsonArray()) {
	    JsonArray array = parent.getAsJsonArray();
	    int index = -1;
	    if (child.equals("-"))
		throw new JsonSchemaException("Path component - is not a valid array index here");

	    try {
		index = Integer.parseInt(child);
	    }
	    catch (NumberFormatException ex) {
		throw new JsonSchemaException("Path component " + child + " is not a valid array index");
	    }

	    if (index < 0 || index >= array.size())
		throw new JsonSchemaException("Path component " + child + " is not a valid array index");

	    return array.get(index);
	}

	return null;
    }

    private static void patchAdd(JsonElement parent, String child, JsonElement meta) throws JsonSchemaException {
	if (parent.isJsonObject())
	    parent.getAsJsonObject().add(child, meta);
	else if (parent.isJsonArray()) {
	    JsonArray array = parent.getAsJsonArray();
	    int index = -1;
	    if (child.equals("-"))
		index = array.size();
	    else {
		try {
		    index = Integer.parseInt(child);
		}
		catch (NumberFormatException ex) {
		    throw new JsonSchemaException("Path component " + child + " is not a valid array index");
		}

		if (index < 0 || index > array.size())
		    throw new JsonSchemaException("Path component " + child + " is not a valid array index");
	    }

	    if (index == array.size())
		array.add(meta);
	    else {
		JsonElement[] temp = new JsonElement[array.size() - index];
		for (int i = index; i < array.size(); ++i)
		    temp[i - index] = array.get(i);

		array.set(index, meta);
		for (int i = index + 1; i < array.size(); ++i)
		    array.set(i, temp[i - index - 1]);

		array.add(temp[temp.length - 1]);
	    }
	}
    }

    @Nullable
    private static JsonElement patchRemove(JsonElement parent, String child) throws JsonSchemaException {
	if (parent.isJsonObject())
	    return parent.getAsJsonObject().remove(child);
	else if (parent.isJsonArray()) {
	    JsonArray array = parent.getAsJsonArray();
	    int index = -1;
	    if (child.equals("-"))
		throw new JsonSchemaException("Path component - is not a valid array index for remove operations");

	    try {
		index = Integer.parseInt(child);
	    }
	    catch (NumberFormatException ex) {
		throw new JsonSchemaException("Path component " + child + " is not a valid array index");
	    }

	    if (index < 0 || index >= array.size())
		throw new JsonSchemaException("Path component " + child + " is not a valid array index");

	    return array.remove(index);
	}

	return null;
    }

    @Nullable
    private static JsonElement patchTest(JsonElement parent, String child) throws JsonSchemaException {
	if (parent.isJsonObject())
	    return parent.getAsJsonObject().get(child);
	else if (parent.isJsonArray()) {
	    JsonArray array = parent.getAsJsonArray();
	    int index = -1;
	    if (child.equals("-"))
		throw new JsonSchemaException("Path component - is not a valid array index for test operations");

	    try {
		index = Integer.parseInt(child);
	    }
	    catch (NumberFormatException ex) {
		throw new JsonSchemaException("Path component " + child + " is not a valid array index");
	    }

	    if (index < 0 || index >= array.size())
		throw new JsonSchemaException("Path component " + child + " is not a valid array index");

	    return array.get(index);
	}

	return null;
    }

    @Nullable
    private static Pair<JsonElement, String> parsePath(JsonElement top, String fullPath) {
	JsonElement parent = top;
	String[] path = fullPath.split("/");
	for (int i = 0; i < path.length; ++i) {
	    path[i] = path[i].replace("~1", "/").replace("~0", "~");
	    if (i < path.length - 1) {
		if (parent.isJsonObject()) {
		    parent = parent.getAsJsonObject().get(path[i]);
		    if (parent == null)
			throw new JsonSchemaException("Path component " + path[i] + " not found");
		}
		else if (parent.isJsonArray()) {
		    int index = -1;
		    if (path[i].equals("-"))
			throw new JsonSchemaException("Path component " + path[i] + " is not allowed here");

		    try {
			index = Integer.parseInt(path[i]);
		    }
		    catch (NumberFormatException ex) {
			throw new JsonSchemaException("Path component " + path[i] + " is not a valid array index");
		    }

		    parent = parent.getAsJsonArray().get(index);
		    if (parent == null)
			throw new JsonSchemaException("Path component " + path[i] + " not found");
		}
		else
		    throw new JsonSchemaException("Path component " + path[i] + " is not an object or array");
	    }
	}

	return Pair.of(parent, path[path.length - 1]);
    }

    public static JsonPatch parsePatch(JsonObject patch) throws JsonSchemaException {
	JsonPrimitive path = JsonUtils.getPrimitiveOrThrow("path", patch);
	JsonPrimitive op = JsonUtils.getPrimitiveOrThrow("op", patch);
	JsonElement meta = null;
	switch (op.getAsString()) {
	case "add":
	case "replace":
	case "test":
	    meta = JsonUtils.getOrThrow("value", patch);
	    break;
	case "copy":
	case "move":
	    meta = JsonUtils.getOrThrow("from", patch);
	    break;
	case "remove": break;
	default: throw new JsonSchemaException("invalid op");
	}

	return new JsonPatch(JsonPatch.PatchOp.fromString(op.getAsString()), path.getAsString(),
		meta != null ? meta : JsonNull.INSTANCE);
    }

    public static boolean applyPatch(JsonObject working, JsonPatch p) {
	Pair<JsonElement, String> path = parsePath(working, p.path);
	if (path == null)
	    return false;

	Logger log = ThaumcraftFix.instance.getLogger();
	switch (p.op) {
	case ADD: {
	    patchAdd(path.getLeft(), path.getRight(), p.meta);
	    break;
	}
	case REMOVE: {
	    if (patchRemove(path.getLeft(), path.getRight()) == null)
		log.warn("Key " + path.getRight() + " was supposed to be removed, but already did not exist");

	    break;
	}
	case COPY: {
	    Pair<JsonElement, String> from = parsePath(working, p.meta.getAsString());
	    if (from == null)
		return false;

	    JsonElement val = getValue(from.getLeft(), from.getRight());
	    if (val == null)
		return false;

	    patchAdd(path.getLeft(), path.getRight(), val);
	    break;
	}
	case MOVE: {
	    Pair<JsonElement, String> from = parsePath(working, p.meta.getAsString());
	    if (from == null)
		return false;

	    JsonElement val = patchRemove(from.getLeft(), from.getRight());
	    if (val == null)
		return false;

	    patchAdd(path.getLeft(), path.getRight(), val);
	    break;
	}
	case REPLACE: {
	    if (patchRemove(path.getLeft(), path.getRight()) == null)
		log.warn("Key " + path.getRight() + " did not exist for replace");

	    patchAdd(path.getLeft(), path.getRight(), p.meta);
	    break;
	}
	case TEST: {
	    JsonElement val = patchTest(path.getLeft(), path.getRight());
	    if (val == null)
		log.warn("Key " + path.getRight() + " did not exist for test");
	    else if (!val.equals(p.meta))
		return false;

	    break;
	}
	default: break;
	}

	return true;
    }

}
