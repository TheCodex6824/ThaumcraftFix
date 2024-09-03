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

package thecodex6824.thaumcraftfix.api.event;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import thaumcraft.common.entities.EntityFluxRift;

/**
 * This event is fired whenever a {@link thaumcraft.common.entities.EntityFluxRift EntityFluxRift} attempts
 * to destroy a block contained in its bounding box.
 * <p>
 * Canceling this event will result in the block not being destroyed.
 */
@Cancelable
public class FluxRiftDestroyBlockEvent extends EntityEvent {

    protected final BlockPos pos;
    protected final IBlockState state;

    /**
     * Creates a new <code>FluxRiftDestroyBlockEvent</code>.
     * @param rift The {@link thaumcraft.common.entities.EntityFluxRift EntityFluxRift} attempting to destroy a block
     * @param position The {@link net.minecraft.util.math.BlockPos BlockPos} of the block
     * @param destroyedState The {@link net.minecraft.block.state.IBlockState IBlockState} of the block
     */
    public FluxRiftDestroyBlockEvent(EntityFluxRift rift, BlockPos position, IBlockState destroyedState) {
	super(rift);
	pos = position;
	state = destroyedState;
    }

    /**
     * Returns the {@link thaumcraft.common.entities.EntityFluxRift EntityFluxRift} that is attempting
     * to break a block.
     * @return The rift attempting to break the block
     */
    public EntityFluxRift getRift() {
	return (EntityFluxRift) getEntity();
    }

    /**
     * Returns the {@link net.minecraft.util.math.BlockPos BlockPos} of the block the rift is attempting to break.
     * @return The position of the block the rift is attempting to break
     */
    public BlockPos getPosition() {
	return pos;
    }

    /**
     * Returns the {@link net.minecraft.block.state.IBlockState IBlockState} of the block
     * the rift is attempting to destroy.
     * @return The state of the block the rift is attempting to break
     */
    public IBlockState getDestroyedBlock() {
	return state;
    }

}
