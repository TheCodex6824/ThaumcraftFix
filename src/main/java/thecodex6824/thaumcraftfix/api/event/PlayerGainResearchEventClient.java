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

package thecodex6824.thaumcraftfix.api.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.research.ResearchEvent;
import thecodex6824.thaumcraftfix.api.ThaumcraftFixApi;

/**
 * This event is fired on the <strong>client side only</strong> when a player completes a research stage.
 * <p>
 * Unlike {@link thaumcraft.api.research.ResearchEvent.Research ResearchEvent.Research}, this event
 * will always fire on the client, even outside of singleplayer. Note that because this event inherits
 * <code>ResearchEvent.Research</code>, event handlers listening for that event will also receive this one.
 */
public class PlayerGainResearchEventClient extends ResearchEvent.Research {

    private static final String CANCEL_LOG_TEXT =
	    "Something tried to cancel a client-side research event (added by ThaumcraftFix)." + System.lineSeparator()
	    + "This is unsupported, but will only be a no-op instead of crashing the game." + System.lineSeparator()
	    + "To silence this warning, make sure that the event trying to be canceled is a server-side event, where canceling actually does things.";

    /**
     * Creates a new <code>PlayerGainResearchEventClient</code>.
     * @param player The {@link net.minecraft.entity.player.EntityPlayer EntityPlayer} receiving the research.
     * Since this is the client event, this will always be the local player.
     * @param researchKey The key of the research awarded
     */
    public PlayerGainResearchEventClient(EntityPlayer player, String researchKey) {
	super(player, researchKey);
    }

    @Override
    public boolean isCancelable() {
	return false;
    }

    @Override
    public void setCanceled(boolean cancel) {
	// people might be registered for the normal event and also get this event,
	// which they might try to cancel - so throwing an exception here is not good
	Logger logger = LogManager.getLogger(ThaumcraftFixApi.PROVIDES);
	logger.error(CANCEL_LOG_TEXT, new UnsupportedOperationException("Cannot cancel PlayerGainResearchEventClient"));
    }

}
