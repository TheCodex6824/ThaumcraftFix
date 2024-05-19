package thecodex6824.thaumcraftfix.api.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.research.ResearchEvent;
import thecodex6824.thaumcraftfix.api.ThaumcraftFixAPI;

public class PlayerGainResearchEventClient extends ResearchEvent.Research {

    private static final String CANCEL_LOG_TEXT =
	    "Something tried to cancel a client-side research event (added by ThaumcraftFix)." + System.lineSeparator()
	    + "This is unsupported, but will only be a no-op instead of crashing the game." + System.lineSeparator()
	    + "To silence this warning, make sure that the event trying to be canceled is a server-side event, where canceling actually does things.";

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
	Logger logger = LogManager.getLogger(ThaumcraftFixAPI.PROVIDES);
	logger.error(CANCEL_LOG_TEXT, new UnsupportedOperationException("Cannot cancel PlayerGainResearchEventClient"));
    }

}
