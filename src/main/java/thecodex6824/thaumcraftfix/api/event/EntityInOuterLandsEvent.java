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

import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.Event.HasResult;

/**
 * This event is fired whenever an {@link net.minecraft.entity.Entity Entity} is trying to determine
 * if it is in the Outer Lands. As the Outer Lands do not exist in TC6, normally this check would only pass
 * if the entity is in a dimension that happens to share an ID with what Thaumcraft would have assigned
 * to the Outer Lands. This event allows overriding that check to control entity behavior.
 * <p>
 * A {@link net.minecraftforge.fml.common.eventhandler.Event.Result Result} of <code>DENY</code> will have
 * the entity believe it is not in the Outer Lands, regardless of the normal Thaumcraft behavior.
 * <p>
 * A <code>Result</code> of <code>ALLOW</code> will similarly have the entity believe it is in the Outer Lands.
 * <p>
 * A <code>Result</code> of <code>DEFAULT</code> will use the default Thaumcraft behavior described above.
 * <p>
 * This event is not cancelable.
 */
@HasResult
public class EntityInOuterLandsEvent extends EntityEvent {

    /**
     * Creates a new <code>EntityInOuterLandsEvent</code>.
     * @param entity The {@link net.minecraft.entity.Entity Entity} that is checking if it is
     * currently located in the Outer Lands
     */
    public EntityInOuterLandsEvent(Entity entity) {
	super(entity);
    }

}
