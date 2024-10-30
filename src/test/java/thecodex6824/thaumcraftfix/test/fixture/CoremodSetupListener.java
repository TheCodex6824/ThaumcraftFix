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

package thecodex6824.thaumcraftfix.test.fixture;

import java.io.File;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.MixinEnvironment.Side;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.Proxy;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.llamalad7.mixinextras.MixinExtrasBootstrap;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.minecraft.init.Bootstrap;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.common.config.ConfigResearch;
import thecodex6824.thaumcraftfix.api.internal.ThaumcraftFixApiBridge;
import thecodex6824.thaumcraftfix.common.internal.DefaultApiImplementation;
import thecodex6824.thaumcraftfix.core.ThaumcraftFixCore;

public class CoremodSetupListener implements LauncherSessionListener {

    private static final String SRG_MCP_PROP = "net.minecraftforge.gradle.GradleStart.srg.srg-mcp";

    private static AtomicBoolean coremodInit = new AtomicBoolean();

    @Override
    public void launcherSessionOpened(LauncherSession session) {
	// we must lock in case another session is opened while the first is setting up the coremod
	// the init flag will keep the later session from initing again, but it may start doing things before we are done
	synchronized (this) {
	    // this probably doesn't need to be atomic due to lock but this only runs a few times total
	    if (coremodInit.compareAndSet(false, true)) {
		// this MUST run before test discovery, so we must run everything immediately
		MixinBootstrap.init();
		MixinEnvironment.getDefaultEnvironment().setActiveTransformer(Proxy.transformer);
		MixinExtrasBootstrap.init();
		MixinEnvironment.getDefaultEnvironment().setSide(Side.CLIENT);

		// TODO: figure this out from the build environment
		// passing arguments/properties from Gradle to the IDE runs seems to be a challenge though...
		System.getProperties().computeIfAbsent(SRG_MCP_PROP, obj -> new File(
			"./build/createSrgToMcp/output.srg").getAbsolutePath());
		FMLDeobfuscatingRemapper.INSTANCE.setup(null, new LaunchClassLoader(
			((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs()), null);

		ThaumcraftFixCore coremod = new ThaumcraftFixCore();
		coremod.injectData(ImmutableMap.of());
		// early configs were already handled in injectData
		Mixins.addConfigurations(ThaumcraftFixCore.getLateMixinConfigs().toArray(new String[0]));

		UnitTestClassFileTransformer transformer = new UnitTestClassFileTransformer();
		for (String c : coremod.getASMTransformerClass()) {
		    try {
			transformer.registerTransformer(
				(IClassTransformer) ReflectionUtils.newInstance(Class.forName(c)));
		    }
		    catch (ReflectiveOperationException ex) {
			throw new RuntimeException(ex);
		    }
		}

		transformer.registerExclusion("java/");
		transformer.registerExclusion("javax/");
		transformer.registerExclusion("sun/");
		transformer.registerExclusion("com/sun/");
		transformer.registerExclusion("org/apache/logging/");
		transformer.registerExclusion("org/junit/");
		transformer.registerExclusion("org/spongepowered/");
		transformer.registerExclusion("com/llamalad7/mixinextras/");
		transformer.registerExclusion(CoremodSetupListener.class.getPackage().getName().replace('.', '/') + "/");

		// The agent may already be started, in which case this will just work
		try {
		    ByteBuddyAgent.getInstrumentation().addTransformer(transformer, false);
		}
		catch (IllegalStateException ex) {
		    try {
			// The agent is not started yet, so this is probably a non-Gradle IDE run
			// Starting it up here requires a JDK install (and pre Java 9, only certain types)
			// Since these are the tests, the user has to have a JDK so this is fine
			ByteBuddyAgent.install().addTransformer(transformer, false);
		    }
		    catch (IllegalStateException e2) {
			System.err.println(
				"The ByteBuddy Agent (used for transforming classes in the tests) cannot be started.\n"
					+ "Your setup is probably not supported by ByteBuddy's runtime agent attachment system.\n"
					+ "To successfully run, please add the JVM argument \"-javaagent:byte-buddy-agent-<version>.jar\",\n"
					+ "where <version> is the version of ByteBuddy Agent found in build.gradle in the dependencies section.\n"
					+ "Depending on if the jar is in your boot classpath or not,\n"
					+ "you may need to provide the full path to the jar instead of just the file name.");
			throw e2;
		    }
		}

		// Anything below here can use Minecraft / transformed classes safely
		Bootstrap.register();
		ConfigResearch.init();
		DefaultApiImplementation impl = new DefaultApiImplementation();
		ThaumcraftFixApiBridge.setImplementation(impl);
		impl.setAllowedTheorycraftCategories(ImmutableSet.copyOf(ResearchCategories.researchCategories.values()));
	    }
	}
    }

}
