package thecodex6824.thaumcraftfix.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import thaumcraft.api.crafting.Part;
import thaumcraft.client.gui.GuiResearchPage;
import thaumcraft.client.gui.GuiResearchPage.BlueprintBlockAccess;
import thecodex6824.thaumcraftfix.core.transformer.hooks.ClientTransformersHooks;

@Mixin(value = GuiResearchPage.class, remap = false)
public class GuiResearchPageMixin {

    @ModifyExpressionValue(method = "renderBluePrint", at = @At(
	    value = "INVOKE",
	    target = "Lnet/minecraft/block/Block;hasTileEntity(Lnet/minecraft/block/state/IBlockState;)Z"
	    ))
    private boolean doCustomRenderForFastTesr(boolean hasTile, BlueprintBlockAccess ba, int x, int y, float scale,
	    Part[][][] blueprint, int mx, int my, ItemStack[] ingredients, @Local(ordinal = 0) BlockPos pos, @Local(ordinal = 0) IBlockState state) {
	boolean result = hasTile;
	if (result) {
	    // having to create the tile again is not great, but we need to check the TE before it enters the if block
	    TileEntity testTile = state.getBlock().createTileEntity(Minecraft.getMinecraft().world, state);
	    if (testTile.hasFastRenderer()) {
		ClientTransformersHooks.renderFastTESRBlueprint(testTile, pos, ba);
		result = false;
	    }
	}
	
	return result;
    }
    
    @Inject(method = "renderBluePrint", at = @At(
	    value = "INVOKE",
	    target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;render(Lnet/minecraft/tileentity/TileEntity;DDDF)V"
	    ))
    private void cleanupAfterRenderNormalTile(CallbackInfo ci) {
	// rebind standard texture in case it was changed during TE render
	Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
    }
    
}
