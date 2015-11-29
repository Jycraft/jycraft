print('Importing command definitions...')

from net.canarymod import Canary
from net.canarymod import LineTracer
from net.canarymod.api.world.blocks import BlockType
from net.canarymod.api.world.effects import Particle
from net.canarymod.api import GameMode
from net.canarymod.api.world.position import Location
from net.canarymod.api.world.position import Position
from net.canarymod.commandsys import Command, CommandListener, CanaryCommand
from net.canarymod.chat import MessageReceiver
from net.canarymod.plugin import Priority, PluginListener
from net.canarymod.hook import Dispatcher

from time import *
from random import *
from math import *

SERVER 	= Canary.getServer()
WORLD 	= SERVER.getDefaultWorld()
MORNING = 2000
NOON 	= 6000
EVENING = 14000
NIGHT 	= 18000

#full list of BlockTypes available in JavaDocs on canarymod.net
AIR                 = BlockType.Air
STONE               = BlockType.Stone
GRASS               = BlockType.Grass
DIRT                = BlockType.Dirt
COBBLESTONE         = BlockType.Cobble
WOOD_PLANKS         = BlockType.OakWood
SAPLING             = BlockType.OakSapling
BEDROCK             = BlockType.Bedrock
WATER_FLOWING       = BlockType.WaterFlowing
WATER               = WATER_FLOWING
WATER_STATIONARY    = BlockType.Water
LAVA_FLOWING        = BlockType.LavaFlowing
LAVA                = LAVA_FLOWING
LAVA_STATIONARY     = BlockType.Lava
SAND                = BlockType.Sand
GRAVEL              = BlockType.Gravel
GOLD_ORE            = BlockType.GoldOre
IRON_ORE            = BlockType.IronOre
COAL_ORE            = BlockType.CoalOre
WOOD                = BlockType.OakLog
LEAVES              = BlockType.OakLeaves
GLASS               = BlockType.Glass
LAPIS_LAZULI_ORE    = BlockType.LapisOre
LAPIS_LAZULI_BLOCK  = BlockType.LapisBlock
SANDSTONE           = BlockType.Sandstone
BED                 = BlockType.Bed
COBWEB              = BlockType.Web
GRASS_TALL          = BlockType.TallGrass
WOOL                = BlockType.WhiteWool
FLOWER_YELLOW       = BlockType.Dandelion
FLOWER_CYAN         = BlockType.BlueOrchid
MUSHROOM_BROWN      = BlockType.BrownMushroom
MUSHROOM_RED        = BlockType.RedMushroom
GOLD_BLOCK          = BlockType.GoldBlock
IRON_BLOCK          = BlockType.IronBlock
STONE_SLAB_DOUBLE   = BlockType.DoubleStoneSlab
STONE_SLAB          = BlockType.StoneSlab
BRICK_BLOCK         = BlockType.BrickBlock
TNT                 = BlockType.TNT
BOOKSHELF           = BlockType.Bookshelf
MOSS_STONE          = BlockType.MossyCobble
OBSIDIAN            = BlockType.Obsidian
TORCH               = BlockType.Torch
FIRE                = BlockType.FireBlock
STAIRS_WOOD         = BlockType.OakStairs
CHEST               = BlockType.Chest
DIAMOND_ORE         = BlockType.DiamondOre
DIAMOND_BLOCK       = BlockType.DiamondBlock
CRAFTING_TABLE      = BlockType.Workbench
FARMLAND            = BlockType.Farmland
FURNACE_INACTIVE    = BlockType.Furnace
FURNACE_ACTIVE      = BlockType.BurningFurnace
DOOR_WOOD           = BlockType.WoodenDoor
LADDER              = BlockType.Ladder
STAIRS_COBBLESTONE  = BlockType.StoneStairs
DOOR_IRON           = BlockType.IronDoor
REDSTONE_ORE        = BlockType.RedstoneOre
SNOW                = BlockType.Snow
ICE                 = BlockType.Ice
SNOW_BLOCK          = BlockType.SnowBlock
CACTUS              = BlockType.Cactus
CLAY                = BlockType.Clay
SUGAR_CANE          = BlockType.Reed
FENCE               = BlockType.Fence
GLOWSTONE_BLOCK     = BlockType.GlowStone
STONE_BRICK         = BlockType.StoneBrick
GLASS_PANE          = BlockType.GlassPane
MELON               = BlockType.Melon
FENCE_GATE          = BlockType.FenceGate

def pos(positionable):
	return positionable.getPosition()

def parseargswithpos(args, kwargs, asint=True, ledger={}):
	results = {}
	if isinstance(args[0], Position):
		base = 1
		if asint:
			pos = (args[0].getBlockX(), args[0].getBlockY(), args[0].getBlockZ())
		else:
			pos = (args[0].getX(), args[0].getY(), args[0].getZ())
	else:
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
	pl = SERVER.getPlayerNameList()
	return getplayer(choice(pl))

def yell(message):
	SERVER.broadcastMessage(message)

def time(time):
	WORLD.setTime(time)

def weather(rainsnow, thunder):
	WORLD.setRaining(rainsnow)
	WORLD.setthundering(thunder)

def explosion(*args, **kwargs):
	r = parseargswithpos(args, kwargs, ledger={'power':['power', 0, 8]})
	WORLD.makeExplosion(None, r['x'], r['y'], r['z'], r['power'], True)

def teleport(*args, **kwargs):
	r = parseargswithpos(args, kwargs, ledger={'whom':['whom', 0, 'GameStartSchool']})
	someone = getplayer(r['whom'])
	someone.teleportTo(r['x'], r['y'], r['z'])

def setblock(*args, **kwargs):
	r = parseargswithpos(args, kwargs, ledger={'type':['type', 0, BlockType.Cobble]})
	WORLD.setBlockAt(r['x'], r['y'], r['z'], r['type'])

def cube(*args, **kwargs):
	r = parseargswithpos(args, kwargs, ledger={
		'type':['type', 0, BlockType.Cobble], 
		'size':['size', 1, 4]})
	size = min(r['size'], 12)
	for x in range(size):
		for y in range(size):
			for z in range(size):
				setblock(x + r['x'], y + r['y'], z + r['z'], r['type'])

def bolt(*args, **kwargs):
	r = parseargswithpos(args, kwargs)
	WORLD.makeLightningBolt(r['x'], r['y'], r['z'])

def bless(*args, **kwargs):
	r = parseargswithpos(args, kwargs, ledger={
		'type':['type', 0, Particle.Type.REDSTONE], 
		'vx':['vx', 1, 1],
		'vy':['vy', 2, 1],
		'vz':['vz', 3, 1],
		'sp':['sp', 4, 100],
		'q':['q', 5, 100]})
	par = Particle(r['x'], r['y'], r['z'], r['vx'], r['vy'], r['vz'], r['sp'], r['q'], r['type'])
	WORLD.spawnParticle(par)

def lookingat(player):
	return LineTracer(player).getTargetBlock()

class ChatCommand(Command):
	def __init__(self, names, min=2, max=2, permissions=[""], toolTip="", description="", parent="", helpLookup="", searchTerms=[], version=1):
		self.var_names = names
		self.var_min = min
		self.var_max = max
		self.var_permissions = permissions
		self.var_toolTip = toolTip
		self.var_description = description
		self.var_parent = parent
		self.var_helpLookup = helpLookup
		self.var_searchTerms = searchTerms
		self.var_version = version
	def aliases(self): return self.var_names
	def permissions(self): return self.var_permissions
	def toolTip(self): return self.var_toolTip
	def description(self): return self.var_description
	def parent(self): return self.var_parent
	def helpLookup(self): return self.var_helpLookup
	def min(self): return self.var_min
	def max(self): return self.var_max
	def searchTerms(self): return self.var_searchTerms
	def version(self): return self.var_version

class CanaryChatCommand(CanaryCommand):
	def __init__(self, chatcommand, owner, execfunc):
		super(CanaryCommand, self).__init__(chatcommand, owner, None)
		self.execfunc = execfunc
	def execute(self, caller, parameters):
		self.execfunc(caller, parameters)

class PluginEventListener(PluginListener):
	pass

class EventDispatcher(Dispatcher):
	def __init__(self, execfunc):
		self.execfunc = execfunc
	def execute(self, listener, hook):
		self.execfunc(listener, hook)

def registercommand(name, min, max, execfunc):
	# Use like this:
	# >>> def functiontest(caller, params):
	# ...	 yell(params[1])
	# >>> registercommand("test", 2, 2, functiontest)
	Canary.commands().registerCommand(CanaryChatCommand(ChatCommand(names=[name], min=min, max=max), SERVER, execfunc), SERVER, True)

def registerhook(hookCls, execfunc):
	# Use like this:
	# >>> from net.canarymod.hook.player import BlockDestroyHook
	# >>> def hookfunc(listener, hook):
	# ...	 yell(str(hook.getBlock().getType()))
	# >>> registerhook(BlockDestroyHook,hookfunc)
	Canary.hooks().registerHook(PluginEventListener(), Canary.manager().getPlugin('CanaryConsole'), hookCls, EventDispatcher(execfunc), Priority.NORMAL)
