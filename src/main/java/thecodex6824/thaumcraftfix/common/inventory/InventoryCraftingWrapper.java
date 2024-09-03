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

package thecodex6824.thaumcraftfix.common.inventory;

import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class InventoryCraftingWrapper extends InventoryCrafting {

    private final InventoryCrafting wrapped;

    public InventoryCraftingWrapper(InventoryCrafting toWrap) {
	super(toWrap.eventHandler, 3, 3);
	wrapped = toWrap;
    }

    private int convertRowColToIndex(int row, int col) {
	if (row < 0 || col < 0 || row >= getWidth() || col >= getHeight()) {
	    return -1;
	}

	return row + col * getWidth();
    }

    private boolean rangeCheck(int index) {
	return index >= 0 && index < getSizeInventory();
    }

    @Override
    public void clear() {
	for (int slot = 0; slot < getSizeInventory(); ++slot) {
	    wrapped.setInventorySlotContents(slot, ItemStack.EMPTY);
	}
    }

    @Override
    public void closeInventory(EntityPlayer player) {
	wrapped.closeInventory(player);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
	return rangeCheck(index) ? wrapped.decrStackSize(index, count) : ItemStack.EMPTY;
    }

    @Override
    public void fillStackedContents(RecipeItemHelper helper) {
	for (int slot = 0; slot < getSizeInventory(); ++slot) {
	    helper.accountStack(wrapped.getStackInSlot(slot));
	}
    }

    @Override
    public ITextComponent getDisplayName() {
	return wrapped.getDisplayName();
    }

    @Override
    public int getField(int id) {
	return wrapped.getField(id);
    }

    @Override
    public int getFieldCount() {
	return wrapped.getFieldCount();
    }

    @Override
    public int getHeight() {
	// intentionally using super call instead of wrapping
	return super.getHeight();
    }

    @Override
    public int getInventoryStackLimit() {
	return wrapped.getInventoryStackLimit();
    }

    @Override
    public String getName() {
	return wrapped.getName();
    }

    @Override
    public int getSizeInventory() {
	// intentionally using super call instead of wrapping
	return super.getSizeInventory();
    }

    @Override
    public ItemStack getStackInRowAndColumn(int row, int column) {
	int index = convertRowColToIndex(row, column);
	return rangeCheck(index) ? wrapped.getStackInSlot(index) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
	return rangeCheck(index) ? wrapped.getStackInSlot(index) : ItemStack.EMPTY;
    }

    @Override
    public int getWidth() {
	// intentionally using super call instead of wrapping
	return super.getWidth();
    }

    @Override
    public boolean hasCustomName() {
	return wrapped.hasCustomName();
    }

    @Override
    public boolean isEmpty() {
	for (int slot = 0; slot < getSizeInventory(); ++slot) {
	    if (!wrapped.getStackInSlot(slot).isEmpty()) {
		return false;
	    }
	}

	return true;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
	return rangeCheck(index) ? wrapped.isItemValidForSlot(index, stack) : false;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
	return wrapped.isUsableByPlayer(player);
    }

    @Override
    public void markDirty() {
	wrapped.markDirty();
    }

    @Override
    public void openInventory(EntityPlayer player) {
	wrapped.openInventory(player);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
	return rangeCheck(index) ? wrapped.removeStackFromSlot(index) : ItemStack.EMPTY;
    }

    @Override
    public void setField(int id, int value) {
	wrapped.setField(id, value);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
	if (rangeCheck(index)) {
	    wrapped.setInventorySlotContents(index, stack);
	}
    }

}
