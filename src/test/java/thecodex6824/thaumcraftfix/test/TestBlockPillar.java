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
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import thaumcraft.api.blocks.BlocksTC;
import thecodex6824.thaumcraftfix.testlib.lib.MockWorld;

public class TestBlockPillar {

    @Test
    public void testDoesNotDropItemsInBreakMethod() {
	Block pillar = BlocksTC.pillarArcane;
	MockWorld world = new MockWorld() {
	    @Override
	    public boolean spawnEntity(Entity entity) {
		fail("Break callback tried to spawn an entity: " + entity.getClass().toString());
		return false;
	    }
	};
	pillar.breakBlock(world, new BlockPos(0, 0, 0), pillar.getDefaultState());
    }

    @Test
    public void testDropsItemsCorrectly() {
	Block pillar = BlocksTC.pillarArcane;
	assertEquals(Item.getItemFromBlock(BlocksTC.stoneArcane), pillar.getItemDropped(pillar.getDefaultState(), null, 0));
	assertEquals(2, pillar.quantityDropped(null));
    }

}
