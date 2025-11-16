package thecodex6824.thaumcraftfix.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.common.blocks.basic.BlockBannerTC;
import thaumcraft.common.items.consumables.ItemPhial;

@Mixin(value = BlockBannerTC.class, remap = false)
public class BlockBannerTCMixin {
    /**
     * @author Invadermonky
     * @reason Fixes item phials being consumed when used to decorate banners.
     */
    @Redirect(
            method = "onBlockActivated",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;shrink(I)V",
                    ordinal = 0
            ),
            remap = true
    )
    private void consumeEssentiaContainerMixin(ItemStack stack, int count, @Local(argsOnly = true)EntityPlayer player) {
        if(!player.isCreative()) {
            if (stack.getItem() instanceof ItemPhial) {
                ItemStack emptyPhial = new ItemStack(ItemsTC.phial);
                if (!player.addItemStackToInventory(emptyPhial)) {
                    player.dropItem(emptyPhial, true);
                }
            }
            stack.shrink(1);
        }
    }
}
