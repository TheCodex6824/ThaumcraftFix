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

package thecodex6824.thaumcraftfix.common.network;

import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import thecodex6824.thaumcraftfix.api.ThaumcraftFixApi;

public class ThaumcraftFixNetworkHandler extends SimpleNetworkWrapper {

    public ThaumcraftFixNetworkHandler() {
	super(ThaumcraftFixApi.MODID);
	int id = 0;
	registerMessage(PacketGainKnowledge.Handler.class, PacketGainKnowledge.class, id++, Side.CLIENT);
	registerMessage(PacketGainResearch.Handler.class, PacketGainResearch.class, id++, Side.CLIENT);
	registerMessage(PacketConfigSync.Handler.class, PacketConfigSync.class, id++, Side.CLIENT);
    }

}
