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

package thecodex6824.thaumcraftfix.client.internal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.client.renderers.models.gear.ModelCustomArmor;
import thaumcraft.common.tiles.crafting.TileFocalManipulator;

@SuppressWarnings("unused")
public final class ThaumcraftFixHooksClient {

    private ThaumcraftFixHooksClient() {}

    public static float getRobeRotationDivisor(Entity entity) {
	float f = 1.0F;
	if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getTicksElytraFlying() > 4) {
	    f = (float) (entity.motionX * entity.motionX + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ);
	    f /= 0.2F;
	    f = Math.max(f * f * f, 1.0F);
	}

	return f;
    }

    public static void correctRotationPoints(ModelBiped model) {
	if (model instanceof ModelCustomArmor) {
	    if (model.isSneak) {
		model.bipedRightLeg.rotationPointY = 13.0F;
		model.bipedLeftLeg.rotationPointY = 13.0F;
		model.bipedHead.rotationPointY = 4.5F;

		model.bipedBody.rotationPointY = 4.5F;
		model.bipedRightArm.rotationPointY = 5.0F;
		model.bipedLeftArm.rotationPointY = 5.0F;
	    }
	    else {
		model.bipedBody.rotationPointY = 0.0F;
		model.bipedRightArm.rotationPointY = 2.0F;
		model.bipedLeftArm.rotationPointY = 2.0F;
	    }

	    model.bipedHeadwear.rotationPointX = model.bipedHead.rotationPointX;
	    model.bipedHeadwear.rotationPointY = model.bipedHead.rotationPointY;
	    model.bipedHeadwear.rotationPointZ = model.bipedHead.rotationPointZ;
	}
    }

    public static boolean modifyFocalManipulatorCraftValid(boolean origResult, int totalComplexity,
	    int maxComplexity, TileFocalManipulator table, boolean emptyNodes, boolean validCrystals) {

	EntityPlayer player = Minecraft.getMinecraft().player;
	if (!player.isCreative()) {
	    return origResult;
	}

	// waive crystal and xp requirement in creative
	return totalComplexity <= maxComplexity && !emptyNodes;
    }

}
