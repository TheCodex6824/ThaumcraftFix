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

package thecodex6824.thaumcraftfix.client;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.lib.events.RenderEventHandler;
import thaumcraft.common.entities.monster.EntityFireBat;
import thaumcraft.common.entities.monster.EntityWisp;
import thaumcraft.common.items.ItemTCBase;
import thaumcraft.common.items.tools.ItemThaumometer;
import thecodex6824.thaumcraftfix.ThaumcraftFix;
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

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
	if (event.phase == Phase.START) {
	    boolean validMeter = false;
	    Entity thaum = RenderEventHandler.thaumTarget;
	    if (thaum != null && !thaum.isDead && (!(thaum instanceof EntityLivingBase) || ((EntityLivingBase) thaum).getHealth() > 0.0F)) {
		Entity renderView = Minecraft.getMinecraft().getRenderViewEntity();
		if (renderView instanceof EntityLivingBase) {
		    EntityLivingBase living = (EntityLivingBase) renderView;
		    if (living.getHeldItemMainhand().getItem() instanceof ItemThaumometer ||
			    living.getHeldItemOffhand().getItem() instanceof ItemThaumometer) {
			validMeter = true;
		    }
		}
	    }

	    if (!validMeter) {
		RenderEventHandler.thaumTarget = null;
	    }
	}
    }

    private static Field PARTICLES = null;
    private static Field PARTICLES_DELAYED = null;
    private static Field PARTICLE_DELAY_DIM = null;

    private static int getDimension(Object particleDelay) {
	try {
	    return PARTICLE_DELAY_DIM.getInt(particleDelay);
	}
	catch (Exception ex) {
	    throw new RuntimeException(ex);
	}
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void onClientWorldUnload(WorldEvent.Unload event) {
	int unloadedDim = event.getWorld().provider.getDimension();
	try {
	    if (PARTICLES == null || PARTICLES_DELAYED == null || PARTICLE_DELAY_DIM == null) {
		PARTICLES = ParticleEngine.class.getDeclaredField("particles");
		PARTICLES.setAccessible(true);

		PARTICLES_DELAYED = ParticleEngine.class.getDeclaredField("particlesDelayed");
		PARTICLES_DELAYED.setAccessible(true);

		// ParticleDelay is not public so we can't name it
		Class<?> particleDelay = null;
		for (Class<?> c : ParticleEngine.class.getDeclaredClasses()) {
		    if (c.getSimpleName().equals("ParticleDelay")) {
			particleDelay = c;
			break;
		    }
		}

		if (particleDelay == null) {
		    throw new RuntimeException("Can't find ParticleEngine$ParticleDelay");
		}

		PARTICLE_DELAY_DIM = particleDelay.getDeclaredField("dim");
		PARTICLE_DELAY_DIM.setAccessible(true);
	    }

	    Map<Integer, ? extends List<Particle>>[] particleMaps = (Map<Integer, ? extends List<Particle>>[]) PARTICLES.get(null);
	    for (int layer = 0; layer < particleMaps.length; ++layer) {
		List<Particle> list = particleMaps[layer].get(unloadedDim);
		if (list != null) {
		    for (Particle p : list) {
			p.setExpired();
		    }

		    list.clear();
		}
	    }

	    List<?> delayed = (List<?>) PARTICLES_DELAYED.get(null);
	    delayed.removeIf(p -> getDimension(p) == unloadedDim);
	}
	catch (Exception ex) {
	    ThaumcraftFix.instance.getLogger().error("Failed to clear Thaumcraft particle lists", ex);
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
