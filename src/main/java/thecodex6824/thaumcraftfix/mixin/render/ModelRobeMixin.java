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

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import thaumcraft.client.renderers.models.gear.ModelRobe;

@Mixin(ModelRobe.class)
public class ModelRobeMixin extends ModelBiped {

    @Shadow(remap = false)
    private ModelRenderer ShoulderR;
    @Shadow(remap = false)
    private ModelRenderer ShoulderL;

    @Shadow(remap = false)
    private ModelRenderer FrontclothR1;
    @Shadow(remap = false)
    private ModelRenderer FrontclothL1;
    @Shadow(remap = false)
    private ModelRenderer FrontclothR2;
    @Shadow(remap = false)
    private ModelRenderer FrontclothL2;
    @Shadow(remap = false)
    private ModelRenderer ClothBackR1;
    @Shadow(remap = false)
    private ModelRenderer ClothBackL1;
    @Shadow(remap = false)
    private ModelRenderer ClothBackR2;
    @Shadow(remap = false)
    private ModelRenderer ClothBackL2;
    @Shadow(remap = false)
    private ModelRenderer ClothBackR3;
    @Shadow(remap = false)
    private ModelRenderer ClothBackL3;

    private boolean doClothReparent;
    private ModelRenderer FrontclothR1Left;
    private ModelRenderer FrontclothL1Left;
    private ModelRenderer FrontclothR2Left;
    private ModelRenderer FrontclothL2Left;
    private ModelRenderer ClothBackR1Left;
    private ModelRenderer ClothBackL1Left;
    private ModelRenderer ClothBackR2Left;
    private ModelRenderer ClothBackL2Left;
    private ModelRenderer ClothBackR3Left;
    private ModelRenderer ClothBackL3Left;

    @Inject(
	    method = "<init>(F)V",
	    at = @At(
		    value = "INVOKE",
		    target = "Lnet/minecraft/client/model/ModelRenderer;addBox(FFFIII)Lnet/minecraft/client/model/ModelRenderer;",
		    ordinal = 28,
		    unsafe = true
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
		    ordinal = 32,
		    unsafe = true
		    ),
	    remap = false
	    )
    private void fixupLeftShoulderTexture(CallbackInfo info) {
	ShoulderL.mirror = true;
    }

    @Inject(
	    method = "<init>(F)V",
	    at = @At(
		    value = "INVOKE",
		    target = "Lnet/minecraft/client/model/ModelRenderer;addBox(FFFIII)Lnet/minecraft/client/model/ModelRenderer;",
		    ordinal = 0,
		    unsafe = true
		    ),
	    remap = false
	    )
    private void initClothReparent(float scale, CallbackInfo info) {
	doClothReparent = scale < 1.0F;
    }

    @WrapOperation(
	    method = "<init>(F)V",
	    at = @At(
		    value = "INVOKE",
		    target = "Lnet/minecraft/client/model/ModelRenderer;setRotationPoint(FFF)V",
		    unsafe = true
		    ),
	    remap = false
	    )
    private void clothRotPointOffset(ModelRenderer target, float x, float y, float z, Operation<Float> original) {
	if (doClothReparent) {
	    x -= bipedRightLeg.rotationPointX;
	    y -= bipedRightLeg.rotationPointY;
	}

	original.call(target, x, y, z);
    }

    @Inject(method = "<init>(F)V", at = @At("RETURN"), remap = false)
    private void initLegModels(float scale, CallbackInfo info) {
	if (doClothReparent) {
	    FrontclothR1.rotateAngleX = 0.0F;
	    bipedBody.childModels.remove(FrontclothR1);
	    bipedRightLeg.addChild(FrontclothR1);
	    FrontclothR1Left = new ModelRenderer(this, 108, 38);
	    FrontclothR1Left.addBox(0.0F, 0.0F, 0.0F, 3, 8, 1);
	    FrontclothR1Left.setRotationPoint(-3.0F - bipedLeftLeg.rotationPointX, 11.0F - bipedLeftLeg.rotationPointY, -2.9F);
	    FrontclothR1Left.setTextureSize(128, 64);
	    bipedLeftLeg.addChild(FrontclothR1Left);

	    FrontclothR2.rotateAngleX = -0.2268928F;
	    bipedBody.childModels.remove(FrontclothR2);
	    bipedRightLeg.addChild(FrontclothR2);
	    FrontclothR2Left = new ModelRenderer(this, 108, 47);
	    FrontclothR2Left.addBox(0.0F, 7.5F, 1.7F, 3, 3, 1);
	    FrontclothR2Left.setRotationPoint(-3.0F - bipedLeftLeg.rotationPointX, 11.0F - bipedLeftLeg.rotationPointY, -2.9F);
	    FrontclothR2Left.setTextureSize(128, 64);
	    FrontclothR2Left.rotateAngleX = -0.2268928F;
	    bipedLeftLeg.addChild(FrontclothR2Left);

	    FrontclothL1.rotateAngleX = 0.0F;
	    bipedBody.childModels.remove(FrontclothL1);
	    bipedRightLeg.addChild(FrontclothL1);
	    FrontclothL1Left = new ModelRenderer(this, 108, 38);
	    FrontclothL1Left.mirror = true;
	    FrontclothL1Left.addBox(0.0F,0.0F, 0.0F, 3, 8, 1);
	    FrontclothL1Left.setRotationPoint(-bipedLeftLeg.rotationPointX, 11.0F - bipedLeftLeg.rotationPointY, -2.9F);
	    FrontclothL1Left.setTextureSize(128, 64);
	    bipedLeftLeg.addChild(FrontclothL1Left);

	    FrontclothL2.rotateAngleX = -0.2268928F;
	    bipedBody.childModels.remove(FrontclothL2);
	    bipedRightLeg.addChild(FrontclothL2);
	    FrontclothL2Left = new ModelRenderer(this, 108, 47);
	    FrontclothL2Left.mirror = true;
	    FrontclothL2Left.addBox(0.0F, 7.5F, 1.7F, 3, 3, 1);
	    FrontclothL2Left.setRotationPoint(-bipedLeftLeg.rotationPointX, 11.0F - bipedLeftLeg.rotationPointY, -2.9F);
	    FrontclothL2Left.setTextureSize(128, 64);
	    FrontclothL2Left.rotateAngleX = -0.2268928F;
	    bipedLeftLeg.addChild(FrontclothL2Left);

	    ClothBackR1.rotateAngleX = 0.0F;
	    bipedBody.childModels.remove(ClothBackR1);
	    bipedRightLeg.addChild(ClothBackR1);
	    ClothBackR1Left = new ModelRenderer(this, 118, 16);
	    ClothBackR1Left.mirror = true;
	    ClothBackR1Left.addBox(0.0F, 0.0F, 0.0F, 4, 8, 1);
	    ClothBackR1Left.setRotationPoint(-4.0F - bipedLeftLeg.rotationPointX, 11.5F - bipedLeftLeg.rotationPointY, 2.9F);
	    ClothBackR1Left.setTextureSize(128, 64);
	    bipedLeftLeg.addChild(ClothBackR1Left);

	    ClothBackR2.rotateAngleX = 0.122173F;
	    bipedBody.childModels.remove(ClothBackR2);
	    bipedRightLeg.addChild(ClothBackR2);
	    ClothBackR2Left = new ModelRenderer(this, 123, 9);
	    ClothBackR2Left.addBox(0.0F, 7.8F, -0.9F, 1, 2, 1);
	    ClothBackR2Left.setRotationPoint(-4.0F - bipedLeftLeg.rotationPointX, 11.5F - bipedLeftLeg.rotationPointY, 2.9F);
	    ClothBackR2Left.setTextureSize(128, 64);
	    ClothBackR2Left.rotateAngleX = 0.122173F;
	    bipedLeftLeg.addChild(ClothBackR2Left);

	    ClothBackR3.rotateAngleX = 0.122173F;
	    bipedBody.childModels.remove(ClothBackR3);
	    bipedRightLeg.addChild(ClothBackR3);
	    ClothBackR3Left = new ModelRenderer(this, 120, 12);
	    ClothBackR3Left.mirror = true;
	    ClothBackR3Left.addBox(1.0F, 7.8F, -0.9F, 3, 3, 1);
	    ClothBackR3Left.setRotationPoint(-4.0F - bipedLeftLeg.rotationPointX, 11.5F - bipedLeftLeg.rotationPointY, 2.9F);
	    ClothBackR3Left.setTextureSize(128, 64);
	    ClothBackR3Left.rotateAngleX = 0.122173F;
	    bipedLeftLeg.addChild(ClothBackR3Left);

	    ClothBackL1.rotateAngleX = 0.0F;
	    bipedBody.childModels.remove(ClothBackL1);
	    bipedRightLeg.addChild(ClothBackL1);
	    ClothBackL1Left = new ModelRenderer(this, 118, 16);
	    ClothBackL1Left.addBox(0.0F, 0.0F, 0.0F, 4, 8, 1);
	    ClothBackL1Left.setRotationPoint(-bipedLeftLeg.rotationPointX, 11.5F - bipedLeftLeg.rotationPointY, 2.9F);
	    ClothBackL1Left.setTextureSize(128, 64);
	    bipedLeftLeg.addChild(ClothBackL1Left);

	    ClothBackL2.rotateAngleX = 0.122173F;
	    bipedBody.childModels.remove(ClothBackL2);
	    bipedRightLeg.addChild(ClothBackL2);
	    ClothBackL2Left = new ModelRenderer(this, 123, 9);
	    ClothBackL2Left.mirror = true;
	    ClothBackL2Left.addBox(3.0F, 7.8F, -0.9F, 1, 2, 1);
	    ClothBackL2Left.setRotationPoint(-bipedLeftLeg.rotationPointX, 11.5F - bipedLeftLeg.rotationPointY, 2.9F);
	    ClothBackL2Left.setTextureSize(128, 64);
	    ClothBackL2Left.rotateAngleX = 0.122173F;
	    bipedLeftLeg.addChild(ClothBackL2Left);

	    ClothBackL3.rotateAngleX = 0.122173F;
	    bipedBody.childModels.remove(ClothBackL3);
	    bipedRightLeg.addChild(ClothBackL3);
	    ClothBackL3Left = new ModelRenderer(this, 120, 12);
	    ClothBackL3Left.addBox(0.0F, 7.8F, -0.9F, 3, 3, 1);
	    ClothBackL3Left.setRotationPoint(-bipedLeftLeg.rotationPointX, 11.5F - bipedLeftLeg.rotationPointY, 2.9F);
	    ClothBackL3Left.setTextureSize(128, 64);
	    ClothBackL3Left.rotateAngleX = 0.122173F;
	    bipedLeftLeg.addChild(ClothBackL3Left);
	}
    }

    @WrapOperation(
	    method = "Lthaumcraft/client/renderers/models/gear/ModelRobe;render(Lnet/minecraft/entity/Entity;FFFFFF)V",
	    at = @At(
		    value = "FIELD",
		    opcode = Opcodes.PUTFIELD,
		    target = "Lnet/minecraft/client/model/ModelRenderer;rotateAngleX:F"
		    )
	    )
    private void cancelRotateChanges(ModelRenderer target, float newValue, Operation<Float> original) {
	if (!doClothReparent) {
	    original.call(target, newValue);
	}
    }

    private void setLegModelVisibility(boolean rightLegBackFurther) {
	FrontclothR1.isHidden = rightLegBackFurther;
	FrontclothR2.isHidden = rightLegBackFurther;
	FrontclothL1.isHidden = rightLegBackFurther;
	FrontclothL2.isHidden = rightLegBackFurther;
	FrontclothR1Left.isHidden = !rightLegBackFurther;
	FrontclothR2Left.isHidden = !rightLegBackFurther;
	FrontclothL1Left.isHidden = !rightLegBackFurther;
	FrontclothL2Left.isHidden = !rightLegBackFurther;

	ClothBackR1.isHidden = !rightLegBackFurther;
	ClothBackR2.isHidden = !rightLegBackFurther;
	ClothBackR3.isHidden = !rightLegBackFurther;
	ClothBackL1.isHidden = !rightLegBackFurther;
	ClothBackL2.isHidden = !rightLegBackFurther;
	ClothBackL3.isHidden = !rightLegBackFurther;
	ClothBackR1Left.isHidden = rightLegBackFurther;
	ClothBackR2Left.isHidden = rightLegBackFurther;
	ClothBackR3Left.isHidden = rightLegBackFurther;
	ClothBackL1Left.isHidden = rightLegBackFurther;
	ClothBackL2Left.isHidden = rightLegBackFurther;
	ClothBackL3Left.isHidden = rightLegBackFurther;
    }

    @Inject(
	    method = "Lthaumcraft/client/renderers/models/gear/ModelRobe;render(Lnet/minecraft/entity/Entity;FFFFFF)V",
	    at = @At(
		    value = "FIELD",
		    opcode = Opcodes.GETFIELD,
		    target = "Lthaumcraft/client/renderers/models/gear/ModelRobe;isChild:Z",
		    ordinal = 0
		    )
	    )
    private void activateCorrectLegModels(Entity entity, float limbSwing, float limbSwingAmount,
	    float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo info) {

	if (doClothReparent) {
	    float angleRight = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
	    float angleLeft = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
	    setLegModelVisibility(angleRight > angleLeft);
	}
    }

}
