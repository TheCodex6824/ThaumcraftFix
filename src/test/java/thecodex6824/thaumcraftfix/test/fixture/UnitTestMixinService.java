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

import java.io.InputStream;
import java.util.Collection;

import org.spongepowered.asm.launch.platform.container.ContainerHandleVirtual;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.logging.LoggerAdapterConsole;
import org.spongepowered.asm.mixin.MixinEnvironment.Phase;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.IClassProvider;
import org.spongepowered.asm.service.IClassTracker;
import org.spongepowered.asm.service.IMixinAuditTrail;
import org.spongepowered.asm.service.ITransformerProvider;
import org.spongepowered.asm.service.MixinServiceAbstract;

import com.google.common.collect.ImmutableList;

public class UnitTestMixinService extends MixinServiceAbstract {

    private final UnitTestClassProvider classProvider;
    private final ILogger logger;

    public UnitTestMixinService() {
	classProvider = new UnitTestClassProvider();
	logger = new LoggerAdapterConsole(getName());
    }

    @Override
    public Phase getInitialPhase() {
	return Phase.DEFAULT;
    }

    @Override
    public synchronized ILogger getLogger(String name) {
	return logger;
    }

    @Override
    public IMixinAuditTrail getAuditTrail() {
	return null;
    }

    @Override
    public IClassBytecodeProvider getBytecodeProvider() {
	return classProvider;
    }

    @Override
    public IClassProvider getClassProvider() {
	return classProvider;
    }

    @Override
    public IClassTracker getClassTracker() {
	return null;
    }

    @Override
    public String getName() {
	return "Unit Test";
    }

    @Override
    public Collection<String> getPlatformAgents() {
	return ImmutableList.of("org.spongepowered.asm.launch.platform.MixinPlatformAgentDefault");
    }

    @Override
    public IContainerHandle getPrimaryContainer() {
	return new ContainerHandleVirtual(getName());
    }

    @Override
    public Collection<IContainerHandle> getMixinContainers() {
	return ImmutableList.of();
    }

    @Override
    public InputStream getResourceAsStream(String name) {
	return classProvider.getResourceAsStream(name);
    }

    @Override
    public ITransformerProvider getTransformerProvider() {
	return null;
    }

    @Override
    public boolean isValid() {
	return true;
    }

}
