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

import java.util.Optional;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Default implementation for the OriginalAuraInfo capability.
 * @see IOriginalAuraInfo
 */
public class OriginalAuraInfo implements IOriginalAuraInfo, INBTSerializable<NBTTagCompound> {

    protected static final String KEY_BASE = "base";
    protected static final String KEY_VIS = "vis";
    protected static final String KEY_FLUX = "flux";

    protected Optional<Short> base;
    protected Optional<Float> vis;
    protected Optional<Float> flux;

    public OriginalAuraInfo() {
	base = Optional.empty();
	vis = Optional.empty();
	flux = Optional.empty();
    }

    @Override
    public Optional<Short> getBase() {
	return base;
    }

    @Override
    public void setBase(short newBase) {
	base = Optional.of(newBase);
    }

    @Override
    public Optional<Float> getVis() {
	return vis;
    }

    @Override
    public void setVis(float newVis) {
	vis = Optional.of(newVis);
    }

    @Override
    public Optional<Float> getFlux() {
	return flux;
    }

    @Override
    public void setFlux(float newFlux) {
	flux = Optional.of(newFlux);
    }

    @Override
    public NBTTagCompound serializeNBT() {
	NBTTagCompound tag = new NBTTagCompound();
	if (base.isPresent()) {
	    tag.setShort(KEY_BASE, base.get());
	}
	if (vis.isPresent()) {
	    tag.setFloat(KEY_VIS, vis.get());
	}
	if (flux.isPresent()) {
	    tag.setFloat(KEY_FLUX, flux.get());
	}
	return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
	if (nbt.hasKey(KEY_BASE, NBT.TAG_SHORT)) {
	    base = Optional.of(nbt.getShort("base"));
	}
	if (nbt.hasKey(KEY_VIS, NBT.TAG_FLOAT)) {
	    vis = Optional.of(nbt.getFloat("vis"));
	}
	if (nbt.hasKey(KEY_FLUX, NBT.TAG_FLOAT)) {
	    flux = Optional.of(nbt.getFloat("flux"));
	}
    }

}
