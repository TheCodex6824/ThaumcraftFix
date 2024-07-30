package thecodex6824.thaumcraftfix.common.inventory;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import thaumcraft.api.crafting.IArcaneWorkbench;

public class FakeArcaneWorkbenchInventory extends InventoryCrafting implements IArcaneWorkbench {

    public FakeArcaneWorkbenchInventory(Container container, int width, int height) {
	super(container, width, height);
    }

}
