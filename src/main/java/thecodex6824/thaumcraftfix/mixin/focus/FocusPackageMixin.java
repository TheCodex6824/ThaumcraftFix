package thecodex6824.thaumcraftfix.mixin.focus;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.EntityLivingBase;
import thaumcraft.api.casters.FocusPackage;
import thecodex6824.thaumcraftfix.core.transformer.hooks.CastingTransformersHooks;

@Mixin(FocusPackage.class)
public class FocusPackageMixin {

    @Inject(method = "initialize(Lnet/minecraft/entity/EntityLivingBase;)V", at = @At(value = "RETURN"), remap = false)
    private void initializeSplitPackages(EntityLivingBase caster, CallbackInfo info) {
	CastingTransformersHooks.initializeFocusPackage((FocusPackage)((Object) this), caster);
    }

    @Inject(method = "setCasterUUID(Ljava/util/UUID;)V", at = @At(value = "RETURN"), remap = false)
    private void setIdSplitPackages(UUID caster, CallbackInfo info) {
	CastingTransformersHooks.setFocusPackageCasterUUID((FocusPackage)((Object) this), caster);
    }

}
