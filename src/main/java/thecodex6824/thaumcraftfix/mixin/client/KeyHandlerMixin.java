package thecodex6824.thaumcraftfix.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.api.casters.ICaster;
import thaumcraft.common.lib.events.KeyHandler;

@SideOnly(Side.CLIENT)
@Mixin(value = KeyHandler.class, remap = false)
public class KeyHandlerMixin {
    @Shadow public static KeyBinding keyF;
    @Shadow public static KeyBinding keyG;

    /**
     * @author Invadermonky
     * @reason Shifting the Change Caster Focus keybind up one letter value (F to G)
     */
    @ModifyConstant(
            method = "<clinit>",
            constant = @Constant(intValue = 33)
    )
    private static int modifyChangeCasterFocusKeyMixin(int constant) {
        return 34;
    }

    /**
     * @author Invadermonky
     * @reason Shifting the Caster Toggle keybind up one letter value (G to H)
     */
    @ModifyConstant(
            method = "<clinit>",
            constant = @Constant(intValue = 34)
    )
    private static int modifyCasterToggleKeyMixin(int constant) {
        return 35;
    }

    /**
     * @author Invadermonky
     * @reason Fixes Thaumcraft's Change Caster Focus keybind overwriting all other conflicting keybinds.
     */
    @Inject(method = "<clinit>", at = @At(value = "TAIL"))
    private static void modifyKeyBindMixin(CallbackInfo ci) {
        keyF.setKeyModifierAndCode(KeyModifier.SHIFT, keyF.getKeyCode());
        keyF.setKeyConflictContext(
                new IKeyConflictContext() {
                    @Override
                    public boolean isActive() {
                        EntityPlayerSP playerSP = Minecraft.getMinecraft().player;
                        if(playerSP != null) {
                            return playerSP.getHeldItemMainhand().getItem() instanceof ICaster || playerSP.getHeldItemOffhand().getItem() instanceof ICaster;
                        }
                        return false;
                    }

                    @Override
                    public boolean conflicts(IKeyConflictContext other) {
                        return this.isActive() || other.isActive();
                    }
                }
        );

        keyG.setKeyConflictContext(
                new IKeyConflictContext() {
                    @Override
                    public boolean isActive() {
                        if (FMLClientHandler.instance().getClient().inGameHasFocus) {
                            EntityPlayerSP playerSP = Minecraft.getMinecraft().player;
                            if (playerSP != null) {
                                return playerSP.getHeldItemMainhand().getItem() instanceof ICaster || playerSP.getHeldItemOffhand().getItem() instanceof ICaster;
                            }
                        }
                        return false;
                    }

                    @Override
                    public boolean conflicts(IKeyConflictContext other) {
                        return this.isActive() || other.isActive();
                    }
                }
        );
    }
}
