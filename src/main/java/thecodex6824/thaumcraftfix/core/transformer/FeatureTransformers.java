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
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import thecodex6824.coremodlib.FieldDefinition;
import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.coremodlib.PatchStateMachine;
import thecodex6824.thaumcraftfix.api.FeatureControlApi;

public class FeatureTransformers {

    public static final class Hooks {

	public static int shouldGenerateAura(int original, Chunk chunk) {
	    if (original >= 0 || !FeatureControlApi.isControllingAuraGen()) {
		return original;
	    }
	    else if (!FeatureControlApi.auraAllowedDimensions().contains(chunk.getWorld().provider.getDimensionType())) {
		return 1;
	    }
	    else if (!FeatureControlApi.auraAllowedBiomes().contains(chunk.getWorld().getBiome(
		    chunk.getWorld().getHeight(new BlockPos(chunk.x * 16 + 8, 0, chunk.z * 16 + 8))))) {
		return 1;
	    }

	    return -1;
	}

	public static boolean shouldGenerateCrystals(boolean original, World world, int chunkX, int chunkZ) {
	    if (!original || !FeatureControlApi.isControllingCrystalGen()) {
		return original;
	    }
	    else if (!FeatureControlApi.crystalAllowedDimensions().contains(world.provider.getDimensionType())) {
		return false;
	    }
	    else if (!FeatureControlApi.crystalAllowedBiomes().contains(world.getBiome(
		    world.getHeight(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8))))) {
		return false;
	    }

	    return true;
	}

	public static int shouldGenerateVegetation(int original, World world, int chunkX, int chunkZ) {
	    if (original >= 0 || !FeatureControlApi.isControllingVegetationGen()) {
		return original;
	    }
	    else if (!FeatureControlApi.vegetationAllowedDimensions().contains(world.provider.getDimensionType())) {
		return 1;
	    }
	    else if (!FeatureControlApi.vegetationAllowedBiomes().contains(world.getBiome(
		    world.getHeight(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8))))) {
		return 1;
	    }

	    return -1;
	}

    }

    private static final String HOOKS = Type.getInternalName(Hooks.class);

    public static final Supplier<ITransformer> GENERATE_AURA = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(new MethodDefinition(
			"thaumcraft/common/world/aura/AuraHandler",
			false,
			"generateAura",
			Type.VOID_TYPE,
			Types.CHUNK, Types.RANDOM
			))
		.findNextMethodCall(new MethodDefinition(
			"thaumcraft/common/world/biomes/BiomeHandler",
			false,
			"getBiomeBlacklist",
			Type.INT_TYPE,
			Type.INT_TYPE
			))
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new MethodDefinition(
				HOOKS,
				false,
				"shouldGenerateAura",
				Type.INT_TYPE,
				Type.INT_TYPE, Types.CHUNK
				).asMethodInsnNode(Opcodes.INVOKESTATIC)
			)
		.build()
		);
    };

    public static final Supplier<ITransformer> GENERATE_CRYSTALS = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(new MethodDefinition(
			"thaumcraft/common/world/ThaumcraftWorldGenerator",
			false,
			"generateOres",
			Type.VOID_TYPE,
			Types.WORLD, Types.RANDOM, Type.INT_TYPE, Type.INT_TYPE, Type.BOOLEAN_TYPE
			))
		.findNextFieldAccess(new FieldDefinition(
			"thaumcraft/common/config/ModConfig$CONFIG_WORLD",
			"generateCrystals",
			Type.BOOLEAN_TYPE
			))
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 1),
			new VarInsnNode(Opcodes.ILOAD, 3),
			new VarInsnNode(Opcodes.ILOAD, 4),
			new MethodDefinition(
				HOOKS,
				false,
				"shouldGenerateCrystals",
				Type.BOOLEAN_TYPE,
				Type.BOOLEAN_TYPE, Types.WORLD, Type.INT_TYPE, Type.INT_TYPE
				).asMethodInsnNode(Opcodes.INVOKESTATIC)
			)
		.build()
		);
    };

    public static final Supplier<ITransformer> GENERATE_VEGETATION = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(new MethodDefinition(
			"thaumcraft/common/world/ThaumcraftWorldGenerator",
			false,
			"generateVegetation",
			Type.VOID_TYPE,
			Types.WORLD, Types.RANDOM, Type.INT_TYPE, Type.INT_TYPE, Type.BOOLEAN_TYPE
			))
		.findNextMethodCall(new MethodDefinition(
			"thaumcraft/common/world/biomes/BiomeHandler",
			false,
			"getBiomeBlacklist",
			Type.INT_TYPE,
			Type.INT_TYPE
			))
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 1),
			new VarInsnNode(Opcodes.ILOAD, 3),
			new VarInsnNode(Opcodes.ILOAD, 4),
			new MethodDefinition(
				HOOKS,
				false,
				"shouldGenerateVegetation",
				Type.INT_TYPE,
				Type.INT_TYPE, Types.WORLD, Type.INT_TYPE, Type.INT_TYPE
				).asMethodInsnNode(Opcodes.INVOKESTATIC)
			)
		.build()
		);
    };

}
