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

import com.google.common.collect.ImmutableSet;

import net.minecraft.entity.EntityList;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import thaumcraft.api.ThaumcraftMaterials;
import thaumcraft.api.capabilities.IPlayerKnowledge.EnumKnowledgeType;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.api.research.ResearchEntry;
import thaumcraft.api.research.ResearchStage;
import thaumcraft.api.research.ResearchStage.Knowledge;
import thaumcraft.common.entities.monster.EntitySpellBat;
import thecodex6824.thaumcraftfix.api.ThaumcraftFixApi;
import thecodex6824.thaumcraftfix.api.internal.ThaumcraftFixApiBridge;
import thecodex6824.thaumcraftfix.api.research.ResearchCategoryTheorycraftFilter;
import thecodex6824.thaumcraftfix.common.internal.DefaultApiImplementation;
import thecodex6824.thaumcraftfix.common.network.ThaumcraftFixNetworkHandler;

@Mod(modid = ThaumcraftFixApi.MODID, name = "Thaumcraft Fix", version = ThaumcraftFix.VERSION, useMetadata = true,
certificateFingerprint = "@FINGERPRINT@")
@EventBusSubscriber
public class ThaumcraftFix {

    public static final String VERSION = "@VERSION@";

    @Instance(ThaumcraftFixApi.MODID)
    public static ThaumcraftFix instance;

    @SidedProxy(modId = ThaumcraftFixApi.MODID, serverSide = "thecodex6824.thaumcraftfix.ServerProxy",
	    clientSide = "thecodex6824.thaumcraftfix.ClientProxy")
    public static IProxy proxy;

    private Logger logger;
    private ThaumcraftFixNetworkHandler network;

    @EventHandler
    public void construction(FMLConstructionEvent event) {
	proxy.construction();
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
	logger = event.getModLog();
	ThaumcraftFixApiBridge.setImplementation(new DefaultApiImplementation());
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

    private static void setToolMaterialRepairItem(ToolMaterial material, ItemStack repair) {
	try {
	    material.setRepairItem(repair);
	}
	catch (RuntimeException ex) {
	    // someone already set repair items
	}
    }

    private static void setArmorMaterialRepairItem(ArmorMaterial material, ItemStack repair) {
	try {
	    material.setRepairItem(repair);
	}
	catch (RuntimeException ex) {
	    // someone already set repair items
	}
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
	// reset repair materials for TC materials
	// if the class was loaded before now, the items used will have been null and won't register
	// the individual items have extra code to allow repair, but these are required for generic support
	setToolMaterialRepairItem(ThaumcraftMaterials.TOOLMAT_ELEMENTAL, new ItemStack(ItemsTC.ingots, 1, 0));
	setToolMaterialRepairItem(ThaumcraftMaterials.TOOLMAT_THAUMIUM, new ItemStack(ItemsTC.ingots, 1, 0));
	setToolMaterialRepairItem(ThaumcraftMaterials.TOOLMAT_VOID, new ItemStack(ItemsTC.ingots, 1, 1));

	// these just don't have repair materials defined
	// should these be replaced with plates? would also need to override every class usage...
	setArmorMaterialRepairItem(ThaumcraftMaterials.ARMORMAT_CULTIST_LEADER, new ItemStack(Items.IRON_INGOT));
	setArmorMaterialRepairItem(ThaumcraftMaterials.ARMORMAT_CULTIST_PLATE, new ItemStack(Items.IRON_INGOT));
	setArmorMaterialRepairItem(ThaumcraftMaterials.ARMORMAT_CULTIST_ROBE, new ItemStack(Items.IRON_INGOT));
	setArmorMaterialRepairItem(ThaumcraftMaterials.ARMORMAT_FORTRESS, new ItemStack(ItemsTC.ingots, 1, 0));
	setArmorMaterialRepairItem(ThaumcraftMaterials.ARMORMAT_THAUMIUM, new ItemStack(ItemsTC.ingots, 1, 0));
	setArmorMaterialRepairItem(ThaumcraftMaterials.ARMORMAT_VOID, new ItemStack(ItemsTC.ingots, 1, 1));
	setArmorMaterialRepairItem(ThaumcraftMaterials.ARMORMAT_VOIDROBE, new ItemStack(ItemsTC.ingots, 1, 1));

	// these don't have repair materials and are shared across multiple items
	// it doesn't really make sense to have a single repair material for it
	// ThaumcraftMaterials.ARMORMAT_SPECIAL

	// delete broken spellbat spawn egg
	EntityEntry spellbat = EntityRegistry.getEntry(EntitySpellBat.class);
	if (spellbat != null) {
	    spellbat.setEgg(null);
	    EntityList.ENTITY_EGGS.remove(spellbat.getRegistryName());
	}
    }

    @EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
	ThaumcraftFixApiBridge.InternalImplementation impl = ThaumcraftFixApiBridge.implementation();
	if (impl instanceof DefaultApiImplementation) {
	    ImmutableSet.Builder<ResearchCategory> allowed = ImmutableSet.builder();
	    for (ResearchCategory category : ResearchCategories.researchCategories.values()) {
		for (ResearchEntry entry : category.research.values()) {
		    if (entry.getStages() != null) {
			for (ResearchStage stage : entry.getStages()) {
			    if (stage.getKnow() != null) {
				for (Knowledge know : stage.getKnow()) {
				    if (know.type == EnumKnowledgeType.THEORY) {
					allowed.add(know.category);
				    }
				}
			    }
			}
		    }
		}
	    }

	    ((DefaultApiImplementation) impl).setAllowedTheorycraftCategories(allowed.build());
	    logger.debug("The following research categories will be allowed for theorycrafting:");
	    for (ResearchCategory c : ResearchCategoryTheorycraftFilter.getAllowedTheorycraftCategories()) {
		logger.debug("{}", c.key);
	    }
	}
    }

    @EventHandler
    public static void onFingerPrintViolation(FMLFingerprintViolationEvent event) {
	if (!event.isDirectory()) {
	    Logger tempLogger = LogManager.getLogger(ThaumcraftFixApi.MODID);
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
