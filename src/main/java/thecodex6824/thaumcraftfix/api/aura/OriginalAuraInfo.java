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

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Default implementation for the OriginalAuraInfo capability.
 * @see IOriginalAuraInfo
 */
public class OriginalAuraInfo implements IOriginalAuraInfo, INBTSerializable<NBTTagCompound> {

    protected short base;
    protected float vis;
    protected float flux;

    public OriginalAuraInfo() {}

    @Override
    public short getBase() {
	return base;
    }

    @Override
    public void setBase(short newBase) {
	base = newBase;
    }

    @Override
    public float getVis() {
	return vis;
    }

    @Override
    public void setVis(float newVis) {
	vis = newVis;
    }

    @Override
    public float getFlux() {
	return flux;
    }

    @Override
    public void setFlux(float newFlux) {
	flux = newFlux;
    }

    @Override
    public NBTTagCompound serializeNBT() {
	NBTTagCompound tag = new NBTTagCompound();
	tag.setShort("base", base);
	tag.setFloat("vis", vis);
	tag.setFloat("flux", flux);
	return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
	base = nbt.getShort("base");
	vis = nbt.getFloat("vis");
	flux = nbt.getFloat("flux");
    }

}
