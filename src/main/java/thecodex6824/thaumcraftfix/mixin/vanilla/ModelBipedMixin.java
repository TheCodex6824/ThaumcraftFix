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

package thecodex6824.thaumcraftfix.mixin.vanilla;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import thecodex6824.thaumcraftfix.core.transformer.hooks.EntityTransformersHooksClient;

@Mixin(ModelBiped.class)
public class ModelBipedMixin extends ModelBase {

    @Shadow
    public boolean isSneak;
    @Shadow
    public ModelRenderer bipedRightLeg;
    @Shadow
    public ModelRenderer bipedLeftLeg;
    @Shadow
    public ModelRenderer bipedHead;
    @Shadow
    public ModelRenderer bipedBody;
    @Shadow
    public ModelRenderer bipedRightArm;
    @Shadow
    public ModelRenderer bipedLeftArm;
    @Shadow
    public ModelRenderer bipedHeadwear;

    @Inject(method = "setRotationAngles(FFFFFFLnet/minecraft/entity/Entity;)V",
	    at = @At(
		    value = "INVOKE",
		    target = "Lnet/minecraft/util/math/MathHelper;cos(F)F",
		    ordinal = 0
		    ),
	    slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelBiped;isSneak:Z"))
	    )
    private void setupRotation(CallbackInfo ci) {
	// the double cast is not strictly necessary but my IDE autoremoves it
	// if it is just an Object cast

	// Calling a hook method because class-loading TC's ModelCustomArmor seems
	// to cause issues with other transformers (#83)
	EntityTransformersHooksClient.correctRotationPoints((ModelBiped) ((Object) this));
    }

}
