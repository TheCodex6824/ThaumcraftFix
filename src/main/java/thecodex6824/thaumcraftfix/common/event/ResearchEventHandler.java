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

package thecodex6824.thaumcraftfix.common.event;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thaumcraft.api.internal.CommonInternals;
import thecodex6824.thaumcraftfix.ThaumcraftFix;
import thecodex6824.thaumcraftfix.api.ThaumcraftFixApi;
import thecodex6824.thaumcraftfix.api.event.research.ResearchEntryLoadEvent;
import thecodex6824.thaumcraftfix.api.event.research.ResearchLoadEvent;
import thecodex6824.thaumcraftfix.api.internal.ThaumcraftFixApiBridge;
import thecodex6824.thaumcraftfix.api.internal.ThaumcraftFixApiBridge.InternalImplementation.ResearchPatchSource;
import thecodex6824.thaumcraftfix.common.json.JsonPatch;
import thecodex6824.thaumcraftfix.common.json.JsonSchemaException;
import thecodex6824.thaumcraftfix.common.json.JsonUtils;
import thecodex6824.thaumcraftfix.common.json.PatchHelper;

@EventBusSubscriber(modid = ThaumcraftFixApi.MODID)
public final class ResearchEventHandler {

    private ResearchEventHandler() {}

    private static final HashMap<String, ArrayList<ArrayList<JsonPatch>>> PATCHES = new HashMap<>();
    private static final List<ResourceLocation> SOURCES = new ArrayList<>();

    // gson exposes deep copy in a later version than forge ships in 1.12
    private static final Method JSON_DEEP_COPY;

    static {
	try {
	    JSON_DEEP_COPY = JsonElement.class.getDeclaredMethod("deepCopy");
	    JSON_DEEP_COPY.setAccessible(true);
	}
	catch (Exception ex) {
	    throw new RuntimeException(ex);
	}
    }

    @SubscribeEvent
    public static void onStartLoadResearch(ResearchLoadEvent.Pre event) {
	SOURCES.clear();
	SOURCES.addAll(ThaumcraftFixApiBridge.implementation().getFilesystemResearchEntrySources());
	for (ResourceLocation res : SOURCES) {
	    CommonInternals.jsonLocs.put(res.toString(), res);
	}

	// in case we are reloading and something broke the exit patch
	PATCHES.clear();
	Logger log = ThaumcraftFix.instance.getLogger();
	JsonParser parser = new JsonParser();
	for (ResearchPatchSource source : ThaumcraftFixApiBridge.implementation().getResearchPatchSources()) {
	    try {
		for (Map.Entry<String, ? extends InputStream> entry : source.open().entrySet()) {
		    try {
			String content = IOUtils.toString(entry.getValue(), StandardCharsets.UTF_8);
			JsonElement element = parser.parse(content);
			List<JsonObject> objects = JsonUtils.getObjectOrArrayContainedObjects(element);
			for (JsonObject o : objects) {
			    JsonPrimitive key = JsonUtils.getPrimitiveOrThrow("key", o);
			    JsonArray ops = JsonUtils.getArrayOrThrow("ops", o);
			    ArrayList<JsonPatch> insertTo = new ArrayList<>();
			    for (JsonElement e : ops.getAsJsonArray()) {
				if (!e.isJsonObject()) {
				    throw new JsonSchemaException(e + ": Patch entry not an object");
				}

				insertTo.add(PatchHelper.parsePatch(e.getAsJsonObject()));
			    }

			    ArrayList<ArrayList<JsonPatch>> list = PATCHES.get(key.getAsString());
			    if (list == null) {
				list = new ArrayList<>();
				PATCHES.put(key.getAsString(), list);
			    }

			    list.add(insertTo);
			}
		    }
		    catch (Exception ex) {
			log.error("File {}: Error processing patch: {}", entry.getKey(), ex.getMessage());
		    }
		    finally {
			entry.getValue().close();
		    }
		}
	    }
	    catch (IOException ex) {
		log.error("File {}: Error reading file(s): {}", source.getDescriptor(), ex.getMessage());
	    }
	}
    }

    @SubscribeEvent
    public static void onEndLoadResearch(ResearchLoadEvent.Post event) {
	// no need to waste memory on holding patches
	PATCHES.clear();
	for (ResourceLocation res : SOURCES) {
	    CommonInternals.jsonLocs.remove(res.toString());
	}
	SOURCES.clear();
    }

    @SuppressWarnings("unchecked")
    private static <T extends JsonElement> T deepCopy(T element) {
	try {
	    return (T) JSON_DEEP_COPY.invoke(element);
	}
	catch (Exception ex) {
	    throw new RuntimeException(ex);
	}
    }

    @SubscribeEvent
    public static void onLoadResearchEntry(ResearchEntryLoadEvent.Pre event) {
	boolean allowLoading = true;
	Logger log = ThaumcraftFix.instance.getLogger();
	JsonObject original = event.getResearchJson();
	JsonElement key = original.get("key");
	if (key != null && key.isJsonPrimitive()) {
	    ArrayList<ArrayList<JsonPatch>> apply = PATCHES.get(key.getAsString());
	    if (apply != null) {
		for (ArrayList<JsonPatch> patchList : apply) {
		    if (!patchList.isEmpty()) {
			boolean applyChanges = true;
			JsonObject working = deepCopy(original);
			for (JsonPatch p : patchList) {
			    try {
				applyChanges &= PatchHelper.applyPatch(working, p);
			    }
			    catch (JsonSchemaException ex) {
				log.warn(ex.getMessage());
				applyChanges = false;
			    }

			    if (!applyChanges) {
				break;
			    }
			}

			if (applyChanges) {
			    // removing directly results in CME
			    HashSet<String> toRemove = new HashSet<>();
			    for (Map.Entry<String, JsonElement> entry : original.entrySet()) {
				toRemove.add(entry.getKey());
			    }

			    for (String s : toRemove) {
				original.remove(s);
			    }

			    for (Map.Entry<String, JsonElement> entry : working.entrySet()) {
				original.add(entry.getKey(), deepCopy(entry.getValue()));
			    }
			}
		    }
		}

		allowLoading = JsonUtils.tryGetPrimitive("key", original).isPresent();
	    }

	}
	else {
	    // we don't have file info so dump json to debug log
	    log.error("A research entry is missing a key (before patching), it will not be loaded. See the debug log for a json dump");
	    log.debug(original.toString());
	    allowLoading = false;
	}

	event.setCanceled(!allowLoading);
    }

}
