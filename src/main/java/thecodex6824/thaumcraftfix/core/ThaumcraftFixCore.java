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

package thecodex6824.thaumcraftfix.core;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.spongepowered.asm.mixin.Mixins;

import com.google.common.collect.ImmutableList;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

@Name("Thaumcraft Fix Core Plugin")
@MCVersion("1.12.2")
@SortingIndex(1100)
@TransformerExclusions("thecodex6824.thaumcraftfix.core")
public class ThaumcraftFixCore implements IFMLLoadingPlugin {

    protected static final String AUG_GOOD_VERSION = "2.1.14";

    private static Logger log = LogManager.getLogger("thaumcraftfixcore");
    private static boolean debug = false;
    private static boolean ready = false;
    private static boolean oldAug = false;
    private static boolean thaumicWands = false;

    public static Logger getLogger() {
	return log;
    }

    public static boolean isDebugEnabled() {
	return debug;
    }

    public static boolean isInitComplete() {
	return ready;
    }

    public static boolean isOldThaumicAugmentationDetected() {
	return oldAug;
    }

    public static boolean isThaumicWandsDetected() {
	return thaumicWands;
    }

    @Override
    public String getAccessTransformerClass() {
	return null;
    }

    @Override
    public String[] getASMTransformerClass() {
	return new String[] { "thecodex6824.thaumcraftfix.core.TransformerExecutor" };
    }

    @Override
    public String getModContainerClass() {
	return null;
    }

    @Override
    public String getSetupClass() {
	return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
	// I know addConfigurations exists, but the single-argument form doesn't exist in FermiumBooter
	// trying to use the 2 argument version here runs into classloader issues from IMixinConfigSource
	try {
	    for (String c : getEarlyMixinConfigs()) {
		Mixins.addConfiguration(c);
	    }
	}
	catch (NoClassDefFoundError ex) {
	    log.error("No Mixin provider is loaded. The game will probably fail to load very soon.", ex);
	    // we don't want the game to start, but we also want to show a nice message
	    // there is a handler in PreInit that will do that, so avoid crashing for now
	    return;
	}
	// determine if we should use MixinBooter or FermiumBooter for late Mixins
	try {
	    Class.forName("zone.rong.mixinbooter.ILateMixinLoader");
	    // if we got here then we have MixinBooter, so MixinBooterLateMixinLoader will be found and used
	    log.info("Using MixinBooter for late Mixin loading");
	}
	catch (ClassNotFoundException ex) {
	    // We do not have MixinBooter, so FermiumBooter needs configuration
	    try {
		Class<?> registryClass = Class.forName("fermiumbooter.FermiumRegistryAPI");
		Method enqueue = registryClass.getMethod("enqueueMixin", boolean.class, String.class);
		for (String c : getLateMixinConfigs()) {
		    enqueue.invoke(null, true, c);
		}
		log.info("Using FermiumBooter for late Mixin loading");
	    }
	    catch (ReflectiveOperationException e2) {
		// This time it's actually a problem
		throw new RuntimeException(e2);
	    }
	}

	String debugProp = System.getProperty("thaumcraftfix.debug");
	boolean debugBoolTrue = Boolean.parseBoolean(debugProp);
	boolean debugIntTrue = false;
	if (!debugBoolTrue) {
	    try {
		debugIntTrue = Integer.parseInt(debugProp) == 1;
	    }
	    catch (NumberFormatException ex) {}
	}

	debug = debugBoolTrue || debugIntTrue;

	// Thaumic Augmentation detection
	try {
	    Class<?> augApi = Class.forName("thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI");
	    String apiVersion = (String) augApi.getField("API_VERSION").get(null);
	    oldAug = new ComparableVersion(apiVersion).compareTo(new ComparableVersion(AUG_GOOD_VERSION)) < 0;
	}
	catch (Exception ex) {
	    // assume we don't have it
	}

	// Thaumic Wands detection
	// I don't like depending on internal classes existing, but the coremod list from the
	// injectData map doesn't seem to be working with cleanroom (?)
	try {
	    Class.forName("de.zpenguin.thaumicwands.asm.ThaumicWandsCore");
	    thaumicWands = true;
	}
	catch (Exception ex) {}

	ready = true;
	log.info("Thaumcraft Fix coremod initialized");
    }

    public static List<String> getEarlyMixinConfigs() {
	return ImmutableList.of("mixin/vanilla.json");
    }

    public static List<String> getLateMixinConfigs() {
	return ImmutableList.of("mixin/aura.json", "mixin/block.json", "mixin/client.json",
		"mixin/entities.json", "mixin/event.json", "mixin/focus.json", "mixin/golem.json",
		"mixin/inventory.json", "mixin/item.json", "mixin/network.json", "mixin/render.json",
		"mixin/tile.json", "mixin/util.json");
    }

}
