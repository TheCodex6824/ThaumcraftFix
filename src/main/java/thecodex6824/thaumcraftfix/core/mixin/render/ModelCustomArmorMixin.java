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

package thecodex6824.thaumcraftfix.core.mixin.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import thaumcraft.client.renderers.models.gear.ModelCustomArmor;

@Mixin(ModelCustomArmor.class)
public class ModelCustomArmorMixin extends ModelBiped {

    /**
     * @author TheCodex6824
     * @reason TC's implementation of this method is a copy-paste that has bugs,
     * and also prevents any other mixins / ASM into ModelBiped from working
     */
    @Override
    @Overwrite
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
	    float headPitch, float scaleFactor, Entity entityIn) {

	super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw,
		headPitch, scaleFactor, entityIn);
    }

}
