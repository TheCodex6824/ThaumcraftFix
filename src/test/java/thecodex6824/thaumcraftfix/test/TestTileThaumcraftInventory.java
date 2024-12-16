package thecodex6824.thaumcraftfix.test;

import org.junit.jupiter.api.Test;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import thaumcraft.common.tiles.crafting.TileResearchTable;
import thecodex6824.thaumcraftfix.test.lib.MockWorld;

public class TestTileThaumcraftInventory {

    @Test
    public void testSyncSlotsClient() {
	TileResearchTable table = new TileResearchTable() {
	    @Override
	    public void markDirty() {}
	};
	table.setWorld(new MockWorld(true));
	table.setInventorySlotContents(1, new ItemStack(Items.PAPER, 64));
    }

}
