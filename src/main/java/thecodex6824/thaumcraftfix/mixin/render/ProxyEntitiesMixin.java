package thecodex6824.thaumcraftfix.mixin.render;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import thaumcraft.proxies.ProxyEntities;

@SideOnly(Side.CLIENT)
@Mixin(value = ProxyEntities.class, remap = false)
public class ProxyEntitiesMixin {
    //TODO: Find a better fix for this than modifying the giant taintacle body segment count.
    //  I think it has to do with 14 body segments being rendered more than 256 times.
    /**
     * @author Invadermonky
     * @reason For some reason Taintacles with 14 or more body segments result in a stack overflow error when rendering.
     *         Reducing the 14 body segments to 13 for the Giant Taintacle fixes the error spam.
     */
    @ModifyConstant(method = "setupEntityRenderers", constant = @Constant(intValue = 14))
    private int modifyGiantTaintacleSegments(int constant) {
        return 13;
    }
}
