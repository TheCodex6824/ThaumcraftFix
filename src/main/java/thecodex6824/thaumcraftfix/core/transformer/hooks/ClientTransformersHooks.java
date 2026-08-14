package thecodex6824.thaumcraftfix.core.transformer.hooks;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.client.model.animation.AnimationTESR;
import net.minecraftforge.common.animation.Event;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.animation.CapabilityAnimation;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;
import thaumcraft.client.gui.GuiResearchPage.BlueprintBlockAccess;

public class ClientTransformersHooks {

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void renderFastTESRBlueprint(TileEntity tile, BlockPos pos, BlueprintBlockAccess world) {
        TileEntitySpecialRenderer<TileEntity> tesr = TileEntityRendererDispatcher.instance.getRenderer(tile);
        if (tesr != null) {
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            try {
                if (tesr instanceof AnimationTESR) {
                    // modified version of normal renderer that doesn't look up world in render cache
                    IAnimationStateMachine capability = tile.getCapability(CapabilityAnimation.ANIMATION_CAPABILITY, null);
                    if (capability != null) {
                        IBlockState state = world.getBlockState(pos);
                        if (state.getPropertyKeys().contains(Properties.StaticProperty)) {
                            state = state.withProperty(Properties.StaticProperty, false);
                        }
                        if (state instanceof IExtendedBlockState) {
                            IExtendedBlockState exState = (IExtendedBlockState) state;
                            if (exState.getUnlistedNames().contains(Properties.AnimationProperty)) {
                        	Minecraft mc = Minecraft.getMinecraft();
                                float time = Animation.getWorldTime(mc.world, mc.getRenderPartialTicks());
                                Pair<IModelState, Iterable<Event>> pair = capability.apply(time);
                                ((AnimationTESR) tesr).handleEvents(tile, time, pair.getRight());
                                BlockRendererDispatcher blockRenderer = mc.getBlockRendererDispatcher();
                                IBakedModel model = mc.getBlockRendererDispatcher().getBlockModelShapes().getModelForState(exState.getClean());
                                exState = exState.withProperty(Properties.AnimationProperty, pair.getLeft());
                                blockRenderer.getBlockModelRenderer().renderModel(world, model, exState, pos, buffer, false);
                            }
                        }
                    }
                }
                else {
                    // No idea what it can do, so just hope for the best
                    tesr.renderTileEntityFast(tile, pos.getX(), pos.getY(), pos.getZ(), Minecraft.getMinecraft().getRenderPartialTicks(),
                            0, 1.0F, buffer);
                }
            }
            catch (Exception ex) {
                // something doesn't like the fake world, not much we can do
            }
            finally {
                Tessellator.getInstance().draw();
            }
        }
    }
    
}
