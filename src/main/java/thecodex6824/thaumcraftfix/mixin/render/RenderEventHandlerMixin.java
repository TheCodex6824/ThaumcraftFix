package thecodex6824.thaumcraftfix.mixin.render;

import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.client.lib.events.RenderEventHandler;
import thaumcraft.common.lib.events.EssentiaHandler;

@SideOnly(Side.CLIENT)
@Mixin(value = RenderEventHandler.class, remap = false)
public abstract class RenderEventHandlerMixin {

    /**
     * @author kaduvill
     * @reason Prevents a client crash when Thaumcraft's essentia particle queue contains
     * stale/null EssentiaSourceFX entries or entries missing their start/end position.
     */
    @Inject(method = "clientWorldTick", at = @At("HEAD"))
    private static void thaumcraftfix$removeInvalidEssentiaSourceFX(TickEvent.ClientTickEvent event, CallbackInfo ci) {
        if (event.side == Side.SERVER || event.phase != TickEvent.Phase.START
                || EssentiaHandler.sourceFX == null || EssentiaHandler.sourceFX.isEmpty()) {
            return;
        }

        EssentiaHandler.sourceFX.entrySet().removeIf(entry -> {
            EssentiaHandler.EssentiaSourceFX fx = entry.getValue();
            return fx == null || fx.start == null || fx.end == null;
        });
    }

}
