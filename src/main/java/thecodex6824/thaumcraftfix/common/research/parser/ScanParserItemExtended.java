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

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.research.IScanThing;
import thaumcraft.api.research.ScanItem;
import thecodex6824.thaumcraftfix.api.scan.IScanParser;
import thecodex6824.thaumcraftfix.common.json.JsonSchemaException;
import thecodex6824.thaumcraftfix.common.json.JsonUtils;
import thecodex6824.thaumcraftfix.common.research.ScanItemExtended;

public class ScanParserItemExtended implements IScanParser {

    protected IScanThing parseElement(String key, JsonElement e) {
	if (e.isJsonArray())
	    throw new JsonSchemaException("Invalid object entry: must be object or primitive");

	if (e.isJsonPrimitive()) {
	    ResourceLocation loc = new ResourceLocation(e.getAsString());
	    Item item = ForgeRegistries.ITEMS.getValue(loc);
	    if (item == null)
		throw new NullPointerException(key + ": Item " + loc + " does not exist");

	    return new ScanItem(key, new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE));
	}
	else {
	    ResourceLocation loc = new ResourceLocation(JsonUtils.getPrimitiveOrThrow("name", e.getAsJsonObject()).getAsString());
	    JsonPrimitive damage = JsonUtils.tryGetPrimitive("meta", e.getAsJsonObject()).orNull();
	    JsonPrimitive strictVal = JsonUtils.tryGetPrimitive("strict", e.getAsJsonObject()).orNull();
	    boolean strict = strictVal != null ? strictVal.getAsBoolean() : false;
	    JsonElement nbt = JsonUtils.tryGet("nbt", e.getAsJsonObject()).orNull();
	    if (nbt != null && nbt.isJsonArray())
		throw new JsonSchemaException(key + ": nbt must be object or string");
	    JsonElement caps = JsonUtils.tryGet("caps", e.getAsJsonObject()).orNull();
	    if (caps != null && caps.isJsonArray())
		throw new JsonSchemaException(key + ": caps must be object or string");

	    Item item = ForgeRegistries.ITEMS.getValue(loc);
	    if (item == null)
		throw new NullPointerException(key + ": Item " + loc + " does not exist");

	    int meta = strict ? 0 : OreDictionary.WILDCARD_VALUE;
	    if (damage != null) {
		try {
		    meta = damage.getAsInt();
		}
		catch (NumberFormatException ex) {
		    throw new JsonSchemaException(key + ": Meta value " + damage.getAsString() + " is not valid");
		}
	    }

	    NBTTagCompound capNbt = null;
	    if (caps != null) {
		try {
		    capNbt = JsonToNBT.getTagFromJson(caps.isJsonObject() ?
			    new Gson().toJson(caps.getAsJsonObject()) : caps.getAsString());
		}
		catch (NBTException ex) {
		    throw new JsonSchemaException(key + ": Invalid caps nbt: " + ex);
		}
	    }

	    ItemStack stack = new ItemStack(item, 1, meta);
	    if (nbt != null) {
		try {
		    stack.setTagCompound(JsonToNBT.getTagFromJson(nbt.isJsonObject() ?
			    new Gson().toJson(nbt.getAsJsonObject()) : nbt.getAsString()));
		}
		catch (NBTException ex) {
		    throw new JsonSchemaException(key + ": Invalid nbt: " + ex);
		}
	    }

	    return new ScanItemExtended(key, stack, capNbt, strict);
	}
    }

    @Override
    public boolean matches(ResourceLocation type) {
	return type.getNamespace().equals("tcresearchpatcher") && type.getPath().equals("item_extended");
    }

    @Override
    public Collection<IScanThing> parseScan(String key, ResourceLocation type, JsonElement input) {
	if (input.isJsonArray()) {
	    ArrayList<IScanThing> things = new ArrayList<>();
	    for (JsonElement e : input.getAsJsonArray())
		things.add(parseElement(key, e));

	    return things;
	}
	else
	    return Lists.newArrayList(parseElement(key, input));
    }

}
