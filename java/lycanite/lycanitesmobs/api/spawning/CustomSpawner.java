package lycanite.lycanitesmobs.api.spawning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lycanite.lycanitesmobs.LycanitesMobs;
import lycanite.lycanitesmobs.api.info.ObjectLists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockVine;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.EnumStatus;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

public class CustomSpawner {
    public static CustomSpawner instance;

    // ==================================================
    //                     Constructor
    // ==================================================
	public CustomSpawner() {
		instance = this;
	}
	
	
	// ==================================================
	//                 Entity Update Event
	// ==================================================
    public List<SpawnTypeBase> updateSpawnTypes = new ArrayList<SpawnTypeBase>();
	public Map<EntityPlayer, Long> entityUpdateTicks = new HashMap<EntityPlayer, Long>();
	
    /*public List<SpawnTypeBase> shadowSpawnTypes = new ArrayList<SpawnTypeBase>();
	public Map<EntityPlayer, ChunkCoordinates> entityLightCoords = new HashMap<EntityPlayer, ChunkCoordinates>();
	public Map<EntityPlayer, int[][]> entityLightLevel = new HashMap<EntityPlayer, int[][]>();*/
	
	/** This uses the player update events to spawn mobs around each player randomly over time. **/
	@SubscribeEvent
	public void onEntityUpdate(LivingUpdateEvent event) {
		EntityLivingBase entity = event.entityLiving;
		if(entity == null || !(entity instanceof EntityPlayer) || entity.worldObj == null || entity.worldObj.isRemote || event.isCanceled())
			return;
		
		// ========== Spawn Near Players ==========
		EntityPlayer player = (EntityPlayer)entity;
		World world = player.worldObj;
		int x = (int)player.posX;
		int y = (int)player.posY;
		int z = (int)player.posZ;
		
		if(!entityUpdateTicks.containsKey(player))
			entityUpdateTicks.put(player, (long)0);
		long entityUpdateTick = entityUpdateTicks.get(player);
		
		// Custom Mob Spawning:
		int tickOffset = 0;
		for(SpawnTypeBase spawnType : this.updateSpawnTypes) {
			spawnType.spawnMobs(entityUpdateTick - tickOffset, world, x, y, z, player);
			tickOffset += 105;
		}
		
		/*/ ========== Spawn On Sudden Light to Dark ==========
		if(!player.capabilities.isCreativeMode && entityUpdateTick % 4 == 0) {
			ChunkCoordinates coordsPrev = null;
			int checkRange = 6;
			if(this.entityLightCoords.containsKey(player))
				coordsPrev = this.entityLightCoords.get(player);
			if(!this.entityLightLevel.containsKey(player))
				this.entityLightLevel.put(player, new int[3][3]);
			
			boolean mobSpawned = false;
			for(int xSection = -1; xSection <= 1; xSection++) {
				for(int zSection = -1; zSection <= 1; zSection++) {
					
					// Check Light Level Change:
					if(!mobSpawned && coordsPrev != null) {
						int xOffset = coordsPrev.posX + (checkRange * xSection);
						int zOffset = coordsPrev.posZ + (checkRange * zSection);
						
						int lightLevelPrev = entityLightLevel.get(player)[xSection + 1][zSection + 1];
						//boolean solidBlocks = lightLevelPrev < 0 || !world.isAirBlock(xOffset, coordsPrev.posY, zOffset);
						boolean solidBlocks = lightLevelPrev < 0 || world.isSideSolid(xOffset, coordsPrev.posY, zOffset, ForgeDirection.DOWN, true);
						if(!solidBlocks && lightLevelPrev >= 10 && world.getBlockLightValue(xOffset, coordsPrev.posY, zOffset) <= 5) {
							for(SpawnTypeBase spawnType : this.shadowSpawnTypes) {
								spawnType.spawnMobs(entityUpdateTick, world, xOffset, coordsPrev.posY, zOffset);
							}
						}
					}
					
					// Set Next Coord and Light Level:
					ChunkCoordinates coordsCurrent = player.getPlayerCoordinates();
					int lightLevelCurrent = world.getBlockLightValue(coordsCurrent.posX + (checkRange * xSection), coordsCurrent.posY, coordsCurrent.posZ + (checkRange * zSection));
					if(world.isSideSolid(coordsCurrent.posX + (checkRange * xSection), coordsCurrent.posY, coordsCurrent.posZ + (checkRange * zSection), ForgeDirection.DOWN, true))
					//if(!world.isAirBlock(coordsCurrent.posX + (checkRange * xSection), coordsCurrent.posY, coordsCurrent.posZ + (checkRange * zSection)))
							lightLevelCurrent = -10;
					this.entityLightLevel.get(player)[xSection + 1][zSection + 1] = lightLevelCurrent;
				}
			}
			
			this.entityLightCoords.put(player, player.getPlayerCoordinates());
		}*/
		
		entityUpdateTicks.put(player, entityUpdateTick + 1);
	}
	
	
	// ==================================================
	//                 Entity Death Event
	// ==================================================
    public List<SpawnTypeDeath> deathSpawnTypes = new ArrayList<SpawnTypeDeath>();
	
	/** This uses the entity death events to spawn mobs when other mobs/players die. **/
	@SubscribeEvent
	public void onEntityDeath(LivingDeathEvent event) {
		EntityLivingBase entity = event.entityLiving;
		if(entity == null || entity.worldObj == null || entity.worldObj.isRemote || event.isCanceled())
			return;
		
		// ========== Get Killer ==========
		Entity killerEntity = event.source.getSourceOfDamage();
		if(!(killerEntity instanceof EntityLivingBase))
			return;
		EntityLivingBase killer = (EntityLivingBase)killerEntity;
		if(!(killer instanceof EntityPlayer) && !(entity instanceof EntityPlayer))
			return;
		
		// ========== Get Coords ==========
		World world = entity.worldObj;
		int x = (int)entity.posX;
		int y = (int)entity.posY;
		int z = (int)entity.posZ;
		
		// ========== Pass To Spawners ==========
		for(SpawnTypeDeath spawnType : this.deathSpawnTypes) {
			if(spawnType.isValidKill(entity, killer))
				spawnType.spawnMobs(0, world, x, y, z, null);
		}
	}

	
	// ==================================================
	//                 Harvest Drops Event
	// ==================================================
    public List<SpawnTypeBlockBreak> blockSpawnTypes = new ArrayList<SpawnTypeBlockBreak>();
	/** This uses the block harvest drops events to spawn mobs around blocks when they are destroyed. **/
	@SubscribeEvent
	public void onHarvestDrops(HarvestDropsEvent event) {
		EntityPlayer player = event.harvester;
		if(event.block == null || event.world == null || event.world.isRemote || event.isCanceled())
			return;
		if(player != null && player.capabilities.isCreativeMode) // No Spawning for Creative Players
			return;
		
		// Spawn On Block Harvest:
		World world = event.world;
		int x = (int)event.x;
		int y = (int)event.y;
		int z = (int)event.z + 1;

        for(SpawnTypeBlockBreak spawnType : this.blockSpawnTypes) {
            if(spawnType.validBlockHarvest(event.block, world, x, y, z, player))
                spawnType.spawnMobs(0, world, x, y, z, player, event.block);
        }
	}


    // ==================================================
    //                 Break Block Event
    // ==================================================
    /** This uses the block break events to spawn mobs around blocks when they are destroyed. **/
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        EntityPlayer player = event.getPlayer();
        if(event.block == null || event.world == null || event.world.isRemote || event.isCanceled())
            return;
        if(player == null || (player != null && player.capabilities.isCreativeMode)) // No Spawning for Creative Players
            return;

        // Spawn On Block Harvest:
        World world = event.world;
        int x = (int)event.x;
        int y = (int)event.y;
        int z = (int)event.z + 1;

        for(SpawnTypeBlockBreak spawnType : this.blockSpawnTypes) {
            if(spawnType.validBlockBreak(event.block, world, x, y, z, player))
                spawnType.spawnMobs(0, world, x, y, z, player);
        }
    }

	
	// ==================================================
	//                Player Use Bed Event
	// ==================================================
    public List<SpawnTypeSleep> sleepSpawnTypes = new ArrayList<SpawnTypeSleep>();
	/** This uses the player sleep in bed event to spawn mobs. **/
	@SubscribeEvent
	public void onSleep(PlayerSleepInBedEvent event) {
		EntityPlayer player = event.entityPlayer; // Only fire when used by a player.
		if(player == null || event.isCanceled())
			return;
		
		// Get Coords:
		World world = player.worldObj;
		int x = (int)event.x;
		int y = (int)event.y;
		int z = (int)event.z + 1;
		
		if(world == null || world.isRemote || world.provider.isDaytime())
			return;
		
		// Run Spawners:
		boolean interrupted = false;
		for(SpawnTypeBase spawnType : this.sleepSpawnTypes) {
			if(spawnType.spawnMobs(0, world, x, y, z, player))
				interrupted = true;
		}
		
		// Possible Interrupt:
		if(interrupted)
			event.result = EnumStatus.NOT_SAFE;
	}

	
	// ==================================================
	//              Lightning Strike Event
	// ==================================================
    public List<SpawnTypeStorm> lightningStrikeTypes = new ArrayList<SpawnTypeStorm>();
	/** This uses the lightning strike event to spawn mobs. **/
	public void onLightningStrike() {
		//TODO Lightning strike detection.
	}
}
