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

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;

import org.spongepowered.asm.mixin.Mixin;

import thaumcraft.client.renderers.entity.mob.RenderEldritchCrab;
import thaumcraft.common.entities.monster.EntityEldritchCrab;

@Mixin(RenderEldritchCrab.class)
public abstract class RenderEldritchCrabMixin extends RenderLiving<EntityEldritchCrab> {

    public RenderEldritchCrabMixin(RenderManager rendermanagerIn, ModelBase modelbaseIn, float shadowsizeIn) {
        super(rendermanagerIn, modelbaseIn, shadowsizeIn);
    }

    // Makes eldritch crabs rotate all the way during death like spiders, this would make more sense visually.
    @Override
    public float getDeathMaxRotation(EntityEldritchCrab entity) {
        return 180.0F;
    }

}
