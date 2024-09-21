package thecodex6824.thaumcraftfix.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.collect.ImmutableList;

import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.common.items.curios.ItemPrimordialPearl;
import thaumcraft.common.lib.events.CraftingEvents;

public class TestPrimordialPearl {

    @BeforeAll
    static void setup() {
	Bootstrap.register();
	ItemsTC.primordialPearl = new ItemPrimordialPearl();
    }

    static Stream<? extends Arguments> testPearlDurabilityProperties() {
	// we are in the normal classloader so this has to be repeated
	Bootstrap.register();
	int maxMeta = new ItemStack(new ItemPrimordialPearl()).getMaxDamage();
	return IntStream.rangeClosed(0, maxMeta)
		.mapToObj(i -> Arguments.of(i, (double) i / maxMeta));
    }

    @ParameterizedTest
    @MethodSource
    void testPearlDurabilityProperties(int metadata, double damageRatio) {
	ItemStack stack = new ItemStack(ItemsTC.primordialPearl, 1, metadata);
	assertEquals(metadata != 0, stack.getItem().showDurabilityBar(stack));
	assertEquals(damageRatio, stack.getItem().getDurabilityForDisplay(stack));
    }

    @Test
    void testPearlNotDamageable() {
	ItemStack pearl = new ItemStack(ItemsTC.primordialPearl);
	assertFalse(pearl.isItemStackDamageable());
	assertTrue(pearl.getHasSubtypes());
    }

    // can't use ItemStack due to classloader issues
    static final List<Arguments> testAnvilHandlerIsDisabled = ImmutableList.of(
	    Arguments.of(false, false),
	    Arguments.of(true, false),
	    Arguments.of(false, true),
	    Arguments.of(true, true)
	    );

    @ParameterizedTest
    @FieldSource
    void testAnvilHandlerIsDisabled(boolean left, boolean right) {
	ItemStack pearl = new ItemStack(ItemsTC.primordialPearl);
	AnvilUpdateEvent event = new AnvilUpdateEvent(left ? pearl.copy() : ItemStack.EMPTY,
		right ? pearl.copy() : ItemStack.EMPTY, "test", 1) {
	    @Override
	    public void setCanceled(boolean cancel) {
		fail("Anvil event handler not disabled");
	    }
	};
	CraftingEvents.onAnvil(event);
    }

}
