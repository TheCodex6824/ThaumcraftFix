package thecodex6824.thaumcraftfix.mixin.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.common.container.ContainerResearchTable;
import thaumcraft.common.tiles.crafting.TileResearchTable;

@Mixin(value = ContainerResearchTable.class, remap = false)
public abstract class ContainerResearchTableMixin extends Container {
    @Shadow public TileResearchTable tileEntity;

    /**
     * @author Invadermonky
     * @reason Fixes the original implementation of the {@link ContainerResearchTable#transferStackInSlot(EntityPlayer, int)}
     *         method. The unaltered method attempted to insert items into the clicked slot index instead of attempting to insert
     *         the clicked item into the Research Table slots.
     */
    @Redirect(
            method = "transferStackInSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/common/tiles/crafting/TileResearchTable;isItemValidForSlot(ILnet/minecraft/item/ItemStack;)Z",
                    ordinal = 1
            ),
            remap = true
    )
    private boolean isItemValidForSlotMixin(TileResearchTable instance, int slotIndex, ItemStack stack) {
        return this.tileEntity.isItemValidForSlot(0, stack) || this.tileEntity.isItemValidForSlot(1, stack);
    }
}
