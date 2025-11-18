package thecodex6824.thaumcraftfix.mixin.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.entities.EntityFluxRift;

@Mixin(value = EntityFluxRift.class, remap = false)
public abstract class EntityFluxRiftMixin extends Entity {
    @Shadow(remap = false) public abstract boolean getCollapse();
    @Shadow(remap = false) private void completeCollapse() {}

    public EntityFluxRiftMixin(World worldIn) {
	super(worldIn);
    }

    /**
     * @author Invadermonky
     * @reason Fixes Flux Rifts not collapsing when drained entirely by Void Siphon or other similar effects
     */
    @Inject(method = "setRiftSize", at = @At("RETURN"))
    private void handleRiftCollapse(int size, CallbackInfo ci) {
	if(size <= 1 && !this.getCollapse()) {
	    if (this.rand.nextBoolean()) {
		AuraHelper.addVis(this.world, this.getPosition(), 1.0F);
	    } else {
		AuraHelper.polluteAura(this.world, this.getPosition(), 1.0F, false);
	    }
	    this.world.createExplosion(this, this.posX + this.rand.nextGaussian() * 2.0F, this.posY + this.rand.nextGaussian() * 2.0F, this.posZ + this.rand.nextGaussian() * 2.0F, this.rand.nextFloat() / 2.0F, false);
	    this.completeCollapse();
	}
    }
}
