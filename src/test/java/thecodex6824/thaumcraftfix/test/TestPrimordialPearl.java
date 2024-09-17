package thecodex6824.thaumcraftfix.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import thaumcraft.common.items.curios.ItemPrimordialPearl;

public class TestPrimordialPearl {

    @BeforeAll
    static void setup() {
	Bootstrap.register();
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
	ItemPrimordialPearl pearl = new ItemPrimordialPearl();
	ItemStack stack = new ItemStack(pearl, 1, metadata);
	assertEquals(metadata != 0, stack.getItem().showDurabilityBar(stack));
	assertEquals(damageRatio, stack.getItem().getDurabilityForDisplay(stack));
    }

    @Test
    void testPearlNotDamageable() {
	ItemStack pearl = new ItemStack(new ItemPrimordialPearl());
	assertFalse(pearl.isItemStackDamageable());
	assertTrue(pearl.getHasSubtypes());
    }

}
