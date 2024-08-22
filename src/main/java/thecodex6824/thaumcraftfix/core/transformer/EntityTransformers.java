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

package thecodex6824.thaumcraftfix.core.transformer;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Supplier;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.client.renderers.models.gear.ModelCustomArmor;
import thaumcraft.common.config.ModConfig;
import thaumcraft.common.entities.EntityFluxRift;
import thaumcraft.common.entities.construct.EntityArcaneBore;
import thaumcraft.common.lib.enchantment.EnumInfusionEnchantment;
import thaumcraft.common.lib.utils.Utils;
import thecodex6824.coremodlib.FieldAccessType;
import thecodex6824.coremodlib.FieldDefinition;
import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.coremodlib.PatchStateMachine;
import thecodex6824.thaumcraftfix.api.event.EntityInOuterLandsEvent;
import thecodex6824.thaumcraftfix.api.event.FluxRiftDestroyBlockEvent;
import thecodex6824.thaumcraftfix.common.util.NoEquipSoundFakePlayer;
import thecodex6824.thaumcraftfix.core.transformer.custom.ChangeVariableTypeTransformer;
import thecodex6824.thaumcraftfix.core.transformer.custom.EntityAspectPrefixRemoverTransformer;
import thecodex6824.thaumcraftfix.core.transformer.custom.ThrowingTransformerWrapper;
import thecodex6824.thaumcraftfix.core.transformer.custom.TransformerBipedRotationCustomArmor;

public class EntityTransformers {

    public static final class HooksCommon {

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

	public static FakePlayer makeBoreFakePlayer(FakePlayer original) {
	    if (!(original instanceof NoEquipSoundFakePlayer)) {
		FakePlayer newPlayer = new NoEquipSoundFakePlayer(original.getServerWorld(), original.getGameProfile());
		setFakePlayerMapEntry(newPlayer);
		original = newPlayer;
	    }

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

	public static Vec3d modRotation(Vec3d old, EntityArcaneBore bore) {
	    EnumFacing facing = bore.getFacing();
	    // add 0.5 instead of eyeheight, since players probably want symmetrical tunnels
	    // flooring the coordinates instead of just truncating is also required
	    Vec3d src = new Vec3d(MathHelper.floor(bore.posX) + 0.5 + facing.getXOffset(),
		    MathHelper.floor(bore.posY) + 0.5 + facing.getYOffset(),
		    MathHelper.floor(bore.posZ) + 0.5 + facing.getZOffset());
	    Vec3d vec = new Vec3d(0, bore.currentRadius, 0);
	    vec = Utils.rotateAroundZ(vec, (float) Math.toRadians(bore.spiral));
	    vec = Utils.rotateAroundY(vec, (float) Math.toRadians(facing.getHorizontalAngle()));
	    vec = Utils.rotateAroundX(vec, (float) Math.PI / 2.0F * facing.getYOffset());
	    Vec3d res = src.add(vec.x, vec.y, vec.z);
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

    @SideOnly(Side.CLIENT)
    public static final class HooksClient {

	public static void correctRotationPoints(ModelBiped model) {
	    if (model instanceof ModelCustomArmor) {
		if (model.isSneak) {
		    model.bipedRightLeg.rotationPointY = 13.0F;
		    model.bipedLeftLeg.rotationPointY = 13.0F;
		    model.bipedHead.rotationPointY = 4.5F;

		    model.bipedBody.rotationPointY = 4.5F;
		    model.bipedRightArm.rotationPointY = 5.0F;
		    model.bipedLeftArm.rotationPointY = 5.0F;
		}
		else {
		    model.bipedBody.rotationPointY = 0.0F;
		    model.bipedRightArm.rotationPointY = 2.0F;
		    model.bipedLeftArm.rotationPointY = 2.0F;
		}

		model.bipedHeadwear.rotationPointX = model.bipedHead.rotationPointX;
		model.bipedHeadwear.rotationPointY = model.bipedHead.rotationPointY;
		model.bipedHeadwear.rotationPointZ = model.bipedHead.rotationPointZ;
	    }
	}

	public static float getRobeRotationDivisor(Entity entity) {
	    float f = 1.0F;
	    if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getTicksElytraFlying() > 4) {
		f = (float) (entity.motionX * entity.motionX + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ);
		f /= 0.2F;
		f = Math.max(f * f * f, 1.0F);
	    }

	    return f;
	}

	public static TextureAtlasSprite getBlockParticleTexture(TextureAtlasSprite old, IBlockState state) {
	    return Minecraft.getMinecraft().getBlockRendererDispatcher()
		    .getModelForState(state).getParticleTexture();
	}

	private static boolean boreHasLamplighter(EntityArcaneBore bore) {
	    ItemStack held = bore.getHeldItemMainhand();
	    return !held.isEmpty() && EnumInfusionEnchantment.getInfusionEnchantmentLevel(held,
		    EnumInfusionEnchantment.LAMPLIGHT) > 0;
	}

	public static boolean doesBoreHaveProperties(EntityArcaneBore bore, boolean original) {
	    return original || boreHasLamplighter(bore);
	}

	public static int drawLamplightText(EntityArcaneBore bore, int position) {
	    if (boreHasLamplighter(bore)) {
		String text = I18n.format("enchantment.infusion.LAMPLIGHT");
		Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, 4.0F, 34 + position, 0xffff00);
		position += 9;
	    }

	    return position;
	}

    }

    private static final String HOOKS_COMMON = Type.getInternalName(HooksCommon.class);

    @SideOnly(Side.CLIENT)
    private static final String HOOKS_CLIENT = Type.getInternalName(HooksClient.class);

    // pretty much rewrites a model rotation method to not be incompatible with everything
    // this does not use ThrowingTransformerWrapper because it was intentionally designed to be exceptionally brittle
    // if it can't apply, it should not crash the game, and it will dump debug info itself
    public static final ITransformer CUSTOM_ARMOR_NOT_CALLING_SUPER = new TransformerBipedRotationCustomArmor();

    // compensates for the above transformer by adding a hook to set custom rotation points
    public static final ITransformer CUSTOM_ARMOR_ROTATION_POINTS = new GenericStateMachineTransformer(
	    PatchStateMachine.builder(
		    TransformUtil.remapMethod(new MethodDefinition(
			    "net/minecraft/client/model/ModelBiped",
			    false,
			    "func_78087_a",
			    Type.VOID_TYPE,
			    Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE,
			    Type.FLOAT_TYPE, Types.ENTITY
			    )
			    ))
	    .findNextLocalAccess(3)
	    .insertInstructions((node, matches) -> {
		InsnList toAdd = new InsnList();
		toAdd.add(new VarInsnNode(Opcodes.ALOAD, 0));
		toAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
			HOOKS_CLIENT,
			"correctRotationPoints",
			Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType("Lnet/minecraft/client/model/ModelBiped;")),
			false
			));
		AbstractInsnNode match = matches.get(matches.size() - 1).matchStart();
		node.instructions.insertBefore(match.getPrevious().getPrevious().getPrevious(), toAdd);
		return ImmutableList.copyOf(toAdd.iterator());
	    })
	    .build(), true, 1 // important: just do this once
	    );

    // fixes annoying robe legging flapping (as if the player is walking) while elytra flying
    public static final ITransformer ELYTRA_ROBE_FLAPPING = new GenericStateMachineTransformer(
	    PatchStateMachine.builder(
		    TransformUtil.remapMethod(new MethodDefinition(
			    "thaumcraft/client/renderers/models/gear/ModelRobe",
			    false,
			    "func_78088_a",
			    Type.VOID_TYPE,
			    Types.ENTITY, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE,
			    Type.FLOAT_TYPE, Type.FLOAT_TYPE
			    )
			    ))
	    .findNextMethodCall(new MethodDefinition(
		    "java/lang/Math",
		    false,
		    "min",
		    Type.FLOAT_TYPE,
		    Type.FLOAT_TYPE, Type.FLOAT_TYPE
		    ))
	    .insertInstructionsAfter(
		    new VarInsnNode(Opcodes.ALOAD, 1),
		    new MethodInsnNode(Opcodes.INVOKESTATIC,
			    HOOKS_CLIENT,
			    "getRobeRotationDivisor",
			    Type.getMethodDescriptor(Type.FLOAT_TYPE, Types.ENTITY),
			    false
			    ),
		    new InsnNode(Opcodes.FDIV)
		    )
	    .build(), true, 1
	    );

    // required because TC always creates fog near eldritch guardians if not in the outer lands
    // but since the outer lands don't exist they always do it
    // even if it did exist its hardcodedness is problematic
    public static final ITransformer ELDRITCH_GUARDIAN_FOG = new GenericStateMachineTransformer(
	    PatchStateMachine.builder(
		    TransformUtil.remapMethod(new MethodDefinition(
			    "thaumcraft/common/entities/monster/EntityEldritchGuardian",
			    false,
			    "func_70071_h_",
			    Type.VOID_TYPE
			    )
			    ))
	    .findConsecutive()
	    .findNextMethodCall(new MethodDefinition(
		    "net/minecraft/world/WorldProvider",
		    false,
		    "getDimension",
		    Type.INT_TYPE
		    ))
	    .findNextFieldAccess(new FieldDefinition(
		    "thaumcraft/common/config/ModConfig$CONFIG_WORLD",
		    "dimensionOuterId",
		    Type.INT_TYPE
		    ))
	    .endConsecutive()
	    .insertInstructionsAfter(new VarInsnNode(Opcodes.ALOAD, 0),
		    new MethodInsnNode(Opcodes.INVOKESTATIC,
			    HOOKS_COMMON,
			    "isInOuterLands",
			    Type.getMethodDescriptor(Type.INT_TYPE, Type.INT_TYPE, Types.ENTITY),
			    false
			    )
		    )
	    .build()
	    );

    private static Supplier<ITransformer> makeEntityProcessInteractTransformer(String className) {
	return () -> {
	    return new GenericStateMachineTransformer(
		    PatchStateMachine.builder(
			    TransformUtil.remapMethod(new MethodDefinition(
				    className,
				    false,
				    "func_184645_a",
				    Type.BOOLEAN_TYPE,
				    Types.ENTITY_PLAYER, Types.ENUM_HAND
				    )
				    ))
		    .findNextFieldAccess(TransformUtil.remapField(new FieldDefinition(
			    className,
			    "field_70128_L",
			    Type.BOOLEAN_TYPE
			    )))
		    .insertInstructionsAfter(
			    new VarInsnNode(Opcodes.ALOAD, 0),
			    new MethodInsnNode(Opcodes.INVOKESTATIC,
				    HOOKS_COMMON,
				    "isEntityDeadForProcessInteract",
				    Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.BOOLEAN_TYPE, Types.ENTITY_LIVING_BASE),
				    false
				    )
			    )
		    .build()
		    );
	};
    }

    public static final Supplier<ITransformer> ENTITY_ASPECTS = () ->
    new ThrowingTransformerWrapper(new EntityAspectPrefixRemoverTransformer());

    public static final Supplier<ITransformer> ADVANCED_CROSSBOW_PROCESS_INTERACT_DEAD =
	    makeEntityProcessInteractTransformer("thaumcraft/common/entities/construct/EntityTurretCrossbowAdvanced");

    public static final Supplier<ITransformer> BORE_PROCESS_INTERACT_DEAD =
	    makeEntityProcessInteractTransformer(Types.ENTITY_ARCANE_BORE.getInternalName());

    public static final Supplier<ITransformer> CROSSBOW_PROCESS_INTERACT_DEAD =
	    makeEntityProcessInteractTransformer("thaumcraft/common/entities/construct/EntityTurretCrossbow");

    public static final Supplier<ITransformer> GOLEM_PROCESS_INTERACT_DEAD =
	    makeEntityProcessInteractTransformer("thaumcraft/common/golems/EntityThaumcraftGolem");

    public static final Supplier<ITransformer> OWNED_CONSTRUCT_PROCESS_INTERACT_DEAD =
	    makeEntityProcessInteractTransformer("thaumcraft/common/entities/construct/EntityOwnedConstruct");

    public static final Supplier<ITransformer> OWNED_CONSTRUCT_ZERO_DROP_CHANCES = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/common/entities/construct/EntityOwnedConstruct",
				false,
				"<init>",
				Type.VOID_TYPE,
				Types.WORLD
				)
			)
		.findNextOpcode(Opcodes.RETURN)
		.insertInstructionsBefore(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_COMMON,
				"clearDropChances",
				Type.getMethodDescriptor(Type.VOID_TYPE, Types.ENTITY_LIVING),
				false
				)
			)
		.build()
		);
    };

    public static final Supplier<ITransformer> PECH_ADD_STACK = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/common/container/ContainerPech",
				false,
				"addStack",
				Type.VOID_TYPE,
				Types.ITEM_STACK
				)
			)
		.findNextMethodCall(TransformUtil.remapMethod(new MethodDefinition(
			Types.ITEM_STACK.getInternalName(),
			false,
			"func_190917_f",
			Type.VOID_TYPE,
			Type.INT_TYPE
			)))
		.insertInstructionsAfter(
			new InsnNode(Opcodes.RETURN)
			)
		.build()
		);
    };

    // fixes armor counting twice visually for void robe armor
    // I get a lot of reports/questions about it, so here it is
    public static final ITransformer VOID_ROBE_ARMOR_DISPLAY = new GenericStateMachineTransformer(
	    PatchStateMachine.builder(
		    new MethodDefinition(
			    "thaumcraft/common/items/armor/ItemVoidRobeArmor",
			    false,
			    "getArmorDisplay",
			    Type.INT_TYPE,
			    Types.ENTITY_PLAYER, Types.ITEM_STACK, Type.INT_TYPE
			    )
		    )
	    .findNextOpcode(Opcodes.IRETURN)
	    .insertInstructionsBefore(new InsnNode(Opcodes.ICONST_0))
	    .build()
	    );

    // to fire an event when a flux rift eats a block
    public static final ITransformer FLUX_RIFT_DESTROY_BLOCK_EVENT = new GenericStateMachineTransformer(
	    PatchStateMachine.builder(
		    TransformUtil.remapMethod(new MethodDefinition(
			    "thaumcraft/common/entities/EntityFluxRift",
			    false,
			    "func_70071_h_",
			    Type.VOID_TYPE
			    )
			    ))
	    .findConsecutive()
	    .findNextLocalAccess(0)
	    .findNextFieldAccess(TransformUtil.remapField(new FieldDefinition(
		    "thaumcraft/common/entities/EntityFluxRift",
		    "field_70170_p",
		    Types.WORLD
		    )))
	    .findNextLocalAccess(5)
	    .findNextMethodCall(TransformUtil.remapMethod(new MethodDefinition(
		    "net/minecraft/world/World",
		    false,
		    "func_175623_d",
		    Type.BOOLEAN_TYPE,
		    Types.BLOCK_POS
		    )))
	    .findNextInstructionType(JumpInsnNode.class)
	    .endConsecutive()
	    .insertInstructions((node, matches) -> {
		InsnList toAdd = new InsnList();
		toAdd.add(new VarInsnNode(Opcodes.ALOAD, 0));
		toAdd.add(new VarInsnNode(Opcodes.ALOAD, 5));
		toAdd.add(new VarInsnNode(Opcodes.ALOAD, 6));
		toAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
			HOOKS_COMMON,
			"fireFluxRiftDestroyBlockEvent",
			Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType("Lthaumcraft/common/entities/EntityFluxRift;"),
				Types.BLOCK_POS, Types.I_BLOCK_STATE),
			false
			));
		JumpInsnNode originalJump = (JumpInsnNode) matches.get(0).matchEnd();
		toAdd.add(new JumpInsnNode(originalJump.getOpcode(), originalJump.label));

		ImmutableList<AbstractInsnNode> added = ImmutableList.copyOf(toAdd.iterator());
		node.instructions.insertBefore(matches.get(0).matchStart(), toAdd);

		return added;
	    })
	    .build()
	    );

    public static final Supplier<ITransformer> BORE_FIX_RUMBLE_AND_LAMPLIGHT = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				Types.ENTITY_ARCANE_BORE.getInternalName(),
				false,
				"dig",
				Type.BOOLEAN_TYPE
				)
			)
		.findNextMethodCall(TransformUtil.remapMethod(new MethodDefinition(
			Types.WORLD.getInternalName(),
			false,
			"func_175698_g",
			Type.BOOLEAN_TYPE,
			Types.BLOCK_POS
			)))
		.insertInstructionsSurrounding()
		.before(new InsnNode(Opcodes.DUP2))
		.after(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_COMMON,
				"isBoreTargetAir",
				Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Types.WORLD, Types.BLOCK_POS,
					Type.BOOLEAN_TYPE, Types.ENTITY_ARCANE_BORE),
				false
				)
			)
		.endAction()
		.build()
		);
    };

    public static final Supplier<ITransformer> BORE_GUI_PROPERTIES = () -> {
	MethodDefinition hasSilkTouch = new MethodDefinition(
		Types.ENTITY_ARCANE_BORE.getInternalName(),
		false,
		"hasSilkTouch",
		Type.BOOLEAN_TYPE
		);
	return new GenericStateMachineTransformer(PatchStateMachine.builder(
		TransformUtil.remapMethod(new MethodDefinition(
			"thaumcraft/client/gui/GuiArcaneBore",
			false,
			"func_146976_a",
			Type.VOID_TYPE,
			Type.FLOAT_TYPE, Type.INT_TYPE, Type.INT_TYPE
			)))
		.findNextMethodCall(hasSilkTouch)
		.insertInstructionsSurrounding()
		.before(new InsnNode(Opcodes.DUP))
		.after(
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_CLIENT,
				"doesBoreHaveProperties",
				Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Types.ENTITY_ARCANE_BORE, Type.BOOLEAN_TYPE),
				false
				)
			)
		.endAction()
		.findNextMethodCall(hasSilkTouch)
		.insertInstructionsBefore(
			new InsnNode(Opcodes.DUP),
			new VarInsnNode(Opcodes.ILOAD, 8),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_CLIENT,
				"drawLamplightText",
				Type.getMethodDescriptor(Type.INT_TYPE, Types.ENTITY_ARCANE_BORE, Type.INT_TYPE),
				false
				),
			new VarInsnNode(Opcodes.ISTORE, 8)
			)
		.build()
		);
    };

    public static final Supplier<ITransformer> BORE_NO_EQUIP_SOUND = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				Types.ENTITY_ARCANE_BORE.getInternalName(),
				false,
				"dig",
				Type.BOOLEAN_TYPE
				)
			)
		.findNextMethodCall(new MethodDefinition(
			Types.FAKE_PLAYER_FACTORY.getInternalName(),
			false,
			"get",
			Types.FAKE_PLAYER,
			Types.WORLD_SERVER, Types.GAME_PROFILE
			))
		.insertInstructionsAfter(
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_COMMON,
				"makeBoreFakePlayer",
				Type.getMethodDescriptor(Types.FAKE_PLAYER, Types.FAKE_PLAYER),
				false
				)
			)
		.build()
		);
    };

    public static final Supplier<ITransformer> BORE_PARTICLE_TEXTURE = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/client/fx/particles/FXBoreParticles",
				false,
				"<init>",
				Type.VOID_TYPE,
				Types.WORLD, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE,
				Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE,
				Types.I_BLOCK_STATE, Type.INT_TYPE
				)
			)
		.findNextMethodCall(TransformUtil.remapMethod(new MethodDefinition(
			"thaumcraft/client/fx/particles/FXBoreParticles",
			false,
			"func_187117_a",
			Type.VOID_TYPE,
			Types.TEXTURE_ATLAS_SPRITE
			)))
		.insertInstructionsBefore(
			new VarInsnNode(Opcodes.ALOAD, 14),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_CLIENT,
				"getBlockParticleTexture",
				Type.getMethodDescriptor(Types.TEXTURE_ATLAS_SPRITE, Types.TEXTURE_ATLAS_SPRITE,
					Types.I_BLOCK_STATE),
				false
				)
			)
		.build()
		);
    };

    public static final Supplier<ITransformer> BORE_SPIRAL_MISSES = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				Types.ENTITY_ARCANE_BORE.getInternalName(),
				false,
				"findNextBlockToDig",
				Type.VOID_TYPE
				)
			)
		.findNextMethodCall(TransformUtil.remapMethod(new MethodDefinition(
			"thaumcraft/common/entities/construct/EntityArcaneBore",
			false,
			"func_174831_c",
			Type.DOUBLE_TYPE,
			Types.BLOCK_POS
			)))
		.insertInstructionsSurrounding()
		.before(new InsnNode(Opcodes.DUP2))
		.after(
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_COMMON,
				"getFixedDistanceSq",
				Type.getMethodDescriptor(Type.DOUBLE_TYPE, Types.ENTITY_ARCANE_BORE,
					Types.BLOCK_POS, Type.DOUBLE_TYPE),
				false
				)
			)
		.endAction()
		.findNextFieldAccess(new FieldDefinition(
			Types.ENTITY_ARCANE_BORE.getInternalName(),
			"spiral",
			Type.INT_TYPE
			), FieldAccessType.STORE)
		.insertInstructionsBefore(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_COMMON,
				"modSpiral",
				Type.getMethodDescriptor(Type.INT_TYPE, Type.INT_TYPE,
					Types.ENTITY_ARCANE_BORE),
				false
				)
			)
		.findNextMethodCall(new MethodDefinition(
			"thaumcraft/common/lib/utils/Utils",
			false,
			"rotateAroundX",
			Types.VEC_3D,
			Types.VEC_3D, Type.FLOAT_TYPE
			))
		.findNextMethodCall(TransformUtil.remapMethod(new MethodDefinition(
			Types.VEC_3D.getInternalName(),
			false,
			"func_72441_c",
			Types.VEC_3D,
			Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE
			)))
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_COMMON,
				"modRotation",
				Type.getMethodDescriptor(Types.VEC_3D, Types.VEC_3D,
					Types.ENTITY_ARCANE_BORE),
				false
				)
			)
		.build()
		);
    };

    // since the arrow type returned by createArrow is EntityArrow and not EntityTippedArrow,
    // most method and field accesses need to be converted to use EntityArrow
    // both the new instance and setPotionEffect call are excluded
    public static final Supplier<ITransformer> CROSSBOW_FIRE_ARROW_CLASS = () -> new ThrowingTransformerWrapper(
	    new ChangeVariableTypeTransformer(
		    TransformUtil.remapMethod(new MethodDefinition(
			    "thaumcraft/common/entities/construct/EntityTurretCrossbow",
			    false,
			    "func_82196_d",
			    Type.VOID_TYPE,
			    Types.ENTITY_LIVING_BASE, Type.FLOAT_TYPE
			    )),
		    Types.ENTITY_TIPPED_ARROW,
		    Types.ENTITY_ARROW,
		    false,
		    ImmutableSet.of(
			    TransformUtil.remapMethod(new MethodDefinition(
				    Types.ENTITY_TIPPED_ARROW.getInternalName(),
				    false,
				    "func_184555_a",
				    Type.VOID_TYPE,
				    Types.ITEM_STACK
				    )).name()),
		    true
		    ));

    public static final Supplier<ITransformer> CROSSBOW_FIRE_ARROW_LOGIC = () -> {
	LabelNode afterArrowCreation = new LabelNode(new Label());
	LabelNode afterShrink = new LabelNode(new Label());
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			TransformUtil.remapMethod(new MethodDefinition(
				"thaumcraft/common/entities/construct/EntityTurretCrossbow",
				false,
				"func_82196_d",
				Type.VOID_TYPE,
				Types.ENTITY_LIVING_BASE, Type.FLOAT_TYPE
				)))
		.findNextNewObject(Types.ENTITY_TIPPED_ARROW)
		.findNextMethodCall(TransformUtil.remapMethod(new MethodDefinition(
			Types.ENTITY_TIPPED_ARROW.getInternalName(),
			false,
			"func_184555_a",
			Type.VOID_TYPE,
			Types.ITEM_STACK
			)))
		.combineLastTwoMatches()
		.insertInstructionsSurrounding()
		.before(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new VarInsnNode(Opcodes.FLOAD, 2),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_COMMON,
				"fireArrow",
				Type.getMethodDescriptor(Types.ENTITY_ARROW, Types.ENTITY_LIVING_BASE, Type.FLOAT_TYPE),
				false
				),
			new InsnNode(Opcodes.DUP),
			new JumpInsnNode(Opcodes.IFNONNULL, afterArrowCreation)
			)
		.after(
			afterArrowCreation,
			// note: do not add an arrow local here, as there is an append frame later on doing the same thing
			new FrameNode(Opcodes.F_SAME1, 0, null, 1, new Object[] { Types.ENTITY_ARROW.getInternalName() }),
			new VarInsnNode(Opcodes.ASTORE, 3)
			)
		.endAction()
		.findConsecutive()
		.findNextLocalAccess(0)
		.findNextMethodCall(TransformUtil.remapMethod(new MethodDefinition(
			"thaumcraft/common/entities/construct/EntityTurretCrossbow",
			false,
			"func_184614_ca",
			Types.ITEM_STACK
			)))
		// this could be ILOAD_1, ILOAD, LDC, etc if someone rewrote this method
		.findAny()
		.findNextMethodCall(TransformUtil.remapMethod(new MethodDefinition(
			Types.ITEM_STACK.getInternalName(),
			false,
			"func_190918_g",
			Type.VOID_TYPE,
			Type.INT_TYPE
			)))
		.endConsecutive()
		.insertInstructionsSurrounding()
		.before(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS_COMMON,
				"isArrowInfinite",
				Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Types.ENTITY_LIVING_BASE),
				false
				),
			new JumpInsnNode(Opcodes.IFNE, afterShrink)
			)
		.after(
			afterShrink,
			new FrameNode(Opcodes.F_SAME, 0, null, 0, null)
			)
		.endAction()
		.build(), true, 1
		);
    };

}
