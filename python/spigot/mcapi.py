print('Importing command definitions...')

from jycraft.plugin.interpreter import PyContext

from org.bukkit import Bukkit
from org.bukkit import Location
from org.bukkit import Material
from org.bukkit import Effect
from org.bukkit.command import Command
from org.bukkit.event import Listener, EventPriority

from random import *

SERVER 	= Bukkit.getServer()
WORLD 	= SERVER.getWorlds().get(0)
MORNING = 2000
NOON 	= 6000
EVENING = 14000
NIGHT 	= 18000

# reflection to get command map
_commandMapField = SERVER.getClass().getDeclaredField("commandMap")
_commandMapField.setAccessible(True)
_commandMap = _commandMapField.get(SERVER)

#full list of BlockTypes available in JavaDocs on canarymod.net
AIR                 = Material.AIR
STONE               = Material.STONE
GRASS               = Material.GRASS
DIRT                = Material.DIRT
COBBLESTONE         = Material.COBBLESTONE
WOOD_PLANKS         = Material.WOOD
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
    return Location(WORLD, *args)


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
    return SERVER.getPlayer(name)


def randomplayer():
    pl = SERVER.getOnlinePlayers()
    return choice(pl)


def yell(message):
    SERVER.broadcastMessage(message)


def time(time):
    WORLD.setTime(time)


def weather(rainsnow, thunder):
    WORLD.setStorm(rainsnow)
    WORLD.setThundering(thunder)


def explosion(*args, **kwargs):
    r = parseargswithpos(args, kwargs, ledger={'power':['power', 0, 8]})
    WORLD.createExplosion(r['x'], r['y'], r['z'], r['power'], True)


def teleport(*args, **kwargs):
    r = parseargswithpos(args, kwargs, ledger={'whom':['whom', 0, 'GameStartSchool']})
    someone = getplayer(r['whom'])
    someone.teleport(pos(r['x'], r['y'], r['z']))


def setblock(*args, **kwargs):
    r = parseargswithpos(args, kwargs, ledger={'type':['type', 0, COBBLESTONE]})
    WORLD.getBlockAt(r['x'], r['y'], r['z']).setType(r['type'])


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
    WORLD.strikeLightning(pos(r['x'], r['y'], r['z']))


def bless(*args, **kwargs):
    r = parseargswithpos(args, kwargs, ledger={
        'type':['type', 0, Effect.COLOURED_DUST],
        'vx':['vx', 1, 1],
        'vy':['vy', 2, 1],
        'vz':['vz', 3, 1],
        'sp':['sp', 4, 100],
        'q':['q', 5, 100],
        'r':['r', 6, 20],
        'block':['block', 7, COBBLESTONE],
        'data':['data', 8, 0]})
    WORLD.spigot().playEffect(pos(r['x'], r['y'], r['z']),
                              r['type'], r['block'].getId(),
                              r['data'], r['vx'], r['vy'], r['vz'],
                              r['sp'], r['q'], r['r'])

# don't know how to do this in spigot
# def lookingat(player):
#     return LineTracer(player).getTargetBlock()


class SpigotCommand(Command):
    def __init__(self, name, execfunc):
        super(SpigotCommand, self).__init__(name)
        self.execfunc = execfunc

    def execute(self, caller, label, parameters):
        self.execfunc(caller, parameters)


def registercommand(name, execfunc):
    # Use like this:
    # >>> def functiontest(caller, params):
    # ...	 yell(params[0])
    # >>> registercommand("test", functiontest)
    _commandMap.register("jycraft", SpigotCommand(name, execfunc))


class EventListener(Listener):
    def __init__(self, func):
        self.func = func

    def execute(self, event):
        self.func(event)


def execute(listener, event):
    listener.execute(event)


def registerhook(hookCls, execfunc, priority=EventPriority.NORMAL):
    # Use like this:
    # >>> from mcapi import *
    # >>> from org.bukkit.event.block import BlockPlaceEvent
    # >>> def place(e):
    # ...    yell("Placed {}".format(e.getBlockPlaced()))
    # >>> registerhook(BlockPlaceEvent, place)
    SERVER.getPluginManager().registerEvent(hookCls, EventListener(execfunc), priority, execute, PyContext.getPlugin())
