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
import java.util.function.Supplier;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import thaumcraft.common.container.ContainerFocalManipulator;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.coremodlib.PatchStateMachine;

public class SoundTransformers {

    public static final class Hooks {

	public static void fixupPlayerSound(EntityLivingBase player, SoundEvent sound, float volume, float pitch) {
	    if (player instanceof EntityPlayerMP) {
		// send the sound to the originating player, since the server won't do it
		SPacketSoundEffect packet = new SPacketSoundEffect(sound, player.getSoundCategory(), player.posX, player.posY, player.posZ, volume, pitch);
		((EntityPlayerMP) player).connection.sendPacket(packet);
	    }
	}

	private static Field containerFocalManipulatorTile;

	public static void fixupFocalManipulatorCraftFail(EntityPlayer player) throws Exception {
	    if (player instanceof EntityPlayerMP) {
		if (containerFocalManipulatorTile == null) {
		    containerFocalManipulatorTile = ContainerFocalManipulator.class.getDeclaredField("table");
		    containerFocalManipulatorTile.setAccessible(true);
		}

		TileEntity tile = ((TileEntity) containerFocalManipulatorTile.get(player.openContainer));
		BlockPos pos = tile.getPos();
		SPacketSoundEffect sound = new SPacketSoundEffect(SoundsTC.craftfail, SoundCategory.BLOCKS, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.33F, 1.0F);
		((EntityPlayerMP) player).connection.sendPacket(sound);
	    }
	}

    }

    private static final String HOOKS = Type.getInternalName(Hooks.class);

    private static Supplier<ITransformer> makeSoundFixupTransformer(MethodDefinition target, Type playerType, int playerIndex) {
	return () -> {
	    return new GenericStateMachineTransformer(
		    PatchStateMachine.builder(target)
		    .findConsecutive()
		    .findNextLocalAccess(playerIndex)
		    .findNextFieldAccessOfType(Types.SOUND_EVENT)
		    .endConsecutive()
		    .insertInstructionsAfter(new InsnNode(Opcodes.DUP2))
		    .findNextMethodCall(TransformUtil.remapMethod(new MethodDefinition(
			    playerType.getInternalName(),
			    false,
			    "func_184185_a",
			    Type.VOID_TYPE,
			    Types.SOUND_EVENT, Type.FLOAT_TYPE, Type.FLOAT_TYPE
			    )))
		    .insertInstructionsBefore(
			    new InsnNode(Opcodes.DUP2_X2),
			    new MethodInsnNode(Opcodes.INVOKESTATIC,
				    HOOKS,
				    "fixupPlayerSound",
				    Type.getMethodDescriptor(Type.VOID_TYPE, Types.ENTITY_LIVING_BASE,
					    Types.SOUND_EVENT, Type.FLOAT_TYPE, Type.FLOAT_TYPE),
				    false
				    )
			    )
		    .build()
		    );
	};
    }

    public static final Supplier<ITransformer> SOUND_FIX_CASTER_TICK = makeSoundFixupTransformer(new MethodDefinition(
	    "thaumcraft/common/items/casters/CasterManager",
	    false,
	    "changeFocus",
	    Type.VOID_TYPE,
	    Types.ITEM_STACK, Types.WORLD, Types.ENTITY_PLAYER, Types.STRING
	    ),
	    Types.ENTITY_PLAYER, 2);

    public static final Supplier<ITransformer> SOUND_FIX_FOCAL_MANIPULATOR_CONTAINER = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(TransformUtil.remapMethod(new MethodDefinition(
			"thaumcraft/common/container/ContainerFocalManipulator",
			false,
			"func_75140_a",
			Type.BOOLEAN_TYPE,
			Types.ENTITY_PLAYER, Type.INT_TYPE
			)))
		.findNextMethodCall(TransformUtil.remapMethod(new MethodDefinition(
			Types.WORLD.getInternalName(),
			false,
			"func_184133_a",
			Type.VOID_TYPE,
			Types.ENTITY_PLAYER, Types.BLOCK_POS, Types.SOUND_EVENT,
			Type.getType("Lnet/minecraft/util/SoundCategory;"), Type.FLOAT_TYPE, Type.FLOAT_TYPE
			)))
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 1),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS,
				"fixupFocalManipulatorCraftFail",
				Type.getMethodDescriptor(Type.VOID_TYPE, Types.ENTITY_PLAYER),
				false
				)
			)
		.build()
		);
    };

    public static final Supplier<ITransformer> SOUND_FIX_LOOT_BAG = makeSoundFixupTransformer(TransformUtil.remapMethod(new MethodDefinition(
	    "thaumcraft/common/items/curios/ItemLootBag",
	    false,
	    "func_77659_a",
	    Type.getType("Lnet/minecraft/util/ActionResult;"),
	    Types.WORLD, Types.ENTITY_PLAYER, Types.ENUM_HAND
	    )),
	    Types.ENTITY_PLAYER, 2);

    public static final Supplier<ITransformer> SOUND_FIX_PHIAL_FILL = makeSoundFixupTransformer(new MethodDefinition(
	    "thaumcraft/common/items/consumables/ItemPhial",
	    false,
	    "onItemUseFirst",
	    Type.getType("Lnet/minecraft/util/EnumActionResult;"),
	    Types.ENTITY_PLAYER, Types.WORLD, Types.BLOCK_POS, Types.ENUM_FACING, Type.FLOAT_TYPE,
	    Type.FLOAT_TYPE, Type.FLOAT_TYPE, Types.ENUM_HAND
	    ),
	    Types.ENTITY_PLAYER, 1);

    public static final Supplier<ITransformer> SOUND_FIX_JAR_FILL = makeSoundFixupTransformer(new MethodDefinition(
	    "thaumcraft/common/blocks/essentia/BlockJarItem",
	    false,
	    "onItemUseFirst",
	    Type.getType("Lnet/minecraft/util/EnumActionResult;"),
	    Types.ENTITY_PLAYER, Types.WORLD, Types.BLOCK_POS, Types.ENUM_FACING, Type.FLOAT_TYPE,
	    Type.FLOAT_TYPE, Type.FLOAT_TYPE, Types.ENUM_HAND
	    ),
	    Types.ENTITY_PLAYER, 1);

    public static final Supplier<ITransformer> SOUND_FIX_WIND_SWORD_USE = makeSoundFixupTransformer(new MethodDefinition(
	    "thaumcraft/common/items/tools/ItemElementalSword",
	    false,
	    "onUsingTick",
	    Type.VOID_TYPE,
	    Types.ITEM_STACK, Types.ENTITY_LIVING_BASE, Type.INT_TYPE
	    ),
	    Types.ENTITY_LIVING_BASE, 2);

    public static final Supplier<ITransformer> SOUND_FIX_MIRROR_USE = makeSoundFixupTransformer(TransformUtil.remapMethod(new MethodDefinition(
	    "thaumcraft/common/items/tools/ItemHandMirror",
	    false,
	    "func_77659_a",
	    Type.getType("Lnet/minecraft/util/ActionResult;"),
	    Types.WORLD, Types.ENTITY_PLAYER, Types.ENUM_HAND
	    )),
	    Types.ENTITY_PLAYER, 2);

    public static final Supplier<ITransformer> SOUND_FIX_MIRROR_TRANSPORT = makeSoundFixupTransformer(new MethodDefinition(
	    "thaumcraft/common/items/tools/ItemHandMirror",
	    false,
	    "transport",
	    Type.BOOLEAN_TYPE,
	    Types.ITEM_STACK, Types.ITEM_STACK, Types.ENTITY_PLAYER, Types.WORLD
	    ),
	    Types.ENTITY_PLAYER, 2);

}
