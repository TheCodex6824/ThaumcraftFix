/**
 *  Thaumcraft Fix
 *  Copyright (c) 2026 TheCodex6824.
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

import javax.annotation.Nullable;

import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import thaumcraft.api.casters.FocusMedium;

/**
 * Base event for observing or modifying the RayTraceResult from foci that use them,
 * such as Touch and Bolt focus mediums. Cancel this event to make the focus "miss" and hit
 * nothing.
 * 
 * This event was formerly known as FocusTouchGetEntityEvent when it was in Thaumic Augmentation.
 * This version of the event only requires a FocusMedium instead of FocusMediumTouch to potentially
 * support addon foci. You can always instanceof/cast to FocusMediumTouch if needed.
 */
@Cancelable
public class RaytraceFocusGetEntityEvent extends WorldEvent {

    protected FocusMedium focus;
    protected thaumcraft.api.casters.Trajectory trajectory;
    protected RayTraceResult result;
    protected double range;
    
    public RaytraceFocusGetEntityEvent(FocusMedium focus, thaumcraft.api.casters.Trajectory trajectory, RayTraceResult original, double range) {
        super(focus.getPackage().world);
        this.focus = focus;
        this.trajectory = trajectory;
        result = original;
        this.range = range;
    }
    
    /**
     * Returns the focus medium currently doing the raytrace check.
     * @return The focus medium raytracing
     */
    public FocusMedium getFocus() {
        return focus;
    }
    
    /**
     * Returns the Trajectory associated with this focus medium.
     * @return The trajectory used for the raytrace
     */
    public thaumcraft.api.casters.Trajectory getTrajectory() {
        return trajectory;
    }
    
    /**
     * Returns the current RayTraceResult of the focus. To make this trajectory have no result,
     * set a null result with setRay or cancel this event.
     * Note that the result may be null if it was explicitly set or never hit anything in the first place.
     * @return The possibly null RayTraceResult
     */
    @Nullable
    public RayTraceResult getRay() {
        return result;
    }
    
    /**
     * Returns the range in blocks used for this raytrace.
     * @return The range
     */
    public double getRange() {
        return range;
    }
    
    /**
     * Set the RayTraceResult for this trajectory. Null results are allowed.
     * @param ray The raytrace
     */
    public void setRay(RayTraceResult ray) {
        result = ray;
    }
    
    /**
     * Event subclass fired by supplyTrajectories().
     */
    public static class Trajectory extends RaytraceFocusGetEntityEvent {
        
        public Trajectory(FocusMedium focus, thaumcraft.api.casters.Trajectory trajectory, RayTraceResult original, double range) {
            super(focus, trajectory, original, range);
        }
        
    }
    
    /**
     * Event subclass fired by supplyTargets().
     */
    public static class Target extends RaytraceFocusGetEntityEvent {
        
        public Target(FocusMedium focus, thaumcraft.api.casters.Trajectory trajectory, RayTraceResult original, double range) {
            super(focus, trajectory, original, range);
        }
        
    }
    
}
