package thecodex6824.thaumcraftfix.common.util;

import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.Logger;
import thaumcraft.common.entities.monster.EntityPech;
import thecodex6824.thaumcraftfix.ThaumcraftFix;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * A helper for adding and removing pech trades. Pech trades are initialized in {@link thaumcraft.common.config.ConfigEntities#initEntities(IForgeRegistry)}
 * and stored in {@link EntityPech#tradeInventory}.
 * <p>
 * The trades are stored in a map as the format:  (int) PechVariant : { (int) TradeLevel, (ItemStack) TradeItem }
 */
@SuppressWarnings("rawtypes") //Thaumcraft uses rawtypes for all the trade values the errors are suppressed.
public class PechTradeHelper {
    public static void fixPechTrades() {
        Logger logger = ThaumcraftFix.instance.getLogger();
        try {
            //Removing Pech trades with broken potion items caused by invalid metadata values.
            if(removePechTrade(EnumPechType.MAGE, Ingredient.fromStacks(new ItemStack(Items.POTIONITEM, 1, 8193)))) {
                addPechTrade(EnumPechType.MAGE, 2, PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.REGENERATION));
                logger.info("Fixed Regeneration potion Pech trade.");
            }
            if(removePechTrade(EnumPechType.MAGE, Ingredient.fromStacks(new ItemStack(Items.POTIONITEM, 1, 8261)))) {
                addPechTrade(EnumPechType.MAGE, 2, PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.HEALING));
                logger.info("Fixed Healing potion Pech trade.");
            }
        } catch (Exception e) {
            logger.error("Suppressed Error: " + e.getMessage());
            logger.error("Failed to fix Pech Trades. Please check the log file for more information.");
        }
    }

    public static void addPechTrade(EnumPechType pechType, int trustLevel, ItemStack stack) {
        ArrayList<List> trades = getPechTrades(pechType);
        if(trades == null) {
            getPechTrades().put(pechType.ordinal(), new ArrayList<>());
            addPechTrade(pechType, trustLevel, stack);
        } else {
            trades.add(Arrays.asList(trustLevel, stack));
        }
    }

    public static boolean removePechTrade(EnumPechType pechType, Ingredient ingredient) {
        boolean did = false;
        ArrayList<List> trades = getPechTrades(pechType);
        if(trades != null) {
            did = trades.removeIf(list -> ingredient.apply((ItemStack) list.get(1)));
        }
        return did;
    }

    private static HashMap<Integer, ArrayList<List>> getPechTrades() {
        return EntityPech.tradeInventory;
    }

    @Nullable
    private static  ArrayList<List> getPechTrades(EnumPechType pechType) {
        return getPechTrades().get(pechType.ordinal());
    }

    /** Enum to make accessing pech trades a little bit easier. */
    public enum EnumPechType {
        MINER,
        MAGE,
        ARCHER,
        COMMON
    }
}
