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
import org.objectweb.asm.tree.IntInsnNode;

import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.coremodlib.PatchStateMachine;

public class WorldGenTransformers {

    private static final MethodDefinition BLOCKPOS_ADD = TransformUtil.remapMethod(
	    new MethodDefinition(
		    Types.BLOCK_POS.getInternalName(),
		    false,
		    "func_177982_a",
		    Types.BLOCK_POS,
		    Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE
		    )
	    );

    public static final Supplier<ITransformer> MAGICAL_FOREST_DECORATE_CASCADING = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(TransformUtil.remapMethod(
			new MethodDefinition(
				"thaumcraft/common/world/biomes/BiomeGenMagicalForest",
				false,
				"func_180624_a",
				Type.VOID_TYPE,
				Types.WORLD, Types.RANDOM, Types.BLOCK_POS
				)
			))
		// only the last part of this method is problematic, so skip the first parts
		.findNextMethodCall(BLOCKPOS_ADD)
		.findNextMethodCall(BLOCKPOS_ADD)
		.findNextMethodCall(BLOCKPOS_ADD)
		.findNextMethodCall(BLOCKPOS_ADD)
		.insertInstructionsAfter(
			new IntInsnNode(Opcodes.BIPUSH, 8),
			new InsnNode(Opcodes.ICONST_0),
			new IntInsnNode(Opcodes.BIPUSH, 8),
			BLOCKPOS_ADD.asMethodInsnNode(Opcodes.INVOKEVIRTUAL)
			)

		.build(), true, 1
		);
    };

}
