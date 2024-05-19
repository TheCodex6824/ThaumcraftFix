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

import java.util.function.Supplier;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.coremodlib.PatchStateMachine;

public class SoundTransformers {

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
				    TransformUtil.HOOKS_COMMON,
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

    public static final Supplier<ITransformer> SOUND_FIX_LOOT_BAG = makeSoundFixupTransformer(TransformUtil.remapMethod(new MethodDefinition(
	    "thaumcraft/common/items/curios/ItemLootBag",
	    false,
	    "func_77659_a",
	    Type.getType("Lnet/minecraft/util/ActionResult;"),
	    Types.WORLD, Types.ENTITY_PLAYER, Type.getType("Lnet/minecraft/util/EnumHand;")
	    )),
	    Types.ENTITY_PLAYER, 2);

    public static final Supplier<ITransformer> SOUND_FIX_PHIAL_FILL = makeSoundFixupTransformer(new MethodDefinition(
	    "thaumcraft/common/items/consumables/ItemPhial",
	    false,
	    "onItemUseFirst",
	    Type.getType("Lnet/minecraft/util/EnumActionResult;"),
	    Types.ENTITY_PLAYER, Types.WORLD, Types.BLOCK_POS, Types.ENUM_FACING, Type.FLOAT_TYPE,
	    Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.getType("Lnet/minecraft/util/EnumHand;")
	    ),
	    Types.ENTITY_PLAYER, 1);

    public static final Supplier<ITransformer> SOUND_FIX_JAR_FILL = makeSoundFixupTransformer(new MethodDefinition(
	    "thaumcraft/common/blocks/essentia/BlockJarItem",
	    false,
	    "onItemUseFirst",
	    Type.getType("Lnet/minecraft/util/EnumActionResult;"),
	    Types.ENTITY_PLAYER, Types.WORLD, Types.BLOCK_POS, Types.ENUM_FACING, Type.FLOAT_TYPE,
	    Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.getType("Lnet/minecraft/util/EnumHand;")
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
	    Types.WORLD, Types.ENTITY_PLAYER, Type.getType("Lnet/minecraft/util/EnumHand;")
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
