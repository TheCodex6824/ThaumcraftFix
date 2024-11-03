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

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thecodex6824.thaumcraftfix.IProxy;
import thecodex6824.thaumcraftfix.ThaumcraftFix;

public class PacketConfigSync implements IMessage {

    private JsonObject payload;

    public PacketConfigSync() {}

    public PacketConfigSync(JsonObject sync) {
	payload = sync;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
	String raw = ByteBufUtils.readUTF8String(buf);
	payload = new Gson().fromJson(raw, JsonObject.class);
    }

    @Override
    public void toBytes(ByteBuf buf) {
	ByteBufUtils.writeUTF8String(buf, new Gson().toJson(payload));
    }

    public static class Handler implements IMessageHandler<PacketConfigSync, IMessage> {

	@Override
	public IMessage onMessage(PacketConfigSync message, MessageContext ctx) {
	    IProxy proxy = ThaumcraftFix.proxy;
	    proxy.scheduleTask(ctx.side,() -> {
		ThaumcraftFix.instance.getConfig().deserializeNetwork(message.payload);
	    });
	    return null;
	}

    }

}
