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

package thecodex6824.thaumcraftfix.common.research.parser;

import java.util.ArrayList;
import java.util.Collection;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.research.IScanThing;
import thaumcraft.api.research.ScanEntity;
import thecodex6824.thaumcraftfix.api.scan.IScanParser;
import thecodex6824.thaumcraftfix.common.json.JsonSchemaException;
import thecodex6824.thaumcraftfix.common.json.JsonUtils;

public class ScanParserEntity implements IScanParser {

    protected IScanThing parseElement(String key, JsonElement e) {
	if (e.isJsonArray())
	    throw new JsonSchemaException(key + ": Invalid object entry: must be object or primitive");

	if (e.isJsonPrimitive()) {
	    ResourceLocation loc = new ResourceLocation(e.getAsString());
	    EntityEntry entry = ForgeRegistries.ENTITIES.getValue(loc);
	    if (entry == null)
		throw new NullPointerException(key + ": Entity " + loc + " does not exist");

	    return new ScanEntity(key, entry.getEntityClass(), true);
	}
	else {
	    ResourceLocation loc = new ResourceLocation(JsonUtils.getPrimitiveOrThrow("name", e.getAsJsonObject()).getAsString());
	    JsonPrimitive inherit = JsonUtils.tryGetPrimitive("inherit", e.getAsJsonObject()).orNull();
	    JsonElement nbt = JsonUtils.tryGet("nbt", e.getAsJsonObject()).orNull();
	    if (nbt != null && nbt.isJsonArray())
		throw new JsonSchemaException(key + ": nbt must be object or string");

	    EntityEntry entry = ForgeRegistries.ENTITIES.getValue(loc);
	    if (entry == null)
		throw new NullPointerException(key + ": Entity " + loc + " does not exist");

	    ThaumcraftApi.EntityTagsNBT[] tcNbt = null;
	    if (nbt != null) {
		try {
		    NBTTagCompound tag = JsonToNBT.getTagFromJson(nbt.isJsonObject() ?
			    new Gson().toJson(nbt.getAsJsonObject()) : nbt.getAsString());
		    tcNbt = new ThaumcraftApi.EntityTagsNBT[tag.getSize()];
		    int i = 0;
		    for (String s : tag.getKeySet()) {
			tcNbt[i] = new ThaumcraftApi.EntityTagsNBT(s, ThaumcraftApiHelper.getNBTDataFromId(tag, tag.getTagId(s), s));
			++i;
		    }
		}
		catch (NBTException ex) {
		    throw new JsonSchemaException(key + ": Invalid nbt: " + ex);
		}
	    }

	    return new ScanEntity(key, entry.getEntityClass(), inherit != null ? inherit.getAsBoolean() : true, tcNbt);
	}
    }

    @Override
    public boolean matches(ResourceLocation type) {
	return type.getNamespace().equals("thaumcraft") && type.getPath().equals("entity");
    }

    @Override
    public Collection<IScanThing> parseScan(String key, ResourceLocation type, JsonElement input) {
	if (input.isJsonArray()) {
	    ArrayList<IScanThing> things = new ArrayList<>();
	    for (JsonElement e : input.getAsJsonArray()) {
		things.add(parseElement(key, e));
	    }

	    return things;
	}
	else
	    return Lists.newArrayList(parseElement(key, input));
    }

}
