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

package thecodex6824.thaumcraftfix.mixin.block;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thaumcraft.common.blocks.world.plants.BlockLogsTC;
import thecodex6824.thaumcraftfix.api.casting.ICustomExchangeState;

@Mixin(BlockLogsTC.class)
public abstract class BlockLogsTCMixin extends Block implements ICustomExchangeState {

    private BlockLogsTCMixin() {
	super(null);
    }

    @Override
    public IBlockState getPlacedExchangeState(World world, BlockPos pos, EntityLivingBase caster, ItemStack targetStack,
	    IBlockState original) {
	return getDefaultState();
    }

}
