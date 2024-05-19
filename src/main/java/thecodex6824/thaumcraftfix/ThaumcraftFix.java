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

package thecodex6824.thaumcraftfix;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import thecodex6824.thaumcraftfix.api.ThaumcraftFixAPI;
import thecodex6824.thaumcraftfix.common.network.ThaumcraftFixNetworkHandler;

@Mod(modid = ThaumcraftFixAPI.MODID, name = "Thaumcraft Fix", version = ThaumcraftFix.VERSION, useMetadata = true,
certificateFingerprint = "@FINGERPRINT@")
@EventBusSubscriber
public class ThaumcraftFix {

    public static final String VERSION = "@VERSION@";

    @Instance(ThaumcraftFixAPI.MODID)
    public static ThaumcraftFix instance;

    @SidedProxy(modId = ThaumcraftFixAPI.MODID, serverSide = "thecodex6824.thaumcraftfix.ServerProxy",
	    clientSide = "thecodex6824.thaumcraftfix.ClientProxy")
    public static IProxy proxy;

    private Logger logger;
    private ThaumcraftFixNetworkHandler network;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
	logger = event.getModLog();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void afterThaumcraftRegistersItems(RegistryEvent<Item> event) {
	// we want to run a bunch of static initializers at a consistent time
	try {
	    Class.forName("thaumcraft.common.golems.GolemProperties");
	}
	catch (ClassNotFoundException ex) {
	    // if we don't have GolemProperties golems will be broken...
	    throw new RuntimeException(ex);
	}
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
	network = new ThaumcraftFixNetworkHandler();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {}

    @EventHandler
    public static void onFingerPrintViolation(FMLFingerprintViolationEvent event) {
	if (!event.isDirectory()) {
	    Logger tempLogger = LogManager.getLogger(ThaumcraftFixAPI.MODID);
	    tempLogger.warn("A file failed to match with the signing key.");
	    tempLogger.warn("If you *know* this is a homebrew/custom build then this is expected, carry on.");
	    tempLogger.warn("Otherwise, you might want to redownload this mod from the *official* CurseForge page.");
	}
    }

    public Logger getLogger() {
	return logger;
    }

    public SimpleNetworkWrapper getNetworkHandler() {
	return network;
    }

}
