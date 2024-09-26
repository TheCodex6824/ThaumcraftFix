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

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.net.URLClassLoader;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.commons.util.ReflectionUtils;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.MixinEnvironment.Side;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.Proxy;
import org.spongepowered.asm.service.MixinService;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.MixinExtrasBootstrap;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import thecodex6824.thaumcraftfix.core.ThaumcraftFixCore;

public class CoremodInvocationInterceptor implements InvocationInterceptor {

    private static final String SRG_MCP_PROP = "net.minecraftforge.gradle.GradleStart.srg.srg-mcp";

    static {
	MixinBootstrap.init();
	MixinEnvironment.getDefaultEnvironment().setActiveTransformer(Proxy.transformer);
	MixinExtrasBootstrap.init();
	MixinEnvironment.getDefaultEnvironment().setSide(Side.CLIENT);

	// TODO: figure this out from the build environment
	// passing arguments/properties from Gradle to the IDE runs seems to be a challenge though...
	System.getProperties().computeIfAbsent(SRG_MCP_PROP, obj -> new File(
		"./build/createSrgToMcp/output.srg").getAbsolutePath());
	FMLDeobfuscatingRemapper.INSTANCE.setup(null, new LaunchClassLoader(
		((URLClassLoader) CoremodInvocationInterceptor.class.getClassLoader()).getURLs()), null);

	ThaumcraftFixCore coremod = new ThaumcraftFixCore();
	coremod.injectData(ImmutableMap.of());
	// early configs were already handled in injectData
	Mixins.addConfigurations(ThaumcraftFixCore.getLateMixinConfigs().toArray(new String[0]));
	for (String c : coremod.getASMTransformerClass()) {
	    try {
		((UnitTestMixinService) MixinService.getService()).registerTransformer(
			(IClassTransformer) ReflectionUtils.newInstance(Class.forName(c)));
	    }
	    catch (ReflectiveOperationException ex) {
		throw new RuntimeException(ex);
	    }
	}
    }

    @SuppressWarnings("unchecked")
    private <T, E extends Executable> T runWithClassLoader(Invocation<T> invocation,
	    ReflectiveInvocationContext<E> invocationContext, ExtensionContext extensionContext) throws Throwable {

	invocation.skip();
	ClassLoader old = Thread.currentThread().getContextClassLoader();
	Thread.currentThread().setContextClassLoader(((UnitTestMixinService) MixinService.getService()).getClassLoader());

	try {
	    Class<?> invokingClass = invocationContext.getExecutable().getDeclaringClass();
	    String method = invocationContext.getExecutable().getName();
	    Class<?>[] parameters = invocationContext.getExecutable().getParameterTypes();
	    Class<?> classWithNewLoader = Thread.currentThread().getContextClassLoader().loadClass(invokingClass.getName());
	    Object thing = ReflectionUtils.newInstance(classWithNewLoader);
	    Method target = ReflectionUtils.findMethod(classWithNewLoader, method, parameters).get();
	    return (T) ReflectionUtils.invokeMethod(target, thing, invocationContext.getArguments().toArray());
	}
	finally {
	    Thread.currentThread().setContextClassLoader(old);
	}
    }

    @SuppressWarnings("unchecked")
    private <T> T constructWithClassLoader(Invocation<T> invocation,
	    ReflectiveInvocationContext<Constructor<T>> invocationContext, ExtensionContext extensionContext) throws Throwable {

	invocation.skip();
	ClassLoader old = Thread.currentThread().getContextClassLoader();
	Thread.currentThread().setContextClassLoader(((UnitTestMixinService) MixinService.getService()).getClassLoader());
	try {
	    Class<?> invokingClass = invocationContext.getExecutable().getDeclaringClass();
	    Class<?>[] parameters = invocationContext.getExecutable().getParameterTypes();
	    Class<?> classWithNewLoader = Thread.currentThread().getContextClassLoader().loadClass(invokingClass.getName());
	    return (T) classWithNewLoader.getDeclaredConstructor(parameters).newInstance(invocationContext.getArguments().toArray());
	}
	finally {
	    Thread.currentThread().setContextClassLoader(old);
	}
    }

    @Override
    public void interceptAfterAllMethod(Invocation<Void> invocation,
	    ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {

	runWithClassLoader(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptAfterEachMethod(Invocation<Void> invocation,
	    ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {

	runWithClassLoader(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptBeforeAllMethod(Invocation<Void> invocation,
	    ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {

	runWithClassLoader(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation,
	    ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {

	runWithClassLoader(invocation, invocationContext, extensionContext);
    }

    @Override
    public <T> T interceptTestClassConstructor(Invocation<T> invocation,
	    ReflectiveInvocationContext<Constructor<T>> invocationContext, ExtensionContext extensionContext)
		    throws Throwable {

	return constructWithClassLoader(invocation, invocationContext, extensionContext);
    }

    @Override
    public <T> T interceptTestFactoryMethod(Invocation<T> invocation,
	    ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {

	return runWithClassLoader(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
	    ExtensionContext extensionContext) throws Throwable {

	runWithClassLoader(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation,
	    ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {

	runWithClassLoader(invocation, invocationContext, extensionContext);
    }

}
