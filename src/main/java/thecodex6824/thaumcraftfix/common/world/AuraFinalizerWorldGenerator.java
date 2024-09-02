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

package thecodex6824.thaumcraftfix.common.world;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import thaumcraft.common.world.aura.AuraChunk;
import thaumcraft.common.world.aura.AuraHandler;
import thecodex6824.thaumcraftfix.api.FeatureControlApi;
import thecodex6824.thaumcraftfix.api.aura.CapabilityOriginalAuraInfo;
import thecodex6824.thaumcraftfix.api.aura.IOriginalAuraInfo;

public class AuraFinalizerWorldGenerator implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
	    IChunkProvider chunkProvider) {

	if (FeatureControlApi.isControllingAuraGen()) {
	    Biome biome = world.getBiome(new BlockPos(chunkX * 16 + 8, 64, chunkZ * 16 + 8));
	    // we already stopped Thaumcraft earlier, but someone else might have set something
	    if (!FeatureControlApi.auraAllowedDimensions().contains(world.provider.getDimensionType()) ||
		    !FeatureControlApi.auraAllowedBiomes().contains(biome)) {
		AuraChunk chunk = AuraHandler.getAuraChunk(world.provider.getDimension(), chunkX, chunkZ);
		chunk.setBase((short) 0);
		chunk.setVis(0.0F);
		chunk.setFlux(0.0F);
	    }
	}

	IOriginalAuraInfo info = world.getChunk(chunkX, chunkZ).getCapability(CapabilityOriginalAuraInfo.AURA_INFO, null);
	if (info != null) {
	    AuraChunk chunk = AuraHandler.getAuraChunk(world.provider.getDimension(), chunkX, chunkZ);
	    info.setBase(chunk.getBase());
	    info.setVis(chunk.getVis());
	    info.setFlux(chunk.getFlux());
	}
    }

}
