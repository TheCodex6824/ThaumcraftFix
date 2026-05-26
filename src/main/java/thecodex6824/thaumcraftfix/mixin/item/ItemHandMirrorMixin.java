package thecodex6824.thaumcraftfix.mixin.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import thaumcraft.common.items.tools.ItemHandMirror;

@Mixin(value = ItemHandMirror.class, remap = false)
public abstract class ItemHandMirrorMixin extends Item {

    @ModifyReturnValue(method = "onItemUseFirst", at = @At(value = "RETURN", ordinal = 2), remap = false)
    private EnumActionResult allowBlockInteraction(EnumActionResult original, EntityPlayer player, World world, BlockPos pos,
	    EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {

	ItemStack mirror = player.getHeldItem(hand);
	boolean linked = mirror.hasTagCompound() && mirror.getTagCompound().hasKey("linkDim", NBT.TAG_INT);
	return !linked || player.isSneaking() ? EnumActionResult.PASS : original;
    }

    // method does not exist in original code
    // this is not strictly required but will hopefully help other mods' blocks be less confused
    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
	return true;
    }

}
