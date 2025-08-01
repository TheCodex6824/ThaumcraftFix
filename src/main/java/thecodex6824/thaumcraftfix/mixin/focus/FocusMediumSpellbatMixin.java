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

package thecodex6824.thaumcraftfix.mixin.focus;

import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import thaumcraft.api.casters.FocusMedium;
import thaumcraft.api.casters.Trajectory;
import thaumcraft.common.items.casters.foci.FocusMediumSpellBat;

@Mixin(FocusMediumSpellBat.class)
public abstract class FocusMediumSpellbatMixin extends FocusMedium {

    @Inject(method = "execute", at = @At(value = "RETURN"), remap = false)
    public void spellbatFocusSound(Trajectory trajectory, CallbackInfoReturnable<Boolean> cir) {
        try {
            this.getPackage().world.playSound(null, this.getPackage().getCaster().getPosition().up(), SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.PLAYERS, 0.45F, 1.0F + (float) (this.getPackage().getCaster().world.rand.nextGaussian() * 0.05F));
        } catch (Exception ignored) {
        }
    }

}
