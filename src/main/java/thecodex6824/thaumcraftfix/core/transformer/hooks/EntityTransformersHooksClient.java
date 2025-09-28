/**
 *  Thaumcraft Fix
 *  Copyright (c) 2025 TheCodex6824 and other contributors.
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

package thecodex6824.thaumcraftfix.core.transformer.hooks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.client.renderers.models.gear.ModelCustomArmor;
import thaumcraft.common.entities.construct.EntityArcaneBore;
import thaumcraft.common.lib.enchantment.EnumInfusionEnchantment;

@SideOnly(Side.CLIENT)
public class EntityTransformersHooksClient {

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

    public static TextureAtlasSprite getBlockParticleTexture(TextureAtlasSprite old, IBlockState state) {
	return Minecraft.getMinecraft().getBlockRendererDispatcher()
		.getModelForState(state).getParticleTexture();
    }

    private static boolean boreHasLamplighter(EntityArcaneBore bore) {
	ItemStack held = bore.getHeldItemMainhand();
	return !held.isEmpty() && EnumInfusionEnchantment.getInfusionEnchantmentLevel(held,
		EnumInfusionEnchantment.LAMPLIGHT) > 0;
    }

    public static boolean doesBoreHaveProperties(EntityArcaneBore bore, boolean original) {
	return original || boreHasLamplighter(bore);
    }

    public static int drawLamplightText(EntityArcaneBore bore, int position) {
	if (boreHasLamplighter(bore)) {
	    String text = I18n.format("enchantment.infusion.LAMPLIGHT");
	    Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, 4.0F, 34 + position, 0xffff00);
	    position += 9;
	}

	return position;
    }

}
