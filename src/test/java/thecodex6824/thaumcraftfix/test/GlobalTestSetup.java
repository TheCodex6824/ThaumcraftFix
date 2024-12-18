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

package thecodex6824.thaumcraftfix.test;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.BaublesContainer;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.block.Block;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.common.blocks.basic.BlockPillar;
import thaumcraft.common.blocks.basic.BlockStoneTC;
import thaumcraft.common.config.ConfigResearch;
import thaumcraft.common.items.baubles.ItemCuriosityBand;
import thaumcraft.common.items.curios.ItemPrimordialPearl;
import thaumcraft.common.lib.network.PacketHandler;
import thecodex6824.thaumcraftfix.ThaumcraftFix;
import thecodex6824.thaumcraftfix.api.internal.ThaumcraftFixApiBridge;
import thecodex6824.thaumcraftfix.common.internal.DefaultApiImplementation;
import thecodex6824.thaumcraftfix.test.lib.MockProxy;

public class GlobalTestSetup {

    private static <T extends Block> T registerBlock(T block) {
	ForgeRegistries.BLOCKS.register(block);
	return block;
    }

    private static <T extends Block> Pair<T, ItemBlock> registerBlockWithItemBlock(T block) {
	ForgeRegistries.BLOCKS.register(block);
	ItemBlock item = (ItemBlock) new ItemBlock(block).setRegistryName(block.getRegistryName());
	ForgeRegistries.ITEMS.register(item);
	return Pair.of(block, item);
    }

    @SuppressWarnings("unchecked")
    public static void init() {
	// this registers all vanilla blocks, items, etc
	// without this, many things will throw exceptions, like ItemStack
	Bootstrap.register();

	// initialize this mod a bit
	ThaumcraftFix.instance = new ThaumcraftFix();
	ThaumcraftFix.proxy = new MockProxy();
	ThaumcraftFix.instance.construction(new FMLConstructionEvent(new Object[] { null, null, null }));
	ThaumcraftFix.instance.preInit(new FMLPreInitializationEvent(new Object[] { null, null, null }) {
	    @Override
	    public Logger getModLog() {
		return LogManager.getLogger(ThaumcraftFix.class);
	    }
	});

	// work around Side.BUKKIT issue
	// if there is a full dev environment, BUKKIT won't exist
	// however, if someone just cloned the repo and ran tests, the issue will be present
	// I *think* this is due to the binpatch setup only patching Minecraft itself and not Forge

	// if you found this comment in an attempt to fix a NullPointerException in NetworkRegistry.newChannel,
	// try running the "runClient" task and then regenerating your IDE configs/runs
	// If it still doesn't work, see build.gradle for how to remove mergetool from the classpath
	// If using IDE runs, make sure your runtime classpath has the forge jar ending in -recomp.jar and not others
	try {
	    Side maybeBukkit = Side.valueOf("BUKKIT");
	    Field channels = NetworkRegistry.class.getDeclaredField("channels");
	    channels.setAccessible(true);
	    ((EnumMap<Side, Map<String,FMLEmbeddedChannel>>)
		    channels.get(NetworkRegistry.INSTANCE)).put(maybeBukkit, Maps.newConcurrentMap());
	}
	catch (ReflectiveOperationException ex) {
	    throw new RuntimeException(ex);
	}
	catch (IllegalArgumentException ex) {
	    // it doesn't exist, do nothing
	}

	// initialize TC network messages and channel
	PacketHandler.preInit();
	// null out internals so we get an NPE thrown instead of netty just logging
	// TODO: make packets assertable somehow
	try {
	    Field channels = SimpleNetworkWrapper.class.getDeclaredField("channels");
	    channels.setAccessible(true);
	    channels.set(PacketHandler.INSTANCE, null);
	}
	catch (ReflectiveOperationException ex) {
	    throw new RuntimeException(ex);
	}

	// initialize TC research
	ConfigResearch.init();

	// set up API
	((DefaultApiImplementation) ThaumcraftFixApiBridge.implementation()).setAllowedTheorycraftCategories(
		ImmutableSet.copyOf(ResearchCategories.researchCategories.values()));

	// make forge think we are registering things as thaumcraft
	ModMetadata meta = new ModMetadata();
	meta.modId = "thaumcraft";
	// thank you forge :3
	Loader.instance().setupTestHarness(new DummyModContainer(meta));
	// register TC blocks/items needed for tests
	BlocksTC.pillarArcane = registerBlock(new BlockPillar("pillar_arcane"));
	BlocksTC.stoneArcane = registerBlockWithItemBlock(new BlockStoneTC("stone_arcane", true)).getKey();

	ItemsTC.bandCuriosity = new ItemCuriosityBand();
	ItemsTC.primordialPearl = new ItemPrimordialPearl();

	// capability registration
	ASMDataTable table = new ASMDataTable();
	table.addContainer(Loader.instance().activeModContainer());
	table.addASMData(null, CapabilityInject.class.getName(), BaublesCapabilities.class.getName(),
		"CAPABILITY_BAUBLES", ImmutableMap.of("value", Type.getType(IBaublesItemHandler.class)));
	CapabilityManager.INSTANCE.injectCapabilities(table);

	CapabilityManager.INSTANCE.register(IBaublesItemHandler.class,
		new BaublesCapabilities.CapabilityBaubles<IBaublesItemHandler>(), () -> new BaublesContainer());
    }

}
