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

package thecodex6824.thaumcraftfix.mixin.entity;

import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import thaumcraft.api.casters.FocusEffect;
import thaumcraft.common.entities.projectile.EntityFocusMine;
import thaumcraft.common.lib.SoundsTC;

// Normally focus mines are a little confusing without some sounds to indicate on what they're doing.
@Mixin(EntityFocusMine.class)
public abstract class EntityFocusMineMixin extends EntityThrowable {

    @Shadow(remap = false)
    public int counter;

    @Shadow(remap = false)
    FocusEffect[] effects;

    public EntityFocusMineMixin(World world) {
        super(world);
    }

    @Inject(method = "onUpdate", at = @At(value = "HEAD"))
    public void onUpdateSounds(CallbackInfo ci) {
        try {
            // Plays when the focus mine despawns.
            if (this.ticksExisted > 1200 || (!this.world.isRemote && this.getThrower() == null)) {
                this.playSound(SoundsTC.craftfail, 1.0F, 1.0F + (rand.nextFloat() * 0.5F));
            }

            // Plays when the focus mine is ready and armed.
            if (this.isEntityAlive()) {
                if (this.counter == 1 && this.effects == null) {
                    this.playSound(SoundsTC.hhoff, 1.0F, 1.0F + (rand.nextFloat() * 0.5F));
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Inject(method = "onImpact", at = @At(value = "HEAD"))
    protected void onImpactSound(final RayTraceResult mop, CallbackInfo ci) {
        try {
            // Plays when the focus mine is setting itself up.
            if (this.counter > 0) {
                this.playSound(SoundsTC.ticks, 1.0F, 1.0F + (rand.nextFloat() * 0.5F));
            }
        } catch (Exception ignored) {
        }
    }

}
