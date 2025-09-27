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

package thecodex6824.thaumcraftfix.test;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Method;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.api.research.ResearchEntry;
import thaumcraft.common.lib.network.playerdata.PacketSyncProgressToServer;
import thecodex6824.thaumcraftfix.testlib.lib.MockPlayer;
import thecodex6824.thaumcraftfix.testlib.lib.MockWorld;

public class TestResearchPacket {

    private Method reqs;

    public TestResearchPacket() throws Exception {
	reqs = PacketSyncProgressToServer.class.getDeclaredMethod("checkRequisites", EntityPlayer.class, String.class);
	reqs.setAccessible(true);
    }

    @Test
    void testUnlockFakeResearch() throws Exception {
	MockPlayer player = new MockPlayer(new MockWorld(false),
		new GameProfile(UUID.randomUUID(), "UnitTest"));
	player.addCapability(ThaumcraftCapabilities.KNOWLEDGE, ThaumcraftCapabilities.KNOWLEDGE.getDefaultInstance());
	PacketSyncProgressToServer packet = new PacketSyncProgressToServer("FAKE",
		true, false, false);
	assertFalse((Boolean) reqs.invoke(packet, player, "FAKE"));
    }

    @Test
    void testUnlockResearchIllegally() throws Exception {
	MockPlayer player = new MockPlayer(new MockWorld(false),
		new GameProfile(UUID.randomUUID(), "UnitTest"));
	player.addCapability(ThaumcraftCapabilities.KNOWLEDGE, ThaumcraftCapabilities.KNOWLEDGE.getDefaultInstance());

	for (ResearchCategory category : ResearchCategories.researchCategories.values()) {
	    for (ResearchEntry entry : category.research.values()) {
		if (!entry.getKey().equals("FIRSTSTEPS")) {
		    for (int i = 0; i < entry.getStages().length; ++i) {
			PacketSyncProgressToServer packet = new PacketSyncProgressToServer(entry.getKey(),
				true, false, false);
			assertFalse((Boolean) reqs.invoke(packet, player, entry.getKey()));
		    }
		}
	    }
	}

    }

}
