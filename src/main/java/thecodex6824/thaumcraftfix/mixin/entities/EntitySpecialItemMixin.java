package thecodex6824.thaumcraftfix.mixin.entities;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.common.entities.EntitySpecialItem;

@Mixin(value = EntitySpecialItem.class, remap = false)
public abstract class EntitySpecialItemMixin extends EntityItem {
    public EntitySpecialItemMixin(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    /**
     * @author Invadermonky
     * @reason Fixes inconsistent floating behavior for Primordial Pearl when dropped from Thaumcraft bosses.
     */
    @Inject(method = "onUpdate", at = @At("HEAD"), cancellable = true, remap = true)
    private void onUpdateOverwriteMixin(CallbackInfo ci) {
        super.onUpdate();
        this.motionX *= 0.9;
        this.motionY *= 0.9;
        this.motionZ *= 0.9;
        ci.cancel();
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0 || pass == 1;
    }
}
