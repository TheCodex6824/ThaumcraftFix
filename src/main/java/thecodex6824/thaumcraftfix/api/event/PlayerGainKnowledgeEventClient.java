package thecodex6824.thaumcraftfix.api.event;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.capabilities.IPlayerKnowledge.EnumKnowledgeType;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.api.research.ResearchEvent;
import thecodex6824.thaumcraftfix.api.ThaumcraftFixApi;

public class PlayerGainKnowledgeEventClient extends ResearchEvent.Knowledge {

    private static final String CANCEL_LOG_TEXT =
	    "Something tried to cancel a client-side knowledge event (added by ThaumcraftFix)." + System.lineSeparator()
	    + "This is unsupported, but will only be a no-op instead of crashing the game." + System.lineSeparator()
	    + "To silence this warning, make sure that the event trying to be canceled is a server-side event, where canceling actually does things.";

    public PlayerGainKnowledgeEventClient(EntityPlayer player, EnumKnowledgeType type, @Nullable ResearchCategory category, int amount) {
	super(player, type, category, amount);
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
	logger.error(CANCEL_LOG_TEXT, new UnsupportedOperationException("Cannot cancel PlayerGainKnowledgeEventClient"));
    }

}
