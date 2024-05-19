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

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumcraft.api.capabilities.IPlayerKnowledge.EnumKnowledgeType;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.api.research.ResearchEvent;
import thecodex6824.thaumcraftfix.IProxy;
import thecodex6824.thaumcraftfix.ThaumcraftFix;
import thecodex6824.thaumcraftfix.api.event.PlayerGainKnowledgeEventClient;

public class PacketGainKnowledge implements IMessage {

    private EnumKnowledgeType type;
    private ResearchCategory category;
    private int amount;

    public PacketGainKnowledge() {}

    public PacketGainKnowledge(EnumKnowledgeType type, @Nullable ResearchCategory category, int amount) {
	this.type = type;
	this.category = category;
	this.amount = amount;
    }

    public EnumKnowledgeType type() {
	return type;
    }

    @Nullable
    public ResearchCategory category() {
	return category;
    }

    public int amount() {
	return amount;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
	type = EnumKnowledgeType.values()[buf.readByte()];
	amount = buf.readInt();
	String maybeCategory = ByteBufUtils.readUTF8String(buf);
	if (!maybeCategory.isEmpty()) {
	    category = ResearchCategories.getResearchCategory(maybeCategory);
	}
    }

    @Override
    public void toBytes(ByteBuf buf) {
	buf.writeByte(type.ordinal());
	buf.writeInt(amount);
	ByteBufUtils.writeUTF8String(buf, category != null ? category.key : "");
    }

    public static class Handler implements IMessageHandler<PacketGainKnowledge, IMessage> {

	@Override
	public IMessage onMessage(PacketGainKnowledge message, MessageContext ctx) {
	    IProxy proxy = ThaumcraftFix.proxy;
	    proxy.scheduleTask(ctx.side,() -> {
		ResearchEvent.Knowledge event = new PlayerGainKnowledgeEventClient(proxy.getClientPlayer(),
			message.type(), message.category(), message.amount());
		MinecraftForge.EVENT_BUS.post(event);
	    });
	    return null;
	}

    }

}
