package thecodex6824.thaumcraftfix.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import thaumcraft.client.lib.events.RenderEventHandler;

@SideOnly(Side.CLIENT)
@Mixin(value = RenderEventHandler.class, remap = false)
public class RenderEventHandlerMixin {
    /**
     * @author WaitingIdly
     * @reason When finding the relevant line to place the aspect icons,
     * check that this line doesn't have any other text to avoid mis-identifying and
     * attempting to render the aspect icons over text.
     */
    @WrapOperation(method = "tooltipEvent(Lnet/minecraftforge/client/event/RenderTooltipEvent$PostBackground;)V", at = @At(value = "INVOKE", target = "Ljava/lang/String;contains(Ljava/lang/CharSequence;)Z"))
    private static boolean ensureCorrectLine(String instance, CharSequence charSequence, Operation<Boolean> original) {
        return StringUtils.isBlank(StringUtils.remove(instance, TextFormatting.GRAY.toString())) && original.call(instance, charSequence);
    }
}
