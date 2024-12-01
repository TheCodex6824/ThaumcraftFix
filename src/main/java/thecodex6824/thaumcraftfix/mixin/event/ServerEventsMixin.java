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

package thecodex6824.thaumcraftfix.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraftforge.fml.common.gameevent.TickEvent;
import thaumcraft.common.lib.events.ServerEvents;
import thaumcraft.common.world.aura.AuraThread;
import thecodex6824.thaumcraftfix.api.aura.CapabilityAuraProcessor;
import thecodex6824.thaumcraftfix.common.aura.GenericAuraThread;

@Mixin(ServerEvents.class)
public class ServerEventsMixin {

    @Redirect(method = "worldTick(Lnet/minecraftforge/fml/common/gameevent/TickEvent$WorldTickEvent;)V",
	    at = @At(value = "NEW", target = "(I)Lthaumcraft/common/world/aura/AuraThread;"), remap = false)
    private static AuraThread redirectCreateAuraThread(int dim, TickEvent.WorldTickEvent event) {
	return new GenericAuraThread(dim,
		event.world.getCapability(CapabilityAuraProcessor.AURA_PROCESSOR, null));
    }

}
