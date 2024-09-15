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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.Proxy;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.IClassProvider;

public class UnitTestClassProvider implements IClassProvider, IClassBytecodeProvider {

    private final UnitTestClassLoader loader;

    public UnitTestClassProvider() {
	loader = new UnitTestClassLoader(((URLClassLoader) getClass().getClassLoader()).getURLs(),
		getClass().getClassLoader());
	registerExclusion("java.");
	registerExclusion("javax.");
	registerExclusion("sun.");
	registerExclusion("com.sun.");
	registerExclusion("org.apache.logging.");
	registerExclusion("org.junit.");
	registerExclusion("org.spongepowered.");
	registerExclusion("com.llamalad7.mixinextras.");
    }

    public UnitTestClassLoader getClassLoader() {
	return loader;
    }

    @Override
    public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
	return Class.forName(name, initialize, loader.getParent());
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
	return loader.loadClass(name);
    }

    @Override
    public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
	return Class.forName(name, initialize, loader);
    }

    @Override
    public URL[] getClassPath() {
	return loader.getURLs();
    }

    @Override
    public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException {
	return getClassNode(name, true);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
	ClassNode node = new ClassNode(Opcodes.ASM5);
	new ClassReader(loader.getClassBytes(name.replace('.', '/') + ".class")).accept(node, ClassReader.EXPAND_FRAMES);
	if (runTransformers) {
	    Proxy.transformer.transformClass(MixinEnvironment.getCurrentEnvironment(), name, node);
	}
	return node;
    }

    public InputStream getResourceAsStream(String name) {
	return loader.getResourceAsStream(name);
    }

    public void registerExclusion(String name) {
	loader.registerExclusion(name);
    }

}
