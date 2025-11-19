/**
 *  Thaumcraft Fix
 *  Copyright (c) 2025 TheCodex6824.
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

package thecodex6824.thaumcraftfix.api.casting;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Interface for blocks that want to cancel exchanging or place a blockstate other than the state corresponding
 * directly to the item targeted by the Exchange focus. This is generally only needed if the block normally
 * adjusts its state when placed manually by players, and the default state chosen is not correct.
 * Thaumcraft logs are an example of this.
 */
public interface ICustomExchangeState {

    /**
     * Return a state to place from the Exchange operation.
     *
     * <br><strong>Please do not use this method to try to implement your own swap logic. It is
     * very easy to accidentally create duplication bugs if swapping is not handled properly.</strong>
     *
     * @param world The World where the swap is happening
     * @param pos The position where the swap is happening
     * @param caster The entity casting the exchange focus
     * @param targetStack The ItemStack that was used as the "swapper target" for the Exchange focus. This is the stack the player selected ahead of time,
     * not the block that is being replaced.
     * @param original The IBlockState that Thaumcraft Fix would normally place. It is guaranteed to be the same Block as the class it is calling this method on.
     * @return The state that should be placed, or null to not place a state.
     * Returning null does not modify blocks at all - if you instead want to place air for some reason,
     * return an actual air state.
     */
    public @Nullable IBlockState getPlacedExchangeState(World world, BlockPos pos, EntityLivingBase caster, ItemStack targetStack, IBlockState original);

}
