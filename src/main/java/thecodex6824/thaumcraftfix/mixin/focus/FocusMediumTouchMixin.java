package thecodex6824.thaumcraftfix.mixin.focus;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;
import thaumcraft.api.casters.FocusMedium;
import thaumcraft.api.casters.Trajectory;
import thaumcraft.common.items.casters.foci.FocusMediumTouch;
import thecodex6824.thaumcraftfix.api.event.RaytraceFocusGetEntityEvent;

@Mixin(value = FocusMediumTouch.class, remap = false)
public abstract class FocusMediumTouchMixin extends FocusMedium {

    @ModifyExpressionValue(method = "supplyTrajectories",
	    at = @At(
		    value = "INVOKE",
		    target = "Lthaumcraft/common/lib/utils/EntityUtils;getPointedEntityRay(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;DDFZ)Lnet/minecraft/util/math/RayTraceResult;"
		    ))
    private RayTraceResult supplyTrajectoriesEvent(RayTraceResult original, @Local(ordinal = 0) Trajectory trajectory, @Local(ordinal = 0) double range) {
	RaytraceFocusGetEntityEvent.Trajectory event = new RaytraceFocusGetEntityEvent.Trajectory(this, trajectory, original, range);
	MinecraftForge.EVENT_BUS.post(event);
	return !event.isCanceled() ? event.getRay() : null;
    }
    
    @ModifyExpressionValue(method = "supplyTargets",
	    at = @At(
		    value = "INVOKE",
		    target = "Lthaumcraft/common/lib/utils/EntityUtils;getPointedEntityRay(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;DDFZ)Lnet/minecraft/util/math/RayTraceResult;"
		    ))
    private RayTraceResult supplyTargetsEvent(RayTraceResult original, @Local(ordinal = 0) Trajectory trajectory, @Local(ordinal = 0) double range) {
	RaytraceFocusGetEntityEvent.Target event = new RaytraceFocusGetEntityEvent.Target(this, trajectory, original, range);
	MinecraftForge.EVENT_BUS.post(event);
	return !event.isCanceled() ? event.getRay() : null;
    }
    
}
