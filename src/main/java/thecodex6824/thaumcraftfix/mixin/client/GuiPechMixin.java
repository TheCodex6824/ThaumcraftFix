package thecodex6824.thaumcraftfix.mixin.client;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.client.gui.GuiPech;

@SideOnly(Side.CLIENT)
@Mixin(GuiPech.class)
public abstract class GuiPechMixin extends GuiContainer {
    public GuiPechMixin(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    /**
     * @author Invadermonky
     * @reason Fixes incorrect positioning of the Pech Trade Gui
     */
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 232))
    private int textureYSizeMixin(int constant) {
        return 166;
    }

    /**
     * @author Invadermonky
     * @reason Fixes item shader issue with Pech Trading
     */
    @Redirect(method = "drawGuiContainerBackgroundLayer", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V", remap = false))
    private void gl11ColorMixin(float red, float green, float blue, float alpha) {
        GlStateManager.color(red, green, blue, alpha);
    }

    /**
     * @author Invadermonky
     * @reason Fixes item shader issue with Pech Trading
     */
    @Redirect(method = "drawGuiContainerBackgroundLayer", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V", remap = false))
    private void glEnableBlendMixin(int cap) {
        GlStateManager.enableBlend();
    }

    /**
     * @author Invadermonky
     * @reason Fixes item shader issue with Pech Trading
     */
    @Redirect(method = "drawGuiContainerBackgroundLayer", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V", remap = false))
    private void glDisableBlendMixin(int cap) {
        GlStateManager.disableBlend();
    }
}
