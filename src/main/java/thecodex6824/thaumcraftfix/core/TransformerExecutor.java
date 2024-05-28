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

package thecodex6824.thaumcraftfix.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import net.minecraft.launchwrapper.IClassTransformer;
import thecodex6824.thaumcraftfix.core.transformer.BlockTransformers;
import thecodex6824.thaumcraftfix.core.transformer.CastingTransformers;
import thecodex6824.thaumcraftfix.core.transformer.EntityTransformers;
import thecodex6824.thaumcraftfix.core.transformer.ITransformer;
import thecodex6824.thaumcraftfix.core.transformer.ItemTransformers;
import thecodex6824.thaumcraftfix.core.transformer.MiscTransformers;
import thecodex6824.thaumcraftfix.core.transformer.NetworkTransformers;
import thecodex6824.thaumcraftfix.core.transformer.ResearchTransformers;
import thecodex6824.thaumcraftfix.core.transformer.SoundTransformers;
import thecodex6824.thaumcraftfix.core.transformer.WorldGenTransformers;

public class TransformerExecutor implements IClassTransformer {

    private static final String AUG_GOOD_VERSION = "2.1.14";

    private List<ITransformer> transformers;

    private boolean isOldAugmentationPresent() {
	try {
	    Class<?> augApi = Class.forName("thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI");
	    String apiVersion = (String) augApi.getField("API_VERSION").get(null);
	    return new ComparableVersion(apiVersion).compareTo(new ComparableVersion(AUG_GOOD_VERSION)) < 0;
	}
	catch (Exception ex) {
	    // assume we don't have it
	    return false;
	}
    }

    private void initTransformers() {
	transformers = new ArrayList<>();
	if (!isOldAugmentationPresent()) {
	    transformers.add(EntityTransformers.CUSTOM_ARMOR_NOT_CALLING_SUPER);
	    transformers.add(EntityTransformers.CUSTOM_ARMOR_ROTATION_POINTS);
	    transformers.add(EntityTransformers.ELDRITCH_GUARDIAN_FOG);
	    transformers.add(EntityTransformers.ELYTRA_ROBE_FLAPPING);
	    transformers.add(EntityTransformers.FLUX_RIFT_DESTROY_BLOCK_EVENT);
	    transformers.add(EntityTransformers.VOID_ROBE_ARMOR_DISPLAY);
	    transformers.add(ItemTransformers.CYCLE_ITEM_NON_DAMAGEABLE);
	    transformers.add(ItemTransformers.RUNIC_SHIELD_INFUSION_BAUBLE_CAP);
	    transformers.add(SoundTransformers.SOUND_FIX_CASTER_TICK.get());
	    transformers.add(SoundTransformers.SOUND_FIX_JAR_FILL.get());
	    transformers.add(SoundTransformers.SOUND_FIX_LOOT_BAG.get());
	    transformers.add(SoundTransformers.SOUND_FIX_MIRROR_TRANSPORT.get());
	    transformers.add(SoundTransformers.SOUND_FIX_MIRROR_USE.get());
	    transformers.add(SoundTransformers.SOUND_FIX_PHIAL_FILL.get());
	    transformers.add(SoundTransformers.SOUND_FIX_WIND_SWORD_USE.get());
	}
	else {
	    ThaumcraftFixCore.getLogger().warn("An old version of Thaumic Augmentation was detected."
		    + " Some of the fixes normally provided by Thaumcraft Fix will be handled by Thaumic Augmentation instead."
		    + " Consider updating Thaumic Augmentation to version {} or newer, as Thaumcraft Fix will probably have better compatibility and less of a chance of issues occuring.",
		    AUG_GOOD_VERSION);
	}

	transformers.add(BlockTransformers.FOCAL_MANIPULATOR_BLACK_FOCUS_GLITCH);
	transformers.add(BlockTransformers.FOCAL_MANIPULATOR_MAX_COMPLEXITY.get());
	transformers.add(BlockTransformers.FOCAL_MANIPULATOR_COMPONENTS);
	transformers.add(BlockTransformers.FOCAL_MANIPULATOR_COMPONENTS_CLIENT);
	transformers.add(BlockTransformers.FOCAL_MANIPULATOR_XP_COST_GUI);
	transformers.add(BlockTransformers.TABLE_TOP_SOLID.get());
	transformers.add(CastingTransformers.EXCHANGE_MOD_INTERFACEIFY);
	transformers.add(CastingTransformers.FOCUS_PACKAGE_INIT);
	transformers.add(CastingTransformers.FOCUS_PACKAGE_SET_CASTER_UUID);
	transformers.add(CastingTransformers.TOUCH_MOD_AVOID_PLAYER_CAST_TARGET);
	transformers.add(CastingTransformers.TOUCH_MOD_AVOID_PLAYER_CAST_TRAJECTORY);
	transformers.add(ItemTransformers.COMPARE_TAGS_RELAXED_NULL_CHECK.get());
	transformers.add(MiscTransformers.AURA_CHUNK_THREAD_SAFETY);
	transformers.add(NetworkTransformers.LOGISTICS_REQUEST.get());
	transformers.add(NetworkTransformers.NOTE_HANDLER.get());
	transformers.add(NetworkTransformers.PROGRESS_SYNC_CHECKS.get());
	transformers.add(NetworkTransformers.PROGRESS_SYNC_REQS.get());
	transformers.add(NetworkTransformers.RESEARCH_TABLE_AIDS.get());
	transformers.add(NetworkTransformers.THAUMATORIUM_RECIPE_SELECTION.get());
	transformers.add(ResearchTransformers.KNOWLEDGE_GAIN_EVENT_CLIENT.get());
	transformers.add(ResearchTransformers.RESEARCH_GAIN_EVENT_CLIENT.get());
	transformers.add(WorldGenTransformers.MAGICAL_FOREST_DECORATE_CASCADING.get());
    }

    private boolean isTransformNeeded(String transformedName) {
	for (ITransformer t : transformers) {
	    if (t.isTransformationNeeded(transformedName))
		return true;
	}

	return false;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
	if (transformers == null) {
	    initTransformers();
	}

	if (isTransformNeeded(transformedName)) {
	    ClassNode node = new ClassNode();
	    ClassReader reader = new ClassReader(basicClass);
	    reader.accept(node, 0);

	    boolean didSomething = false;
	    for (ITransformer transformer : transformers) {
		if (transformer.isTransformationNeeded(transformedName)) {
		    boolean transformerDidSomething = false;
		    try {
			transformerDidSomething = transformer.transform(node, name, transformedName);
		    }
		    catch (Throwable anything) {
			ThaumcraftFixCore.getLogger().error("A class transformer has failed!");
			ThaumcraftFixCore.getLogger().error("Class: " + transformedName + ", Transformer: " + transformer.getClass());
			ThaumcraftFixCore.getLogger().error("Additional information: ", anything);
			throw anything;
		    }

		    didSomething |= transformerDidSomething;
		}
	    }

	    if (didSomething) {
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		node.accept(writer);
		ThaumcraftFixCore.getLogger().info("Successfully transformed class " + transformedName);
		return writer.toByteArray();
	    }
	}

	return basicClass;
    }

}
