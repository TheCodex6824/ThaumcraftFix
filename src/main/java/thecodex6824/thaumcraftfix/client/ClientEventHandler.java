package thecodex6824.thaumcraftfix.client;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.common.items.ItemTCBase;
import thecodex6824.thaumcraftfix.api.ThaumcraftFixApi;

@EventBusSubscriber(modid = ThaumcraftFixApi.MODID, value = Side.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onRegisterModels(ModelRegistryEvent event) {
	ItemTCBase pearl = (ItemTCBase) ItemsTC.primordialPearl;
	ModelResourceLocation model = pearl.getCustomModelResourceLocation(pearl.getVariantNames()[0]);
	for (int i = 0; i < 8; ++i) {
	    ModelLoader.setCustomModelResourceLocation(ItemsTC.primordialPearl, i, model);
	}
    }

}
