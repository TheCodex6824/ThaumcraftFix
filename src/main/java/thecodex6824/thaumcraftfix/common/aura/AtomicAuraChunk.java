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

public class AtomicAuraChunk extends AuraChunk implements IAtomicAuraChunk {

    private AtomicInteger baseAtomic;
    private AtomicDouble visAtomic;
    private AtomicDouble fluxAtomic;

    public AtomicAuraChunk(Chunk chunk, short base, float vis, float flux) {
	super(chunk, base, vis, flux);
	baseAtomic = new AtomicInteger(Math.max(base, 0));
	visAtomic = new AtomicDouble(Math.max(vis, 0.0f));
	fluxAtomic = new AtomicDouble(Math.max(flux, 0.0f));
    }

    public AtomicAuraChunk(ChunkPos pos) {
	super(pos);
	baseAtomic = new AtomicInteger(0);
	visAtomic = new AtomicDouble(0.0f);
	fluxAtomic = new AtomicDouble(0.0f);
    }

    @Override
    public short getBase() {
	return baseAtomic.shortValue();
    }

    @Override
    public void setBase(short base) {
	baseAtomic.set(Math.max(Math.min(base, Short.MAX_VALUE), 0));
    }

    @Override
    public boolean compareAndSetBase(short compare, short newValue) {
	return baseAtomic.compareAndSet(compare, newValue);
    }

    @Override
    public short addBase(short add) {
	while (true) {
	    // current is an int to ensure the math ops below are handled as ints
	    // this makes sure overflow to a negative value won't happen and instead clamps at max
	    int current = baseAtomic.shortValue();
	    short target = (short) Math.max(Math.min(current + add, Short.MAX_VALUE), 0);
	    // at least on my platform, compareAndSet returns false if current == target
	    // can't find any docs explaining it so just avoid trying it
	    if (current == target || visAtomic.compareAndSet(current, target)) {
		return (short) (target - current);
	    }
	}
    }

    @Override
    public short getAndSetBase(short set) {
	return (short) baseAtomic.getAndSet(Math.max(set, 0));
    }

    @Override
    public float getVis() {
	return visAtomic.floatValue();
    }

    @Override
    public void setVis(float newVis) {
	visAtomic.set(newVis);
    }

    @Override
    public boolean compareAndSetVis(float compare, float newValue) {
	return visAtomic.compareAndSet(compare, newValue);
    }

    @Override
    public float addVis(float add) {
	while (true) {
	    double current = visAtomic.get();
	    float target = (float) Math.max(Math.min(current + add, Float.MAX_VALUE), 0);
	    if (current == target || visAtomic.compareAndSet(current, target)) {
		return (float) (target - current);
	    }
	}
    }

    @Override
    public float getAndSetVis(float set) {
	return (float) visAtomic.getAndSet(Math.max(set, 0));
    }

    @Override
    public float getFlux() {
	return fluxAtomic.floatValue();
    }

    @Override
    public void setFlux(float newFlux) {
	fluxAtomic.set(newFlux);
    }

    @Override
    public boolean compareAndSetFlux(float compare, float newValue) {
	return fluxAtomic.compareAndSet(compare, newValue);
    }

    @Override
    public float addFlux(float add) {
	while (true) {
	    double current = fluxAtomic.floatValue();
	    float target = (float) Math.max(Math.min(current + add, Float.MAX_VALUE), 0);
	    if (current == target || fluxAtomic.compareAndSet(current, target)) {
		return (float) (target - current);
	    }
	}
    }

    @Override
    public float getAndSetFlux(float set) {
	return (float) fluxAtomic.getAndSet(Math.max(set, 0));
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

}
