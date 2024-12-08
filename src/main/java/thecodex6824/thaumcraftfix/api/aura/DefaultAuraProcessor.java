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

package thecodex6824.thaumcraftfix.api.aura;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.google.common.collect.Lists;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class DefaultAuraProcessor implements IAuraProcessor {

    protected static final class PhaseModifier {
	public final float auraChange;
	public final float baseMultiplier;

	public PhaseModifier(float auraMod, float baseMod) {
	    auraChange = auraMod;
	    baseMultiplier = baseMod;
	}
    }

    protected static final PhaseModifier[] PHASE_TO_AURA_MODS = new PhaseModifier[] {
	    new PhaseModifier(0.25f, 1.15f),
	    new PhaseModifier(0.15f, 1.05f),
	    new PhaseModifier(0.1f, 1.0f),
	    new PhaseModifier(0.05f, 0.95f),
	    new PhaseModifier(0.0f, 0.85f),
	    new PhaseModifier(0.05f, 0.95f),
	    new PhaseModifier(0.1f, 1.0f),
	    new PhaseModifier(0.15f, 1.05f)
    };
    protected static final int TICK_RATE = 20;

    protected volatile int lastMoonPhase;
    protected volatile long totalWorldTime;
    protected long lastUpdateTime;

    @Override
    public void gameTick(World world) {
	lastMoonPhase = world.provider.getMoonPhase(world.getWorldTime()) % PHASE_TO_AURA_MODS.length;
	totalWorldTime = world.getTotalWorldTime();
    }

    @Override
    public void auraTick(IAuraWorld world) {
	// the abs call deals with overflow or negative world time
	if (Math.abs(totalWorldTime - lastUpdateTime) >= TICK_RATE) {
	    lastUpdateTime = totalWorldTime;
	    for (IAuraChunk chunk : world.getAllAuraChunks()) {
		equalizeWithNeighbors(world, chunk);
		generateVisAndFlux(world, chunk);
		checkForRifts(world, chunk);
	    }
	}
    }

    protected void equalizeWithNeighbors(IAuraWorld world, IAuraChunk center) {
	ArrayList<EnumFacing> offsets = Lists.newArrayList(EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST);
	Collections.shuffle(offsets, ThreadLocalRandom.current());
	int centerX = center.getPosition().x;
	int centerZ = center.getPosition().z;
	IAuraChunk lowestVis = null;
	IAuraChunk lowestFlux = null;
	for (EnumFacing offset : offsets) {
	    IAuraChunk chunk = world.getAuraChunk(centerX + offset.getXOffset(), centerZ + offset.getZOffset());
	    if (chunk != null) {
		if ((lowestVis == null || lowestVis.getVis() > chunk.getVis()) &&
			chunk.getVis() + chunk.getFlux() < chunk.getBase() * PHASE_TO_AURA_MODS[lastMoonPhase].baseMultiplier) {

		    lowestVis = chunk;
		}
		if (lowestFlux == null || lowestFlux.getFlux() > chunk.getFlux()) {
		    lowestFlux = chunk;
		}
	    }
	}

	float myVis = center.getVis();
	if (lowestVis != null && lowestVis.getVis() < myVis && lowestVis.getVis() / myVis < 0.75f) {
	    float inc = Math.min(myVis - lowestVis.getVis(), 1.0f);
	    inc = -center.addVis(-inc);
	    lowestVis.addVis(inc);
	}

	float myFlux = center.getFlux();
	if (lowestFlux != null && myFlux > Math.max(5.0f, center.getBase() / 10.0f) &&
		lowestFlux.getFlux() < myFlux / 1.75f) {

	    float inc = Math.min(myFlux - lowestFlux.getFlux(), 1.0f);
	    inc = -center.addFlux(-inc);
	    lowestFlux.addFlux(inc);
	}
    }

    protected void generateVisAndFlux(IAuraWorld world, IAuraChunk chunk) {
	float vis = chunk.getVis();
	float flux = chunk.getFlux();
	short base = chunk.getBase();
	PhaseModifier phase = PHASE_TO_AURA_MODS[lastMoonPhase];
	float phaseFlux = PHASE_TO_AURA_MODS[0].auraChange - phase.auraChange;
	Random rand = ThreadLocalRandom.current();
	if (vis + flux < base) {
	    float inc = Math.min(base - vis + flux, phase.auraChange);
	    chunk.addVis(inc);
	}
	else if (vis > base * 1.25f && rand.nextFloat() < 0.1f) {
	    chunk.addFlux(phaseFlux);
	    chunk.addVis(-phaseFlux);
	}
	else if (vis <= base * 0.1f && vis >= flux && rand.nextFloat() < 0.1f) {
	    chunk.addFlux(phaseFlux);
	}
    }

    protected void checkForRifts(IAuraWorld world, IAuraChunk chunk) {
	float flux = chunk.getFlux();
	if (flux > chunk.getBase() * 0.75f && ThreadLocalRandom.current().nextFloat() < flux / 500.0F / 10.0F) {
	    ChunkPos pos = chunk.getPosition();
	    world.markPositionForRift(new BlockPos(pos.x * 16, 0, pos.z * 16), true);
	}
    }

}
