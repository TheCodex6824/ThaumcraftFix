/**
 *  Thaumcraft Fix
 *  Copyright (c) 2025 TheCodex6824 and other contributors.
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

package thecodex6824.thaumcraftfix.core.transformer.hooks;

import java.lang.reflect.Field;
import java.util.Map;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.common.config.ModConfig;
import thaumcraft.common.entities.EntityFluxRift;
import thaumcraft.common.entities.construct.EntityArcaneBore;
import thaumcraft.common.lib.enchantment.EnumInfusionEnchantment;
import thaumcraft.common.lib.utils.Utils;
import thecodex6824.thaumcraftfix.api.event.EntityInOuterLandsEvent;
import thecodex6824.thaumcraftfix.api.event.FluxRiftDestroyBlockEvent;
import thecodex6824.thaumcraftfix.common.util.NoEquipSoundFakePlayer;

public class EntityTransformersHooksCommon {

    public static int isInOuterLands(int entityDim, Entity entity) {
	EntityInOuterLandsEvent event = new EntityInOuterLandsEvent(entity);
	MinecraftForge.EVENT_BUS.post(event);
	boolean pass = event.getResult() == Result.ALLOW || (event.getResult() == Result.DEFAULT &&
		entity.getEntityWorld().provider.getDimension() == ModConfig.CONFIG_WORLD.dimensionOuterId);
	// if we want the check to pass, we return the entity dimension so the condition on TC's side passes
	// otherwise, we pass a different dimension so the check will fail
	return pass ? entityDim : entityDim + 1;
    }

    public static void clearDropChances(EntityLiving entity) {
	for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
	    entity.setDropChance(slot, 0.0f);
	}
    }

    public static boolean isEntityDeadForProcessInteract(boolean original, EntityLivingBase entity) {
	return original || entity.getHealth() <= 1.0e-5f;
    }

    public static boolean fireFluxRiftDestroyBlockEvent(EntityFluxRift rift, BlockPos pos, IBlockState state) {
	return MinecraftForge.EVENT_BUS.post(new FluxRiftDestroyBlockEvent(rift, pos, state));
    }

    public static EntityArrow fireArrow(EntityLivingBase shooter, float range) {
	ItemStack arrow = shooter.getHeldItemMainhand();
	EntityArrow entity = null;
	if (arrow.getItem() instanceof ItemArrow) {
	    entity = ((ItemArrow) arrow.getItem()).createArrow(shooter.getEntityWorld(), arrow, shooter);
	    // if damage would be less than normal Thaumcraft would make it, increase it
	    if (entity.getDamage() < 2.25) {
		entity.setDamage(2.25 + range * 2.0F + shooter.getRNG().nextGaussian() * 0.25);
	    }
	}

	return entity;
    }

    public static boolean isArrowInfinite(EntityLivingBase shooter) {
	EntityPlayer fakePlayer = null;
	// might as well check this in case anyones adds a fake player at some point
	if (shooter instanceof EntityPlayer) {
	    fakePlayer = (EntityPlayer) shooter;
	}
	else if (!shooter.getEntityWorld().isRemote) {
	    String fakeUsername = "TF";
	    if (shooter instanceof IEntityOwnable && ((IEntityOwnable) shooter).getOwnerId() != null) {
		// do not use getOwner here, as the owner may be offline
		fakeUsername += Long.toHexString(((IEntityOwnable) shooter).getOwnerId().getLeastSignificantBits());
		if (fakeUsername.length() > 16) {
		    // trim username to normal length
		    fakeUsername = fakeUsername.substring(0, 16);
		}
	    }
	    else {
		// spelling is to keep username <= 16 characters
		fakeUsername += "UnownedCrssbw";
	    }
	    fakePlayer = FakePlayerFactory.get((WorldServer) shooter.getEntityWorld(),
		    new GameProfile(null, fakeUsername));
	    // synchronize the worlds, as the crossbow could be in a different dimension
	    fakePlayer.setWorld(shooter.getEntityWorld());
	    ((FakePlayer) fakePlayer).interactionManager.setWorld((WorldServer) shooter.getEntityWorld());
	}

	ItemStack arrow = shooter.getHeldItemMainhand();
	// we pass a normal bow as the firing item so the arrow is consumed if it normally would
	// special "arrows" like quivers will always return true here and instead damage the item or such
	// this makes those items work as intended instead of being eaten by the crossbow
	return arrow.getItem() instanceof ItemArrow ?
		((ItemArrow) arrow.getItem()).isInfinite(arrow, new ItemStack(Items.BOW), fakePlayer) : null;
    }

    private static Field FAKE_PLAYER_MAP = null;

    @SuppressWarnings("unchecked")
    private static void setFakePlayerMapEntry(FakePlayer toInsert) {
	try {
	    if (FAKE_PLAYER_MAP == null) {
		FAKE_PLAYER_MAP = FakePlayerFactory.class.getDeclaredField("fakePlayers");
		FAKE_PLAYER_MAP.setAccessible(true);
	    }

	    ((Map<GameProfile, FakePlayer>) FAKE_PLAYER_MAP.get(null)).put(toInsert.getGameProfile(),
		    toInsert);
	}
	catch (Exception ex) {
	    throw new RuntimeException(ex);
	}
    }

    public static FakePlayer makeBoreFakePlayer(FakePlayer original, EntityArcaneBore bore) {
	if (!(original instanceof NoEquipSoundFakePlayer)) {
	    FakePlayer newPlayer = new NoEquipSoundFakePlayer(original.getServerWorld(), original.getGameProfile());
	    setFakePlayerMapEntry(newPlayer);
	    original = newPlayer;
	}

	float radiusLeft = bore.getDigRadius() - bore.currentRadius;
	int destructive = EnumInfusionEnchantment.getInfusionEnchantmentLevel(bore.getHeldItemMainhand(),
		EnumInfusionEnchantment.DESTRUCTIVE);
	// sneaking will cause only a single block to be broken, to keep the circle shape
	original.setSneaking(radiusLeft <= destructive);
	return original;
    }

    public static boolean isBoreTargetAir(World world, BlockPos target, boolean original, EntityArcaneBore bore) {
	// TC calls setBlockToAir and checks the return value, expecting it to be true (block set to air)
	// however, the block was already set to air, so it returns false
	boolean air = world.isAirBlock(target);
	if (air && world.getLight(target) < 10 && EnumInfusionEnchantment.getInfusionEnchantmentLevel(
		bore.getHeldItemMainhand(), EnumInfusionEnchantment.LAMPLIGHT) > 0) {

	    world.setBlockState(target, BlocksTC.effectGlimmer.getDefaultState());
	}

	return air;
    }

    public static int modSpiral(int old, EntityArcaneBore bore) {
	// remove the extra angle increment based on radius
	// this seems to cause the bore to "miss" some blocks
	int radMod = Math.max(0, (10 - Math.abs((int) bore.currentRadius)) * 2);
	return old - radMod;
    }

    public static float modCurrentRadius(float old, EntityArcaneBore bore) {
	// 1 was already added by Thaumcraft
	int spiralStep = EnumInfusionEnchantment.getInfusionEnchantmentLevel(bore.getHeldItemMainhand(),
		EnumInfusionEnchantment.DESTRUCTIVE) * 2; // + 1;
	return old + spiralStep;
    }

    public static float modCurrentRadiusReset(float zero, EntityArcaneBore bore) {
	int spiralStep = EnumInfusionEnchantment.getInfusionEnchantmentLevel(bore.getHeldItemMainhand(),
		EnumInfusionEnchantment.DESTRUCTIVE) * 2 + 1;
	return spiralStep > 1 ? (bore.currentRadius - bore.getDigRadius()) % spiralStep : 0;
    }

    public static Vec3d modRotation(Vec3d old, EntityArcaneBore bore) {
	EnumFacing facing = bore.getFacing();
	// add 0.5 instead of eyeheight, since players probably want symmetrical tunnels
	// flooring the coordinates instead of just truncating is also required
	Vec3d src = new Vec3d(MathHelper.floor(bore.posX) + 0.5 + facing.getXOffset(),
		MathHelper.floor(bore.posY) + 0.5 + facing.getYOffset(),
		MathHelper.floor(bore.posZ) + 0.5 + facing.getZOffset());
	Vec3d vec = new Vec3d(0, bore.currentRadius, 0);
	vec = Utils.rotateAroundZ(vec, (float) Math.toRadians(bore.spiral));
	if (facing.getAxis() != Axis.Y) {
	    // if the facing is in the Y axis, the horizontal angle is -90
	    vec = Utils.rotateAroundY(vec, (float) Math.toRadians(facing.getHorizontalAngle()));
	}
	else {
	    vec = Utils.rotateAroundX(vec, (float) Math.PI / 2.0F * facing.getYOffset());
	}
	Vec3d res = src.add(vec.x, vec.y, vec.z);
	//bore.world.setBlockState(new BlockPos(res), Blocks.AIR.getDefaultState());
	return res;
    }

    public static double getFixedDistanceSq(EntityArcaneBore bore, BlockPos target, double old) {
	// TC's check compares the center of the target block with the uncentered bore pos
	// this causes the top parts of the tunnel to be different from the bottom

	// flooring the coordinates also helps make sure the tunnel doesn't become asymmetrical
	// from the bore not being at the exact center of the block
	return target.distanceSqToCenter(MathHelper.floor(bore.posX) + 0.5, MathHelper.floor(bore.posY) + 0.5,
		MathHelper.floor(bore.posZ) + 0.5);
    }

}
