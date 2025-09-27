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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.transformer.Proxy;

import net.minecraft.launchwrapper.IClassTransformer;
import scala.actors.threadpool.Arrays;

public class UnitTestClassFileTransformer implements ClassFileTransformer {

    private final Set<String> exclusions;
    private final List<IClassTransformer> asmTransformers;
    private boolean minecraftOk;
    private Map<String, String> minecraftClassesLoadedTooEarly;

    public UnitTestClassFileTransformer() {
	exclusions = new HashSet<>();
	asmTransformers = new ArrayList<>();
	minecraftOk = false;
	minecraftClassesLoadedTooEarly = new HashMap<>();
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
	    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

	if (!minecraftOk && className.startsWith("net/minecraft/") && !minecraftClassesLoadedTooEarly.containsKey(className)) {
	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    new Throwable().printStackTrace(pw);
	    minecraftClassesLoadedTooEarly.put(className, sw.toString());
	}

	if (exclusions.stream().anyMatch(s -> className.startsWith(s))) {
	    return null;
	}

	byte[] classCopy = Arrays.copyOf(classfileBuffer, classfileBuffer.length);
	String classNameWithDots = className.replace('/', '.');
	for (IClassTransformer t : asmTransformers) {
	    classCopy = t.transform(classNameWithDots, classNameWithDots, classCopy);
	}
	classCopy = Proxy.transformer.transformClassBytes(classNameWithDots, classNameWithDots, classCopy);
	return classCopy;
    }

    public void registerExclusion(String name) {
	exclusions.add(name);
    }

    public void registerTransformer(IClassTransformer transformer) {
	asmTransformers.add(transformer);
    }

    public void allowMinecraftClassLoading() {
	if (!minecraftClassesLoadedTooEarly.isEmpty()) {
	    System.err.println("The following classes were loaded too early:");
	    for (Map.Entry<String, String> e : minecraftClassesLoadedTooEarly.entrySet()) {
		System.err.println(e.getKey());
		System.err.println(e.getValue());
		System.err.println();
	    }
	    throw new RuntimeException("Minecraft was loaded too early");
	}
	minecraftClassesLoadedTooEarly.clear();
	minecraftOk = true;
    }

}
