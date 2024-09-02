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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonUtils {

    public static Optional<JsonElement> tryGet(String key, JsonObject obj) {
	JsonElement e = obj.get(key);
	if (e == null)
	    return Optional.absent();

	return Optional.of(e);
    }

    public static JsonElement getOrThrow(String key, JsonObject obj) throws JsonSchemaException {
	return getOrThrow(key, obj, null);
    }

    public static JsonElement getOrThrow(String key, JsonObject obj, @Nullable String parentKey) throws JsonSchemaException {
	JsonElement e = obj.get(key);
	if (e == null)
	    throw new JsonSchemaException("Key " + key + (parentKey != null ? " on object " + parentKey : "") + " is missing");

	return e;
    }

    public static Optional<JsonPrimitive> tryGetPrimitive(String key, JsonObject obj) {
	JsonElement e = obj.get(key);
	if (e == null || !e.isJsonPrimitive())
	    return Optional.absent();

	return Optional.of(e.getAsJsonPrimitive());
    }

    public static JsonPrimitive getPrimitiveOrThrow(String key, JsonObject obj) throws JsonSchemaException {
	return getPrimitiveOrThrow(key, obj, null);
    }

    public static JsonPrimitive getPrimitiveOrThrow(String key, JsonObject obj, @Nullable String parentKey) throws JsonSchemaException {
	JsonElement e = obj.get(key);
	if (e == null)
	    throw new JsonSchemaException("Key " + key + (parentKey != null ? " on object " + parentKey : "") + " is missing");
	else if (!e.isJsonPrimitive())
	    throw new JsonSchemaException("Key " + key + (parentKey != null ? " on object " + parentKey : "") + " does not have a primitive value");

	return e.getAsJsonPrimitive();
    }

    public static Optional<JsonArray> tryGetArray(String key, JsonObject obj) {
	JsonElement e = obj.get(key);
	if (e == null || !e.isJsonArray())
	    return Optional.absent();

	return Optional.of(e.getAsJsonArray());
    }

    public static JsonArray getArrayOrThrow(String key, JsonObject obj) throws JsonSchemaException {
	return getArrayOrThrow(key, obj, null);
    }

    public static JsonArray getArrayOrThrow(String key, JsonObject obj, @Nullable String parentKey) throws JsonSchemaException {
	JsonElement e = obj.get(key);
	if (e == null)
	    throw new JsonSchemaException("Key " + key + (parentKey != null ? " on object " + parentKey : "") + " is missing");
	else if (!e.isJsonArray())
	    throw new JsonSchemaException("Key " + key + (parentKey != null ? " on object " + parentKey : "") + " does not have an array value");

	return e.getAsJsonArray();
    }

    public static Optional<JsonObject> tryGetObject(String key, JsonObject obj) {
	JsonElement e = obj.get(key);
	if (e == null || !e.isJsonObject())
	    return Optional.absent();

	return Optional.of(e.getAsJsonObject());
    }

    public static JsonObject getObjectOrThrow(String key, JsonObject obj) throws JsonSchemaException {
	return getObjectOrThrow(key, obj, null);
    }

    public static JsonObject getObjectOrThrow(String key, JsonObject obj, @Nullable String parentKey) throws JsonSchemaException {
	JsonElement e = obj.get(key);
	if (e == null)
	    throw new JsonSchemaException("Key " + key + (parentKey != null ? " on object " + parentKey : "") + " is missing");
	else if (!e.isJsonObject())
	    throw new JsonSchemaException("Key " + key + (parentKey != null ? " on object " + parentKey : "") + " does not have an object value");

	return e.getAsJsonObject();
    }

    public static List<JsonObject> getObjectOrArrayContainedObjects(JsonElement element) throws JsonSchemaException {
	return getObjectOrArrayContainedObjects(element, false);
    }

    public static List<JsonObject> getObjectOrArrayContainedObjects(JsonElement element, boolean allowSkipInvalid) throws JsonSchemaException {
	if (element.isJsonPrimitive())
	    throw new JsonSchemaException("Value must be an object or array");
	else if (element.isJsonObject())
	    return Lists.newArrayList(element.getAsJsonObject());
	else {
	    ArrayList<JsonObject> objects = new ArrayList<>();
	    for (JsonElement e : element.getAsJsonArray()) {
		if (e.isJsonObject())
		    objects.add(e.getAsJsonObject());
		else if (!allowSkipInvalid)
		    throw new JsonSchemaException("Array must only contain objects");
	    }

	    return objects;
	}
    }

}
