package thecodex6824.thaumcraftfix.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.api.casters.ICaster;
import thaumcraft.client.lib.events.HudHandler;

@SideOnly(Side.CLIENT)
@Mixin(value = HudHandler.class, remap = false)
public abstract class HudHandlerMixin {
    @Shadow @Final ResourceLocation HUD;

    /**
     * @author Invadermonky
     * @reason Fixes the Thaumometer hud not binding the Gui texture before drawing the HUD
     */
    @Inject(method = "renderThaumometerHud", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V", shift = At.Shift.AFTER))
    private void bindTextureThaumometerHudMixin(Minecraft mc, float partialTicks, EntityPlayer player, long time, int ww, int hh, int shifty, CallbackInfo ci) {
        mc.renderEngine.bindTexture(this.HUD);
    }

    /**
     * @author Invadermonky
     * @reason Fixes the Caster Gui rendering the sprite map instead of the correct Gui after rendering the picked block ItemStack
     */
    @Inject(method = "renderSanityHud", at = @At(value = "HEAD"))
    private void bindTextureSanityHudMixin(Minecraft mc, Float partialTicks, EntityPlayer player, long time, int shifty, CallbackInfo ci) {
        mc.renderEngine.bindTexture(this.HUD);
    }

    /**
     * @author Invadermonky
     * @reason Fixes the Caster Gui rendering the sprite map instead of the correct Gui while rendering the plan spell focus
     */
    @Inject(method = "renderCastingWandHud", at = @At("HEAD"))
    private void bindTextureCastingWandHudMixin(Minecraft mc, float partialTicks, EntityPlayer player, long time, ItemStack wandstack, int shifty, CallbackInfo ci) {
        mc.renderEngine.bindTexture(this.HUD);
    }

    /**
     * @author Invadermonky
     * @reason Fixes the Caster Gui icon location when holding the casting gauntlet in the offhand
     */
    @Redirect(method = "renderCastingWandHud", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V", ordinal = 1))
    private void modifyDialLocation(float x, float y, float z, @Local(argsOnly = true) int shifty) {
        if(shifty > 0 && y == 0) {
            y = shifty;
        }
        GL11.glTranslatef(x, y, z);
    }

    /**
     * @author Invadermonky
     * @reason Fixes the Caster Gui rendering the incorrect picked block when being held in the offhand.
     */
    @Redirect(
            method = "renderCastingWandHud",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/api/casters/ICaster;getPickedBlock(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;"
            )
    )
    private ItemStack getPickedBlockMixin(ICaster caster, ItemStack stack, @Local(argsOnly = true) ItemStack wandStack) {
        return caster.getPickedBlock(wandStack);
    }
}
