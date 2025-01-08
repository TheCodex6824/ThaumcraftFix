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

package thecodex6824.thaumcraftfix.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import thaumcraft.common.world.aura.AuraHandler;
import thecodex6824.thaumcraftfix.test.lib.MockWorld;

public class TestAuraHandler {

    private void runInThreadPool(Runnable run) {
	ExecutorService threadPool = Executors.newWorkStealingPool();
	for (int i = 0; i < 8; ++i) {
	    threadPool.submit(run);
	}
	threadPool.shutdown();
	while (!threadPool.isTerminated()) {
	    try {
		threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
	    }
	    catch (InterruptedException ex) {}
	}
    }

    @Test
    @ResourceLock(TestConstants.RESOURCE_AURA)
    public void multiThreadedAddVis() {
	MockWorld dim0 = new MockWorld();
	try {
	    final float visStep = 1.1f;
	    AuraHandler.addAuraWorld(dim0.provider.getDimension());
	    AuraHandler.addAuraChunk(dim0.provider.getDimension(), new Chunk(dim0, 0, 0),
		    (short) 500, 0.0f, 0.0f);
	    BlockPos pos = new BlockPos(0, 0, 0);
	    runInThreadPool(() -> {
		for (int j = 0; j < 50; ++j) {
		    AuraHandler.addVis(dim0, pos, visStep);
		}
	    });

	    assertEquals(50 * visStep * 8, AuraHandler.getVis(dim0, pos), 0.0001f);
	}
	finally {
	    AuraHandler.removeAuraChunk(dim0.provider.getDimension(), 0, 0);
	    AuraHandler.removeAuraWorld(dim0.provider.getDimension());
	}
    }

    @Test
    @ResourceLock(TestConstants.RESOURCE_AURA)
    public void multiThreadedDrainVis() {
	MockWorld dim0 = new MockWorld();
	try {
	    final float visStep = 1.0f;
	    AuraHandler.addAuraWorld(dim0.provider.getDimension());
	    AuraHandler.addAuraChunk(dim0.provider.getDimension(), new Chunk(dim0, 0, 0),
		    (short) 500, 500.0f, 0.0f);
	    BlockPos pos = new BlockPos(0, 0, 0);
	    runInThreadPool(() -> {
		for (int j = 0; j < 50; ++j) {
		    assertEquals(visStep, AuraHandler.drainVis(dim0, pos, visStep, false));
		}
	    });

	    // we intentionally only allow an epsilon for the totals
	    // for the drainVis etc steps, we are expecting the exact value we passed to be returned,
	    // unless vis or flux would have went out of bounds
	    assertEquals(500 - 50 * visStep * 8, AuraHandler.getVis(dim0, pos), 0.0001f);
	}
	finally {
	    AuraHandler.removeAuraChunk(dim0.provider.getDimension(), 0, 0);
	    AuraHandler.removeAuraWorld(dim0.provider.getDimension());
	}
    }

    @Test
    @ResourceLock(TestConstants.RESOURCE_AURA)
    public void multiThreadedAddFlux() {
	MockWorld dim0 = new MockWorld();
	try {
	    final float fluxStep = 1.3f;
	    AuraHandler.addAuraWorld(dim0.provider.getDimension());
	    AuraHandler.addAuraChunk(dim0.provider.getDimension(), new Chunk(dim0, 0, 0),
		    (short) 500, 0.0f, 0.0f);
	    BlockPos pos = new BlockPos(0, 0, 0);
	    runInThreadPool(() -> {
		for (int j = 0; j < 50; ++j) {
		    AuraHandler.addFlux(dim0, pos, fluxStep);
		}
	    });
	    assertEquals(50 * fluxStep * 8, AuraHandler.getFlux(dim0, pos), 0.0001f);
	}
	finally {
	    AuraHandler.removeAuraChunk(dim0.provider.getDimension(), 0, 0);
	    AuraHandler.removeAuraWorld(dim0.provider.getDimension());
	}
    }

    @Test
    @ResourceLock(TestConstants.RESOURCE_AURA)
    public void multiThreadedDrainFlux() {
	MockWorld dim0 = new MockWorld();
	try {
	    final float fluxStep = 1.123456789f;
	    AuraHandler.addAuraWorld(dim0.provider.getDimension());
	    AuraHandler.addAuraChunk(dim0.provider.getDimension(), new Chunk(dim0, 0, 0),
		    (short) 500, 0.0f, 500.0f);
	    BlockPos pos = new BlockPos(0, 0, 0);
	    runInThreadPool(() -> {
		for (int j = 0; j < 50; ++j) {
		    assertEquals(fluxStep, AuraHandler.drainFlux(dim0, pos, fluxStep, false));
		}
	    });
	    assertEquals(500 - 50 * fluxStep * 8, AuraHandler.getFlux(dim0, pos), 0.0001f);
	}
	finally {
	    AuraHandler.removeAuraChunk(dim0.provider.getDimension(), 0, 0);
	    AuraHandler.removeAuraWorld(dim0.provider.getDimension());
	}
    }

    @Test
    @ResourceLock(TestConstants.RESOURCE_AURA)
    public void drainDecimalVis() {
	MockWorld dim0 = new MockWorld();
	try {
	    final float visStep = 1.8f;
	    AuraHandler.addAuraWorld(dim0.provider.getDimension());
	    AuraHandler.addAuraChunk(dim0.provider.getDimension(), new Chunk(dim0, 0, 0),
		    (short) 500, 300.0f, 0.0f);
	    BlockPos pos = new BlockPos(0, 0, 0);
	    for (int i = 0; i < 50; ++i) {
		assertEquals(visStep, AuraHandler.drainVis(dim0, pos, visStep, false));
	    }

	    assertEquals(300 - visStep * 50, AuraHandler.getVis(dim0, pos), 0.0001f);
	}
	finally {
	    AuraHandler.removeAuraChunk(dim0.provider.getDimension(), 0, 0);
	    AuraHandler.removeAuraWorld(dim0.provider.getDimension());
	}
    }

}
