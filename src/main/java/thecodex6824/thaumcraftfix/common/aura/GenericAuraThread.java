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

import java.util.concurrent.Semaphore;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import thaumcraft.Thaumcraft;
import thaumcraft.common.lib.events.ServerEvents;
import thaumcraft.common.world.aura.AuraHandler;
import thaumcraft.common.world.aura.AuraThread;
import thaumcraft.common.world.aura.AuraWorld;
import thecodex6824.thaumcraftfix.api.aura.IAuraChunk;
import thecodex6824.thaumcraftfix.api.aura.IAuraProcessor;
import thecodex6824.thaumcraftfix.api.aura.IAuraWorld;

public class GenericAuraThread extends AuraThread implements IListeningAuraThread {

    private final IAuraProcessor processor;
    private final Semaphore waitSem;
    // these must be volatile since they are set and read by different threads
    private volatile boolean stop;

    public GenericAuraThread(int dim, IAuraProcessor worldProcessor) {
	super(dim);
	waitSem = new Semaphore(0);
	processor = worldProcessor;
    }

    @Override
    public void notifyUpdate(World world) {
	processor.gameTick(world);
	waitSem.release();
    }

    @Override
    public void unloadChunk(World world, Chunk chunk) {
	AuraWorld auraWorld = AuraHandler.getAuraWorld(dim);
	if (auraWorld instanceof IAuraWorld) {
	    IAuraChunk aura = ((IAuraWorld) auraWorld).getAuraChunk(chunk.x, chunk.z);
	    if (aura != null && aura.isModified()) {
		chunk.markDirty();
	    }
	}
	// actually removing the chunk is done by Thaumcraft after it saves it
    }

    @Override
    public void run() {
	Thaumcraft.log.info("Starting aura thread for dim {}", dim);
	while (!stop) {
	    // this is guaranteed to be immune to spurious wakeups
	    try {
		waitSem.acquire();
	    }
	    catch (InterruptedException ex) {
		// something probably wants us to stop but doesn't know about stop()
		stop = true;
	    }

	    // we may have been woken up by someone calling stop() (or got interrupted) so check again
	    if (stop) {
		break;
	    }

	    AuraWorld auraWorld = AuraHandler.getAuraWorld(dim);
	    if (auraWorld instanceof IAuraWorld) {
		processor.auraTick((IAuraWorld) auraWorld);
	    }
	    else {
		Thaumcraft.log.info("Aura for dim {} was unloaded, stopping aura thread", dim);
		stop();
		break;
	    }
	}

	Thaumcraft.log.info("Stopping aura thread for dim {}", dim);
	ServerEvents.auraThreads.remove(dim);
    }

    @Override
    public void stop() {
	stop = true;
	waitSem.release();
    }

}
