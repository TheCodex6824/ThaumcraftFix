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

import net.minecraft.util.math.ChunkPos;

public interface IAuraChunk {

    public ChunkPos getPosition();

    public short getBase();
    public void setBase(short newBase);
    public boolean compareAndSetBase(short compare, short newValue);
    public short addBase(short add);
    public short getAndSetBase(short set);

    public float getVis();
    public void setVis(float newVis);
    public boolean compareAndSetVis(float compare, float newValue);
    public float addVis(float add);
    public float getAndSetVis(float set);

    public float getFlux();
    public void setFlux(float newFlux);
    public boolean compareAndSetFlux(float compare, float newValue);
    public float addFlux(float add);
    public float getAndSetFlux(float set);

}
