package com.flansmod.common;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.flansmod.client.AimType;
import com.flansmod.client.FlanMouseButton;
import com.flansmod.client.FlansCrash;
import com.flansmod.client.FlansModClient;
import com.flansmod.client.model.GunAnimations;
import com.flansmod.common.eventhandlers.PlayerLoginEventListener;
import com.flansmod.common.eventhandlers.ServerTickEvent;
import com.flansmod.common.sync.SyncEventHandler;
import com.flansmod.common.types.IGunboxDescriptionable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.CommandHandler;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import com.flansmod.client.debug.EntityDebugDot;
import com.flansmod.client.debug.EntityDebugVector;
import com.flansmod.common.driveables.EntityPlane;
import com.flansmod.common.driveables.EntitySeat;
import com.flansmod.common.driveables.EntityVehicle;
import com.flansmod.common.driveables.EntityWheel;
import com.flansmod.common.driveables.ItemPlane;
import com.flansmod.common.driveables.ItemVehicle;
import com.flansmod.common.driveables.mechas.EntityMecha;
import com.flansmod.common.driveables.mechas.ItemMecha;
import com.flansmod.common.driveables.mechas.ItemMechaAddon;
import com.flansmod.common.enchantments.EnchantmentModule;
import com.flansmod.common.eventhandlers.PlayerDeathEventListener;
import com.flansmod.common.guns.BulletType;
import com.flansmod.common.guns.EntityAAGun;
import com.flansmod.common.guns.EntityBullet;
import com.flansmod.common.guns.EntityGrenade;
import com.flansmod.common.guns.EntityMG;
import com.flansmod.common.guns.GunType;
import com.flansmod.common.guns.ItemAAGun;
import com.flansmod.common.guns.ItemAttachment;
import com.flansmod.common.guns.ItemBullet;
import com.flansmod.common.guns.ItemGrenade;
import com.flansmod.common.guns.ItemGun;
import com.flansmod.common.guns.boxes.BlockGunBox;
import com.flansmod.common.guns.boxes.GunBoxType;
import com.flansmod.common.network.PacketHandler;
import com.flansmod.common.paintjob.BlockPaintjobTable;
import com.flansmod.common.paintjob.TileEntityPaintjobTable;
import com.flansmod.common.parts.ItemPart;
import com.flansmod.common.teams.ArmourType;
import com.flansmod.common.teams.BlockArmourBox;
import com.flansmod.common.teams.BlockSpawner;
import com.flansmod.common.teams.CommandTeams;
import com.flansmod.common.teams.EntityFlag;
import com.flansmod.common.teams.EntityFlagpole;
import com.flansmod.common.teams.EntityGunItem;
import com.flansmod.common.teams.EntityTeamItem;
import com.flansmod.common.teams.ItemFlagpole;
import com.flansmod.common.teams.ItemOpStick;
import com.flansmod.common.teams.ItemTeamArmour;
import com.flansmod.common.teams.PlayerClass;
import com.flansmod.common.teams.Team;
import com.flansmod.common.teams.TeamsManager;
import com.flansmod.common.teams.TeamsManagerRanked;
import com.flansmod.common.teams.TileEntitySpawner;
import com.flansmod.common.tools.EntityParachute;
import com.flansmod.common.tools.ItemTool;
import com.flansmod.common.types.EnumType;
import com.flansmod.common.types.InfoType;
import com.flansmod.common.types.TypeFile;

@Mod(modid = FlansMod.MODID, name = "Flan's Mod", version = FlansMod.VERSION, acceptableRemoteVersions = "@ALLOWED_VERSIONS@", guiFactory = "com.flansmod.client.gui.config.ModGuiFactory")
public class FlansMod
{
	//Core mod stuff
	public static Logger log;
	public static boolean DEBUG = false;
	public static Configuration configFile;
	public static final String MODID = "flansmod";
	public static final String VERSION = "@VERSION@";
	@Instance(MODID)
	public static FlansMod INSTANCE;
	public static boolean printDebugLog = true;
    public static boolean printStackTrace = false;
    public static int noticeSpawnKillTime = 10;
	public static boolean enableKillMessages = true;
    public static boolean gunCarryLimitEnable = false;
    public static int gunCarryLimit = 3;
    public static int breakableArmor = 0;
    public static int defaultArmorDurability = 500;
    public static boolean armsEnable = true;
    public static boolean casingEnable = true;
    public static boolean crosshairEnable = false;
    public static boolean realisticRecoil = false;
    public static boolean enableSightDownwardMovement = true;
    public static boolean hitCrossHairEnable = true;
    public static boolean hdHitCrosshair = false;
    public static boolean bulletGuiEnable = true;
    public static float[] hitCrossHairColor = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
	public static boolean addAllPaintjobsToCreative = false;
	
	@SidedProxy(clientSide = "com.flansmod.client.ClientProxy", serverSide = "com.flansmod.common.CommonProxy")
	public static CommonProxy proxy;
	//A standardised ticker for all bits of the mod to call upon if they need one
	public static int ticker = 0;
	public static long lastTime;
	public static File flanDir, modDir;
	public static final float soundRange = 50F;
	public static float driveableUpdateRange = 400F;
	public static final int numPlayerSnapshots = 20;
	public static int armourEnchantability = 0;
	public static boolean kickNonMatchingHashes = false;
	public static boolean disableSprintHipFireByDefault = false;
	public static boolean useNewPenetrationSystem = false;
	public static boolean gunsInDeadPartsWork = false;
	public static boolean fancyCrosshair = true;
	public static boolean showDistanceInKillMessage = true;
	public static boolean driveableHitboxes = true;
	public static boolean reloadOnRightClick = true;
	public static float defaultADSSpreadMultiplier = 0.2F;
	public static float defaultADSSpreadMultiplierShotgun = 0.8F;
	public static boolean seatCollisions = true;
	public static boolean showItemDescriptions = true;
	public static boolean showMuzzleFlashParticlesDefault = true;
	public static boolean showFlashesWhenWounded = true;
	public static boolean isApocalypseLoaded = false;
	public static boolean addGunpowderRecipe = true;
	public static boolean shootOnRightClick = false;
	public static boolean forceUpdateJSONs = false;
	public static boolean enchantmentModuleEnabled = true;
	
	public static float armourSpawnRate = 0.25F;
	
	public static int dungeonLootChance = 500;
	
	/**
	 * The spectator team. Moved here to avoid a concurrent modification error
	 */
	public static Team spectators = new Team("spectators", "Spectators", 0x404040, '7');
	
	//Handlers
	public static final PacketHandler packetHandler = new PacketHandler();
	public static final PlayerHandler playerHandler = new PlayerHandler();
	public static final TeamsManager teamsManager = new TeamsManagerRanked();
	public static final CommonTickHandler tickHandler = new CommonTickHandler();
	public static FlansHooks hooks = new FlansHooks();
	public static final ContentManager contentManager = new ContentManager();
	public static final EnchantmentModule enchantmentModule = new EnchantmentModule();
	public static HashMap<String, String> modelDirectories = new HashMap<>();
	private static final HashMap<String, String> modelLocations = new HashMap<>();

	public static boolean isInFlash = false;
	public static int flashTime = 10;
	
	//Items and creative tabs
	public static BlockFlansWorkbench workbench;
	public static ItemBlockManyNames workbenchItem;
	public static Item gunpowderBlockItem;
	public static BlockSpawner spawner;
	public static Block gunpowderBlock;
	public static ItemBlockManyNames spawnerItem;
	public static ItemOpStick opStick;
	public static ItemFlagpole flag;
	public static Item crosshairsymbol;
	public static ArrayList<BlockGunBox> gunBoxBlocks = new ArrayList<>();
	public static ArrayList<ItemBullet> bulletItems = new ArrayList<>();
	public static ArrayList<ItemGun> gunItems = new ArrayList<>();
	public static ArrayList<ItemAttachment> attachmentItems = new ArrayList<>();
	public static ArrayList<ItemPart> partItems = new ArrayList<>();
	public static ArrayList<ItemPlane> planeItems = new ArrayList<>();
	public static ArrayList<ItemVehicle> vehicleItems = new ArrayList<>();
	public static ArrayList<ItemMechaAddon> mechaToolItems = new ArrayList<>();
	public static ArrayList<ItemMecha> mechaItems = new ArrayList<>();
	public static ArrayList<ItemAAGun> aaGunItems = new ArrayList<>();
	public static ArrayList<ItemGrenade> grenadeItems = new ArrayList<>();
	public static ArrayList<ItemTool> toolItems = new ArrayList<>();
	public static ArrayList<ItemTeamArmour> armourItems = new ArrayList<>();
	public static ArrayList<BlockArmourBox> armourBoxBlocks = new ArrayList<>();
	public static CreativeTabFlan tabFlanGuns = new CreativeTabFlan(0), tabFlanDriveables = new CreativeTabFlan(1),
			tabFlanParts = new CreativeTabFlan(2), tabFlanTeams = new CreativeTabFlan(3), tabFlanMechas = new CreativeTabFlan(4);

	//Gun animations
	/**
	 * Gun animation variables for each entity holding a gun. Currently only applicable to the player
	 */
	public static HashMap<EntityLivingBase, GunAnimations> gunAnimationsRight = new HashMap<>(), gunAnimationsLeft = new HashMap<>();

	public static boolean debugMode = true;

	/**
	 * Custom paintjob item
	 */
	public static Item rainbowPaintcan;
	public static BlockPaintjobTable paintjobTable;
	
	private static Random rewardsRandom = new Random();
	
	public static float Pick(float totalWeight)
	{
		return rewardsRandom.nextFloat() * totalWeight;
	}
	
	// Register an old-new package redirect.
	// Previously models were in "com.flansmod.client.model.<key>"
	// But now that official packs are mods, different structures may make more sense
	// So for example the Modern pack has model keys like "mw.MP5"
	// Now we want these in "com.flansmod.modernweapons.client.model"
	// So we call this with ("mw", "com.flansmod.modernweapons.client.model")
	public static void RegisterModelRedirect(String key, String redirect)
	{
		modelDirectories.put(key, redirect);
	}
	
	/**
	 * The mod pre-initialiser method
	 */
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		log = event.getModLog();
		log.debug("Pre-initialising Flan's mod.");
		
		MinecraftForge.EVENT_BUS.register(INSTANCE);
		
		proxy.preInit();
		proxy.registerRenderers();
		
		configFile = new Configuration(event.getSuggestedConfigurationFile());
		syncConfig();
		
		if(enchantmentModuleEnabled)
			enchantmentModule.PreInit();
		//TODO : Load properties
		//configuration = new Configuration(event.getSuggestedConfigurationFile());
		//loadProperties();
		
		try
		{
			isApocalypseLoaded = true;
			Class.forName("com.flansmod.apocalypse.common.FlansModApocalypse");
		}
		catch(Exception e)
		{
			isApocalypseLoaded = false;
		}
		
		modDir = new File(event.getModConfigurationDirectory().getParentFile(), "/mods/");
		flanDir = new File(event.getModConfigurationDirectory().getParentFile(), "/Flan/");
		
		if(!flanDir.exists())
		{
			log.info("Flan folder not found. Creating empty folder.%n" +
					"You should get some content packs and put them in the Flan folder.");
			boolean success = flanDir.mkdirs();
			log.info("Created Flan directory: " + success);
		}
		
		//Set up mod blocks and items
		workbench = (BlockFlansWorkbench)(new BlockFlansWorkbench(1, 0).setTranslationKey("flansWorkbench"));
		opStick = new ItemOpStick();
		flag = (ItemFlagpole)(new ItemFlagpole().setTranslationKey("flagpole"));
		spawner = (BlockSpawner)(new BlockSpawner(Material.CARPET).setTranslationKey("teamsSpawner").setBlockUnbreakable().setResistance(1000000F));
		rainbowPaintcan = new Item().setTranslationKey("rainbowPaintcan").setRegistryName("rainbowPaintcan").setCreativeTab(tabFlanGuns);
		paintjobTable = new BlockPaintjobTable();
		workbenchItem = new ItemBlockManyNames(workbench);
		spawnerItem = new ItemBlockManyNames(spawner);
		crosshairsymbol = new Item().setTranslationKey("crosshairsymbol").setRegistryName("crosshairsymbol");
		gunpowderBlock = new Block(Material.TNT).setHardness(0.0F).setTranslationKey("gunpowderblock").setRegistryName("gunpowderblock");
		gunpowderBlockItem = new ItemBlock(gunpowderBlock).setTranslationKey("gunpowderblock").setRegistryName("gunpowderblock");
		
		GameRegistry.registerTileEntity(TileEntitySpawner.class, new ResourceLocation("flansmod:teamsSpawner"));
		GameRegistry.registerTileEntity(TileEntityPaintjobTable.class, new ResourceLocation("flansmod:paintjobTable"));
		GameRegistry.registerTileEntity(TileEntityItemHolder.class, new ResourceLocation("flansmod:itemHolder"));

		//Add missing pack.mcmeta files in content packs so that lang files are properly loaded
		addMissingPackMcMeta();
		//Convert assets from old content packs in Flan folder
		convertAssets();
		
		//Read content packs
		contentManager.FindContentInModsFolder();
		contentManager.FindContentInFlanFolder();
		contentManager.LoadAssetsFromFlanFolder();
		contentManager.RegisterModelRedirects();
		contentManager.LoadTypes();
		contentManager.CreateItems();
		Team.spectators = spectators;
		
		//Automates JSON adding for old content packs
		proxy.addMissingJSONs(InfoType.infoTypes);
		
		//Force Minecraft to reload all resources in order to load content pack resources.
		if(contentManager.LoadedAnyContentFromFlanFolder()) {
			log.warn("Content packs detected, reload resources");
			proxy.forceReload();
		}

		FMLCommonHandler.instance().registerCrashCallable(new FlansCrash());

		Team.spectators = spectators;
		log.debug("Pre-initializing complete.");
	}

	public void addMissingPackMcMeta() {
		File[] packs = flanDir.listFiles();
		if (packs != null)
		{
			for (File packFile : packs)
			{
				if (packFile.isDirectory())
				{
					File packMcMeta = new File(packFile, "pack.mcmeta");
					if (!packMcMeta.exists())
					{
						try
						{
							if (packMcMeta.createNewFile())
							{
								FileWriter myWriter = new FileWriter(packMcMeta);
								myWriter.write("{\n  \"pack\": {\n    \"pack_format\": 3,\n    \"description\": \"Flan's Mod Defaults\"\n  }\n}\n");
								myWriter.close();
							}
							else
							{
								log.error("Could not create pack.mcmeta file in " + packFile.getPath());
							}
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}
	}



	public void convertAssets()
	{
		File[] packs = flanDir.listFiles();
		if (packs != null)
		{
			for (File packFile : packs)
			{
				if (packFile.isDirectory() && !packFile.getName().contains("1.12.2"))
				{
					File assetsDir = new File(packFile, "assets" + File.separator + "flansmod");
					if (assetsDir.isDirectory())
					{
						File armorDir = new File(assetsDir, "armor");
						File guiDir = new File(assetsDir, "gui");
						File langDir = new File(assetsDir, "lang");
						File skinsDir = new File(assetsDir, "skins");
						File soundsDir = new File(assetsDir, "sounds");
						File blockIconsDir = new File(assetsDir, "textures" + File.separator + "blocks");
						File itemIconsDir = new File(assetsDir, "textures" + File.separator + "items");

						renameFilesInDirToLowercase(armorDir, ".png");
						renameFilesInDirToLowercase(guiDir, ".png");
						renameFilesInDirToLowercase(langDir, ".lang");
						renameFilesInDirToLowercase(skinsDir, ".png");
						renameFilesInDirToLowercase(soundsDir, ".ogg");
						renameFilesInDirToLowercase(blockIconsDir, ".png");
						renameFilesInDirToLowercase(itemIconsDir, ".png");
					}
				}
			}
		}
	}

	private void renameFilesInDirToLowercase(File directory, String filesExtension)
	{
		if (directory.isDirectory())
		{
			File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(filesExtension));
			if (files != null)
			{
				for (File file : files)
				{
					String fileName = file.getName();
					if (!fileName.equals(fileName.toLowerCase()))
					{
						File renamedFile = new File(directory, file.getName().toLowerCase());
						boolean success = file.renameTo(renamedFile);
						if (!success)
						{
							FlansMod.log.warn(fileName + " could not be renamed to " + fileName.toLowerCase() + " in " + file.getPath());
						}
					}
				}
			}
		}
	}

	/**
	 * The mod initialiser method
	 */
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		log.info("Initialising Flan's Mod.");
		
		//Do proxy loading
		proxy.init();
		
		if(enchantmentModuleEnabled)
			enchantmentModule.Init();
		
		//Initialising handlers
		packetHandler.initialise();
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new CommonGuiHandler());
		
		// Really randomise the rewards generator
		rewardsRandom = new Random();
		rewardsRandom.setSeed(System.currentTimeMillis() ^ 0x5AB49DE08DE3B1DFL);
		for(int i = 0; i < 10; i++)
		{
			for(int j = 0; j < rewardsRandom.nextInt(10); j++)
			{
				rewardsRandom.nextGaussian();
			}
		}
		
		//Config
		//Starting the EventListener
		new PlayerDeathEventListener();
		new PlayerLoginEventListener();
		new ServerTickEvent();
		log.info("Loading complete.");
	}
	
	@SubscribeEvent
	public void registerRecipes(RegistryEvent.Register<IRecipe> event)
	{
		log.info("Registering Recipes.");
		
		InfoType.InitializeSpecialIngredients();
		
		// Recipes
		for(InfoType type : InfoType.infoTypes.values())
		{
			type.addRecipe(event.getRegistry());
		}
		if(addGunpowderRecipe)
		{
			NonNullList<Ingredient> ingredients = NonNullList.create();
			ingredients.add(Ingredient.fromStacks(new ItemStack(Items.GLOWSTONE_DUST)));
			ingredients.add(Ingredient.fromStacks(new ItemStack(Items.COAL, 1, 1)));
			ingredients.add(Ingredient.fromStacks(new ItemStack(Items.COAL, 1, 1)));
			ingredients.add(Ingredient.fromStacks(new ItemStack(Items.COAL, 1, 1)));
			
			event.getRegistry().register(new ShapelessRecipes(MODID, new ItemStack(Items.GUNPOWDER), ingredients).setRegistryName("FM_Gunpowder"));
		}
		
		// Gunpowder block -> 9 gunpowder
		{
			NonNullList<Ingredient> ingredients = NonNullList.create();
			ingredients.add(Ingredient.fromStacks(new ItemStack(gunpowderBlock)));
			event.getRegistry().register(new ShapelessRecipes(MODID, new ItemStack(Items.GUNPOWDER, 9), ingredients).setRegistryName("GunpowderBlockToDust"));
		}
		// 9 gunpowder -> gunpowder block
		{
			NonNullList<Ingredient> ingredients = NonNullList.create();
			for(int i = 0; i < 9; i++)
				ingredients.add(Ingredient.fromStacks(new ItemStack(Items.GUNPOWDER)));
			event.getRegistry().register(new ShapelessRecipes(MODID, new ItemStack(gunpowderBlock), ingredients).setRegistryName("GunpowderDustToBlock"));
		}
		
		// Add the two workbench recipes
		{
			// ICI C = Cauldron
			// III I = Iron ingot
			NonNullList<Ingredient> ingredients = NonNullList.create();
			ingredients.add(Ingredient.fromStacks(new ItemStack(Items.IRON_INGOT)));
			ingredients.add(Ingredient.fromStacks(new ItemStack(Items.CAULDRON)));
			for(int i = 0; i < 4; i++)
				ingredients.add(Ingredient.fromStacks(new ItemStack(Items.IRON_INGOT)));
			
			event.getRegistry().register(new ShapedRecipes("FlansMod", 3, 2, ingredients, new ItemStack(workbench, 1, 1)).setRegistryName("FM_Workbench"));
		}
		{
			// BBB B = Bowl
			// III I = Iron ingot
			// III
			NonNullList<Ingredient> ingredients = NonNullList.create();
			for(int i = 0; i < 3; i++)
				ingredients.add(Ingredient.fromStacks(new ItemStack(Items.BOWL)));
			for(int i = 0; i < 6; i++)
				ingredients.add(Ingredient.fromStacks(new ItemStack(Items.IRON_INGOT)));
			
			event.getRegistry().register(new ShapedRecipes("FlansMod", 3, 3, ingredients, new ItemStack(workbench, 1, 0)).setRegistryName("FM_Workbench2"));
		}
	}
	
	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event)
	{
		log.info("Registering Items");
		
		for(InfoType type : InfoType.infoTypes.values())
		{
			type.registerItem(event.getRegistry());
		}
		
		event.getRegistry().register(rainbowPaintcan); //, "rainbowPaintcan", MODID);
		event.getRegistry().register(opStick); //, "opStick", MODID);
		event.getRegistry().register(flag); //, "flagpole", MODID);
		
		event.getRegistry().register(workbenchItem);
		event.getRegistry().register(spawnerItem);
		event.getRegistry().register(crosshairsymbol);
		event.getRegistry().register(gunpowderBlockItem);
	}
	
	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event)
	{
		log.info("Registering Blocks");
		
		for(InfoType type : InfoType.infoTypes.values())
		{
			type.registerBlock(event.getRegistry());
		}
		
		event.getRegistry().register(workbench);//, ItemBlockManyNames.class, "flansWorkbench");
		event.getRegistry().register(spawner); // ItemBlockManyNames.class, "teamsSpawner");
		event.getRegistry().register(paintjobTable); //, "paintjobTable");
		event.getRegistry().register(gunpowderBlock);
	}
	
	@SubscribeEvent
	public void registerEntities(RegistryEvent.Register<EntityEntry> event)
	{
		log.info("Registering Entities");
		
		event.getRegistry().register(new EntityEntry(EntityFlagpole.class, "Flagpole").setRegistryName("Flagpole"));
		event.getRegistry().register(new EntityEntry(EntityFlag.class, "Flag").setRegistryName("Flag"));
		event.getRegistry().register(new EntityEntry(EntityTeamItem.class, "TeamsItem").setRegistryName("TeamsItem"));
		event.getRegistry().register(new EntityEntry(EntityGunItem.class, "GunItem").setRegistryName("GunItem"));
		event.getRegistry().register(new EntityEntry(EntityItemCustomRender.class, "CustomItem").setRegistryName("CustomItem"));
		event.getRegistry().register(new EntityEntry(EntityPlane.class, "Plane").setRegistryName("Plane"));
		event.getRegistry().register(new EntityEntry(EntityVehicle.class, "Vehicle").setRegistryName("Vehicle"));
		event.getRegistry().register(new EntityEntry(EntitySeat.class, "Seat").setRegistryName("Seat"));
		event.getRegistry().register(new EntityEntry(EntityWheel.class, "Wheel").setRegistryName("Wheel"));
		event.getRegistry().register(new EntityEntry(EntityParachute.class, "Parachute").setRegistryName("Parachute"));
		event.getRegistry().register(new EntityEntry(EntityMecha.class, "Mecha").setRegistryName("Mecha"));
		event.getRegistry().register(new EntityEntry(EntityBullet.class, "Bullet").setRegistryName("Bullet"));
		event.getRegistry().register(new EntityEntry(EntityGrenade.class, "Grenade").setRegistryName("Grenade"));
		event.getRegistry().register(new EntityEntry(EntityMG.class, "MG").setRegistryName("MG"));
		event.getRegistry().register(new EntityEntry(EntityAAGun.class, "AAGun").setRegistryName("AAGun"));
		event.getRegistry().register(new EntityEntry(EntityDebugVector.class, "DebugVector").setRegistryName("DebugVector"));
		event.getRegistry().register(new EntityEntry(EntityDebugDot.class, "DebugDot").setRegistryName("DebugDot"));
		
		EntityRegistry.registerModEntity(new ResourceLocation("flansmod:CustomItem"), EntityItemCustomRender.class, "CustomItem", 89, this, 100, 20, true);
		EntityRegistry.registerModEntity(new ResourceLocation("flansmod:Plane"), EntityPlane.class, "Plane", 90, this, 200, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation("flansmod:MG"), EntityMG.class, "MG", 91, this, 40, 5, true);
		EntityRegistry.registerModEntity(new ResourceLocation("flansmod:AAGun"), EntityAAGun.class, "AAGun", 92, this, 40, 500, false);
		EntityRegistry.registerModEntity(new ResourceLocation("flansmod:Flagpole"), EntityFlagpole.class, "Flagpole", 93, this, 40, 5, true);
		EntityRegistry.registerModEntity(new ResourceLocation("flansmod:Flag"), EntityFlag.class, "Flag", 94, this, 40, 5, true);
		EntityRegistry.registerModEntity(new ResourceLocation("flansmod:Vehicle"), EntityVehicle.class, "Vehicle", 95, this, 400, 10, true);
		EntityRegistry.registerModEntity(new ResourceLocation("flansmod:Bullet"), EntityBullet.class, "Bullet", 96, this, 200, 20, true);
		EntityRegistry.registerModEntity(new ResourceLocation("flansmod:TeamsItem"), EntityTeamItem.class, "TeamsItem", 97, this, 100, 10000, true);
		EntityRegistry.registerModEntity(new ResourceLocation("flansmod:GunItem"), EntityGunItem.class, "GunItem", 98, this, 100, 20, true);
		EntityRegistry.registerModEntity(new ResourceLocation("flansmod:Seat"), EntitySeat.class, "Seat", 99, this, 250, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation("flansmod:Grenade"), EntityGrenade.class, "Grenade", 100, this, 40, 100, true);
		EntityRegistry.registerModEntity(new ResourceLocation("flansmod:Parachute"), EntityParachute.class, "Parachute", 101, this, 40, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation("flansmod:Mecha"), EntityMecha.class, "Mecha", 102, this, 250, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation("flansmod:Wheel"), EntityWheel.class, "Wheel", 103, this, 200, 20, true);
		EntityRegistry.registerModEntity(new ResourceLocation("flansmod:DebugVector"), EntityDebugVector.class, "DebugVector", 104, this, 250, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation("flansmod:DebugDot"), EntityDebugDot.class, "DebugDot", 105, this, 250, 20, false);
	}
	
	private static ResourceLocation lostCitiesChest = new ResourceLocation("lostcities", "chests/lostcitychest");
	private static ResourceLocation lostCitiesRailChest = new ResourceLocation("lostcities", "chests/raildungeonchest");
	
	@SubscribeEvent
	public void registerLoot(LootTableLoadEvent event)
	{		
		// Add default Flan's loot - extra gunpowder, iron etc
		if(event.getName().equals(LootTableList.CHESTS_ABANDONED_MINESHAFT)
		|| event.getName().equals(LootTableList.CHESTS_VILLAGE_BLACKSMITH)
		|| event.getName().equals(LootTableList.CHESTS_END_CITY_TREASURE)
		|| event.getName().equals(LootTableList.CHESTS_NETHER_BRIDGE)
		|| event.getName().equals(LootTableList.CHESTS_DESERT_PYRAMID)
		)
		{
			for(InfoType type : InfoType.infoTypes.values())
			{
				type.addLoot(event);
			}
			
			LootPool pool = event.getTable().getPool("FlansModBasicLoot");
			if(pool == null)
			{
				event.getTable().addPool(new LootPool(new LootEntry[0], new LootCondition[0], new RandomValueRange(1, 3), new RandomValueRange(1, 2), "FlansModBasicLoot"));
				pool = event.getTable().getPool("FlansModBasicLoot");
			}
			pool.addEntry(new LootEntryItem(gunpowderBlockItem, 8, 1, new LootFunction[] { new SetCount(new LootCondition[0], new RandomValueRange(1, 6)) }, new LootCondition[0], "gpowderblocks"));
			pool.addEntry(new LootEntryItem(workbenchItem, 1, 1, new LootFunction[0], new LootCondition[0], "workbenches"));
			pool.addEntry(new LootEntryItem(ItemBlock.getItemFromBlock(Blocks.IRON_BLOCK), 4, 1, new LootFunction[] { new SetCount(new LootCondition[0], new RandomValueRange(2, 4)) }, new LootCondition[0], "extrairon"));
		}

		// If LostCities is installed, add tons of extra loot
		else if(event.getName().equals(lostCitiesRailChest)
			 || event.getName().equals(lostCitiesChest)
		)
		{
			for(InfoType type : InfoType.infoTypes.values())
			{
				type.addLoot(event);
			}
			
			LootPool pool = event.getTable().getPool("FlansModBasicLoot");
			if(pool == null)
			{
				event.getTable().addPool(new LootPool(new LootEntry[0], new LootCondition[0], new RandomValueRange(1, 7), new RandomValueRange(1, 3), "FlansModBasicLoot"));
				pool = event.getTable().getPool("FlansModBasicLoot");
			}
			
			pool.addEntry(new LootEntryItem(gunpowderBlockItem, 32, 1, new LootFunction[] { new SetCount(new LootCondition[0], new RandomValueRange(1, 6)) }, new LootCondition[0], "gpowderblocks"));
			pool.addEntry(new LootEntryItem(workbenchItem, 8, 1, new LootFunction[0], new LootCondition[0], "workbenches"));
			pool.addEntry(new LootEntryItem(ItemBlock.getItemFromBlock(Blocks.DIAMOND_BLOCK), 1, 1, new LootFunction[0], new LootCondition[0], "diamonde"));
			pool.addEntry(new LootEntryItem(ItemBlock.getItemFromBlock(Blocks.GOLD_BLOCK), 4, 1, new LootFunction[0], new LootCondition[0], "golds"));
			pool.addEntry(new LootEntryItem(ItemBlock.getItemFromBlock(Blocks.IRON_BLOCK), 16, 1, new LootFunction[] { new SetCount(new LootCondition[0], new RandomValueRange(2, 4)) }, new LootCondition[0], "extrairon"));
		}
	}
	
	private class FMLootFunction extends LootFunction
	{
		private int min, max;
		
		protected FMLootFunction(LootCondition[] conditionsIn) 
		{
			super(conditionsIn);
		}

		@Override
		public ItemStack apply(ItemStack stack, Random rand, LootContext context) 
		{
			return null;
		}
		
	}

	
	/**
	 * The mod post-initialisation method
	 */
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		packetHandler.postInitialise();
		
		if(enchantmentModuleEnabled)
			enchantmentModule.PostInit();
		
		hooks.hook();

		MinecraftForge.EVENT_BUS.register(new SyncEventHandler());

		log.info("Starting gunbox mapping.");
		for (BlockGunBox box : gunBoxBlocks) {
			for (GunBoxType.GunBoxPage page : box.type.pages) {
				for (GunBoxType.GunBoxEntryTopLevel entry : page.entries) {
					try {
						if (entry.type != null) {
							IGunboxDescriptionable item = getGunBoxItem(entry.type);

							if (item != null) {
								item.setOriginGunBox(box.getLocalizedName());
							}
						}
						if (!entry.isAmmoNullOrEmpty()) {
							for (GunBoxType.GunBoxEntry ammoEntry : entry.childEntries) {
								IGunboxDescriptionable item = getGunBoxItem(ammoEntry.type);

								if (item != null) {
									item.setOriginGunBox(box.getLocalizedName());
								}
							}
						}
					} catch (Exception e) {
						if (FlansMod.printDebugLog) {
							log.error("A gunbox entry appears to be null");
							e.printStackTrace();
						}
					}
				}
			}
		}
		log.info("Finished gunbox mapping.");
	}
	
	@SubscribeEvent
	public void playerDrops(PlayerDropsEvent event)
	{
		for(int i = event.getDrops().size() - 1; i >= 0; i--)
		{
			EntityItem ent = event.getDrops().get(i);
			InfoType type = InfoType.getType(ent.getItem());
			if(type != null && !type.canDrop)
				event.getDrops().remove(i);
		}
	}
	
	@SubscribeEvent
	public void playerDrops(ItemTossEvent event)
	{
		InfoType type = InfoType.getType(event.getEntityItem().getItem());
		if(type != null && !type.canDrop)
			event.setCanceled(true);
	}
	
	/**
	 * Teams command register method
	 */
	@EventHandler
	public void registerCommand(FMLServerStartedEvent e)
	{
		CommandHandler handler = ((CommandHandler)FMLCommonHandler.instance().getSidedDelegate().getServer().getCommandManager());
		handler.registerCommand(new CommandTeams());
	}
	
	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs)
	{
		if(eventArgs.getModID().equals(MODID))
			syncConfig();
	}
	
	@SubscribeEvent
	public void onLivingSpecialSpawn(EntityJoinWorldEvent event)
	{
		double chance = event.getWorld().rand.nextDouble();
		
		if(chance < armourSpawnRate && event.getEntity() instanceof EntityZombie || event.getEntity() instanceof EntitySkeleton)
		{
			if(event.getWorld().rand.nextBoolean() && ArmourType.armours.size() > 0)
			{
				//Give a completely random piece of armour
				ArmourType armour = ArmourType.armours.get(event.getWorld().rand.nextInt(ArmourType.armours.size()));
				if(armour != null && armour.type != 2)
					event.getEntity().setItemStackToSlot(EntityEquipmentSlot.values()[armour.type + 1], new ItemStack(armour.item));
			}
			else if(Team.teams.size() > 0)
			{
				//Give a random set of armour
				Team team = Team.teams.get(event.getWorld().rand.nextInt(Team.teams.size()));
				if(team.hat != null)
					event.getEntity().setItemStackToSlot(EntityEquipmentSlot.HEAD, team.hat.copy());
				if(team.chest != null)
					event.getEntity().setItemStackToSlot(EntityEquipmentSlot.CHEST, team.chest.copy());
				if(team.legs != null)
					event.getEntity().setItemStackToSlot(EntityEquipmentSlot.LEGS, team.legs.copy());
				if(team.shoes != null)
					event.getEntity().setItemStackToSlot(EntityEquipmentSlot.FEET, team.shoes.copy());
				
				if(team.classes.size() > 0)
				{
					PlayerClass playerClass = team.classes.get(event.getWorld().rand.nextInt(team.classes.size()));
					if(playerClass.hat != null)
						event.getEntity().setItemStackToSlot(EntityEquipmentSlot.HEAD, playerClass.hat.copy());
					if(playerClass.chest != null)
						event.getEntity().setItemStackToSlot(EntityEquipmentSlot.CHEST, playerClass.chest.copy());
					if(playerClass.legs != null)
						event.getEntity().setItemStackToSlot(EntityEquipmentSlot.LEGS, playerClass.legs.copy());
					if(playerClass.shoes != null)
						event.getEntity().setItemStackToSlot(EntityEquipmentSlot.FEET, playerClass.shoes.copy());
				}
			}
		}
	}

	@SubscribeEvent
	public void onAnvilUsedEvent(AnvilUpdateEvent e) {
		if (e.getLeft().getItem() instanceof ItemTeamArmour && armourEnchantability == 0) {
			e.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public void onAttackEntity(AttackEntityEvent event)
	{
		if(event.getEntity() instanceof EntityGunItem)
		{
			event.setCanceled(true);
		}
	}
	
	/**
	 * Reads type files from all content packs
	 */
	@Deprecated
	private void getTypeFiles(List<File> contentPacks)
	{
		for(File contentPack : contentPacks)
		{
			if(contentPack.isDirectory())
			{
				for(EnumType typeToCheckFor : EnumType.values())
				{
					File typesDir = new File(contentPack, "/" + typeToCheckFor.folderName + "/");
					if(!typesDir.exists())
						continue;
					for(File file : typesDir.listFiles())
					{
						try
						{
							BufferedReader reader = new BufferedReader(new FileReader(file));
							String[] splitName = file.getName().split("/");
							TypeFile typeFile = new TypeFile(contentPack.getName(), typeToCheckFor, splitName[splitName.length - 1].split("\\.")[0]);
							for(; ; )
							{
								String line = null;
								try
								{
									line = reader.readLine();
								}
								catch(Exception e)
								{
									break;
								}
								if(line == null)
									break;
								typeFile.parseLine(line);
							}
							reader.close();
						}
						catch(IOException e)
						{
							FlansMod.log.throwing(e);
						}
					}
				}
			}
			else
			{
				try
				{
					ZipFile zip = new ZipFile(contentPack);
					ZipInputStream zipStream = new ZipInputStream(new FileInputStream(contentPack));
					BufferedReader reader = new BufferedReader(new InputStreamReader(zipStream));
					ZipEntry zipEntry = zipStream.getNextEntry();
					do
					{
						zipEntry = zipStream.getNextEntry();
						if(zipEntry == null)
							continue;
						TypeFile typeFile = null;
						for(EnumType type : EnumType.values())
						{
							if(zipEntry.getName().startsWith(type.folderName + "/") && zipEntry.getName().split(type.folderName + "/").length > 1 && zipEntry.getName().split(type.folderName + "/")[1].length() > 0)
							{
								String[] splitName = zipEntry.getName().split("/");
								typeFile = new TypeFile(zip.getName(), type, splitName[splitName.length - 1].split("\\.")[0]);
							}
						}
						if(typeFile == null)
						{
							continue;
						}
						for(; ; )
						{
							String line = null;
							try
							{
								line = reader.readLine();
							}
							catch(Exception e)
							{
								break;
							}
							if(line == null)
								break;
							typeFile.parseLine(line);
						}
					}
					while(zipEntry != null);
					reader.close();
					zip.close();
					zipStream.close();
				}
				catch(IOException e)
				{
					FlansMod.log.throwing(e);
				}
			}
		}
	}

	public static IGunboxDescriptionable getGunBoxItem(InfoType item) {
		if (item instanceof GunType) {
			for (ItemGun gitem : gunItems) {
				if (Objects.equals(gitem.type.shortName, item.shortName)) {
					return gitem;
				}
			}
		} else if (item instanceof BulletType) {
			for (ItemBullet bitem : bulletItems) {
				if (Objects.equals(bitem.type.shortName, item.shortName)) {
					return bitem;
				}
			}
		}

		return null;
	}
		
	public static PacketHandler getPacketHandler()
	{
		return packetHandler;
	}
	
	public static void syncConfig()
	{
		addGunpowderRecipe = configFile.getBoolean("Gunpowder Recipe", Configuration.CATEGORY_GENERAL, addGunpowderRecipe, "Whether or not to add the extra gunpowder recipe (3 charcoal + 1 lightstone)");
		shootOnRightClick = configFile.getBoolean("ShootOnRightClick", Configuration.CATEGORY_GENERAL, shootOnRightClick, "If true, then shoot will be on right click");
		addAllPaintjobsToCreative = configFile.getBoolean("Add All Paintjobs to Creative", Configuration.CATEGORY_GENERAL, addAllPaintjobsToCreative, "Whether all paintjobs should appear in creative");
		forceUpdateJSONs = configFile.getBoolean("ForceUpdateJSONs", Configuration.CATEGORY_GENERAL, forceUpdateJSONs, "Turn this on to force re-create all JSON files. Should only be used in dev environment");
		enchantmentModuleEnabled = configFile.getBoolean("EnchantmentModuleEnabled", Configuration.CATEGORY_GENERAL, enchantmentModuleEnabled, "Enable gun-related enchantments");

		//Teams/Advanced Settings
		printDebugLog = configFile.getBoolean("Print Debug Log", "Teams/advanced settings", printDebugLog, "");
		printStackTrace = configFile.getBoolean("Print Stack Trace", "Teams/advanced settings", printStackTrace, "");
		noticeSpawnKillTime = configFile.getInt("NoticeSpawnKillTime", "Teams/advanced settings", 10, 0, 600, "Min");
		TeamsManager.bulletSnapshotMin = configFile.getInt("BltSS_Min", "Teams/advanced settings", 0, 0, 1000, "Min");
		TeamsManager.bulletSnapshotDivisor = configFile.getInt("BltSS_Divisor", "Teams/advanced settings", 50, 0, 1000, "Divisor");

		//Server/Gameplay Settings (Server-client synced)
		enableKillMessages = configFile.getBoolean("enableKillMessages", "Gameplay Settings (synced)", enableKillMessages,"Enable killMessage display");
		gunCarryLimitEnable = configFile.getBoolean("gunCarryLimitEnable", "Gameplay Settings (synced)", gunCarryLimitEnable, "Enable a soft limit to hotbar weapons, applies slowness++ when >= limit");
		gunCarryLimit = configFile.getInt("gunCarryLimit", "Gameplay Settings (synced)", 3, 2, 9, "Set the soft carry limit for guns(2-9)");
		bulletGuiEnable = configFile.getBoolean("Enable bullet HUD", "Gameplay Settings (synced)", bulletGuiEnable, "Enable bullet gui");
		hitCrossHairEnable = configFile.getBoolean("Enable hitmarkers", "Gameplay Settings (synced)", hitCrossHairEnable, "");
		realisticRecoil = configFile.getBoolean("Enable realistic recoil", "Gameplay Settings (synced)", realisticRecoil, "Changes recoil to be more realistic.");
		enableSightDownwardMovement = configFile.getBoolean("Enable downward movement of the sight after shot", "Gameplay Settings (synced)", enableSightDownwardMovement, "Enable downward movement of the sight after shot.");
		crosshairEnable = configFile.getBoolean("Enable crosshairs", "Gameplay Settings (synced)", crosshairEnable, "Enable default crosshair");
		breakableArmor = configFile.getInt("breakableArmor", "Gameplay Settings (synced)", 0, 0, 2, "0 = Non-breakable, 1 = All breakable, 2 = Refer to armor config");
		defaultArmorDurability = configFile.getInt("defaultArmorDurability", "Gameplay Settings (synced)", 500, 1, 10000, "Default durability if breakable = 1");
		addGunpowderRecipe = configFile.getBoolean("Gunpowder Recipe", "Gameplay Settings (synced)", addGunpowderRecipe, "Whether or not to add the extra gunpowder recipe (3 charcoal + 1 lightstone)");
		armourSpawnRate = configFile.getInt("ArmourSpawnRate", "Gameplay Settings (synced)", 20, 0, 100, "The rate of Zombie or Skeleton to spawn equipped with armor. [0=0%, 100=100%]");
		armourEnchantability = configFile.getInt("ArmourEnchantability", "Gameplay Settings (synced)", 0, 0, 25, "The quality of enchantments recieved for the same level of XP 0=UnEnchantable 25=Gold armor");
		kickNonMatchingHashes = configFile.getBoolean("KickNonMatchingHashes", "Gameplay Settings (synced)", kickNonMatchingHashes, "Wether to kick clients connected to a dedicated server with non-identical packs.");
		disableSprintHipFireByDefault = configFile.getBoolean("DisableSprintHipFireByDefault", "Gameplay Settings (synced)", disableSprintHipFireByDefault, "Wether to disallow players to fire the gun when sprinting and not aiming by default (content packs override).");
		useNewPenetrationSystem = configFile.getBoolean("UseNewPenetrationSystem", "Gameplay Settings (synced)", useNewPenetrationSystem, "Whether to use new penetration system (only content packs designed to work with this system will work as intended with this on).");
		gunsInDeadPartsWork = configFile.getBoolean("GunsInDeadPartsStillWork", "Gameplay Settings (synced)", gunsInDeadPartsWork, "Wether passenger guns should still work after the part of the vehicle they are on dies.");
		showDistanceInKillMessage = configFile.getBoolean("ShowDistanceInKillMessage", "Gameplay Settings (synced)", showDistanceInKillMessage, "List distance between killer and killed in kill message.");
		driveableHitboxes = configFile.getBoolean("Driveable hitboxes", "Gameplay Settings (synced)", driveableHitboxes, "Make a hitbox for the main driveable entity.");
		driveableUpdateRange = configFile.getFloat("Driveable update range", "Gameplay Settings (synced)", driveableUpdateRange, 0, 1000, "Range in blocks for vehicles and planes to be updated.");
		reloadOnRightClick = configFile.getBoolean("Reload on right click", "Gameplay Settings (synced)", reloadOnRightClick, "Whether to reload a gun using right click, forces use of R.");
		defaultADSSpreadMultiplier = configFile.getFloat("Default ADS Spread Modifier", "Gameplay Settings (synced)", defaultADSSpreadMultiplier, 0, 10, "Modifier for spread when the player is aiming.");
		defaultADSSpreadMultiplierShotgun = configFile.getFloat("Default ADS Spread Modifier (Shotguns)", "Gameplay Settings (synced)", defaultADSSpreadMultiplierShotgun, 0, 10, "Modifier for spread when the player is aiming. (Multishot guns only).");
		seatCollisions = configFile.getBoolean("Seat Collisions", "Gameplay Settings (synced)", seatCollisions, "Whether seats should collide with the world. Prevents plane glitching through walls.");
		showMuzzleFlashParticlesDefault = configFile.getBoolean("Muzzle Flash Particles Default (Synced)", "Gameplay Settings (synced)", showMuzzleFlashParticlesDefault, "Enable muzzle flash particles by default. Gun configs can override.");
		showFlashesWhenWounded = configFile.getBoolean("Flashes when player wounded (Synced)", "Gameplay Settings (synced)", showFlashesWhenWounded, "Should show red overlay when player has been wounded?");

		//Client Side Settings
		armsEnable = configFile.getBoolean("Enable Arms", Configuration.CATEGORY_GENERAL, armsEnable, "Enable arms rendering");
		casingEnable = configFile.getBoolean("Enable casings", Configuration.CATEGORY_GENERAL, casingEnable, "Enable bullet casing ejections");
		hdHitCrosshair = configFile.getBoolean("Enable HD hit marker", Configuration.CATEGORY_GENERAL, hdHitCrosshair, "");
		addAllPaintjobsToCreative = configFile.getBoolean("Add All Paintjobs To Creative", Configuration.CATEGORY_GENERAL, addAllPaintjobsToCreative, "Whether to list all available paintjobs in the Creative menu");
		fancyCrosshair = configFile.getBoolean("Fancy Crosshair", Configuration.CATEGORY_GENERAL, fancyCrosshair, "Change colour of crosshair based on hit. (Red = no penetration, green = full damage, light blue = headshot. Overrides normal colour settings.");
		for (int i = 0; i < hitCrossHairColor.length; i++) {
			final String[] COLOR = new String[]{"Alpha", "Red", "Green", "Blue"};
			hitCrossHairColor[i] = configFile.getFloat("HitCrossHairColor" + COLOR[i], Configuration.CATEGORY_GENERAL, hitCrossHairColor[i], 0.0F, 1.0F,
					"Hit cross hair color " + COLOR[i]);
		}
		showItemDescriptions = configFile.getBoolean("Enable fancy descriptions", Configuration.CATEGORY_GENERAL, showItemDescriptions, "Whether to show fancy item descriptions. These can be shown by pressing shift on an item ingame.");

		if(configFile.hasChanged())
			configFile.save();
	}
	
	public static void Assert(boolean b, String string)
	{
		if(!b)
		{
			log.warn(string);
		}
	}
	
	public static EnumParticleTypes getParticleType(String s)
	{
		if(s.equals("hugeexplosion")) return EnumParticleTypes.EXPLOSION_HUGE;
		else if(s.equals("largeexplode")) return EnumParticleTypes.EXPLOSION_LARGE;
		else if(s.equals("explode")) return EnumParticleTypes.EXPLOSION_NORMAL;
		else if(s.equals("fireworksSpark")) return EnumParticleTypes.FIREWORKS_SPARK;
		else if(s.equals("bubble")) return EnumParticleTypes.WATER_BUBBLE;
		else if(s.equals("splash")) return EnumParticleTypes.WATER_SPLASH;
		else if(s.equals("wake")) return EnumParticleTypes.WATER_WAKE;
		else if(s.equals("drop")) return EnumParticleTypes.WATER_DROP;
		else if(s.equals("suspended")) return EnumParticleTypes.SUSPENDED;
		else if(s.equals("depthsuspend")) return EnumParticleTypes.SUSPENDED_DEPTH;
		else if(s.equals("townaura")) return EnumParticleTypes.TOWN_AURA;
		else if(s.equals("crit")) return EnumParticleTypes.CRIT;
		else if(s.equals("magicCrit")) return EnumParticleTypes.CRIT_MAGIC;
		else if(s.equals("smoke")) return EnumParticleTypes.SMOKE_NORMAL;
		else if(s.equals("largesmoke")) return EnumParticleTypes.SMOKE_LARGE;
		else if(s.equals("spell")) return EnumParticleTypes.SPELL;
		else if(s.equals("instantSpell")) return EnumParticleTypes.SPELL_INSTANT;
		else if(s.equals("mobSpell")) return EnumParticleTypes.SPELL_MOB;
		else if(s.equals("mobSpellAmbient")) return EnumParticleTypes.SPELL_MOB_AMBIENT;
		else if(s.equals("witchMagic")) return EnumParticleTypes.SPELL_WITCH;
		else if(s.equals("dripWater")) return EnumParticleTypes.DRIP_WATER;
		else if(s.equals("dripLava")) return EnumParticleTypes.DRIP_LAVA;
		else if(s.equals("angryVillager")) return EnumParticleTypes.VILLAGER_ANGRY;
		else if(s.equals("happyVillager")) return EnumParticleTypes.VILLAGER_HAPPY;
		else if(s.equals("note")) return EnumParticleTypes.NOTE;
		else if(s.equals("portal")) return EnumParticleTypes.PORTAL;
		else if(s.equals("enchantmenttable")) return EnumParticleTypes.ENCHANTMENT_TABLE;
		else if(s.equals("flame")) return EnumParticleTypes.FLAME;
		else if(s.equals("lava")) return EnumParticleTypes.LAVA;
		else if(s.equals("footstep")) return EnumParticleTypes.FOOTSTEP;
		else if(s.equals("cloud")) return EnumParticleTypes.CLOUD;
		else if(s.equals("reddust")) return EnumParticleTypes.REDSTONE;
		else if(s.equals("snowballpoof")) return EnumParticleTypes.SNOWBALL;
		else if(s.equals("snowshovel")) return EnumParticleTypes.SNOW_SHOVEL;
		else if(s.equals("slime")) return EnumParticleTypes.SLIME;
		else if(s.equals("heart")) return EnumParticleTypes.HEART;
		else if(s.equals("barrier")) return EnumParticleTypes.BARRIER;
		
		return EnumParticleTypes.WATER_BUBBLE;
	}

	public static void syncConfig(Side side) {
		addGunpowderRecipe = configFile.getBoolean("Gunpowder Recipe", Configuration.CATEGORY_GENERAL, addGunpowderRecipe, "Whether or not to add the extra gunpowder recipe (3 charcoal + 1 lightstone)");
		shootOnRightClick = configFile.getBoolean("ShootOnRightClick", Configuration.CATEGORY_GENERAL, shootOnRightClick, "If true, then shoot will be on right click");
		addAllPaintjobsToCreative = configFile.getBoolean("Add All Paintjobs to Creative", Configuration.CATEGORY_GENERAL, addAllPaintjobsToCreative, "Whether all paintjobs should appear in creative");
		forceUpdateJSONs = configFile.getBoolean("ForceUpdateJSONs", Configuration.CATEGORY_GENERAL, forceUpdateJSONs, "Turn this on to force re-create all JSON files. Should only be used in dev environment");
		enchantmentModuleEnabled = configFile.getBoolean("EnchantmentModuleEnabled", Configuration.CATEGORY_GENERAL, enchantmentModuleEnabled, "Enable gun-related enchantments");

		//Teams/Advanced Settings
		printDebugLog = configFile.getBoolean("Print Debug Log", "Teams/advanced settings", printDebugLog, "");
		printStackTrace = configFile.getBoolean("Print Stack Trace", "Teams/advanced settings", printStackTrace, "");
		noticeSpawnKillTime = configFile.getInt("NoticeSpawnKillTime", "Teams/advanced settings", 10, 0, 600, "Min");
		TeamsManager.bulletSnapshotMin = configFile.getInt("BltSS_Min", "Teams/advanced settings", 0, 0, 1000, "Min");
		TeamsManager.bulletSnapshotDivisor = configFile.getInt("BltSS_Divisor", "Teams/advanced settings", 50, 0, 1000, "Divisor");

		//Server/Gameplay Settings (Server-client synced)
		enableKillMessages = configFile.getBoolean("enableKillMessages", "Gameplay Settings (synced)", enableKillMessages,"Enable killMessage display");
		gunCarryLimitEnable = configFile.getBoolean("gunCarryLimitEnable", "Gameplay Settings (synced)", gunCarryLimitEnable, "Enable a soft limit to hotbar weapons, applies slowness++ when >= limit");
		gunCarryLimit = configFile.getInt("gunCarryLimit", "Gameplay Settings (synced)", 3, 2, 9, "Set the soft carry limit for guns(2-9)");
		bulletGuiEnable = configFile.getBoolean("Enable bullet HUD", "Gameplay Settings (synced)", bulletGuiEnable, "Enable bullet gui");
		hitCrossHairEnable = configFile.getBoolean("Enable hitmarkers", "Gameplay Settings (synced)", hitCrossHairEnable, "");
		realisticRecoil = configFile.getBoolean("Enable realistic recoil", "Gameplay Settings (synced)", realisticRecoil, "Changes recoil to be more realistic.");
		enableSightDownwardMovement = configFile.getBoolean("Enable downward movement of the sight after shot", "Gameplay Settings (synced)", enableSightDownwardMovement, "Enable downward movement of the sight after shot.");
		crosshairEnable = configFile.getBoolean("Enable crosshairs", "Gameplay Settings (synced)", crosshairEnable, "Enable default crosshair");
		breakableArmor = configFile.getInt("breakableArmor", "Gameplay Settings (synced)", 0, 0, 2, "0 = Non-breakable, 1 = All breakable, 2 = Refer to armor config");
		defaultArmorDurability = configFile.getInt("defaultArmorDurability", "Gameplay Settings (synced)", 500, 1, 10000, "Default durability if breakable = 1");
		addGunpowderRecipe = configFile.getBoolean("Gunpowder Recipe", "Gameplay Settings (synced)", addGunpowderRecipe, "Whether or not to add the extra gunpowder recipe (3 charcoal + 1 lightstone)");
		armourSpawnRate = configFile.getInt("ArmourSpawnRate", "Gameplay Settings (synced)", 20, 0, 100, "The rate of Zombie or Skeleton to spawn equipped with armor. [0=0%, 100=100%]");
		armourEnchantability = configFile.getInt("ArmourEnchantability", "Gameplay Settings (synced)", 0, 0, 25, "The quality of enchantments recieved for the same level of XP 0=UnEnchantable 25=Gold armor");
		kickNonMatchingHashes = configFile.getBoolean("KickNonMatchingHashes", "Gameplay Settings (synced)", kickNonMatchingHashes, "Wether to kick clients connected to a dedicated server with non-identical packs.");
		disableSprintHipFireByDefault = configFile.getBoolean("DisableSprintHipFireByDefault", "Gameplay Settings (synced)", disableSprintHipFireByDefault, "Wether to disallow players to fire the gun when sprinting and not aiming by default (content packs override).");
		useNewPenetrationSystem = configFile.getBoolean("UseNewPenetrationSystem", "Gameplay Settings (synced)", useNewPenetrationSystem, "Whether to use new penetration system (only content packs designed to work with this system will work as intended with this on).");
		gunsInDeadPartsWork = configFile.getBoolean("GunsInDeadPartsStillWork", "Gameplay Settings (synced)", gunsInDeadPartsWork, "Wether passenger guns should still work after the part of the vehicle they are on dies.");
		showDistanceInKillMessage = configFile.getBoolean("ShowDistanceInKillMessage", "Gameplay Settings (synced)", showDistanceInKillMessage, "List distance between killer and killed in kill message.");
		driveableHitboxes = configFile.getBoolean("Driveable hitboxes", "Gameplay Settings (synced)", driveableHitboxes, "Make a hitbox for the main driveable entity.");
		driveableUpdateRange = configFile.getFloat("Driveable update range", "Gameplay Settings (synced)", driveableUpdateRange, 0, 1000, "Range in blocks for vehicles and planes to be updated.");
		reloadOnRightClick = configFile.getBoolean("Reload on right click", "Gameplay Settings (synced)", reloadOnRightClick, "Whether to reload a gun using right click, forces use of R.");
		defaultADSSpreadMultiplier = configFile.getFloat("Default ADS Spread Modifier", "Gameplay Settings (synced)", defaultADSSpreadMultiplier, 0, 10, "Modifier for spread when the player is aiming.");
		defaultADSSpreadMultiplierShotgun = configFile.getFloat("Default ADS Spread Modifier (Shotguns)", "Gameplay Settings (synced)", defaultADSSpreadMultiplierShotgun, 0, 10, "Modifier for spread when the player is aiming. (Multishot guns only).");
		seatCollisions = configFile.getBoolean("Seat Collisions", "Gameplay Settings (synced)", seatCollisions, "Whether seats should collide with the world. Prevents plane glitching through walls.");
		showMuzzleFlashParticlesDefault = configFile.getBoolean("Muzzle Flash Particles Default", "Gameplay Settings (synced)", showMuzzleFlashParticlesDefault, "Enable muzzle flash particles by default. Gun configs can override.");
		showFlashesWhenWounded = configFile.getBoolean("Flashes when player wounded (Synced)", "Gameplay Settings (synced)", showFlashesWhenWounded, "Should show red overlay when player has been wounded?");

		//Client Side Settings
		armsEnable = configFile.getBoolean("Enable Arms", Configuration.CATEGORY_GENERAL, armsEnable, "Enable arms rendering");
		casingEnable = configFile.getBoolean("Enable casings", Configuration.CATEGORY_GENERAL, casingEnable, "Enable bullet casing ejections");
		hdHitCrosshair = configFile.getBoolean("Enable HD hit marker", Configuration.CATEGORY_GENERAL, hdHitCrosshair, "");
		addAllPaintjobsToCreative = configFile.getBoolean("Add All Paintjobs To Creative", Configuration.CATEGORY_GENERAL, addAllPaintjobsToCreative, "Whether to list all available paintjobs in the Creative menu");
		fancyCrosshair = configFile.getBoolean("Fancy Crosshair", Configuration.CATEGORY_GENERAL, fancyCrosshair, "Change colour of crosshair based on hit. (Red = no penetration, green = full damage, light blue = headshot. Overrides normal colour settings.");
		for (int i = 0; i < hitCrossHairColor.length; i++) {
			final String[] COLOR = new String[]{"Alpha", "Red", "Green", "Blue"};
			hitCrossHairColor[i] = configFile.getFloat("HitCrossHairColor" + COLOR[i], Configuration.CATEGORY_GENERAL, hitCrossHairColor[i], 0.0F, 1.0F,
					"Hit cross hair color " + COLOR[i]);
		}
		showItemDescriptions = configFile.getBoolean("Enable fancy descriptions", Configuration.CATEGORY_GENERAL, showItemDescriptions, "Whether to show fancy item descriptions. These can be shown by pressing shift on an item ingame.");

		if (side.isClient()) {
			String aimTypeInput = configFile.getString("Aim Type", "Input Settings", "hold", "The type of aiming that you want to use 'toggle' or 'hold'");
			AimType aimType = AimType.fromString(aimTypeInput);

			if (aimType != null) {
				FlansModClient.aimType = aimType;
			} else {
				log(String.format("The aim type '%s' does not exist.", aimTypeInput));
				FlansModClient.aimType = AimType.TOGGLE;
			}

			String aimButtonInput = configFile.getString("Aim Button", "Input Settings", "right", "The mouse button used to aim a gun 'left' or 'right'");
			FlanMouseButton aimButtonType = FlanMouseButton.fromString(aimButtonInput);

			if (aimButtonType != null) {
				FlansModClient.aimButton = aimButtonType;
			} else {
				log(String.format("The aim button type '%s' does not exist.", aimTypeInput));
				FlansModClient.aimButton = FlanMouseButton.LEFT;
			}

			String shootButtonInput = configFile.getString("Fire Button", "Input Settings", "left", "The mouse button used to fire a gun 'left' or 'right'");
			FlanMouseButton shootButtonType = FlanMouseButton.fromString(shootButtonInput);

			if (shootButtonType != null) {
				FlansModClient.fireButton = shootButtonType;
			} else {
				log(String.format("The fire button type '%s' does not exist.", aimTypeInput));
				FlansModClient.fireButton = FlanMouseButton.RIGHT;
			}

		}

		if (configFile.hasChanged())
			configFile.save();
	}

	public static void updateBltssConfig(int min, int divisor) {
		ConfigCategory category = configFile.getCategory(Configuration.CATEGORY_GENERAL);
		if (category == null) return;
		if (category.containsKey("BltSS_Min")) {
			category.get("BltSS_Min").set(min);
		}
		if (category.containsKey("BltSS_Divisor")) {
			category.get("BltSS_Divisor").set(divisor);
		}

		TeamsManager.bulletSnapshotMin = min;
		TeamsManager.bulletSnapshotDivisor = divisor;
		configFile.save();
	}

	public static void log(Object obj) {
		if (printDebugLog) {
			log.info(obj.toString());
		}
	}

	public static void log(String format, Object... args) {
		log(String.format(format, args));
	}

	/**
	 * Provides a location for a pack name.
	 * <p>
	 * For example:
	 * <code>
	 * FlansMod.registerModelLocation("ww2", "com.flansmod.client.model");
	 * </code>
	 * will set any model name prefixed with "ww2" to be loaded from "com.flansmod.client.model".
	 *
	 * @param name     Content package name.
	 * @param location Fully qualified model location.
	 */
	public static void registerModelLocation(String name, String location) {
		modelLocations.put(name, location);
	}

	public static String getModelLocation(String name) {
		return modelLocations.get(name);
	}
}
