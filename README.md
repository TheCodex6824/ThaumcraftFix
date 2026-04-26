# Thaumcraft Fix

Thaumcraft fix is an addon mod for Thaumcraft 6 that fixes some of the bugs in the mod.

## License

Thaumcraft Fix is licensed under the GNU Lesser Public License v3 (or later). So that means if you want to include this mod in a modpack, go right ahead!

---

## Fixes
- **Arcane Bore**
  - Fixed Arcane Bore Gui position
  - Fixed Arcane Bore having errored particles when digging certain blocks
  - Fixed Arcane Bore having some issues with the Destructive infusion enchantment
  - Fixed Arcane Bore not working with the Lamplighter infusion enchantment

- **Arcane Workbench**
  - Fixed Arcane Workbench causing issues with certain items from other mods like Akashic Tome or Morph-o-Tool

- **Aspects**
  - Fixed items having aspects registered even when there are no aspects available
  - Fixed Thaumcraft mobs having no aspects registered

- **Aura/Vis**
  - Fixed aura chunks sometimes not holding any Vis in multiplayer
  - Fixed Vis draining into chunks it isn't supposed to

- **Automated Crossbows**
  - Fixed Automated Crossbows being unable to fire special arrows
  - Fixed Automated Crossbow Gui positions

- **Crashes**
  - Deregistered spellbat spawn egg to prevent a crash when using it
  - Fixed Flux pollution client crash with large pollution values
  - Fixed Magical Mirrors causing a crash when they link to an invalid dimension

- **Duping**
  - Fixed duplication issue with brain jars
  - Fixed duplication issue with infusion pillars
  - Fixed duplication issue with owned constructs
  - Fixed duplication issue with pechs
  - Fixed duplication issue with primordial pearls (configurable)
  - Fixed duplication issue with Shovel of the Earthmover
  - Fixed duplication issue with the Arcane Workbench
  - Fixed duplication issue with the Infernal Furnace
  - Fixed duplication issue with the Thaumatorium

- **Focal Manipulator/Casting**
  - Fixed the Exchange Focus Effect messing up TC wooden log block rotation
  - Fixed the Exchange Focus Effect's silk touch not having a complexity cost
  - Fixed Focal Manipulator not reporting the actual amount of levels required for crafting
  - Fixed Focal Manipulator sometimes not completing and/or creating glitched foci
  - Fixed Caster's Gauntlet Gui causing rendering issues when gauntlet is in mainhand and Thaumometer or Sanity Checker is held in the offhand
  - Fixed Caster's Gauntlet Gui causing rendering issues when plan focus is equipped and Thaumometer or Sanity Checker is also held
  - Fixed Caster's Gauntlet Gui rendering in the incorrect location if gauntlet is held in the offhand
  - Fixed Caster's Gauntlet select focus keybind breaking swap hands keybind
  - Fixed not being able to put the scatter modifier after any trajectory pattern on the Focal Manipulator

- **Golems**
  - Fixed Golems voiding held items when interacting with Use Seals with "Can use empty hand" enabled

- **Misc**
  - Fixed all plant hitboxes
  - Fixed Burrowing infusion enchantment not dropping any experiencees
  - Fixed Magical Hand Mirror not checking which hand it's in, causing NBT loss and other issues
  - Fixed Primordial Pearl boss drop spawn location and movement logic
  - Fixed Thaumcraft not correctly using a block placement event when swapping blocks
  - Fixed TileThaumcraftInventory trying to send packets to the client on the client side
  - Removed Thaumcraft's single usage of a scala class
  - Silenced all Thaumcraft texture/model errors

- **Pech**
  - Fixed Pech trading Gui position and item shading
  - Fixed Uncraftable Potion Pech trade item

- **Research**
  - Fixed Ancient and Eldritch Infusion Altars being impossible to create
  - Fixed Exploration Research missing or not being granted to players under specific circumstances
  - Fixed extra colored nitor recipes not displaying in Discovering Alchemy
  - Fixed research items not accepting wildcard metadata
  - Fixed research page formatting tags getting dropped if they are the last text in an entry
  - Fixed Research Table shift-click interaction for Paper and Scribing Tools
  - Fixed Runic Shielding infusion not working on items with baubles capability
  - Fixed scribing tools from other addons not being able to be used for celestial note scanning
  - Tightened research packet requirement to only allow research with parent research
  - Tweaked research category detection to work around weird research with null categories

- **Performance**
  - Fixed aura chunks impacting performance
  - Fixed Giant Taintacle spamming log errors when rendering
  - Fixed Magical Forest biome decoration causing cascading lag
  - Fixed unloaded dimensions still retaining Thaumcraft particles
  - Optimized rendering on Flux Rifts (configurable)

- **Sounds**
  - Fixed Arcane Bore sounds being bugged
  - Fixed Focal Manipulator not playing a sound when it fails to craft
  - Fixed sounds not properly playing for casting gauntlets
  - Fixed sounds not properly playing for jars
  - Fixed sounds not properly playing for loot bags
  - Fixed sounds not properly playing for mirrors
  - Fixed sounds not properly playing for phials
  - Fixed sounds not properly playing for the Sword of Zephyr
  - Fixed the 'runicshieldcharge' sound event not playing

- **Thaumometer**
  - Fixed Thaumometer entity aspect display persisting on dead entities and when the item is put away

- **Visual**
  - Fixed custom armor model rendering on armor stands
  - Fixed Eldritch Guardians always creating fog regardless of dimension
  - Fixed Exploration research status text
  - Fixed Firebats not having particles
  - Fixed shoulder textures on some models being mirrored
  - Fixed Thaumcraft OBJ models having a lighting multiplier that made them darker
  - Fixed Void Thaumaturge Robes displaying twice the amount of armor on the hud
  - Fixed Wisps having really small burst particles on death
  - Stopped screen shake and damage sound if fall damage is reduced to 0 from gear

---

## Tweaks
- **Quality of Life**
  - Added a recipe to craft 9 sliver back into quartz
  - Added aspects to more entities that were missing them
  - Fixed Banners consuming Phials when applying essentia icons
  - Gave Primordial Crusher the ability to make dirt/grass paths
  - Made Eldritch Crabs rotate all the way to match the death animations of spiders, silverfish and endermites
  - Made "TC.RUNIC" and "TC.WARP" NBT tags not capped
  - Phials no longer get consumed in creative mode  
  - Sanity Soap no longer gets consumed in creative mode

- **Research**
  - Added back the original Infernal Furnace research icon
  - Added Block of Flesh recipe to the Sanitizing Soap page
  - Added robe recipes from Discovering Infusion to the Basic Infusion page
  - Added rotating icons to Alchemical Metallurgy, Thaumium Fortress Armor, and Void Thaumaturge Armor
  - Rewrote several research entries to fix typos and improve wording
  - Stopped theorycraft cards from giving points in unused categories
  - Updated research images to not be from older versions of Thaumcraft
  - Updated Types of Knowledge to use a different icon to stand out a little more

- **Resources**
  - Added subtitles for all Thaumcraft sounds
  - Darkened the handle of void tools, similar to the Primal Crusher
  - Fixed Primal Crusher's texture not being animated
  - Fixed Redstone Inlay texture having white borders
  - Fixed Triple Meat Treat's texture not being animated
  - Renamed Unnatural Hunger to Unhunger to prevent overlap in the gui
  - Scaled the gauntlet model up slightly
  - Several armor item sprites have had their positions centered
  - Tweaked Flux Sponge texture

---
For much more tweaks (and some fixes), check out [ThaumicTweaker](https://www.curseforge.com/minecraft/mc-mods/thaumictweaker)!