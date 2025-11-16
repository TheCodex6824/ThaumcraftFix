package thecodex6824.thaumcraftfix.mixin.client;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import thaumcraft.client.gui.GuiArcaneBore;

@SideOnly(Side.CLIENT)
@Mixin(GuiArcaneBore.class)
public class GuiArcaneBoreMixin {
    /**
     * @author Invadermonky
     * @reason Fixes incorrect positioning of the Arcane Bore Gui
     */
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 232))
    private int textureYSizeMixin(int constant) {
        return 166;
    }
}
