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

package thecodex6824.thaumcraftfix.test.framework;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.transformer.Proxy;

import net.minecraft.launchwrapper.IClassTransformer;

public class UnitTestClassLoader extends URLClassLoader {

    static {
	registerAsParallelCapable();
    }

    private final Set<String> exclusions;
    private final List<IClassTransformer> asmTransformers;

    public UnitTestClassLoader(URL[] sources, ClassLoader parent) {
	super(sources, parent);
	exclusions = new HashSet<>();
	asmTransformers = new ArrayList<>();
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
	return loadClass(name, false);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
	synchronized (getClassLoadingLock(name)) {
	    Class<?> alreadyLoaded = findLoadedClass(name);
	    if (alreadyLoaded != null) {
		return alreadyLoaded;
	    }

	    if (exclusions.stream().anyMatch(s -> name.startsWith(s))) {
		return super.loadClass(name, resolve);
	    }

	    Class<?> c = findClass(name);
	    if (resolve) {
		resolveClass(c);
	    }

	    return c;
	}
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
	try {
	    byte[] stuff = getClassBytes(name.replace('.', '/') + ".class");
	    for (IClassTransformer t : asmTransformers) {
		stuff = t.transform(name, name, stuff);
	    }
	    stuff = Proxy.transformer.transformClassBytes(name, name, stuff);
	    ClassNode node = new ClassNode(Opcodes.ASM5);
	    new ClassReader(stuff).accept(node, 0);
	    return defineClass(name, stuff, 0, stuff.length);
	}
	catch (IOException ex) {
	    throw new ClassNotFoundException("Could not load class", ex);
	}
    }

    public byte[] getClassBytes(String name) throws IOException {
	try (InputStream input = getResourceAsStream(name)) {
	    if (input == null) throw new IOException();
	    return IOUtils.toByteArray(input);
	}
    }

    public void registerExclusion(String name) {
	exclusions.add(name);
    }

    public void registerTransformer(IClassTransformer transformer) {
	asmTransformers.add(transformer);
    }

}
