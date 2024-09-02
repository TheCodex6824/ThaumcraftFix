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
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import thaumcraft.api.research.IScanThing;
import thaumcraft.api.research.ScanBlock;
import thaumcraft.api.research.ScanBlockState;
import thecodex6824.thaumcraftfix.api.scan.IScanParser;
import thecodex6824.thaumcraftfix.common.json.JsonSchemaException;
import thecodex6824.thaumcraftfix.common.json.JsonUtils;

public class ScanParserBlock implements IScanParser {

    protected static <T extends Comparable<T>> IBlockState getStateWithProperty(IBlockState state, IProperty<T> prop, Comparable<?> val) {
	return state.withProperty(prop, prop.getValueClass().cast(val));
    }

    @Nullable
    protected static IBlockState stateFromString(Block block, String str) {
	Map<IProperty<?>, Comparable<?>> map = null;
	if (str.equals("default"))
	    map = block.getDefaultState().getProperties();
	else {
	    if (str.startsWith("[") && str.endsWith("]"))
		str = str.substring(1, str.length() - 1);

	    ImmutableMap.Builder<IProperty<?>, Comparable<?>> builder = ImmutableMap.builder();
	    BlockStateContainer container = block.getBlockState();
	    for (String s : str.split(",")) {
		String[] equalsSplit = s.split("=", 2);
		if (equalsSplit.length < 2)
		    return null;

		IProperty<?> prop = container.getProperty(equalsSplit[0]);
		if (prop == null)
		    return null;

		Comparable<?> comp = prop.parseValue(equalsSplit[1]).orNull();
		if (comp == null)
		    return null;

		builder.put(prop, comp);
	    }

	    map = builder.build();
	}

	IBlockState state = block.getDefaultState();
	for (Map.Entry<IProperty<?>, Comparable<?>> entry : map.entrySet())
	    state = getStateWithProperty(state, entry.getKey(),entry.getValue());

	return state;
    }

    protected IScanThing parseElement(String key, JsonElement e) {
	if (e.isJsonArray())
	    throw new JsonSchemaException(key + ": Invalid object entry: must be object or primitive");

	if (e.isJsonPrimitive()) {
	    ResourceLocation loc = new ResourceLocation(e.getAsString());
	    Block block = ForgeRegistries.BLOCKS.getValue(loc);
	    if (block == null)
		throw new NullPointerException(key + ": Block " + loc + " does not exist");

	    return new ScanBlock(key, block);
	}
	else {
	    ResourceLocation loc = new ResourceLocation(JsonUtils.getPrimitiveOrThrow("name", e.getAsJsonObject()).getAsString());
	    JsonPrimitive state = JsonUtils.tryGetPrimitive("state", e.getAsJsonObject()).orNull();
	    String blockstate = state != null ? state.getAsString() : null;
	    Block block = ForgeRegistries.BLOCKS.getValue(loc);
	    if (block == null)
		throw new NullPointerException(key + ": Block " + loc + " does not exist");

	    if (blockstate != null) {
		IBlockState bs = stateFromString(block, blockstate);
		if (bs == null)
		    throw new NullPointerException(key + ": BlockState " + blockstate + " is invalid for " + block.getBlockState());

		return new ScanBlockState(key, bs);
	    }
	    else
		return new ScanBlock(key, block);
	}
    }

    @Override
    public boolean matches(ResourceLocation type) {
	return type.getNamespace().equals("thaumcraft") && type.getPath().equals("block");
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
