package thecodex6824.thaumcraftfix.core.transformer;

import java.util.function.Supplier;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.coremodlib.PatchStateMachine;

public class TheorycraftTransformers {

    private static Supplier<ITransformer> makeStringArrayFilterTransformer(String internalName) {
	return () -> {
	    Type strArray = Type.getType("[Ljava/lang/String;");
	    return new GenericStateMachineTransformer(
		    PatchStateMachine.builder(
			    new MethodDefinition(
				    internalName,
				    false,
				    "activate",
				    Type.BOOLEAN_TYPE,
				    Types.ENTITY_PLAYER, Types.RESEARCH_TABLE_DATA
				    )
			    )
		    .findNextLocalAccess(3)
		    .insertInstructionsBefore(
			    new VarInsnNode(Opcodes.ALOAD, 1),
			    new VarInsnNode(Opcodes.ALOAD, 2),
			    new MethodInsnNode(Opcodes.INVOKESTATIC,
				    TransformUtil.HOOKS_COMMON,
				    "filterTheorycraftCategoriesArray",
				    Type.getMethodDescriptor(strArray, strArray,
					    Types.ENTITY_PLAYER, Types.RESEARCH_TABLE_DATA),
				    false
				    )
			    )
		    .build(), true, 1
		    );
	};
    }

    public static final Supplier<ITransformer> CARD_ANALYZE_CATEGORIES = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/api/research/theorycraft/CardAnalyze",
				false,
				"initialize",
				Type.BOOLEAN_TYPE,
				Types.ENTITY_PLAYER, Types.RESEARCH_TABLE_DATA
				)
			)
		.findNextMethodCall(new MethodDefinition(
			Types.ARRAY_LIST.getInternalName(),
			false,
			"size",
			Type.INT_TYPE
			))
		.insertInstructionsBefore(
			new VarInsnNode(Opcodes.ALOAD, 1),
			new VarInsnNode(Opcodes.ALOAD, 2),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				TransformUtil.HOOKS_COMMON,
				"filterTheorycraftCategories",
				Type.getMethodDescriptor(Types.ARRAY_LIST, Types.ARRAY_LIST,
					Types.ENTITY_PLAYER, Types.RESEARCH_TABLE_DATA),
				false
				)
			)
		.build(), true, 1
		);
    };

    public static final Supplier<ITransformer> CARD_CURIO_CATEGORIES =
	    makeStringArrayFilterTransformer("thaumcraft/common/lib/research/theorycraft/CardCurio");

    public static final Supplier<ITransformer> CARD_DARK_WHISPERS_CATEGORIES = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/common/lib/research/theorycraft/CardDarkWhispers",
				false,
				"activate",
				Type.BOOLEAN_TYPE,
				Types.ENTITY_PLAYER, Types.RESEARCH_TABLE_DATA
				)
			)
		.findNextMethodCall(new MethodDefinition(
			Types.RANDOM.getInternalName(),
			false,
			"nextBoolean",
			Type.BOOLEAN_TYPE
			))
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 5),
			new VarInsnNode(Opcodes.ALOAD, 1),
			new VarInsnNode(Opcodes.ALOAD, 2),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				TransformUtil.HOOKS_COMMON,
				"isTheorycraftCategoryAllowed",
				Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.BOOLEAN_TYPE,
					Types.STRING, Types.ENTITY_PLAYER, Types.RESEARCH_TABLE_DATA),
				false
				)
			)
		.build(), true, 1
		);
    };

    public static final Supplier<ITransformer> CARD_DRAGON_EGG_CATEGORIES =
	    makeStringArrayFilterTransformer("thaumcraft/common/lib/research/theorycraft/CardDragonEgg");

    public static final Supplier<ITransformer> CARD_EXPERIMENTATION_CATEGORIES =
	    makeStringArrayFilterTransformer("thaumcraft/api/research/theorycraft/CardExperimentation");

    public static final Supplier<ITransformer> CARD_GLYPHS_CATEGORIES =
	    makeStringArrayFilterTransformer("thaumcraft/common/lib/research/theorycraft/CardGlyphs");

    public static final Supplier<ITransformer> CARD_PORTAL_CATEGORIES =
	    makeStringArrayFilterTransformer("thaumcraft/common/lib/research/theorycraft/CardPortal");

    public static final Supplier<ITransformer> CARD_REALIZATION_CATEGORIES =
	    makeStringArrayFilterTransformer("thaumcraft/common/lib/research/theorycraft/CardRealization");

    public static final Supplier<ITransformer> CARD_REVELATION_CATEGORIES =
	    makeStringArrayFilterTransformer("thaumcraft/common/lib/research/theorycraft/CardRevelation");

    public static final Supplier<ITransformer> RESEARCH_TABLE_DATA_CATEGORIES = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				Types.RESEARCH_TABLE_DATA.getInternalName(),
				false,
				"getAvailableCategories",
				Types.ARRAY_LIST,
				Types.ENTITY_PLAYER
				)
			)
		.findNextOpcode(Opcodes.ARETURN)
		.insertInstructionsBefore(
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				TransformUtil.HOOKS_COMMON,
				"filterTheorycraftCategories",
				Type.getMethodDescriptor(Types.ARRAY_LIST, Types.ARRAY_LIST),
				false
				)
			)
		.build()
		);
    };

}
