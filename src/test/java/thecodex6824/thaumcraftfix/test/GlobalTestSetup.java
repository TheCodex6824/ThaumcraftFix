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

import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.Type;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

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
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.common.blocks.basic.BlockPillar;
import thaumcraft.common.blocks.basic.BlockStoneTC;
import thaumcraft.common.config.ConfigResearch;
import thaumcraft.common.items.baubles.ItemCuriosityBand;
import thaumcraft.common.items.curios.ItemPrimordialPearl;
import thecodex6824.thaumcraftfix.api.internal.ThaumcraftFixApiBridge;
import thecodex6824.thaumcraftfix.common.internal.DefaultApiImplementation;

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

    public static void init() {
	// this registers all vanilla blocks, items, etc
	// without this, many things will throw exceptions, like ItemStack
	Bootstrap.register();

	// initialize TC research
	ConfigResearch.init();

	// set up API
	DefaultApiImplementation impl = new DefaultApiImplementation();
	ThaumcraftFixApiBridge.setImplementation(impl);
	impl.setAllowedTheorycraftCategories(ImmutableSet.copyOf(ResearchCategories.researchCategories.values()));

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
