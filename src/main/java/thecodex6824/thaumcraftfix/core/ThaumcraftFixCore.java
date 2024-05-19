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

package thecodex6824.thaumcraftfix.core;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

@Name("Thaumcraft Fix Core Plugin")
@MCVersion("1.12.2")
@SortingIndex(1100)
@TransformerExclusions("thecodex6824.thaumcraftfix.core")
public class ThaumcraftFixCore implements IFMLLoadingPlugin {

    private static Logger log = LogManager.getLogger("thaumcraftfixcore");
    private static boolean debug = false;

    public static Logger getLogger() {
	return log;
    }

    public static boolean isDebugEnabled() {
	return debug;
    }

    @Override
    public String getAccessTransformerClass() {
	return null;
    }

    @Override
    public String[] getASMTransformerClass() {
	return new String[] { "thecodex6824.thaumcraftfix.core.TransformerExecutor" };
    }

    @Override
    public String getModContainerClass() {
	return null;
    }

    @Override
    public String getSetupClass() {
	return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
	String debugProp = System.getProperty("thaumcraftfix.debug");
	boolean debugBoolTrue = Boolean.parseBoolean(debugProp);
	boolean debugIntTrue = false;
	if (!debugBoolTrue) {
	    try {
		debugIntTrue = Integer.parseInt(debugProp) == 1;
	    }
	    catch (NumberFormatException ex) {}
	}

	debug = debugBoolTrue || debugIntTrue;
    }

}
