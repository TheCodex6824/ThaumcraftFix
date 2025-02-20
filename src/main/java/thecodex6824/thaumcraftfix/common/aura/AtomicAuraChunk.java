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

import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.util.concurrent.AtomicDouble;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import thaumcraft.common.world.aura.AuraChunk;
import thecodex6824.thaumcraftfix.ThaumcraftFix;
import thecodex6824.thaumcraftfix.api.aura.IAuraChunk;

public class AtomicAuraChunk extends AuraChunk implements IAuraChunk {

    private static final int MAX_AURA = Short.MAX_VALUE;

    private AtomicInteger baseAtomic;
    private AtomicDouble visAtomic;
    private AtomicDouble fluxAtomic;
    private volatile boolean modified;

    public AtomicAuraChunk(Chunk chunk, short base, float vis, float flux) {
	super(chunk, base, vis, flux);
	baseAtomic = new AtomicInteger(Math.max(Math.min(base, MAX_AURA), 0));
	visAtomic = new AtomicDouble(Math.max(Math.min(vis, MAX_AURA), 0.0f));
	fluxAtomic = new AtomicDouble(Math.max(Math.min(flux, MAX_AURA), 0.0f));
    }

    public AtomicAuraChunk(ChunkPos pos) {
	super(pos);
	baseAtomic = new AtomicInteger(0);
	visAtomic = new AtomicDouble(0.0f);
	fluxAtomic = new AtomicDouble(0.0f);
    }

    @Override
    public ChunkPos getPosition() {
	return getLoc();
    }

    @Override
    public short getBase() {
	return baseAtomic.shortValue();
    }

    @Override
    public void setBase(short base) {
	baseAtomic.set(Math.max(Math.min(base, MAX_AURA), 0));
	modified = true;
    }

    @Override
    public boolean compareAndSetBase(short compare, short newValue) {
	boolean res = baseAtomic.compareAndSet(compare, newValue);
	if (res) {
	    modified = true;
	}

	return res;
    }

    @Override
    public short addBase(short add) {
	while (true) {
	    // current is an int to ensure the math ops below are handled as ints
	    // this makes sure overflow to a negative value won't happen and instead clamps at max
	    int current = baseAtomic.shortValue();
	    short target = (short) Math.max(Math.min(current + add, MAX_AURA), 0);
	    // at least on my platform, compareAndSet returns false if current == target
	    // can't find any docs explaining it so just avoid trying it
	    if (current == target || visAtomic.compareAndSet(current, target)) {
		modified = true;
		return (short) (target - current);
	    }
	}
    }

    @Override
    public short getAndSetBase(short set) {
	short res = (short) baseAtomic.getAndSet(Math.max(Math.min(set, MAX_AURA), 0));
	modified = true;
	return res;
    }

    @Override
    public float getVis() {
	return visAtomic.floatValue();
    }

    @Override
    public void setVis(float newVis) {
	visAtomic.set(Math.max(Math.min(newVis, MAX_AURA), 0.0f));
	modified = true;
    }

    @Override
    public boolean compareAndSetVis(float compare, float newValue) {
	boolean res = visAtomic.compareAndSet(compare, Math.max(Math.min(newValue, MAX_AURA), 0.0f));
	if (res) {
	    modified = true;
	}

	return res;
    }

    @Override
    public float addVis(float add) {
	while (true) {
	    boolean adjustmentNeeded = false;
	    double current = visAtomic.get();
	    double target = current + add;
	    // this is here so that if current + add is in bounds, we can return add itself
	    // this prevents any floating point issues from coming in due to having to calc target - current
	    // if it is out of bounds, we have to do math and all bets are off anyway
	    // this tries to keep the API the same as Thaumcraft had it, while also not silently throwing away bounds changes
	    if (current + add > Short.MAX_VALUE || current + add < 0) {
		target = Math.max(Math.min(current + add, Short.MAX_VALUE), 0);
		// remember that we can't return the add amount verbatim
		adjustmentNeeded = true;
	    }

	    if (current == target || visAtomic.compareAndSet(current, target)) {
		modified = true;
		return adjustmentNeeded ? (float) (target - current) : add;
	    }
	}
    }

    @Override
    public float getAndSetVis(float set) {
	float res = (float) visAtomic.getAndSet(Math.max(Math.min(set, MAX_AURA), 0));
	modified = true;
	return res;
    }

    @Override
    public float getFlux() {
	return fluxAtomic.floatValue();
    }

    @Override
    public void setFlux(float newFlux) {
	fluxAtomic.set(Math.max(Math.min(newFlux, MAX_AURA), 0.0f));
	modified = true;
    }

    @Override
    public boolean compareAndSetFlux(float compare, float newValue) {
	boolean res = fluxAtomic.compareAndSet(compare, Math.max(Math.min(newValue, MAX_AURA), 0.0f));
	if (res) {
	    modified = true;
	}

	return res;
    }

    @Override
    public float addFlux(float add) {
	while (true) {
	    boolean adjustmentNeeded = false;
	    double current = fluxAtomic.get();
	    double target = current + add;
	    if (current + add > Short.MAX_VALUE || current + add < 0) {
		target = Math.max(Math.min(current + add, Short.MAX_VALUE), 0);
		adjustmentNeeded = true;
	    }

	    if (current == target || fluxAtomic.compareAndSet(current, target)) {
		modified = true;
		return adjustmentNeeded ? (float) (target - current) : add;
	    }
	}
    }

    @Override
    public float getAndSetFlux(float set) {
	float res = (float) fluxAtomic.getAndSet(Math.max(Math.min(set, MAX_AURA), 0));
	modified = true;
	return res;
    }

    @Override
    public void setLoc(ChunkPos loc) {
	// this doesn't update the weak chunk reference held
	// we can't get it ourselves either since we might not be the server thread
	// Thaumcraft doesn't use this, hopefully nobody else is reaching this far into TC internals, so complain loudly
	ThaumcraftFix.instance.getLogger().warn("Someone is calling AuraChunk#setLoc, which is unsafe and probably doesn't do what they are expecting");
	ThaumcraftFix.instance.getLogger().warn("If you are the mod author, call removeAuraChunk and addAuraChunk with a new chunk instead");
	super.setLoc(loc);
    }

    @Override
    public boolean isModified() {
	return modified;
    }

}
