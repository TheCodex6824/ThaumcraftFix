package thecodex6824.thaumcraftfix.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayDeque;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.mojang.authlib.GameProfile;

import thaumcraft.api.casters.FocusMediumRoot;
import thaumcraft.api.casters.FocusModSplit;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.IFocusElement;
import thaumcraft.common.items.casters.foci.FocusEffectFlux;
import thaumcraft.common.items.casters.foci.FocusEffectFrost;
import thaumcraft.common.items.casters.foci.FocusMediumBolt;
import thaumcraft.common.items.casters.foci.FocusMediumCloud;
import thaumcraft.common.items.casters.foci.FocusModSplitTarget;
import thecodex6824.thaumcraftfix.testlib.lib.MockPlayer;
import thecodex6824.thaumcraftfix.testlib.lib.MockWorld;

public class TestFocusPackage {

    private FocusPackage makeSplitPackageFocus() {
	FocusPackage nested1 = new FocusPackage();
	nested1.addNode(new FocusEffectFlux());
	FocusPackage nested2 = new FocusPackage();
	nested2.addNode(new FocusEffectFrost());
	FocusPackage top = new FocusPackage();
	top.addNode(new FocusMediumRoot());
	top.addNode(new FocusMediumBolt());
	top.addNode(new FocusMediumCloud());
	FocusModSplitTarget split = new FocusModSplitTarget();
	split.getSplitPackages().add(nested1);
	split.getSplitPackages().add(nested2);
	top.addNode(split);
	return top;
    }

    @Test
    public void testNestedFocusInitCaster() {
	MockWorld world = new MockWorld();
	MockPlayer caster = new MockPlayer(world, new GameProfile(UUID.randomUUID(), "test"));
	world.spawnEntity(caster);
	FocusPackage test = makeSplitPackageFocus();
	test.initialize(caster);
	assertEquals(caster, test.getCaster());
	ArrayDeque<IFocusElement> toCheck = new ArrayDeque<>(test.nodes);
	while (!toCheck.isEmpty()) {
	    IFocusElement node = toCheck.pop();
	    if (node instanceof FocusModSplit) {
		for (FocusPackage p : (((FocusModSplit) node).getSplitPackages())) {
		    assertEquals(caster, p.getCaster());
		    toCheck.addAll(p.nodes);
		}
	    }
	}
    }

    @Test
    public void testNestedFocusSetCasterId() {
	MockWorld world = new MockWorld();
	MockPlayer caster = new MockPlayer(world, new GameProfile(UUID.randomUUID(), "test"));
	world.spawnEntity(caster);
	FocusPackage test = makeSplitPackageFocus();
	test.setCasterUUID(caster.getUniqueID());
	assertEquals(caster.getUniqueID(), test.getCasterUUID());
	ArrayDeque<IFocusElement> toCheck = new ArrayDeque<>(test.nodes);
	while (!toCheck.isEmpty()) {
	    IFocusElement node = toCheck.pop();
	    if (node instanceof FocusModSplit) {
		for (FocusPackage p : (((FocusModSplit) node).getSplitPackages())) {
		    assertEquals(caster.getUniqueID(), p.getCasterUUID());
		    toCheck.addAll(p.nodes);
		}
	    }
	}
    }

}
