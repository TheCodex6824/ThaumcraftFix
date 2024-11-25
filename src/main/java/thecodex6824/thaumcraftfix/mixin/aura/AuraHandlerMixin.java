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

package thecodex6824.thaumcraftfix.mixin.aura;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import thaumcraft.common.world.aura.AuraChunk;
import thaumcraft.common.world.aura.AuraHandler;
import thecodex6824.thaumcraftfix.common.aura.AtomicAuraChunk;
import thecodex6824.thaumcraftfix.common.aura.IAtomicAuraChunk;

@Mixin(AuraHandler.class)
public class AuraHandlerMixin {

    @Redirect(method = "addAuraChunk(ILnet/minecraft/world/chunk/Chunk;SFF)V",
	    at = @At(value = "NEW", target = "(Lnet/minecraft/world/chunk/Chunk;SFF)Lthaumcraft/common/world/aura/AuraChunk;"))
    private static AuraChunk redirectAddAuraChunk(Chunk chunk, short base, float vis, float flux) {
	return new AtomicAuraChunk(chunk, base, vis, flux);
    }

    /**
     * @author TheCodex6824
     * @reason The original clamps the amount once, which will not work if someone else updated it between
     * that and adding the amount
     */
    @Overwrite
    public static float drainVis(World world, BlockPos pos, float amount, boolean simulate) {
	AuraChunk ac = AuraHandler.getAuraChunk(world.provider.getDimension(),
		pos.getX() >> 4, pos.getZ() >> 4);
	if (ac != null) {
	    if (simulate) {
		return ac.getVis() - amount;
	    }
	    else if (ac instanceof IAtomicAuraChunk) {
		return -((IAtomicAuraChunk) ac).addVis(-amount);
	    }
	}

	return 0.0f;
    }

    /**
     * @author TheCodex6824
     * @reason The original clamps the amount once, which will not work if someone else updated it between
     * that and adding the amount
     */
    @Overwrite
    public static float drainFlux(World world, BlockPos pos, float amount, boolean simulate) {
	AuraChunk ac = AuraHandler.getAuraChunk(world.provider.getDimension(),
		pos.getX() >> 4, pos.getZ() >> 4);
	if (ac != null) {
	    if (simulate) {
		return ac.getFlux() - amount;
	    }
	    else if (ac instanceof IAtomicAuraChunk) {
		return -((IAtomicAuraChunk) ac).addFlux(-amount);
	    }
	}

	return 0.0f;
    }

    /**
     * @author TheCodex6824
     * @reason A redirect will not work since the argument passed to setVis had the potentially stale
     * old vis amount added to it
     */
    @Overwrite
    public static boolean modifyVisInChunk(AuraChunk ac, float amount, boolean notSimulate) {
	if (notSimulate && ac instanceof IAtomicAuraChunk) {
	    ((IAtomicAuraChunk) ac).addVis(amount);
	    return true;
	}

	return false;
    }

    /**
     * @author TheCodex6824
     * @reason A redirect will not work since the argument passed to setFlux had the potentially stale
     * old flux amount added to it
     */
    @Overwrite
    private static boolean modifyFluxInChunk(AuraChunk ac, float amount, boolean notSimulate) {
	if (notSimulate && ac instanceof IAtomicAuraChunk) {
	    ((IAtomicAuraChunk) ac).addFlux(amount);
	    return true;
	}

	return false;
    }

}
