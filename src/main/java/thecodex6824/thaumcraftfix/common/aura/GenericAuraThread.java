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

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.minecraft.world.World;
import thaumcraft.Thaumcraft;
import thaumcraft.common.lib.events.ServerEvents;
import thaumcraft.common.world.aura.AuraChunk;
import thaumcraft.common.world.aura.AuraHandler;
import thaumcraft.common.world.aura.AuraThread;
import thaumcraft.common.world.aura.AuraWorld;
import thecodex6824.thaumcraftfix.api.aura.IAuraChunk;
import thecodex6824.thaumcraftfix.api.aura.IAuraProcessor;
import thecodex6824.thaumcraftfix.api.aura.IAuraWorld;

public class GenericAuraThread extends AuraThread implements IListeningAuraThread {

    private final IAuraProcessor processor;
    // a separate lock is used instead of synchronized(this) to avoid issues if someone is also
    // doing that for some reason (very unlikely but you never know...)
    private final Lock waitLock;
    private final Condition waitCondition;
    // these must be volatile since they are set and read by different threads
    private volatile boolean stop;
    private volatile long totalWorldTime;

    public GenericAuraThread(int dim, IAuraProcessor worldProcessor) {
	super(dim);
	waitLock = new ReentrantLock();
	waitCondition = waitLock.newCondition();
	processor = worldProcessor;
    }

    private void signalAuraLoop() {
	waitLock.lock();
	try {
	    waitCondition.signal();
	}
	finally {
	    waitLock.unlock();
	}
    }

    @Override
    public void notifyUpdate(World world) {
	totalWorldTime = world.getTotalWorldTime();
	processor.gameTick(world);
	signalAuraLoop();
    }

    @Override
    public void run() {
	Thaumcraft.log.info("Starting aura thread for dim {}", dim);
	while (!stop) {
	    waitLock.lock();
	    try {
		waitCondition.await();
	    }
	    catch (InterruptedException ex) {
		// this is fine, we already check if the wakeup is valid or not
	    }
	    finally {
		// If interrupted, it is not specified if we hold the lock or not
		// Since unlocking without holding it will probably throw an exception, catch it
		try {
		    waitLock.unlock();
		}
		catch (Exception ex) {}
	    }

	    // we may have been woken up by someone calling stop()
	    if (stop) {
		break;
	    }

	    // check if we need to update (this may have been a spurious wakeup)
	    if (processor.needsUpdate(totalWorldTime)) {
		AuraWorld auraWorld = AuraHandler.getAuraWorld(dim);
		if (auraWorld instanceof IAuraWorld) {
		    IAuraWorld world = (IAuraWorld) auraWorld;
		    processor.processWorld(world, totalWorldTime);
		    for (AuraChunk auraChunk : auraWorld.getAuraChunks().values()) {
			processor.processChunk(world, (IAuraChunk) auraChunk, totalWorldTime);
		    }
		}
		else {
		    Thaumcraft.log.info("Aura for dim {} was unloaded, stopping aura thread", dim);
		    stop();
		    break;
		}
	    }
	}

	Thaumcraft.log.info("Stopping aura thread for dim {}", dim);
	ServerEvents.auraThreads.remove(dim);
    }

    @Override
    public void stop() {
	stop = true;
	signalAuraLoop();
    }

}
