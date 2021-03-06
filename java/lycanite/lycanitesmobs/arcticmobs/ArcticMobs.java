package lycanite.lycanitesmobs.arcticmobs;

import lycanite.lycanitesmobs.AssetManager;
import lycanite.lycanitesmobs.LycanitesMobs;
import lycanite.lycanitesmobs.ObjectManager;
import lycanite.lycanitesmobs.api.config.ConfigBase;
import lycanite.lycanitesmobs.api.dispenser.DispenserBehaviorMobEggCustom;
import lycanite.lycanitesmobs.api.info.*;
import lycanite.lycanitesmobs.api.item.ItemCustomFood;
import lycanite.lycanitesmobs.api.item.ItemTreat;
import lycanite.lycanitesmobs.api.mobevent.MobEventBase;
import lycanite.lycanitesmobs.api.mobevent.MobEventManager;
import lycanite.lycanitesmobs.api.spawning.SpawnTypeBase;
import lycanite.lycanitesmobs.api.spawning.SpawnTypeLand;
import lycanite.lycanitesmobs.api.spawning.SpawnTypeSky;
import lycanite.lycanitesmobs.arcticmobs.block.*;
import lycanite.lycanitesmobs.arcticmobs.dispenser.*;
import lycanite.lycanitesmobs.arcticmobs.entity.*;
import lycanite.lycanitesmobs.arcticmobs.item.*;
import lycanite.lycanitesmobs.arcticmobs.mobevent.MobEventSubZero;
import lycanite.lycanitesmobs.arcticmobs.mobevent.MobEventWintersGrasp;
import lycanite.lycanitesmobs.arcticmobs.worldgen.WorldGeneratorArctic;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = ArcticMobs.modid, name = ArcticMobs.name, version = LycanitesMobs.version, dependencies = "required-after:" + LycanitesMobs.modid)
public class ArcticMobs {
	
	public static final String modid = "arcticmobs";
	public static final String name = "Lycanites Arctic Mobs";
	public static GroupInfo group;
	
	// Instance:
	@Instance(modid)
	public static ArcticMobs instance;
	
	// Proxy:
	@SidedProxy(clientSide="lycanite.lycanitesmobs.arcticmobs.ClientSubProxy", serverSide="lycanite.lycanitesmobs.arcticmobs.CommonSubProxy")
	public static CommonSubProxy proxy;
	
	// ==================================================
	//                Pre-Initialization
	// ==================================================
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		// ========== Config ==========
		group = new GroupInfo(this, "Arctic Mobs", 6)
				.setDimensionBlacklist("-1,1").setBiomes("COLD, SNOWY, CONIFEROUS, -END").setDungeonThemes("FROZEN, DUNGEON")
                .setEggName("arcticegg");
		group.loadFromConfig();

		// ========== Set Current Group ==========
		ObjectManager.setCurrentGroup(group);

        // ========== Create Fluids ==========
        AssetManager.addSound("ooze", group, "block.ooze");
        ObjectManager.addDamageSource("ooze", new DamageSource("ooze"));
        Fluid fluid = ObjectManager.addFluid(new Fluid("ooze"));
        fluid.setLuminosity(10).setDensity(3000).setViscosity(5000).setTemperature(-1000);
        ObjectManager.addBlock("ooze", new BlockFluidOoze(fluid));
		
		// ========== Create Items ==========
		ObjectManager.addItem("arcticegg", new ItemArcticEgg());
		
		ObjectManager.addItem("yetimeatraw", new ItemCustomFood("yetimeatraw", group, 2, 0.5F, ItemCustomFood.FOOD_CLASS.RAW).setPotionEffect(Potion.moveSlowdown.id, 45, 2, 0.8F));
		ObjectLists.addItem("rawmeat", ObjectManager.getItem("yetimeatraw"));
		OreDictionary.registerOre("listAllporkraw", ObjectManager.getItem("yetimeatraw"));
		
		ObjectManager.addItem("yetimeatcooked", new ItemCustomFood("yetimeatcooked", group, 6, 0.7F, ItemCustomFood.FOOD_CLASS.COOKED).setPotionEffect(Potion.resistance.id, 10, 2, 1.0F).setAlwaysEdible());
		ObjectLists.addItem("cookedmeat", ObjectManager.getItem("yetimeatcooked"));
		OreDictionary.registerOre("listAllporkcooked", ObjectManager.getItem("yetimeatcooked"));
		
		ObjectManager.addItem("palesoup", new ItemCustomFood("palesoup", group, 6, 0.7F, ItemCustomFood.FOOD_CLASS.MEAL).setPotionEffect(Potion.resistance.id, 60, 2, 1.0F).setAlwaysEdible().setMaxStackSize(16), 3, 1, 6);
		ObjectLists.addItem("cookedmeat", ObjectManager.getItem("palesoup"));

		ObjectManager.addItem("frostyfur", new ItemFrostyFur());
		
		ObjectManager.addItem("frostboltcharge", new ItemFrostboltCharge());
		ObjectManager.addItem("frostboltscepter", new ItemScepterFrostbolt(), 2, 1, 1);
		ObjectManager.addItem("frostwebcharge", new ItemFrostwebCharge());
		ObjectManager.addItem("frostwebscepter", new ItemScepterFrostweb(), 2, 1, 1);
		ObjectManager.addItem("tundracharge", new ItemTundraCharge());
		ObjectManager.addItem("tundrascepter", new ItemScepterTundra(), 2, 1, 1);
        ObjectManager.addItem("icefirecharge", new ItemIcefireCharge());
        ObjectManager.addItem("icefirescepter", new ItemScepterIcefire(), 2, 1, 1);
        ObjectManager.addItem("blizzardcharge", new ItemBlizzardCharge());
        ObjectManager.addItem("blizzardscepter", new ItemScepterBlizzard(), 2, 1, 1);

		ObjectManager.addItem("arixtreat", new ItemTreat("arixtreat", group));
        ObjectManager.addItem("serpixtreat", new ItemTreat("serpixtreat", group));

        ObjectManager.addItem("bucketooze", new ItemBucketOoze(fluid).setContainerItem(Items.bucket));

        // ========== Create Blocks ==========
        ObjectManager.addBlock("frostweb", new BlockFrostweb());

        AssetManager.addSound("frostcloud", group, "block.frostcloud");
        ObjectManager.addBlock("frostcloud", new BlockFrostCloud());

        AssetManager.addSound("frostfire", group, "block.frostfire");
        ObjectManager.addBlock("frostfire", new BlockFrostfire());

        AssetManager.addSound("icefire", group, "block.icefire");
        ObjectManager.addBlock("icefire", new BlockIcefire());
		
		// ========== Create Mobs ==========
		BlockDispenser.dispenseBehaviorRegistry.putObject(ObjectManager.getItem("arcticegg"), new DispenserBehaviorMobEggCustom());
        MobInfo newMob;
        
        newMob = new MobInfo(group, "reiver", EntityReiver.class, 0xDDEEFF, 0x99DDEE)
                .setPeaceful(false).setSummonable(true).setSummonCost(2).setDungeonLevel(0)
		        .addSubspecies(new Subspecies("ashen", "uncommon")).addSubspecies(new Subspecies("golden", "uncommon"));
        newMob.spawnInfo.setSpawnTypes("FROSTFIRE, SKY").setBlockCost(8)
        		.setSpawnWeight(8).setAreaLimit(3).setGroupLimits(1, 3).setLightDark(false, true);
        ObjectManager.addMob(newMob);
        
        newMob = new MobInfo(group, "frostweaver", EntityFrostweaver.class, 0xAADDFF, 0x226699)
		        .setPeaceful(false).setSummonable(true).setSummonCost(2).setDungeonLevel(1)
		        .addSubspecies(new Subspecies("light", "uncommon")).addSubspecies(new Subspecies("azure", "uncommon"));
		newMob.spawnInfo.setSpawnTypes("MONSTER")
				.setSpawnWeight(10).setAreaLimit(5).setGroupLimits(1, 2).setLightDark(false, true);
		ObjectManager.addMob(newMob);
		
        newMob = new MobInfo(group, "yeti", EntityYeti.class, 0xEEEEFF, 0x000099)
		        .setPeaceful(true).setSummonCost(2).setDungeonLevel(-1)
		        .addSubspecies(new Subspecies("russet", "uncommon")).addSubspecies(new Subspecies("violet", "uncommon"));
		newMob.spawnInfo.setSpawnTypes("CREATURE").setDespawn(false)
				.setSpawnWeight(10).setAreaLimit(5).setGroupLimits(1, 4).setLightDark(true, false).setDungeonWeight(0);
		ObjectManager.addMob(newMob);
		
        newMob = new MobInfo(group, "wendigo", EntityWendigo.class, 0xCCCCFF, 0x0055FF)
		        .setPeaceful(false).setSummonCost(8).setDungeonLevel(2)
		        .addSubspecies(new Subspecies("keppel", "uncommon")).addSubspecies(new Subspecies("violet", "uncommon"));
		newMob.spawnInfo.setSpawnTypes("MONSTER, OOZE")
				.setSpawnWeight(4).setAreaLimit(1).setGroupLimits(1, 1).setLightDark(false, true);
		ObjectManager.addMob(newMob);

        newMob = new MobInfo(group, "arix", EntityArix.class, 0xDDDDFF, 0x9999FF)
                .setPeaceful(false).setTameable(true).setSummonCost(2).setDungeonLevel(1)
		        .addSubspecies(new Subspecies("azure", "uncommon")).addSubspecies(new Subspecies("violet", "uncommon"));
        newMob.spawnInfo.setSpawnTypes("SKY, OOZE")
                .setSpawnWeight(8).setAreaLimit(3).setGroupLimits(1, 3).setLightDark(false, true);
        ObjectManager.addMob(newMob);

        newMob = new MobInfo(group, "serpix", EntitySerpix.class, 0xCCEEFF, 0x0000BB)
                .setPeaceful(false).setTameable(true).setSummonCost(8).setDungeonLevel(2)
                .addSubspecies(new Subspecies("azure", "uncommon")).addSubspecies(new Subspecies("verdant", "uncommon"));
        newMob.spawnInfo.setSpawnTypes("MONSTER, OOZE")
                .setSpawnWeight(4).setAreaLimit(1).setGroupLimits(1, 1).setLightDark(false, true);
        ObjectManager.addMob(newMob);
		
		// ========== Create Projectiles ==========
		ObjectManager.addProjectile("frostbolt", EntityFrostbolt.class, ObjectManager.getItem("frostboltcharge"), new DispenserBehaviorFrostbolt());
		ObjectManager.addProjectile("frostweb", EntityFrostweb.class, ObjectManager.getItem("frostwebcharge"), new DispenserBehaviorFrostweb());
        ObjectManager.addProjectile("tundra", EntityTundra.class, ObjectManager.getItem("tundracharge"), new DispenserBehaviorTundra());
        ObjectManager.addProjectile("icefireball", EntityIcefireball.class, ObjectManager.getItem("icefirecharge"), new DispenserBehaviorIcefire());
        ObjectManager.addProjectile("blizzard", EntityBlizzard.class, ObjectManager.getItem("blizzard"), new DispenserBehaviorBlizzard());


        // ========== Register Models ==========
		proxy.registerModels();
	}
	
	
	// ==================================================
	//                Initialization
	// ==================================================
	@EventHandler
	public void load(FMLInitializationEvent event) {
		
	}
	
	
	// ==================================================
	//                Post-Initialization
	// ==================================================
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		// ========== Set Current Group ==========
		ObjectManager.setCurrentGroup(group);
		ConfigBase config = ConfigBase.getConfig(group, "spawning");

        // ========== World Generation ==========
        GameRegistry.registerWorldGenerator(new WorldGeneratorArctic(), 0);
		
		// ========== Mob Events ==========
        MobEventBase mobEvent = new MobEventSubZero("subzero", this.group);
        SpawnTypeBase eventSpawner = new SpawnTypeSky("subzero")
            .setChance(1.0D).setBlockLimit(32).setMobLimit(3);
        eventSpawner.materials = new Material[] {Material.air};
        eventSpawner.ignoreBiome = true;
        eventSpawner.ignoreLight = true;
        eventSpawner.forceSpawning = true;
        eventSpawner.ignoreMobConditions = true;
        eventSpawner.addSpawn(MobInfo.getFromName("reiver"));
        mobEvent.addSpawner(eventSpawner);
        MobEventManager.instance.addWorldEvent(mobEvent);

        mobEvent = new MobEventWintersGrasp("wintersgrasp", this.group);
        mobEvent.minDay = 10;
        eventSpawner = new SpawnTypeLand("wintersgrasp")
                .setChance(1.0D).setBlockLimit(32).setMobLimit(3);
        eventSpawner.materials = new Material[] {Material.air};
        eventSpawner.ignoreBiome = true;
        eventSpawner.ignoreLight = true;
        eventSpawner.forceSpawning = true;
        eventSpawner.ignoreMobConditions = true;
        eventSpawner.addSpawn(MobInfo.getFromName("wendigo"));
        eventSpawner.addSpawn(MobInfo.getFromName("serpix"), 2);
        mobEvent.addSpawner(eventSpawner);
        MobEventManager.instance.addWorldEvent(mobEvent);
		
		// ========== Remove Vanilla Spawns ==========
		BiomeGenBase[] biomes = group.biomes;
		if(group.controlVanillaSpawns) {
			EntityRegistry.removeSpawn(EntityZombie.class, EnumCreatureType.monster, biomes);
			EntityRegistry.removeSpawn(EntityCreeper.class, EnumCreatureType.monster, biomes);
			EntityRegistry.removeSpawn(EntitySpider.class, EnumCreatureType.monster, biomes);
			EntityRegistry.removeSpawn(EntityWitch.class, EnumCreatureType.monster, biomes);
		}
		
		// ========== Crafting ==========
		GameRegistry.addRecipe(new ShapelessOreRecipe(
				new ItemStack(ObjectManager.getItem("palesoup"), 1, 0),
				new Object[] {
					Items.milk_bucket.setContainerItem(Items.bucket),
					Items.bowl,
					ObjectManager.getItem("yetimeatcooked")
				}
			));
		GameRegistry.addRecipe(new ShapelessOreRecipe(
				new ItemStack(ObjectManager.getItem("yetimeatcooked"), 1, 0),
				new Object[] { ObjectManager.getItem("palesoup") }
			));

        if(ItemInfo.enableWeaponRecipes) {
            GameRegistry.addRecipe(new ShapedOreRecipe(
                    new ItemStack(ObjectManager.getItem("frostboltscepter"), 1, 0),
                    new Object[]{"CCC", "CRC", "CRC",
                            Character.valueOf('C'), ObjectManager.getItem("frostboltcharge"),
                            Character.valueOf('R'), Items.blaze_rod
                    }));

            GameRegistry.addRecipe(new ShapedOreRecipe(
                    new ItemStack(ObjectManager.getItem("frostwebscepter"), 1, 0),
                    new Object[]{"CCC", "CRC", "CRC",
                            Character.valueOf('C'), ObjectManager.getItem("frostwebcharge"),
                            Character.valueOf('R'), Items.blaze_rod
                    }));

            GameRegistry.addRecipe(new ShapedOreRecipe(
                    new ItemStack(ObjectManager.getItem("tundrascepter"), 1, 0),
                    new Object[]{"SCS", "SRS", "SRS",
                            Character.valueOf('C'), ObjectManager.getItem("tundracharge"),
                            Character.valueOf('S'), ObjectManager.getItem("frostyfur"),
                            Character.valueOf('R'), Items.blaze_rod
                    }));

            GameRegistry.addRecipe(new ShapedOreRecipe(
                    new ItemStack(ObjectManager.getItem("icefirescepter"), 1, 0),
                    new Object[]{"CCC", "CRC", "CRC",
                            Character.valueOf('C'), ObjectManager.getItem("icefirecharge"),
                            Character.valueOf('R'), Items.blaze_rod
                    }));

            GameRegistry.addRecipe(new ShapedOreRecipe(
                    new ItemStack(ObjectManager.getItem("blizzardscepter"), 1, 0),
                    new Object[]{"CCC", "CRC", "CRC",
                            Character.valueOf('C'), ObjectManager.getItem("blizzardcharge"),
                            Character.valueOf('R'), Items.blaze_rod
                    }));
        }
		
		GameRegistry.addRecipe(new ShapedOreRecipe(
				new ItemStack(ObjectManager.getItem("arixtreat"), 1, 0),
				new Object[] { "TTT", "BBT", "TTT",
				Character.valueOf('T'), Blocks.packed_ice,
				Character.valueOf('B'), Items.bone
			}));

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ObjectManager.getItem("serpixtreat"), 1, 0),
                new Object[] { "TTT", "BBT", "TTT",
                        Character.valueOf('T'), ObjectManager.getItem("frostyfur"),
                        Character.valueOf('B'), Items.bone
                }));
		
		// ========== Smelting ==========
		GameRegistry.addSmelting(ObjectManager.getItem("yetimeatraw"), new ItemStack(ObjectManager.getItem("yetimeatcooked"), 1), 0.5f);
	}
}
