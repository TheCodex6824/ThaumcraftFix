/**
 *  Thaumcraft Fix
 *  Copyright (c) 2024 TheCodex6824.
 *
 *  This file is part of Thaumcraft Fix.
 *
 *  Thaumcraft Fix is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Thaumcraft Fix is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Thaumcraft Fix.  If not, see <https://www.gnu.org/licenses/>.
 */

package thecodex6824.thaumcraftfix.mixin.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import thaumcraft.client.renderers.models.gear.ModelRobe;

@Mixin(ModelRobe.class)
public class ModelRobeMixin extends ModelBiped {

    @Shadow(remap = false)
    private ModelRenderer ShoulderR;
    @Shadow(remap = false)
    private ModelRenderer ShoulderL;

    @Inject(
	    method = "<init>(F)V",
	    at = @At(
		    value = "INVOKE",
		    target = "Lnet/minecraft/client/model/ModelRenderer;addBox(FFFIII)Lnet/minecraft/client/model/ModelRenderer;",
		    ordinal = 28
		    ),
	    remap = false
	    )
    private void fixupRightShoulderTexture(CallbackInfo info) {
	ShoulderR.mirror = false;
    }

    @Inject(
	    method = "<init>(F)V",
	    at = @At(
		    value = "INVOKE",
		    target = "Lnet/minecraft/client/model/ModelRenderer;addBox(FFFIII)Lnet/minecraft/client/model/ModelRenderer;",
		    ordinal = 32
		    ),
	    remap = false
	    )
    private void fixupLeftShoulderTexture(CallbackInfo info) {
	ShoulderL.mirror = true;
    }

    @ModifyExpressionValue(method = "Lthaumcraft/client/renderers/models/gear/ModelRobe;render(Lnet/minecraft/entity/Entity;FFFFFF)V",
	    at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(FF)F", ordinal = 0, remap = false))
    private float fixRobeFlapping(float original, Entity entity) {
	float f = 1.0F;
	if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getTicksElytraFlying() > 4) {
	    f = (float) (entity.motionX * entity.motionX + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ);
	    f /= 0.2F;
	    f = Math.max(f * f * f, 1.0F);
	}

	return original / f;
    }

}
