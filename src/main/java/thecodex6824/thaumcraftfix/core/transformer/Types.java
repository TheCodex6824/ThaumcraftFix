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

package thecodex6824.thaumcraftfix.core.transformer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.objectweb.asm.Type;

public final class Types {

    private Types() {}

    public static final Type OBJECT = Type.getType(Object.class);
    public static final Type STRING = Type.getType(String.class);
    public static final Type UUID = Type.getType(UUID.class);
    public static final Type LIST = Type.getType(List.class);
    public static final Type ARRAY_LIST = Type.getType(ArrayList.class);
    public static final Type SET = Type.getType(Set.class);
    public static final Type RANDOM = Type.getType(Random.class);
    public static final Type ITERATOR = Type.getType(Iterator.class);

    public static final Type ENTITY = Type.getType("Lnet/minecraft/entity/Entity;");
    public static final Type ENTITY_LIVING_BASE = Type.getType("Lnet/minecraft/entity/EntityLivingBase;");
    public static final Type ENTITY_LIVING = Type.getType("Lnet/minecraft/entity/EntityLiving;");
    public static final Type ENTITY_PLAYER = Type.getType("Lnet/minecraft/entity/player/EntityPlayer;");
    public static final Type ENTITY_PLAYER_MP = Type.getType("Lnet/minecraft/entity/player/EntityPlayerMP;");

    public static final Type BLOCK = Type.getType("Lnet/minecraft/block/Block;");
    public static final Type BLOCK_FACE_SHAPE = Type.getType("Lnet/minecraft/block/state/BlockFaceShape;");
    public static final Type BLOCK_POS = Type.getType("Lnet/minecraft/util/math/BlockPos;");
    public static final Type ENUM_FACING = Type.getType("Lnet/minecraft/util/EnumFacing;");
    public static final Type I_BLOCK_STATE = Type.getType("Lnet/minecraft/block/state/IBlockState;");
    public static final Type TILE_ENTITY = Type.getType("Lnet/minecraft/tileentity/TileEntity;");

    public static final Type ITEM = Type.getType("Lnet/minecraft/item/Item;");
    public static final Type ITEM_STACK = Type.getType("Lnet/minecraft/item/ItemStack;");
    public static final Type NBT_TAG_COMPOUND = Type.getType("Lnet/minecraft/nbt/NBTTagCompound;");

    public static final Type I_BLOCK_ACCESS = Type.getType("Lnet/minecraft/world/IBlockAccess;");
    public static final Type WORLD = Type.getType("Lnet/minecraft/world/World;");
    public static final Type WORLD_SERVER = Type.getType("Lnet/minecraft/world/WorldServer;");

    public static final Type FOCUS_PACKAGE = Type.getType("Lthaumcraft/api/casters/FocusPackage;");
    public static final Type I_CASTER = Type.getType("Lthaumcraft/api/casters/ICaster;");
    public static final Type ITEM_CASTER = Type.getType("Lthaumcraft/common/items/casters/ItemCaster;");

    public static final Type TILE_FOCAL_MANIPULATOR = Type.getType("Lthaumcraft/common/tiles/crafting/TileFocalManipulator;");

    public static final Type SOUND_EVENT = Type.getType("Lnet/minecraft/util/SoundEvent;");

    public static final Type RESEARCH_TABLE_DATA = Type.getType("Lthaumcraft/api/research/theorycraft/ResearchTableData;");

}
