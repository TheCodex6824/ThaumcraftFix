package thecodex6824.thaumcraftfix.client;

import java.util.Random;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.common.entities.monster.EntityFireBat;
import thaumcraft.common.entities.monster.EntityWisp;
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

    @SubscribeEvent
    public static void onLivingUpdate(LivingUpdateEvent event) {
	if (event.getEntityLiving().getEntityWorld().isRemote) {
	    EntityLivingBase entity = event.getEntityLiving();
	    if (entity instanceof EntityFireBat) {
		World world = entity.getEntityWorld();
		Random random = entity.getRNG();
		world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
			entity.prevPosX + (random.nextFloat() - random.nextFloat()) * 0.2F,
			entity.prevPosY + entity.height / 2.0F + (random.nextFloat() - random.nextFloat()) * 0.2F,
			entity.prevPosZ + (random.nextFloat() - random.nextFloat()) * 0.2F,
			0, 0, 0
			);
		world.spawnParticle(EnumParticleTypes.FLAME,
			entity.prevPosX + (random.nextFloat() - random.nextFloat()) * 0.2F,
			entity.prevPosY + entity.height / 2.0F + (random.nextFloat() - random.nextFloat()) * 0.2F,
			entity.prevPosZ + (random.nextFloat() - random.nextFloat()) * 0.2F,
			0, 0, 0
			);
	    }
	}
    }

    // a size of 1 is already done in the EntityWisp class
    private static final float[] BURST_SIZES = new float[] { 4.5F, 10.0F };

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
	if (event.getEntityLiving().getEntityWorld().isRemote) {
	    EntityLivingBase entity = event.getEntityLiving();
	    if (entity instanceof EntityWisp) {
		for (float size : BURST_SIZES) {
		    FXDispatcher.INSTANCE.burst(entity.posX, entity.posY + entity.height / 2.0,
			    entity.posZ, size);
		}
	    }
	}
    }

}
