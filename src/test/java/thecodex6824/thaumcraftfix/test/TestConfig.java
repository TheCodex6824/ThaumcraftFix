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

package thecodex6824.thaumcraftfix.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import thecodex6824.thaumcraftfix.common.config.ThaumcraftFixConfig;
import thecodex6824.thaumcraftfix.common.config.ThaumcraftFixConfig.ConfigBoolean;
import thecodex6824.thaumcraftfix.common.config.ThaumcraftFixConfig.ConfigValue;

public class TestConfig {

    private static interface TestValueSet {
	public boolean valueSet();
    }

    private static class MockConfigValue<T> extends ConfigValue<T> implements TestValueSet {

	private boolean valueSet = false;

	public MockConfigValue(T initial) {
	    super(initial);
	}

	@Override
	public void setUserValue(T val) {
	    super.setUserValue(val);
	    valueSet = true;
	}

	@Override
	public boolean valueSet() {
	    return valueSet;
	}
    }

    private static class MockConfigBoolean extends ConfigBoolean implements TestValueSet {

	private boolean valueSet = false;

	public MockConfigBoolean(boolean initial) {
	    super(initial);
	}

	@Override
	public void setUserValue(boolean val) {
	    super.setUserValue(val);
	    valueSet = true;
	}

	@Override
	public boolean valueSet() {
	    return valueSet;
	}
    }

    @FunctionalInterface
    private static interface ThrowingBiFunction<T, U, R, E extends Throwable> {
	R apply(T t, U u) throws E;
    }

    private static int doSomethingToEachField(Object obj,
	    ThrowingBiFunction<Object, Field, Boolean, ReflectiveOperationException> func) throws ReflectiveOperationException {
	int count = 0;
	for (Field f : obj.getClass().getFields()) {
	    if (!func.apply(obj, f)) {
		count += doSomethingToEachField(f.get(obj), func);
	    }
	    else {
		++count;
	    }
	}

	return count;
    }

    @Test
    void testBind() throws ReflectiveOperationException {
	ThaumcraftFixConfig config = new ThaumcraftFixConfig();
	int initialFields = doSomethingToEachField(config, (obj, f) -> {
	    boolean handled = false;
	    if (f.getType() == ConfigBoolean.class) {
		f.set(obj, new MockConfigBoolean(false));
		handled = true;
	    }
	    else if (f.getType() == ConfigValue.class) {
		f.set(obj, new MockConfigValue<>(new String[0]));
		handled = true;
	    }

	    return handled;
	});
	config.bind();
	int totalFields = doSomethingToEachField(config, (obj, f) -> {
	    Object thing = f.get(obj);
	    if (thing instanceof TestValueSet) {
		assertTrue(((TestValueSet) thing).valueSet(), "Exposed config option was not synced with config impl");
		return true;
	    }

	    return false;
	});

	// sanity check to make sure test is working
	assertEquals(initialFields, totalFields);
	assertNotEquals(0, totalFields);
    }

    @Test
    void testSync() {
	ThaumcraftFixConfig config = new ThaumcraftFixConfig();
	config.item.primordialPearlDamageFix.setUserValue(false);
	JsonObject sync = config.serializeNetwork();
	config.item.primordialPearlDamageFix.setUserValue(true);
	config.deserializeNetwork(sync);
	assertFalse(config.item.primordialPearlDamageFix.value());
    }

}
