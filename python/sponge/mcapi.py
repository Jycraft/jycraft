print('Importing command definitions...')

from jycraft.plugin.interpreter import PyContext

from org.spongepowered.api.text import Texts
from org.spongepowered.api.world.weather import Weathers
from org.spongepowered.api.world.explosion import Explosion
from org.spongepowered.api.block import BlockTypes
from org.spongepowered.api.entity import EntityTypes
from org.spongepowered.api.event.cause.entity.spawn import SpawnCause, SpawnTypes
from org.spongepowered.api.effect.particle import ParticleEffect, ParticleTypes

from org.spongepowered.api.util.blockray import BlockRay
from org.spongepowered.api.command.spec import CommandSpec

from com.flowpowered.math.vector import Vector3d

from random import *

PLUGIN  = PyContext.getPlugin()
GAME    = PLUGIN.game
SERVER 	= GAME.getServer()
WORLD 	= SERVER.getWorld(SERVER.getDefaultWorld().get().getWorldName()).get()
MORNING = 2000
NOON 	= 6000
EVENING = 14000
NIGHT 	= 18000

#full list of BlockTypes available in JavaDocs on canarymod.net
AIR                 = BlockTypes.AIR
STONE               = BlockTypes.STONE
GRASS               = BlockTypes.GRASS
DIRT                = BlockTypes.DIRT
COBBLESTONE         = BlockTypes.COBBLESTONE
WOOD_PLANKS         = BlockTypes.PLANKS
# SAPLING             = BlockType.OakSapling
# BEDROCK             = BlockType.Bedrock
# WATER_FLOWING       = BlockType.WaterFlowing
# WATER               = WATER_FLOWING
# WATER_STATIONARY    = BlockType.Water
# LAVA_FLOWING        = BlockType.LavaFlowing
# LAVA                = LAVA_FLOWING
# LAVA_STATIONARY     = BlockType.Lava
# SAND                = BlockType.Sand
# GRAVEL              = BlockType.Gravel
# GOLD_ORE            = BlockType.GoldOre
# IRON_ORE            = BlockType.IronOre
# COAL_ORE            = BlockType.CoalOre
# WOOD                = BlockType.OakLog
# LEAVES              = BlockType.OakLeaves
# GLASS               = BlockType.Glass
# LAPIS_LAZULI_ORE    = BlockType.LapisOre
# LAPIS_LAZULI_BLOCK  = BlockType.LapisBlock
# SANDSTONE           = BlockType.Sandstone
# BED                 = BlockType.Bed
# COBWEB              = BlockType.Web
# GRASS_TALL          = BlockType.TallGrass
# WOOL                = BlockType.WhiteWool
# FLOWER_YELLOW       = BlockType.Dandelion
# FLOWER_CYAN         = BlockType.BlueOrchid
# MUSHROOM_BROWN      = BlockType.BrownMushroom
# MUSHROOM_RED        = BlockType.RedMushroom
# GOLD_BLOCK          = BlockType.GoldBlock
# IRON_BLOCK          = BlockType.IronBlock
# STONE_SLAB_DOUBLE   = BlockType.DoubleStoneSlab
# STONE_SLAB          = BlockType.StoneSlab
# BRICK_BLOCK         = BlockType.BrickBlock
# TNT                 = BlockType.TNT
# BOOKSHELF           = BlockType.Bookshelf
# MOSS_STONE          = BlockType.MossyCobble
# OBSIDIAN            = BlockType.Obsidian
# TORCH               = BlockType.Torch
# FIRE                = BlockType.FireBlock
# STAIRS_WOOD         = BlockType.OakStairs
# CHEST               = BlockType.Chest
# DIAMOND_ORE         = BlockType.DiamondOre
# DIAMOND_BLOCK       = BlockType.DiamondBlock
# CRAFTING_TABLE      = BlockType.Workbench
# FARMLAND            = BlockType.Farmland
# FURNACE_INACTIVE    = BlockType.Furnace
# FURNACE_ACTIVE      = BlockType.BurningFurnace
# DOOR_WOOD           = BlockType.WoodenDoor
# LADDER              = BlockType.Ladder
# STAIRS_COBBLESTONE  = BlockType.StoneStairs
# DOOR_IRON           = BlockType.IronDoor
# REDSTONE_ORE        = BlockType.RedstoneOre
# SNOW                = BlockType.Snow
# ICE                 = BlockType.Ice
# SNOW_BLOCK          = BlockType.SnowBlock
# CACTUS              = BlockType.Cactus
# CLAY                = BlockType.Clay
# SUGAR_CANE          = BlockType.Reed
# FENCE               = BlockType.Fence
# GLOWSTONE_BLOCK     = BlockType.GlowStone
# STONE_BRICK         = BlockType.StoneBrick
# GLASS_PANE          = BlockType.GlassPane
# MELON               = BlockType.Melon
# FENCE_GATE          = BlockType.FenceGate


def pos(*args):
    return WORLD.getLocation(*args)


def parseargswithpos(args, kwargs, asint=True, ledger={}):
    results = {}
    base = 3
    tr = [args[0], args[1], args[2]]
    if asint:
        pos = (int(tr[0]), int(tr[1]), int(tr[2]))
    results['x'] = pos[0]
    results['y'] = pos[1]
    results['z'] = pos[2]
    for k,v in ledger.iteritems():
        results[k] = kwargs.get(v[0], None)
        if results[k] is None:
            if len(args) > base+v[1]:
                results[k] = args[base+v[1]]
            else:
                results[k] = v[2]
    return results


def getplayer(name):
    return SERVER.getPlayer(name).orElse(None)


def randomplayer():
    pl = SERVER.getOnlinePlayers()
    return choice(pl)


def yell(message):
    SERVER.getBroadcastSink().sendMessage(Texts.of(message))


# seems this functionality isn't yet implemented in Sponge
# https://github.com/SpongePowered/SpongeAPI/issues/393
# def time(time):
#     WORLD.setTime(time)


def weather(rainsnow, thunder):
    if thunder:
        WORLD.forecast(Weathers.THUNDER_STORM)
    elif rainsnow:
        WORLD.forecast(Weathers.RAIN)
    else:
        WORLD.forecast(Weathers.CLEAR)


def explosion(*args, **kwargs):
    r = parseargswithpos(args, kwargs, ledger={'power':['power', 0, 8]})
    WORLD.triggerExplosion(
        Explosion.builder()
            .world(WORLD)
            .origin(Vector3d(r['x'], r['y'], r['z']))
            .radius(int(r['power']))
            .shouldBreakBlocks(True)
            .canCauseFire(True).build())


def teleport(*args, **kwargs):
    r = parseargswithpos(args, kwargs, ledger={'whom':['whom', 0, 'GameStartSchool']})
    someone = getplayer(r['whom'])
    someone.setLocation(pos(r['x'], r['y'], r['z']))


def setblock(*args, **kwargs):
    r = parseargswithpos(args, kwargs, ledger={'type':['type', 0, COBBLESTONE]})
    WORLD.setBlockType(r['x'], r['y'], r['z'], r['type'], True)


def cube(*args, **kwargs):
    r = parseargswithpos(args, kwargs, ledger={
        'type':['type', 0, COBBLESTONE],
        'size':['size', 1, 4]})
    size = min(r['size'], 12)
    for x in range(size):
        for y in range(size):
            for z in range(size):
                setblock(x + r['x'], y + r['y'], z + r['z'], r['type'])


def bolt(*args, **kwargs):
    r = parseargswithpos(args, kwargs)
    entity = WORLD.createEntity(EntityTypes.LIGHTNING, Vector3d(r['x'], r['y'], r['z']))
    if entity.isPresent():
        WORLD.spawnEntity(entity, SpawnCause.builder().type(SpawnTypes.PLUGIN).build())


def bless(*args, **kwargs):
    r = parseargswithpos(args, kwargs, ledger={
        'type':['type', 0, ParticleTypes.REDSTONE],
        'vx':['vx', 1, 1],
        'vy':['vy', 2, 1],
        'vz':['vz', 3, 1],
        'q':['q', 5, 100]})
    WORLD.spawnParticles(
        ParticleEffect.builder()
            .type(r['type'])
            .count(r['q'])
            .motion(Vector3d(r['vx'], r['vy'], r['vz']))
            .build(),
        Vector3d(r['x'], r['y'], r['z']))


def lookingat(player):
    return getattr(BlockRay, 'from')(player).filter(BlockRay.ONLY_AIR_FILTER).end().orElse(None)


def registercommand(name, execfunc):
    # Use like this:
    # >>> def functiontest(caller, params):
    # ...	 yell(params[0])
    # >>> registercommand("test", functiontest)
    spec = CommandSpec.builder()\
        .executor(execfunc)\
        .build()
    GAME.getCommandDispatcher().register(PyContext.getPlugin(), spec, name)


def registerhook(hookCls, execfunc):
    # Use like this:
    # >>> from mcapi import *
    # >>> from org.spongepowered.api.event.block import ChangeBlockEven
    # >>> def place(e):
    # ...    yell("Placed {}".format(e.getBlockPlaced()))
    # >>> registerhook(ChangeBlockEvent.Break, place)
    GAME.getEventManager().registerListenr(PyContext.getPlugin(), hookCls, execfunc)
