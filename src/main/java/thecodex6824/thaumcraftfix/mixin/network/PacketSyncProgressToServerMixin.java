/**
 *  Thaumcraft Fix
 *  Copyright (c) 2025 TheCodex6824.
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

package thecodex6824.thaumcraftfix.mixin.network;

import java.util.Arrays;

import javax.annotation.Nullable;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.api.research.ResearchEntry;
import thaumcraft.api.research.ResearchEntry.EnumResearchMeta;
import thaumcraft.api.research.ResearchStage;
import thaumcraft.common.lib.network.playerdata.PacketSyncProgressToServer;
import thaumcraft.common.lib.research.ResearchManager;

@Mixin(PacketSyncProgressToServer.class)
public class PacketSyncProgressToServerMixin {

    @WrapOperation(method = "fromBytes", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target =
	    "Lthaumcraft/common/lib/network/playerdata/PacketSyncProgressToServer;checks:Z", remap = false), remap = false)
    private void forceChecksOn(PacketSyncProgressToServer packet, boolean value, Operation<Void> original) {
	original.call(packet, true);
    }

    @WrapOperation(method = "checkRequisites", at = @At(value = "INVOKE", target =
	    "Lthaumcraft/api/research/ResearchEntry;getStages()[Lthaumcraft/api/research/ResearchStage;", ordinal = 0, remap = false), remap = false)
    private ResearchStage[] wrapUnsafeDeref(@Nullable ResearchEntry entry, Operation<ResearchStage[]> original) {
	return entry != null ? original.call(entry) : null;
    }

    @ModifyReturnValue(method = "checkRequisites", at = @At("RETURN"), remap = false)
    private boolean blockNullResearch(boolean original, @Local(ordinal = 0) ResearchEntry entry) {
	return entry != null ? original : false;
    }

    @ModifyExpressionValue(method = "checkRequisites", at = @At(value = "INVOKE", target =
	    "Lthaumcraft/api/capabilities/IPlayerKnowledge;getResearchStage(Ljava/lang/String;)I", remap = false), remap = false)
    private int checkProgressSyncStage(int originalStage, EntityPlayer player, String key,
	    @Local(ordinal = 0) ResearchEntry entry) {

	int logicStage = originalStage;
	boolean hasParents = entry.getParents() != null && entry.getParents().length > 0;
	boolean hidden = entry.getMeta() != null && Arrays.stream(entry.getMeta()).anyMatch(e -> e == EnumResearchMeta.HIDDEN);
	ResearchCategory category = ResearchCategories.getResearchCategory(entry.getCategory());
	boolean noParentPass = !hidden && category != null && (category.researchKey == null || ThaumcraftCapabilities.knowsResearchStrict(player, category.researchKey));
	if (logicStage < 1 && (hasParents || noParentPass) && ResearchManager.doesPlayerHaveRequisites(player, entry.getKey())) {
	    logicStage = Integer.MAX_VALUE;
	}

	return logicStage;
    }

}
