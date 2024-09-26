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
import net.minecraft.entity.Entity;
import thaumcraft.client.renderers.models.gear.ModelCustomArmor;

@Mixin(ModelBiped.class)
public class ModelBipedMixin extends ModelBase {

    @Shadow
    private boolean isSneak;
    @Shadow
    private ModelRenderer bipedRightLeg;
    @Shadow
    private ModelRenderer bipedLeftLeg;
    @Shadow
    private ModelRenderer bipedHead;
    @Shadow
    private ModelRenderer bipedBody;
    @Shadow
    private ModelRenderer bipedRightArm;
    @Shadow
    private ModelRenderer bipedLeftArm;
    @Shadow
    private ModelRenderer bipedHeadwear;

    @Inject(method = "Lnet/minecraft/client/model/ModelBiped;setRotationAngles(FFFFFFLnet/minecraft/entity/Entity;)V",
	    at = @At(
		    value = "FIELD",
		    target = "Lnet/minecraft/client/model/ModelBiped;bipedRightArm:Lnet/minecraft/client/model/ModelRenderer;",
		    ordinal = 0
		    ),
	    slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelBiped;isSneak:Z"))
	    )
    private void setupRotation(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
	    float headPitch, float scaleFactor, Entity entity, CallbackInfo ci) {
	// the double cast is not strictly necessary but my IDE autoremoves it
	// if it is just an Object cast
	if ((ModelBiped) ((Object) this) instanceof ModelCustomArmor) {
	    if (isSneak) {
		bipedRightLeg.rotationPointY = 13.0F;
		bipedLeftLeg.rotationPointY = 13.0F;
		bipedHead.rotationPointY = 4.5F;

		bipedBody.rotationPointY = 4.5F;
		bipedRightArm.rotationPointY = 5.0F;
		bipedLeftArm.rotationPointY = 5.0F;
	    }
	    else {
		bipedBody.rotationPointY = 0.0F;
		bipedRightArm.rotationPointY = 2.0F;
		bipedLeftArm.rotationPointY = 2.0F;
	    }

	    bipedHeadwear.rotationPointX = bipedHead.rotationPointX;
	    bipedHeadwear.rotationPointY = bipedHead.rotationPointY;
	    bipedHeadwear.rotationPointZ = bipedHead.rotationPointZ;
	}
    }

}
