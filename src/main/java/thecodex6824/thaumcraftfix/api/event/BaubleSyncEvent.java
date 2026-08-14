package thecodex6824.thaumcraftfix.api.event;

import baubles.api.IBauble;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.Event.HasResult;

/**
 * Event for receiving notifications when a Bauble is potentially being synced to clients.
 * Set the Result of this event to force (ALLOW), block (DENY), or use default sync behavior (DEFAULT) for a bauble.
 * The other use of this event would be to monitor Bauble syncing to know when to refresh/sync external data structures.
 */
@HasResult
public class BaubleSyncEvent extends EntityEvent {

    protected final IBaublesItemHandler inv;
    protected final IBauble bauble;
    protected final ItemStack stack;
    protected final int slot;
    
    public BaubleSyncEvent(EntityLivingBase holder, IBaublesItemHandler holderInventory, IBauble changedBauble, ItemStack baubleStack, int slotIndex) {
	super(holder);
	inv = holderInventory;
	bauble = changedBauble;
	stack = baubleStack;
	slot = slotIndex;
    }
    
    /**
     * Returns the Baubles inventory that the synced Bauble belongs to.
     * It is guaranteed that the ItemStack in this inventory with the slot from
     * changedSlot will correspond to the synced Bauble.
     * @return The Baubles inventory that the synced Bauble is a part of
     */
    public IBaublesItemHandler baublesInventory() {
	return inv;
    }
    
    /**
     * Returns the IBauble instance that is currently being synced.
     * @return The bauble being synced
     */
    public IBauble changedBauble() {
	return bauble;
    }
    
    /**
     * Returns the ItemStack that is currently being synced.
     * The inventory + slot combination and IBauble instance correspond to this stack.
     * Modifying this stack follows the same rules as modifying a stack returned by getting a stack
     * from an item handler, which means it is generally not a good idea without being very careful.
     * @return The bauble stack being synced
     */
    public ItemStack changedBaubleStack() {
	return stack;
    }
    
    /**
     * Returns the slot index of the Bauble being synced.
     * It is guaranteed that the ItemStack in the inventory from baublesInventory
     * combined with this slot will correspond to the synced Bauble.
     * @return The slot index of the changed slot
     */
    public int changedSlot() {
	return slot;
    }
    
}
