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

package thecodex6824.thaumcraftfix.testlib.fixture;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.spongepowered.asm.service.IGlobalPropertyService;
import org.spongepowered.asm.service.IPropertyKey;

public class UnitTestGlobalPropertyService implements IGlobalPropertyService {

    private Map<IPropertyKey, Object> properties;

    public UnitTestGlobalPropertyService() {
	properties = new ConcurrentHashMap<>();
    }

    private static class StringPropertyKey implements IPropertyKey {

	private final String key;

	public StringPropertyKey(String key) {
	    this.key = key;
	}

	@Override
	public String toString() {
	    return key;
	}

	@Override
	public int hashCode() {
	    return key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
	    return getClass() == obj.getClass() && ((StringPropertyKey) obj).key.equals(key);
	}

    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProperty(IPropertyKey key) {
	return (T) properties.get(key);
    }

    @Override
    public <T> T getProperty(IPropertyKey key, T defaultValue) {
	T maybeThing = getProperty(key);
	return maybeThing != null ? maybeThing : defaultValue;
    }

    @Override
    public String getPropertyString(IPropertyKey key, String defaultValue) {
	Object maybeThing = getProperty(key);
	return maybeThing != null ? maybeThing.toString() : defaultValue;
    }

    @Override
    public IPropertyKey resolveKey(String name) {
	return new StringPropertyKey(name);
    }

    @Override
    public void setProperty(IPropertyKey key, Object value) {
	properties.put(key, value);
    }

}
