# Notice

Lifeguard's code is under the Mozilla Public License 2.0 (see `LICENSE`). Some of its art is derived from other mods'
art, under those mods' licenses, as listed here.

## Bundled library

Lifeguard's jar bundles [MixinSquared](https://github.com/Bawnorton/MixinSquared) (by Bawnorton and LlamaLad7), which lets
an entry adjust another mod's own mixins. It is under the MIT License; its text is in `licenses/MixinSquared-MIT.txt`.

## Derived from Quark

`assets/quark/textures/glint/enchanted_item_glint_<color>.png`, one for each of Dye Depot's 16 colors (amber, aqua,
beige, coral, forest, ginger, indigo, maroon, mint, navy, olive, rose, slate, tan, teal, verdant), are derived from
[Quark](https://github.com/VazkiiMods/Quark)'s rune glint textures, by Vazkii and contributors. Each is the Quark glint
nearest in hue to the Dye Depot dye, with every pixel's hue replaced by the dye's. These textures are licensed under
[CC BY-NC-SA 3.0](https://creativecommons.org/licenses/by-nc-sa/3.0/), like Quark's art.

`assets/lifeguard/textures/glint/` (the `sexy_runic_glints` entry's glints) are mostly derived from Quark's glints too,
recolored, hue-turned or re-shaded per color: `amber`, `aqua`, `beige`, `black`, `brown`, `coral`, `forest`, `ginger`,
`gray`, `green`, `light_blue`, `lime`, `magenta`, `maroon`, `mint`, `olive`, `pink`, `rainbow`, `red`, `rose`, `slate`,
`tan`, `verdant` and the four `*_ramp` ray maps. These are under CC BY-NC-SA 3.0 as well.

## Derived from Alex's Caves

`assets/lifeguard/textures/block/rock_candy_<color>.png`, one for each of Dye Depot's 16 colors, are derived from
[Alex's Caves](https://github.com/AlexModGuy/AlexsCaves)' rock candy textures (by Alexthe668 and Noonyeyz). Each keeps
Alex's Caves' shared rock candy pixel layout and recolors it with a six-color ramp predicted from Alex's Caves' own
wool-to-candy colors.

Alex's Caves is licensed under the
[GNU Lesser General Public License v3.0](https://www.gnu.org/licenses/lgpl-3.0.html), and these modified textures are
released under that same license. Its text, and the GNU GPL v3.0 it builds on, are in `licenses/`.

## Derived from Regions Unexplored

The `spawn_ru_pineify_rotten_wood` entry's bark, `resourcepacks/spawn_ru_pineify_rotten_wood/assets/spawn/textures/block/rotten_log.png`
and `.../assets/lifeguard/textures/block/rotten_log_mossy.png`, is derived from
[Regions Unexplored](https://github.com/Apollounknowndev/RegionsUnexplored)' pine log texture (by Apollo and
contributors): its pixels kept, its seven shades recolored to Regions Unexplored's dead log colors; the mossy one adds
moss down the bark's own grooves, blended from three of Spawn's moss colors. Its stripped log side,
`.../assets/spawn/textures/block/stripped_rotten_log.png`, is Regions Unexplored's stripped pine log texture, its six
shades' lightness spacing kept and recolored to Spawn's stripped rotten log gray, with two soft patches where its
darker blotches turn a sickly olive. Regions Unexplored is under the MIT License, copyright (c) 2026 Apollo; its text is in
`licenses/RegionsUnexplored-MIT.txt`.

## Recolored at runtime, not included

The `dye_depot_sparklers` entry's sparkler and spark textures are
[Caverns & Chasms](https://github.com/team-abnormals/caverns-and-chasms)' own (by Team Abnormals), recolored on the
player's machine from their installed copy of Caverns & Chasms. Its Abnormals License forbids distributing modified
copies, so none ship in this jar: only the color tables in `lifeguard_recolors/`, which are this mod's own.

The `etcetera_shimmering_bismuth_trims` entry's animated bismuth trims are made the same way, on the player's machine
from their installed [Etcetera](https://modrinth.com/mod/etcetera) (by Ninni, All Rights Reserved): its bismuth trim
palette, hue-shifted per frame of its bismuth block. Nothing of Etcetera's is included.

The `spawn_ru_pineify_rotten_wood` entry's rotten log top, anthill and sign textures are made on the player's machine from their installed
[Spawn](https://modrinth.com/mod/spawn) (by Ninni, All Rights Reserved): Spawn's bark colors swapped for the new bark's,
and the anthills' holes laid over the new bark. Nothing of Spawn's is included.

The `ac_thornwood_very_slightly_lighter_planks` entry's thornwood textures are made on the player's machine from their
installed [Alex's Caves](https://modrinth.com/mod/alexs-caves) (by AlexModGuy, art by Ninni): each thornwood wood color
a hair lighter, from the color table in `assets/lifeguard/recolors/ac_thornwood.json`, which is this mod's own. Nothing
of Alex's Caves' is included.

## Original

`indigo`, `navy` and `teal` in `assets/lifeguard/textures/glint/` are procedural glimmers, not derived from Quark's or
Minecraft's art, and are under this mod's license like the code.

