package thecodex6824.thaumcraftfix.mixin.baubles;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import baubles.api.IBauble;
import baubles.api.cap.IBaublesItemHandler;
import baubles.common.event.EventHandlerEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import thecodex6824.thaumcraftfix.api.event.BaubleSyncEvent;

@Mixin(value = EventHandlerEntity.class, remap = false)
public class EventHandlerEntityMixin {

    @ModifyExpressionValue(method = "syncBaubles", at = @At(
	    value = "INVOKE",
	    target = "Lbaubles/api/cap/IBaublesItemHandler;isChanged(I)Z"
	    ))
    private boolean shouldBaubleSync(boolean original, EntityPlayer player, IBaublesItemHandler handler,
	    @Local(ordinal = 0) IBauble bauble, @Local(ordinal = 0) ItemStack stack, @Local(ordinal = 0) int index) {

	BaubleSyncEvent event = new BaubleSyncEvent(player, handler, bauble, stack, index);
	MinecraftForge.EVENT_BUS.post(event);
	return event.getResult() == Result.ALLOW || (event.getResult() == Result.DEFAULT && original);
    }
    
}
