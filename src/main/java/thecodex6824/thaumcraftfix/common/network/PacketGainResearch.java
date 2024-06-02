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

import io.netty.buffer.ByteBuf;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumcraft.api.research.ResearchEvent;
import thecodex6824.thaumcraftfix.IProxy;
import thecodex6824.thaumcraftfix.ThaumcraftFix;
import thecodex6824.thaumcraftfix.api.event.PlayerGainResearchEventClient;

public class PacketGainResearch implements IMessage {

    private String key;

    public PacketGainResearch() {}

    public PacketGainResearch(String researchKey) {
	key = researchKey;
    }

    public String researchKey() {
	return key;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
	key = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
	ByteBufUtils.writeUTF8String(buf, key);
    }

    public static class Handler implements IMessageHandler<PacketGainResearch, IMessage> {

	@Override
	public IMessage onMessage(PacketGainResearch message, MessageContext ctx) {
	    IProxy proxy = ThaumcraftFix.proxy;
	    proxy.scheduleTask(ctx.side, () -> {
		ResearchEvent.Research event = new PlayerGainResearchEventClient(proxy.getClientPlayer(),
			message.researchKey());
		MinecraftForge.EVENT_BUS.post(event);
	    });
	    return null;
	}

    }

}
