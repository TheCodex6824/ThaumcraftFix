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

package thecodex6824.thaumcraftfix.common.event;

import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thaumcraft.common.lib.events.PlayerEvents;
import thecodex6824.thaumcraftfix.api.ThaumcraftFixApi;

@EventBusSubscriber(modid = ThaumcraftFixApi.MODID)
public class CommonEventHandler {

    @SubscribeEvent
    public static void onFallFirst(LivingAttackEvent event) {
	if (event.getSource() == DamageSource.FALL) {
	    LivingHurtEvent fakeEvent = new LivingHurtEvent(event.getEntityLiving(), event.getSource(),
		    event.getAmount());
	    PlayerEvents.onFallDamage(fakeEvent);
	    if (fakeEvent.isCanceled()) {
		event.setCanceled(true);
	    }
	}
    }

}
