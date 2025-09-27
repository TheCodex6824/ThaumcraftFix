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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;
import org.junit.jupiter.api.parallel.ResourceLock;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import thaumcraft.common.world.aura.AuraHandler;
import thecodex6824.thaumcraftfix.api.aura.DefaultAuraProcessor;
import thecodex6824.thaumcraftfix.api.aura.IAuraWorld;
import thecodex6824.thaumcraftfix.common.aura.AtomicAuraChunk;
import thecodex6824.thaumcraftfix.common.aura.GenericAuraThread;
import thecodex6824.thaumcraftfix.common.aura.GenericAuraWorld;
import thecodex6824.thaumcraftfix.testlib.lib.MockWorld;

public class TestAuraProcessor {

    @Test
    public void testRiftTrigger() {
	MockWorld dim0 = new MockWorld();
	Chunk chunk = new Chunk(dim0, 0, 0);
	MutableBoolean rift = new MutableBoolean();
	GenericAuraWorld world = new GenericAuraWorld(dim0.provider.getDimension()) {
	    @Override
	    public void markPositionForRift(BlockPos position, boolean useInexactSpawning) {
		rift.setTrue();
	    }
	};
	world.addAuraChunk(new AtomicAuraChunk(chunk, (short) 500, 0.0f, 5000.0f));

	DefaultAuraProcessor processor = new DefaultAuraProcessor();
	for (int i = 1; i < 21; ++i) {
	    dim0.setTotalWorldTime(i);
	    processor.gameTick(dim0);
	    processor.auraTick(world);
	}

	assertTrue(rift.booleanValue());
    }

    @Test
    @ResourceLock(TestConstants.RESOURCE_AURA)
    // the test should be fast but if we fail for deadlock or such we want to exit at some point
    @Timeout(threadMode = ThreadMode.INFERRED, unit = TimeUnit.SECONDS, value = 15)
    public void testThread() {
	MockWorld dim0 = new MockWorld();
	Chunk center = new Chunk(dim0, 0, 0);
	Chunk east = new Chunk(dim0, 1, 0);
	Chunk south = new Chunk(dim0, 0, 1);
	try {
	    AuraHandler.addAuraWorld(dim0.provider.getDimension());
	    AuraHandler.addAuraChunk(dim0.provider.getDimension(), center,
		    (short) 500, 20.0f, 0.0f);
	    AuraHandler.addAuraChunk(dim0.provider.getDimension(), east,
		    (short) 500, 100.0f, 0.0f);
	    AuraHandler.addAuraChunk(dim0.provider.getDimension(), south,
		    (short) 500, 70.0f, 0.0f);

	    DefaultAuraProcessor processor = new DefaultAuraProcessor() {
		@Override
		public void auraTick(IAuraWorld world) {
		    super.auraTick(world);
		    synchronized(this) {
			notify();
		    }
		}
	    };
	    GenericAuraThread aura = new GenericAuraThread(dim0.provider.getDimension(), processor);
	    Thread thread = new Thread(aura);
	    thread.start();

	    for (int i = 1; i < 61; ++i) {
		dim0.setTotalWorldTime(i);
		try {
		    synchronized(processor) {
			aura.notifyUpdate(dim0);
			processor.wait();
		    }
		}
		catch (InterruptedException ex) {
		    throw new RuntimeException(ex);
		}
	    }

	    // the chunk with the lowest vis should have gained 6 vis (2 from the other chunks per update)
	    // every chunk should have also gained a total of 0.75 vis from regeneration (0.25 per update)
	    aura.unloadChunk(dim0, south);
	    assertEquals(26.75f, AuraHandler.getAuraChunk(dim0.provider.getDimension(), 0, 0).getVis(), 1e-3f);
	    assertEquals(97.75f, AuraHandler.getAuraChunk(dim0.provider.getDimension(), 1, 0).getVis(), 1e-3f);
	    assertEquals(67.75f, AuraHandler.getAuraChunk(dim0.provider.getDimension(), 0, 1).getVis(), 1e-3f);
	    AuraHandler.removeAuraChunk(dim0.provider.getDimension(), 0, 1);

	    for (int i = 1; i < 61; ++i) {
		dim0.setTotalWorldTime(i);
		try {
		    synchronized(processor) {
			aura.notifyUpdate(dim0);
			processor.wait();
		    }
		}
		catch (InterruptedException ex) {
		    throw new RuntimeException(ex);
		}
	    }

	    aura.unloadChunk(dim0, east);
	    aura.unloadChunk(dim0, center);
	    aura.stop();
	    while (thread.isAlive()) {
		try {
		    thread.join();
		}
		catch (InterruptedException ex) {
		    throw new RuntimeException(ex);
		}
	    }

	    assertEquals(30.5f, AuraHandler.getAuraChunk(dim0.provider.getDimension(), 0, 0).getVis(), 1e-3f);
	    assertEquals(95.5f, AuraHandler.getAuraChunk(dim0.provider.getDimension(), 1, 0).getVis(), 1e-3f);

	    assertTrue(center.needsSaving(false));
	    assertTrue(east.needsSaving(false));
	    assertTrue(south.needsSaving(false));
	}
	finally {
	    AuraHandler.removeAuraChunk(dim0.provider.getDimension(), 0, 0);
	    AuraHandler.removeAuraChunk(dim0.provider.getDimension(), 1, 0);
	    AuraHandler.removeAuraChunk(dim0.provider.getDimension(), 0, 1);
	    AuraHandler.removeAuraWorld(dim0.provider.getDimension());
	}
    }

}
