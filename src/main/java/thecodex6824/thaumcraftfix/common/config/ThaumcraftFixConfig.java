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

package thecodex6824.thaumcraftfix.common.config;

import java.util.Optional;
import java.util.OptionalInt;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import thecodex6824.thaumcraftfix.ThaumcraftFix;

public class ThaumcraftFixConfig {

    public static class ConfigValue<T> {
	private Optional<T> forcedValue;
	private T value;

	public ConfigValue(T initialValue) {
	    forcedValue = Optional.empty();
	    value = initialValue;
	}

	public T value() {
	    return forcedValue.orElse(value);
	}

	public T userValue() {
	    return value;
	}

	public void setUserValue(T val) {
	    value = val;
	}

	public void setSessionValue(T val) {
	    forcedValue = Optional.of(val);
	}

	public void unsetSessionValue() {
	    forcedValue = Optional.empty();
	}
    }

    public static class ConfigBoolean {
	private OptionalInt forcedValue;
	private boolean value;

	public ConfigBoolean(boolean initialValue) {
	    forcedValue = OptionalInt.empty();
	    value = initialValue;
	}

	public boolean value() {
	    if (forcedValue.isPresent()) {
		return forcedValue.getAsInt() == 1;
	    }

	    return value;
	}

	public boolean userValue() {
	    return value;
	}

	public void setUserValue(boolean val) {
	    value = val;
	}

	public void setSessionValue(boolean val) {
	    forcedValue = OptionalInt.of(val ? 1 : 0);
	}

	public void unsetSessionValue() {
	    forcedValue = OptionalInt.empty();
	}
    }

    public static class ItemConfig {
	public ConfigBoolean primordialPearlDamageFix = new ConfigBoolean(true);
    }

    public static class WorldConfig {

	public static class AuraConfig {
	    public ConfigBoolean controlAura = new ConfigBoolean(false);
	    public ConfigBoolean biomeAllowList = new ConfigBoolean(false);
	    public ConfigValue<String[]> biomeList = new ConfigValue<>(new String[0]);
	    public ConfigBoolean dimAllowList = new ConfigBoolean(false);
	    public ConfigValue<String[]> dimList = new ConfigValue<>(new String[0]);
	}

	public static class CrystalsConfig {
	    public ConfigBoolean controlCrystals = new ConfigBoolean(false);
	    public ConfigBoolean biomeAllowList = new ConfigBoolean(false);
	    public ConfigValue<String[]> biomeList = new ConfigValue<>(new String[0]);
	    public ConfigBoolean dimAllowList = new ConfigBoolean(false);
	    public ConfigValue<String[]> dimList = new ConfigValue<>(new String[0]);
	}

	public static class VegetationConfig {
	    public ConfigBoolean controlVegetation = new ConfigBoolean(false);
	    public ConfigBoolean biomeAllowList = new ConfigBoolean(false);
	    public ConfigValue<String[]> biomeList = new ConfigValue<>(new String[0]);
	    public ConfigBoolean dimAllowList = new ConfigBoolean(false);
	    public ConfigValue<String[]> dimList = new ConfigValue<>(new String[0]);
	}

	public AuraConfig aura = new AuraConfig();
	public CrystalsConfig crystals = new CrystalsConfig();
	public VegetationConfig vegetation = new VegetationConfig();

    }

    public ItemConfig item = new ItemConfig();
    public WorldConfig world = new WorldConfig();

    public void bind() {
	item.primordialPearlDamageFix.setUserValue(ThaumcraftFixConfigImpl.item.primordialPearlDamageFix);

	world.aura.biomeAllowList.setUserValue(ThaumcraftFixConfigImpl.world.aura.biomeAllowList);
	world.aura.biomeList.setUserValue(ThaumcraftFixConfigImpl.world.aura.biomeList);
	world.aura.controlAura.setUserValue(ThaumcraftFixConfigImpl.world.aura.controlAura);
	world.aura.dimAllowList.setUserValue(ThaumcraftFixConfigImpl.world.aura.dimAllowList);
	world.aura.dimList.setUserValue(ThaumcraftFixConfigImpl.world.aura.dimList);

	world.crystals.biomeAllowList.setUserValue(ThaumcraftFixConfigImpl.world.crystals.biomeAllowList);
	world.crystals.biomeList.setUserValue(ThaumcraftFixConfigImpl.world.crystals.biomeList);
	world.crystals.controlCrystals.setUserValue(ThaumcraftFixConfigImpl.world.crystals.controlCrystals);
	world.crystals.dimAllowList.setUserValue(ThaumcraftFixConfigImpl.world.crystals.dimAllowList);
	world.crystals.dimList.setUserValue(ThaumcraftFixConfigImpl.world.crystals.dimList);

	world.vegetation.biomeAllowList.setUserValue(ThaumcraftFixConfigImpl.world.vegetation.biomeAllowList);
	world.vegetation.biomeList.setUserValue(ThaumcraftFixConfigImpl.world.vegetation.biomeList);
	world.vegetation.controlVegetation.setUserValue(ThaumcraftFixConfigImpl.world.vegetation.controlVegetation);
	world.vegetation.dimAllowList.setUserValue(ThaumcraftFixConfigImpl.world.vegetation.dimAllowList);
	world.vegetation.dimList.setUserValue(ThaumcraftFixConfigImpl.world.vegetation.dimList);
    }

    private static final String ITEM_CAT = "item";
    private static final String PEARL = "primordialPearlDamageFix";

    public void deserializeNetwork(JsonObject json) {
	try {
	    JsonElement itemCat = json.get(ITEM_CAT);
	    if (itemCat != null) {
		JsonElement pearl = itemCat.getAsJsonObject().get(PEARL);
		if (pearl != null) {
		    item.primordialPearlDamageFix.setSessionValue(pearl.getAsBoolean());
		}
	    }
	}
	catch (Exception ex) {
	    ThaumcraftFix.instance.getLogger().warn("Got invalid data in serialized config");
	}
    }

    public JsonObject serializeNetwork() {
	JsonObject rootCat = new JsonObject();
	JsonObject itemCat = new JsonObject();
	// we must use the user value here as singleplayer servers will be affected by the enforced value
	itemCat.add(PEARL, new JsonPrimitive(item.primordialPearlDamageFix.userValue()));
	rootCat.add(ITEM_CAT, itemCat);
	return rootCat;
    }

}
