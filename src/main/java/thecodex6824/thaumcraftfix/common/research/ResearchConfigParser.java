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

package thecodex6824.thaumcraftfix.common.research;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import net.minecraft.util.ResourceLocation;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.IScanThing;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ScanningManager;
import thecodex6824.thaumcraftfix.ThaumcraftFix;
import thecodex6824.thaumcraftfix.api.ResearchApi;
import thecodex6824.thaumcraftfix.common.event.AdvancementResearchEventHandler;
import thecodex6824.thaumcraftfix.common.json.JsonSchemaException;
import thecodex6824.thaumcraftfix.common.json.JsonUtils;

public final class ResearchConfigParser {

    private ResearchConfigParser() {}

    public static boolean loadCategories() {
	boolean researchErrors = false;
	Logger log = ThaumcraftFix.instance.getLogger();
	JsonParser parser = new JsonParser();
	File cats = new File("config/tcresearchpatcher", "categories.json");
	if (cats.isFile()) {
	    try (FileInputStream s = new FileInputStream(cats)) {
		String content = IOUtils.toString(s, StandardCharsets.UTF_8);
		JsonElement element = parser.parse(content);
		List<JsonObject> objects = JsonUtils.getObjectOrArrayContainedObjects(element);
		for (JsonObject o : objects) {
		    JsonPrimitive key = JsonUtils.getPrimitiveOrThrow("key", o);
		    JsonPrimitive requirement = JsonUtils.getPrimitiveOrThrow("requirement", o);
		    AspectList list = new AspectList();
		    JsonObject aspects = JsonUtils.tryGetObject("aspects", o).or(new JsonObject());
		    for (Map.Entry<String, JsonElement> pair : aspects.entrySet()) {
			if (pair.getValue().isJsonPrimitive()) {
			    Aspect aspect = Aspect.getAspect(pair.getKey());
			    if (aspect == null)
				throw new JsonSchemaException(aspects + ": Invalid aspect entry: invalid aspect tag");

			    int amount = -1;
			    try {
				amount = pair.getValue().getAsInt();
			    }
			    catch (ClassCastException ex) {
				throw new JsonSchemaException(aspects + ": Invalid aspect entry: invalid amount");
			    }

			    if (amount > 0)
				list.add(aspect, amount);
			}
		    }

		    JsonPrimitive icon = JsonUtils.getPrimitiveOrThrow("icon", o);
		    JsonPrimitive background = JsonUtils.getPrimitiveOrThrow("background", o);
		    JsonPrimitive backgroundOverlay = JsonUtils.getPrimitiveOrThrow("backgroundOverlay", o);
		    ResearchCategories.registerCategory(key.getAsString(), requirement.getAsString(), list,
			    new ResourceLocation(icon.getAsString()), new ResourceLocation(background.getAsString()),
			    new ResourceLocation(backgroundOverlay.getAsString())
			    );
		}
	    }
	    catch (Exception ex) {
		log.error("categories.json: Error reading file: " + ex.getMessage());
		for (Throwable t : ex.getSuppressed())
		    log.error("Suppressed error: " + t.getMessage());

		researchErrors = true;
	    }
	}

	return researchErrors;
    }

    public static boolean loadScans() {
	boolean researchErrors = false;
	Logger log = ThaumcraftFix.instance.getLogger();
	JsonParser parser = new JsonParser();
	File scans = new File("config/tcresearchpatcher", "scans");
	if (scans.isDirectory()) {
	    File[] files = scans.listFiles(new FileFilter() {
		@Override
		public boolean accept(File f) {
		    return f.isFile() && f.getName().endsWith(".json");
		}
	    });
	    for (File f : files) {
		try (FileInputStream s = new FileInputStream(f)) {
		    String content = IOUtils.toString(s, StandardCharsets.UTF_8);
		    JsonElement element = parser.parse(content);
		    List<JsonObject> objects = JsonUtils.getObjectOrArrayContainedObjects(element);
		    int scanKeys = 0;
		    int totalScans = 0;
		    for (JsonObject o : objects) {
			String key = JsonUtils.getPrimitiveOrThrow("key", o).getAsString();
			ResourceLocation type = new ResourceLocation(JsonUtils.getPrimitiveOrThrow("type", o).getAsString());
			JsonElement obj = JsonUtils.getOrThrow("object", o);
			for (IScanThing thing : ResearchApi.parseScans(key, type, obj)) {
			    ScanningManager.addScannableThing(thing);
			    ++totalScans;
			}

			++scanKeys;
		    }

		    log.info("scans/" + f.getName() + ": loaded " + scanKeys + " scan keys with " + totalScans + " scan entries");
		}
		catch (Exception ex) {
		    log.error("scans/" + f.getName() + ": Error reading file: " + ex.getMessage());
		    for (Throwable t : ex.getSuppressed())
			log.error("Suppressed error: " + t.getMessage());

		    researchErrors = true;
		}
	    }
	}

	return researchErrors;
    }

    public static boolean loadAdvancements() {
	boolean researchErrors = false;
	Logger log = ThaumcraftFix.instance.getLogger();
	JsonParser parser = new JsonParser();
	File advancements = new File("config/tcresearchpatcher", "advancements");
	if (advancements.isDirectory()) {
	    File[] files = advancements.listFiles(new FileFilter() {
		@Override
		public boolean accept(File f) {
		    return f.isFile() && f.getName().endsWith(".json");
		}
	    });
	    for (File f : files) {
		try (FileInputStream s = new FileInputStream(f)) {
		    String content = IOUtils.toString(s, StandardCharsets.UTF_8);
		    JsonElement element = parser.parse(content);
		    int total = 0;
		    for (JsonObject o : JsonUtils.getObjectOrArrayContainedObjects(element)) {
			AdvancementResearchInfo info = new AdvancementResearchInfo(o);
			AdvancementResearchEventHandler.addAdvancementInfo(info.getAdvancementKey(), info);
			++total;
		    }

		    log.info("advancements/" + f.getName() + ": loaded " + total + " advancement keys");
		} catch (Exception ex) {
		    log.error("advancements/" + f.getName() + ": Error reading file: " + ex.getMessage());
		    for (Throwable t : ex.getSuppressed())
			log.error("Suppressed error: " + t.getMessage());

		    researchErrors = true;
		}
	    }
	}

	return researchErrors;
    }

}
