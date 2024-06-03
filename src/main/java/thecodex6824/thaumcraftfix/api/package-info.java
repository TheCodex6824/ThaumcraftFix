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

/**
 * This API provides interfaces and helpers for Thaumcraft Fix.
 *
 * The contents of the API package will remain stable (to the best of the maintainer's ability)
 * throughout the lifetime of the mod, and give ample warning of breaking changes. Anything *outside*
 * the API package is subject to change without notice, so please don't use it - ask if you need something there instead :)
 */
@net.minecraftforge.fml.common.API(owner = ThaumcraftFixApi.MODID, provides = ThaumcraftFixApi.PROVIDES,
apiVersion = ThaumcraftFixApi.API_VERSION)
@javax.annotation.ParametersAreNonnullByDefault
package thecodex6824.thaumcraftfix.api;