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

package thecodex6824.thaumcraftfix.api.event.research;

import com.google.gson.JsonObject;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import thaumcraft.api.research.ResearchEntry;

/**
 * Event superclass for the beginning and end of the research entry loading process.
 * These events fire for each individual research entry, compared to {@link ResearchLoadEvent},
 * which is for the entire research loading process.
 * Subscribing to this event will notify the callback for all subclasses of this class.
 * @author TheCodex6824
 */
public class ResearchEntryLoadEvent extends Event {

    protected final JsonObject json;

    protected ResearchEntryLoadEvent(JsonObject researchJson) {
	json = researchJson;
    }

    public JsonObject getResearchJson() {
	return json;
    }

    /**
     * Event that fires before the research entry is parsed and any patches are applied to this research (if any).
     * Canceling this event will stop this research entry from being loaded.
     * @author TheCodex6824
     */
    @Cancelable
    public static class Pre extends ResearchEntryLoadEvent {

	public Pre(JsonObject researchJson) {
	    super(researchJson);
	}

    }

    /**
     * Event that fires after any the research entry is processed and patches are applied to this research (if any).
     * <p>
     * This event will not fire if {@link ResearchEntryLoadEvent.Pre} stopped this research from loading,
     * or the research is otherwise unable to be loaded (i.e. missing a key). The JSON object contained
     * is also for reference only - changing it at this point will have no effect on the associated research entry.
     * @author TheCodex6824
     */
    public static class Post extends ResearchEntryLoadEvent {

	private final ResearchEntry entry;

	public Post(JsonObject researchJson, ResearchEntry loadedEntry) {
	    super(researchJson);
	    entry = loadedEntry;
	}

	public ResearchEntry getResearchEntry() {
	    return entry;
	}

    }

}
