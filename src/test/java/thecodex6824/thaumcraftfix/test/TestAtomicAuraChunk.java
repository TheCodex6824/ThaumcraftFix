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

import org.junit.jupiter.api.Test;

import net.minecraft.world.chunk.Chunk;
import thecodex6824.thaumcraftfix.common.aura.AtomicAuraChunk;
import thecodex6824.thaumcraftfix.test.lib.MockWorld;

public class TestAtomicAuraChunk {

    private static final float FLOAT_EPSILON = 1e-3f;

    @Test
    public void testConstructionClamp() {
	AtomicAuraChunk aura = new AtomicAuraChunk(new Chunk(new MockWorld(), 0, 0),
		(short) -1, -1.0f, -1.0f);
	assertEquals(0, aura.getBase());
	assertEquals(0.0f, aura.getVis(), FLOAT_EPSILON);
	assertEquals(0.0f, aura.getFlux(), FLOAT_EPSILON);
    }

    @Test
    public void testAddBaseClamp() {
	AtomicAuraChunk aura = new AtomicAuraChunk(new Chunk(new MockWorld(), 0, 0),
		(short) 0, 0.0f, 0.0f);
	assertEquals((short) 0, aura.getBase());
	assertEquals((short) 0, aura.addBase((short) -1));
	assertEquals((short) 0, aura.getBase());

	aura.setBase(Short.MAX_VALUE);
	assertEquals(Short.MAX_VALUE, aura.getBase());
	assertEquals((short) 0, aura.addBase((short) 1));
	assertEquals(Short.MAX_VALUE, aura.getBase());
    }

    @Test
    public void testAddVisClamp() {
	AtomicAuraChunk aura = new AtomicAuraChunk(new Chunk(new MockWorld(), 0, 0),
		(short) 0, 0.0f, 0.0f);
	assertEquals(0.0f, aura.getVis(), FLOAT_EPSILON);
	assertEquals(0.0f, aura.addVis(-1.0f), FLOAT_EPSILON);
	assertEquals(0.0f, aura.getVis(), FLOAT_EPSILON);

	aura.setVis(Short.MAX_VALUE);
	assertEquals(Short.MAX_VALUE, aura.getVis(), FLOAT_EPSILON);
	assertEquals(0.0f, aura.addVis(1.0f), FLOAT_EPSILON);
	assertEquals(Short.MAX_VALUE, aura.getVis(), FLOAT_EPSILON);
    }

    @Test
    public void testAddFluxClamp() {
	AtomicAuraChunk aura = new AtomicAuraChunk(new Chunk(new MockWorld(), 0, 0),
		(short) 0, 0.0f, 0.0f);
	assertEquals(0.0f, aura.getFlux(), FLOAT_EPSILON);
	assertEquals(0.0f, aura.addFlux(-1.0f), FLOAT_EPSILON);
	assertEquals(0.0f, aura.getFlux(), FLOAT_EPSILON);

	aura.setFlux(Short.MAX_VALUE);
	assertEquals(Short.MAX_VALUE, aura.getFlux(), FLOAT_EPSILON);
	assertEquals(0.0f, aura.addFlux(1.0f), FLOAT_EPSILON);
	assertEquals(Short.MAX_VALUE, aura.getFlux(), FLOAT_EPSILON);
    }

}
