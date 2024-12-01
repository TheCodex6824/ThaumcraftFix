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

package thecodex6824.thaumcraftfix.common.aura;

import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import thaumcraft.common.world.aura.AuraWorld;
import thecodex6824.thaumcraftfix.ThaumcraftFix;
import thecodex6824.thaumcraftfix.api.aura.IAuraChunk;
import thecodex6824.thaumcraftfix.api.aura.IAuraWorld;

public class GenericAuraWorld extends AuraWorld implements IAuraWorld {

    protected final int dim;

    public GenericAuraWorld(int dim) {
	super(dim);
	this.dim = dim;
    }

    @Override
    public Set<IAuraChunk> getAllAuraChunks() {
	return ImmutableSet.copyOf(getAuraChunks().values().toArray(new IAuraChunk[0]));
    }

    @Override
    @Nullable
    public IAuraChunk getAuraChunk(ChunkPos pos) {
	return getAuraChunk(pos.x, pos.z);
    }

    @Override
    @Nullable
    public IAuraChunk getAuraChunk(int chunkX, int chunkZ) {
	return (IAuraChunk) getAuraChunkAt(chunkX, chunkZ);
    }

    @Override
    public void markPositionForRift(BlockPos position, boolean useInexactSpawning) {
	ThaumcraftFix.proxy.scheduleTask(Side.SERVER, () -> {
	    World world = DimensionManager.getWorld(dim);
	    if (world != null) {
		MinecraftForge.EVENT_BUS.post(new RiftTriggerEvent(world, position, useInexactSpawning));
	    }
	});
    }

}
