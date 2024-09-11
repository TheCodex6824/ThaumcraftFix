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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Supplier;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.collect.Iterables;
import com.google.gson.JsonObject;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import thaumcraft.api.capabilities.IPlayerKnowledge.EnumKnowledgeType;
import thaumcraft.api.items.IScribeTools;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.api.research.ResearchEntry;
import thecodex6824.coremodlib.LocalVariableDefinition;
import thecodex6824.coremodlib.MethodDefinition;
import thecodex6824.coremodlib.PatchStateMachine;
import thecodex6824.thaumcraftfix.ThaumcraftFix;
import thecodex6824.thaumcraftfix.api.ThaumcraftFixApi;
import thecodex6824.thaumcraftfix.api.event.research.ResearchEntryLoadEvent;
import thecodex6824.thaumcraftfix.api.event.research.ResearchLoadEvent;
import thecodex6824.thaumcraftfix.api.internal.ThaumcraftFixApiBridge;
import thecodex6824.thaumcraftfix.common.network.PacketGainKnowledge;
import thecodex6824.thaumcraftfix.common.network.PacketGainResearch;

public class ResearchTransformers {

    public static final class Hooks {

	public static void sendKnowledgeGainPacket(EntityPlayer player, EnumKnowledgeType type, ResearchCategory category, int amount) {
	    if (player instanceof EntityPlayerMP) {
		ThaumcraftFix.instance.getNetworkHandler().sendTo(
			new PacketGainKnowledge(type, category, amount), (EntityPlayerMP) player);
	    }
	}

	public static void sendResearchGainPacket(EntityPlayer player, String researchKey) {
	    if (player instanceof EntityPlayerMP) {
		ThaumcraftFix.instance.getNetworkHandler().sendTo(
			new PacketGainResearch(researchKey), (EntityPlayerMP) player);
	    }
	}

	public static String[] fixupFirstPassSplit(String rawText, String separator, String[] split) {
	    if (rawText.endsWith(separator)) {
		split = Arrays.copyOf(split, split.length + 1);
		split[split.length - 1] = "";
	    }

	    return split;
	}

	public static void researchLoadStart() {
	    MinecraftForge.EVENT_BUS.post(new ResearchLoadEvent.Pre());
	}

	public static void researchLoadEnd() {
	    MinecraftForge.EVENT_BUS.post(new ResearchLoadEvent.Post());
	}

	public static boolean entryLoadStart(JsonObject json) {
	    ResearchEntryLoadEvent.Pre event = new ResearchEntryLoadEvent.Pre(json);
	    MinecraftForge.EVENT_BUS.post(event);
	    return !event.isCanceled();
	}

	public static void entryLoadEnd(JsonObject json, ResearchEntry entry) {
	    MinecraftForge.EVENT_BUS.post(new ResearchEntryLoadEvent.Post(json, entry));
	}

	public static InputStream getFilesystemStream(InputStream stream, ResourceLocation loc) {
	    String prefix = ThaumcraftFixApiBridge.InternalImplementation.PATH_RESOURCE_PREFIX;
	    if (stream == null && loc.getNamespace().equals(ThaumcraftFixApi.MODID) && loc.getPath().startsWith(prefix)) {
		Path gameDir = ThaumcraftFix.proxy.getGameDirectory().toPath().normalize();
		Path requested = Paths.get(loc.getPath().substring(prefix.length()));
		if (!requested.isAbsolute() && requested.getRoot() == null) {
		    try {
			stream = new FileInputStream(gameDir.resolve(requested.normalize()).toFile());
		    }
		    catch (IOException ex) {
			ThaumcraftFix.instance.getLogger().error("Failed opening research file: ", ex);
		    }
		}
		else {
		    ThaumcraftFix.instance.getLogger().error("Illegal research entry path, paths must be relative to the game directory: " + requested);
		}
	    }

	    return stream;
	}

	public static boolean isPlayerCarryingScribingTools(ItemStack isCarrying, boolean ore, boolean original, EntityPlayer player) {
	    if (original || isCarrying.getItem() != ItemsTC.scribingTools) {
		return original;
	    }

	    for (ItemStack stack : Iterables.concat(player.inventory.offHandInventory, player.inventory.mainInventory)) {
		if (!stack.isEmpty() && stack.getItem() instanceof IScribeTools) {
		    return true;
		}
	    }

	    return false;
	}

    }

    private static final String HOOKS = Type.getInternalName(Hooks.class);

    public static final Supplier<ITransformer> KNOWLEDGE_GAIN_EVENT_CLIENT = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/common/lib/research/ResearchManager",
				false,
				"addKnowledge",
				Type.BOOLEAN_TYPE,
				Types.ENTITY_PLAYER, Type.getType("Lthaumcraft/api/capabilities/IPlayerKnowledge$EnumKnowledgeType;"),
				Type.getType("Lthaumcraft/api/research/ResearchCategory;"), Type.INT_TYPE
				)
			)
		.findNextMethodCall(new MethodDefinition(
			"thaumcraft/api/capabilities/IPlayerKnowledge",
			true,
			"addKnowledge",
			Type.BOOLEAN_TYPE,
			Type.getType("Lthaumcraft/api/capabilities/IPlayerKnowledge$EnumKnowledgeType;"), Type.getType("Lthaumcraft/api/research/ResearchCategory;"), Type.INT_TYPE
			))
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new VarInsnNode(Opcodes.ALOAD, 1),
			new VarInsnNode(Opcodes.ALOAD, 2),
			new VarInsnNode(Opcodes.ILOAD, 3),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS,
				"sendKnowledgeGainPacket",
				Type.getMethodDescriptor(Type.VOID_TYPE, Types.ENTITY_PLAYER,
					Type.getType("Lthaumcraft/api/capabilities/IPlayerKnowledge$EnumKnowledgeType;"),
					Type.getType("Lthaumcraft/api/research/ResearchCategory;"), Type.INT_TYPE),
				false
				)
			)
		.build(), true, 1
		);
    };

    public static final Supplier<ITransformer> RESEARCH_GAIN_EVENT_CLIENT = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/common/lib/research/ResearchManager",
				false,
				"progressResearch",
				Type.BOOLEAN_TYPE,
				Types.ENTITY_PLAYER, Types.STRING, Type.BOOLEAN_TYPE
				)
			)
		.findConsecutive()
		.findNextLocalAccess(2)
		.findNextInstructionType(JumpInsnNode.class)
		.endConsecutive()
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 0),
			new VarInsnNode(Opcodes.ALOAD, 1),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS,
				"sendResearchGainPacket",
				Type.getMethodDescriptor(Type.VOID_TYPE, Types.ENTITY_PLAYER, Types.STRING),
				false
				)
			)
		.build(), true, 1
		);
    };

    public static final Supplier<ITransformer> RESEARCH_PATCHER = () -> {
	LabelNode continueLabel = new LabelNode(new Label());
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/common/lib/research/ResearchManager",
				false,
				"parseAllResearch",
				Type.VOID_TYPE
				)
			)
		.insertInstructionsBefore(
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS,
				"researchLoadStart",
				Type.getMethodDescriptor(Type.VOID_TYPE),
				false
				))
		.findNextMethodCall(new MethodDefinition(
			"java/lang/Class",
			false,
			"getResourceAsStream",
			Types.INPUT_STREAM,
			Types.STRING
			))
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 2),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS,
				"getFilesystemStream",
				Type.getMethodDescriptor(Types.INPUT_STREAM, Types.INPUT_STREAM, Types.RESOURCE_LOCATION),
				false
				)
			)
		.findConsecutive()
		.findNextInstructionType(FrameNode.class)
		.findNextLocalAccess(9)
		.findNextMethodCall(new MethodDefinition(
			Types.ITERATOR.getInternalName(),
			true,
			"hasNext",
			Type.BOOLEAN_TYPE
			))
		.endConsecutive()
		.findNextMethodCall(new MethodDefinition(
			Types.JSON_ELEMENT.getInternalName(),
			false,
			"getAsJsonObject",
			Types.JSON_OBJECT
			))
		.combineLastTwoMatches()
		.insertInstructionsSurrounding()
		.before(
			continueLabel
			)
		.after(
			new IincInsnNode(8, -1),
			new InsnNode(Opcodes.DUP),
			new VarInsnNode(Opcodes.ASTORE, 11),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS,
				"entryLoadStart",
				Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Types.JSON_OBJECT),
				false
				),
			new JumpInsnNode(Opcodes.IFEQ, continueLabel),
			new IincInsnNode(8, 1),
			new VarInsnNode(Opcodes.ALOAD, 11)
			)
		.endAction()
		.findNextMethodCall(new MethodDefinition(
			"thaumcraft/common/lib/research/ResearchManager",
			false,
			"addResearchToCategory",
			Type.VOID_TYPE,
			Types.RESEARCH_ENTRY
			))
		.insertInstructionsAfter(
			new VarInsnNode(Opcodes.ALOAD, 11),
			new VarInsnNode(Opcodes.ALOAD, 12),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS,
				"entryLoadEnd",
				Type.getMethodDescriptor(Type.VOID_TYPE, Types.JSON_OBJECT, Types.RESEARCH_ENTRY),
				false
				)
			)
		.findNextOpcode(Opcodes.RETURN)
		.insertInstructionsBefore(
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS,
				"researchLoadEnd",
				Type.getMethodDescriptor(Type.VOID_TYPE),
				false
				))
		.build(), true, 1
		);
    };

    public static final Supplier<ITransformer> PARSE_PAGE_FIRST_PASS = () -> {
	Type stringArray = Type.getType("[Ljava/lang/String;");
	MethodDefinition split = new MethodDefinition(
		Types.STRING.getInternalName(),
		false,
		"split",
		stringArray,
		Types.STRING
		);
	MethodDefinition fixup = new MethodDefinition(
		HOOKS,
		false,
		"fixupFirstPassSplit",
		stringArray,
		Types.STRING, Types.STRING, stringArray
		);
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/client/gui/GuiResearchPage",
				false,
				"parsePages",
				Type.VOID_TYPE
				)
			)
		// this first match is for <IMG> which we don't need to change
		.findConsecutive()
		.findNextLocalAccess(new LocalVariableDefinition("rawText", Types.STRING))
		.findNextStringConstant("<IMG>")
		.findNextMethodCall(split)
		.endConsecutive()
		// matches ~P
		.findConsecutive()
		.findNextLocalAccess(new LocalVariableDefinition("rawText", Types.STRING))
		.findNextStringConstant("~P")
		.findNextMethodCall(split)
		.endConsecutive()
		.matchLastNodeOnly()
		.insertInstructionsSurrounding()
		.before(new InsnNode(Opcodes.DUP2))
		.after(fixup.asMethodInsnNode(Opcodes.INVOKESTATIC))
		.endAction()
		// matches ~D
		.findConsecutive()
		.findNextLocalAccess(new LocalVariableDefinition("t", Types.STRING))
		.findNextStringConstant("~D")
		.findNextMethodCall(split)
		.endConsecutive()
		.matchLastNodeOnly()
		.insertInstructionsSurrounding()
		.before(new InsnNode(Opcodes.DUP2))
		.after(fixup.asMethodInsnNode(Opcodes.INVOKESTATIC))
		.endAction()
		// matches ~L
		.findConsecutive()
		.findNextLocalAccess(new LocalVariableDefinition("t1", Types.STRING))
		.findNextStringConstant("~L")
		.findNextMethodCall(split)
		.endConsecutive()
		.matchLastNodeOnly()
		.insertInstructionsSurrounding()
		.before(new InsnNode(Opcodes.DUP2))
		.after(fixup.asMethodInsnNode(Opcodes.INVOKESTATIC))
		.endAction()
		// matches ~I
		.findConsecutive()
		.findNextLocalAccess(new LocalVariableDefinition("t2", Types.STRING))
		.findNextStringConstant("~I")
		.findNextMethodCall(split)
		.endConsecutive()
		.matchLastNodeOnly()
		.insertInstructionsSurrounding()
		.before(new InsnNode(Opcodes.DUP2))
		.after(fixup.asMethodInsnNode(Opcodes.INVOKESTATIC))
		.endAction()
		.build(), true, 1
		);
    };

    public static final Supplier<ITransformer> SCAN_SKY_SCRIBE_CHECK = () -> {
	return new GenericStateMachineTransformer(
		PatchStateMachine.builder(
			new MethodDefinition(
				"thaumcraft/common/lib/research/ScanSky",
				false,
				"onSuccess",
				Type.VOID_TYPE,
				Types.ENTITY_PLAYER, Types.OBJECT
				)
			)
		.findNextMethodCall(new MethodDefinition(
			"thaumcraft/common/lib/utils/InventoryUtils",
			false,
			"isPlayerCarryingAmount",
			Type.BOOLEAN_TYPE,
			Types.ENTITY_PLAYER, Types.ITEM_STACK, Type.BOOLEAN_TYPE
			))
		.insertInstructionsSurrounding()
		.before(new InsnNode(Opcodes.DUP2_X1))
		.after(
			new VarInsnNode(Opcodes.ALOAD, 1),
			new MethodInsnNode(Opcodes.INVOKESTATIC,
				HOOKS,
				"isPlayerCarryingScribingTools",
				Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Types.ITEM_STACK, Type.BOOLEAN_TYPE, Type.BOOLEAN_TYPE, Types.ENTITY_PLAYER),
				false
				)
			)
		.endAction()
		.build() // needs to repeat multiple times
		);
    };

}
