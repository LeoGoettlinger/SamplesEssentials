package dev.demora.demoralegendaryweapons;

import dev.demora.demoralegendaryweapons.commands.DemoraHopCommand;
import dev.demora.demoralegendaryweapons.commands.DemoraMenuCommand;
import dev.demora.demoralegendaryweapons.commands.DemoraMenuListener;
import dev.demora.demoralegendaryweapons.commands.DemoraReworkMenuCommand;
import dev.demora.demoralegendaryweapons.listeners.LanguageSelectionListener;
import dev.demora.demoralegendaryweapons.listeners.LegendaryItemProtectionListener;
import dev.demora.demoralegendaryweapons.util.WorldGuardHook;
import dev.demora.demoralegendaryweapons.version.VersionManager;
import dev.demora.demoralegendaryweapons.weapons.admonstick.AdmonStick;
import dev.demora.demoralegendaryweapons.weapons.admonstick.AdmonStickListener;
import dev.demora.demoralegendaryweapons.weapons.artemisbow.ArtemisBow;
import dev.demora.demoralegendaryweapons.weapons.artemisbow.ArtemisBowListener;
import dev.demora.demoralegendaryweapons.weapons.beehiveblaster.BeehiveBlaster;
import dev.demora.demoralegendaryweapons.weapons.beehiveblaster.BeehiveBlasterListener;
import dev.demora.demoralegendaryweapons.weapons.chainsawsword.ChainsawSword;
import dev.demora.demoralegendaryweapons.weapons.chainsawsword.ChainsawSwordListener;
import dev.demora.demoralegendaryweapons.weapons.cloudsword.CloudSword;
import dev.demora.demoralegendaryweapons.weapons.cloudsword.CloudSwordListener;
import dev.demora.demoralegendaryweapons.weapons.dragonkatana.DragonKatana;
import dev.demora.demoralegendaryweapons.weapons.dragonkatana.DragonKatanaListener;
import dev.demora.demoralegendaryweapons.weapons.dragonkatana.rework.AwakenedDragonKatana;
import dev.demora.demoralegendaryweapons.weapons.dragonkatana.rework.AwakenedDragonKatanaListener;
import dev.demora.demoralegendaryweapons.weapons.dragonkatana.rework.DragonReworkCommand;
import dev.demora.demoralegendaryweapons.weapons.elementalstaff.ElementalStaff;
import dev.demora.demoralegendaryweapons.weapons.elementalstaff.ElementalStaffListener;
import dev.demora.demoralegendaryweapons.weapons.emeraldblade.EmeraldBlade;
import dev.demora.demoralegendaryweapons.weapons.emeraldblade.EmeraldBladeListener;
import dev.demora.demoralegendaryweapons.weapons.excalibur.Excalibur;
import dev.demora.demoralegendaryweapons.weapons.excalibur.ExcaliburListener;
import dev.demora.demoralegendaryweapons.weapons.freezingchakram.FreezingChakram;
import dev.demora.demoralegendaryweapons.weapons.freezingchakram.FreezingChakramListener;
import dev.demora.demoralegendaryweapons.weapons.fugucannon.FuguCannon;
import dev.demora.demoralegendaryweapons.weapons.fugucannon.FuguCannonListener;
import dev.demora.demoralegendaryweapons.weapons.golemhammer.GolemHammer;
import dev.demora.demoralegendaryweapons.weapons.golemhammer.GolemHammerListener;
import dev.demora.demoralegendaryweapons.weapons.hypnosisstaff.HypnosisStaff;
import dev.demora.demoralegendaryweapons.weapons.hypnosisstaff.HypnosisStaffListener;
import dev.demora.demoralegendaryweapons.weapons.lichstaff.LichStaff;
import dev.demora.demoralegendaryweapons.weapons.lichstaff.LichStaffListener;
import dev.demora.demoralegendaryweapons.weapons.magmaclub.MagmaClub;
import dev.demora.demoralegendaryweapons.weapons.magmaclub.MagmaClubListener;
import dev.demora.demoralegendaryweapons.weapons.midassword.MidasSword;
import dev.demora.demoralegendaryweapons.weapons.midassword.MidasSwordListener;
import dev.demora.demoralegendaryweapons.weapons.mjolnir.Mjolnir;
import dev.demora.demoralegendaryweapons.weapons.mjolnir.MjolnirListener;
import dev.demora.demoralegendaryweapons.weapons.poseidontrident.PoseidonTrident;
import dev.demora.demoralegendaryweapons.weapons.poseidontrident.PoseidonTridentListener;
import dev.demora.demoralegendaryweapons.weapons.ravagerhorn.RavagerHorn;
import dev.demora.demoralegendaryweapons.weapons.ravagerhorn.RavagerHornListener;
import dev.demora.demoralegendaryweapons.weapons.ravagerhorn.RavagerHornUnavailableListener;
import dev.demora.demoralegendaryweapons.weapons.reaperscythe.ReaperScythe;
import dev.demora.demoralegendaryweapons.weapons.reaperscythe.ReaperScytheListener;
import dev.demora.demoralegendaryweapons.weapons.reinforcedelytra.ReinforcedElytra;
import dev.demora.demoralegendaryweapons.weapons.reinforcedelytra.ReinforcedElytraListener;
import dev.demora.demoralegendaryweapons.weapons.reinforcedelytra.rework.ReworkElytra;
import dev.demora.demoralegendaryweapons.weapons.reinforcedelytra.rework.ReworkElytraListener;
import dev.demora.demoralegendaryweapons.weapons.sculkcrossbow.SculkCrossbow;
import dev.demora.demoralegendaryweapons.weapons.sculkweaverslantern.SculkweaversLantern;
import dev.demora.demoralegendaryweapons.weapons.sculkweaverslantern.SculkweaversLanternListener;
import dev.demora.demoralegendaryweapons.weapons.shadowblade.ShadowBlade;
import dev.demora.demoralegendaryweapons.weapons.shadowblade.ShadowBladeListener;
import dev.demora.demoralegendaryweapons.weapons.shrinkray.ShrinkRay;
import dev.demora.demoralegendaryweapons.weapons.shrinkray.ShrinkRayListener;
import dev.demora.demoralegendaryweapons.weapons.toxiccrossbow.ToxicCrossbow;
import dev.demora.demoralegendaryweapons.weapons.toxiccrossbow.ToxicCrossbowListener;
import dev.demora.demoralegendaryweapons.weapons.villagerstaff.VillagerStaff;
import dev.demora.demoralegendaryweapons.weapons.villagerstaff.VillagerStaffListener;
import dev.demora.demoralegendaryweapons.weapons.voidstaff.VoidStaff;
import dev.demora.demoralegendaryweapons.weapons.voidstaff.VoidStaffListener;
import dev.demora.demoralegendaryweapons.weapons.warpick.WarPick;
import dev.demora.demoralegendaryweapons.weapons.warpick.WarPickListener;
import dev.demora.demoralegendaryweapons.weapons.withersickles.WitherSickles;
import dev.demora.demoralegendaryweapons.weapons.withersickles.WitherSicklesListener;
import dev.demora.demoralegendaryweapons.weapons.withersickles.rework.AwakenedWitherSickles;
import dev.demora.demoralegendaryweapons.weapons.withersickles.rework.AwakenedWitherSicklesListener;
import dev.demora.demoralegendaryweapons.weapons.withersickles.rework.SicklesReworkCommand;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.RecipeChoice.ExactChoice;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class DemoraLegendaryWeapons extends JavaPlugin {
   private static DemoraLegendaryWeapons instance;
   private VersionManager versionManager;
   private WorldGuardHook worldGuardHook;
   private LanguageManager languageManager;
   private LanguageSelectionListener languageSelectionListener;
   private SculkCrossbow sculkCrossbow;
   private MidasSword midasSword;
   private MidasSwordListener midasSwordListener;
   private Mjolnir mjolnir;
   private MjolnirListener mjolnirListener;
   private ShadowBlade shadowBlade;
   private ShadowBladeListener shadowBladeListener;
   private ReaperScythe reaperScythe;
   private ReaperScytheListener reaperScytheListener;
   private VillagerStaff villagerStaff;
   private VillagerStaffListener villagerStaffListener;
   private DragonKatana dragonKatana;
   private DragonKatanaListener dragonKatanaListener;
   private Excalibur excalibur;
   private ExcaliburListener excaliburListener;
   private VoidStaff voidStaff;
   private VoidStaffListener voidStaffListener;
   private AwakenedDragonKatana awakenedDragonKatana;
   private AwakenedDragonKatanaListener awakenedDragonKatanaListener;
   private DragonReworkCommand dragonReworkCommand;
   private MagmaClub magmaClub;
   private MagmaClubListener magmaClubListener;
   private ShrinkRay shrinkRay;
   private ShrinkRayListener shrinkRayListener;
   private ChainsawSword chainsawSword;
   private ChainsawSwordListener chainsawSwordListener;
   private WitherSickles witherSickles;
   private WitherSicklesListener witherSicklesListener;
   private AwakenedWitherSickles awakenedWitherSickles;
   private AwakenedWitherSicklesListener awakenedWitherSicklesListener;
   private SicklesReworkCommand sicklesReworkCommand;
   private CloudSword cloudSword;
   private CloudSwordListener cloudSwordListener;
   private HypnosisStaff hypnosisStaff;
   private HypnosisStaffListener hypnosisStaffListener;
   private EmeraldBlade emeraldBlade;
   private EmeraldBladeListener emeraldBladeListener;
   private GolemHammer golemHammer;
   private GolemHammerListener golemHammerListener;
   private ToxicCrossbow toxicCrossbow;
   private ToxicCrossbowListener toxicCrossbowListener;
   private LichStaffListener lichStaffListener;
   private ArtemisBowListener artemisBowListener;
   private BeehiveBlasterListener beehiveBlasterListener;
   private FuguCannonListener fuguCannonListener;
   private PoseidonTridentListener poseidonTridentListener;
   private ReinforcedElytraListener reinforcedElytraListener;
   private ReworkElytraListener reworkElytraListener;
   private RavagerHorn ravagerHorn;
   private RavagerHornListener ravagerHornListener;
   private FreezingChakram freezingChakram;
   private FreezingChakramListener freezingChakramListener;
   private SculkweaversLantern sculkweaversLantern;
   private SculkweaversLanternListener sculkweaversLanternListener;
   private WarPick warPick;
   private WarPickListener warPickListener;
   private AdmonStickListener admonStickListener;
   private ElementalStaffListener elementalStaffListener;
   private LegendaryItemProtectionListener legendaryItemProtectionListener;
   private RavagerHornUnavailableListener ravagerHornUnavailableListener;
   private Object mjolnirInputListener;
   private DemoraMenuCommand demoraMenuCommand;
   private DemoraMenuListener demoraMenuListener;
   private DemoraReworkMenuCommand demoraReworkMenuCommand;

   public void onEnable() {
      instance = this;
      this.saveDefaultConfig();
      this.versionManager = new VersionManager(this);
      this.versionManager.enableModules();
      this.worldGuardHook = new WorldGuardHook(this);
      this.languageManager = new LanguageManager(this);
      this.languageSelectionListener = new LanguageSelectionListener(this);
      this.initializeWeapons();
      this.registerEvents();
      this.registerRecipes();
      this.registerCommands();
      this.startTasks();
      this.showLanguageSelectionToOnlinePlayers();
      this.getLogger().info("DemoraLegendaryWeapons v" + this.getDescription().getVersion());
      this.getLogger().info("By: __DEMORA__");
   }

   public void onDisable() {
      this.getLogger().info("DemoraLegendaryWeapons disabled!");
   }

   private void initializeWeapons() {
      this.sculkCrossbow = new SculkCrossbow(this);
      this.midasSword = new MidasSword(this);
      this.midasSwordListener = new MidasSwordListener(this.midasSword);
      this.mjolnir = new Mjolnir(this);
      this.mjolnirListener = new MjolnirListener(this.mjolnir, this);
      this.shadowBlade = new ShadowBlade(this);
      this.shadowBladeListener = new ShadowBladeListener(this.shadowBlade);
      this.reaperScythe = new ReaperScythe(this);
      this.reaperScytheListener = new ReaperScytheListener(this.reaperScythe);
      this.villagerStaff = new VillagerStaff(this);
      this.villagerStaffListener = new VillagerStaffListener(this.villagerStaff);
      this.dragonKatana = new DragonKatana(this);
      this.dragonKatanaListener = new DragonKatanaListener(this.dragonKatana);
      this.excalibur = new Excalibur(this);
      this.excaliburListener = new ExcaliburListener(this.excalibur);
      this.voidStaff = new VoidStaff(this);
      this.voidStaffListener = new VoidStaffListener(this.voidStaff);
      this.awakenedDragonKatana = new AwakenedDragonKatana(this);
      this.awakenedDragonKatanaListener = new AwakenedDragonKatanaListener(this, this.awakenedDragonKatana);
      this.dragonReworkCommand = new DragonReworkCommand(this);
      this.magmaClub = new MagmaClub(this);
      this.magmaClubListener = new MagmaClubListener(this, this.magmaClub);
      this.shrinkRay = new ShrinkRay(this);
      this.shrinkRayListener = new ShrinkRayListener(this, this.shrinkRay);
      this.chainsawSword = new ChainsawSword(this);
      this.chainsawSwordListener = new ChainsawSwordListener(this, this.chainsawSword);
      this.witherSickles = new WitherSickles(this);
      this.witherSicklesListener = new WitherSicklesListener(this, this.witherSickles);
      this.awakenedWitherSickles = new AwakenedWitherSickles(this);
      this.awakenedWitherSicklesListener = new AwakenedWitherSicklesListener(this, this.awakenedWitherSickles);
      this.sicklesReworkCommand = new SicklesReworkCommand(this);
      this.cloudSword = new CloudSword(this);
      this.cloudSwordListener = new CloudSwordListener(this, this.cloudSword);
      this.hypnosisStaff = new HypnosisStaff(this);
      this.hypnosisStaffListener = new HypnosisStaffListener(this, this.hypnosisStaff);
      this.emeraldBlade = new EmeraldBlade(this);
      this.emeraldBladeListener = new EmeraldBladeListener(this, this.emeraldBlade);
      this.golemHammer = new GolemHammer(this);
      this.golemHammerListener = new GolemHammerListener(this, this.golemHammer);
      this.toxicCrossbow = new ToxicCrossbow(this);
      this.toxicCrossbowListener = new ToxicCrossbowListener(this, this.toxicCrossbow);
      this.lichStaffListener = new LichStaffListener(this);
      this.artemisBowListener = new ArtemisBowListener(this);
      this.beehiveBlasterListener = new BeehiveBlasterListener(this);
      this.fuguCannonListener = new FuguCannonListener(this);
      this.poseidonTridentListener = new PoseidonTridentListener(this);
      this.reinforcedElytraListener = new ReinforcedElytraListener(this);
      this.reworkElytraListener = new ReworkElytraListener(this);
      this.ravagerHorn = new RavagerHorn(this);
      this.ravagerHornUnavailableListener = new RavagerHornUnavailableListener(this, this.ravagerHorn);
      this.freezingChakram = new FreezingChakram(this);
      this.freezingChakramListener = new FreezingChakramListener(this, this.freezingChakram);
      this.sculkweaversLantern = new SculkweaversLantern(this);
      this.sculkweaversLanternListener = new SculkweaversLanternListener(this, this.sculkweaversLantern);
      this.warPick = new WarPick(this);
      this.warPickListener = new WarPickListener(this, this.warPick);
      this.admonStickListener = new AdmonStickListener(this);
      this.elementalStaffListener = new ElementalStaffListener(this);
      this.legendaryItemProtectionListener = new LegendaryItemProtectionListener(this);

      try {
         Class.forName("org.bukkit.event.player.PlayerInputEvent");
         Class<?> inputListenerClass = Class.forName("dev.demora.demoralegendaryweapons.weapons.mjolnir.MjolnirInputListener");
         this.mjolnirInputListener = inputListenerClass.getConstructor(Mjolnir.class, DemoraLegendaryWeapons.class).newInstance(this.getMjolnir(), this);
         this.getServer().getPluginManager().registerEvents((Listener)this.mjolnirInputListener, this);
         this.ravagerHornListener = new RavagerHornListener(this, this.ravagerHorn);
         this.getMjolnir().registerThrowChargeDecayTick(this.ravagerHornListener::applyThrowChargeDecay);
      } catch (ReflectiveOperationException | NoClassDefFoundError var2) {
         this.mjolnirInputListener = null;
         this.ravagerHornListener = null;
         this.getLogger().warning("Ravager Horn feature disabled: requires Paper 1.21.4+");
      }

      this.demoraMenuCommand = new DemoraMenuCommand(this);
      this.demoraMenuListener = new DemoraMenuListener(this, this.demoraMenuCommand);
      this.demoraReworkMenuCommand = new DemoraReworkMenuCommand(this);
   }

   private void registerEvents() {
      this.getServer().getPluginManager().registerEvents(this.sculkCrossbow, this);
      this.getServer().getPluginManager().registerEvents(this.midasSwordListener, this);
      this.getServer().getPluginManager().registerEvents(this.mjolnirListener, this);
      this.getServer().getPluginManager().registerEvents(this.shadowBladeListener, this);
      this.getServer().getPluginManager().registerEvents(this.reaperScytheListener, this);
      this.getServer().getPluginManager().registerEvents(this.villagerStaffListener, this);
      this.getServer().getPluginManager().registerEvents(this.dragonKatanaListener, this);
      this.getServer().getPluginManager().registerEvents(this.excaliburListener, this);
      this.getServer().getPluginManager().registerEvents(this.voidStaffListener, this);
      this.getServer().getPluginManager().registerEvents(this.awakenedDragonKatanaListener, this);
      this.getServer().getPluginManager().registerEvents(this.magmaClubListener, this);
      this.getServer().getPluginManager().registerEvents(this.shrinkRayListener, this);
      this.getServer().getPluginManager().registerEvents(this.chainsawSwordListener, this);
      this.getServer().getPluginManager().registerEvents(this.witherSicklesListener, this);
      this.getServer().getPluginManager().registerEvents(this.awakenedWitherSicklesListener, this);
      this.getServer().getPluginManager().registerEvents(this.cloudSwordListener, this);
      this.getServer().getPluginManager().registerEvents(this.hypnosisStaffListener, this);
      this.getServer().getPluginManager().registerEvents(this.emeraldBladeListener, this);
      this.getServer().getPluginManager().registerEvents(this.golemHammerListener, this);
      this.getServer().getPluginManager().registerEvents(this.toxicCrossbowListener, this);
      this.getServer().getPluginManager().registerEvents(this.lichStaffListener, this);
      this.getServer().getPluginManager().registerEvents(this.artemisBowListener, this);
      this.getServer().getPluginManager().registerEvents(this.beehiveBlasterListener, this);
      this.getServer().getPluginManager().registerEvents(this.fuguCannonListener, this);
      this.getServer().getPluginManager().registerEvents(this.poseidonTridentListener, this);
      this.getServer().getPluginManager().registerEvents(this.reinforcedElytraListener, this);
      this.getServer().getPluginManager().registerEvents(this.reworkElytraListener, this);
      this.getServer().getPluginManager().registerEvents(this.ravagerHornUnavailableListener, this);
      if (this.ravagerHornListener != null) {
         this.getServer().getPluginManager().registerEvents(this.ravagerHornListener, this);
      }

      this.getServer().getPluginManager().registerEvents(this.freezingChakramListener, this);
      this.getServer().getPluginManager().registerEvents(this.sculkweaversLanternListener, this);
      this.getServer().getPluginManager().registerEvents(this.warPickListener, this);
      this.getServer().getPluginManager().registerEvents(this.admonStickListener, this);
      this.getServer().getPluginManager().registerEvents(this.elementalStaffListener, this);
      this.getServer().getPluginManager().registerEvents(this.legendaryItemProtectionListener, this);
      this.getServer().getPluginManager().registerEvents(this.languageSelectionListener, this);
      this.getServer().getPluginManager().registerEvents(this.demoraMenuListener, this);
   }

   private void registerCommands() {
      DemoraHopCommand commandExecutor = new DemoraHopCommand(this);
      this.getCommand("demorahop").setExecutor(commandExecutor);
      this.getCommand("demorahop").setTabCompleter(commandExecutor);
      this.getCommand("dragonrework").setExecutor(this.dragonReworkCommand);
      this.getCommand("sicklesrework").setExecutor(this.sicklesReworkCommand);
      this.getCommand("demoramenu").setExecutor(this.demoraMenuCommand);
      this.getCommand("dm").setExecutor(this.demoraMenuCommand);
      this.getCommand("dmr").setExecutor(this.demoraReworkMenuCommand);
   }

   private void registerRecipes() {
      NamespacedKey key = new NamespacedKey(this, "rework_elytra");
      ShapedRecipe recipe = new ShapedRecipe(key, this.reworkElytraListener.getReworkElytra().createReworkElytra());
      recipe.shape(new String[]{" W ", "REG", " F "});
      recipe.setIngredient('W', Material.NETHER_WART_BLOCK);
      recipe.setIngredient('R', Material.REDSTONE_BLOCK);
      recipe.setIngredient('E', new ExactChoice(this.getReinforcedElytra().createReinforcedElytra()));
      recipe.setIngredient('G', Material.RED_GLAZED_TERRACOTTA);
      recipe.setIngredient('F', Material.FIREWORK_ROCKET);
      this.getServer().addRecipe(recipe);
   }

   private void startTasks() {
      Bukkit.getScheduler().runTaskTimer(this, this.shadowBlade::updateTimers, 0L, 1L);
      Bukkit.getScheduler().runTaskTimer(this, this.dragonKatana::updateSpeedEffects, 0L, 60L);
      this.getMjolnir().startThrowChargeDecayLoop();
      new CooldownDisplayManager(this);
   }

   private void showLanguageSelectionToOnlinePlayers() {
      if (this.languageManager.isLanguageNotSet()) {
         (new BukkitRunnable() {
            public void run() {
               for (Player player : Bukkit.getOnlinePlayers()) {
                  if (player.isOp()) {
                     DemoraLegendaryWeapons.this.languageManager.addPlayerNeedingSelection(player.getUniqueId());
                     DemoraLegendaryWeapons.this.languageManager.showLanguageSelection(player);
                  }
               }
            }
         }).runTaskLater(this, 40L);
      }
   }

   public static DemoraLegendaryWeapons getInstance() {
      return instance;
   }

   public String getMessage(String path) {
      return this.languageManager.getMessage(path);
   }

   public String getMessageWithPrefix(String path) {
      return this.languageManager.getMessageWithPrefix(path);
   }

   public FileConfiguration getReworkConfig() {
      return YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "rework.yml"));
   }

   public void reloadPluginConfig() {
      this.reloadConfig();
      this.shadowBlade.reloadConfig();
      this.awakenedDragonKatana.reloadConfig();
      if (this.awakenedWitherSicklesListener != null) {
         this.awakenedWitherSicklesListener.reloadConfig();
      }

      this.getLogger().info("Config reloaded");
   }

   public SculkCrossbow getSculkCrossbow() {
      return this.sculkCrossbow;
   }

   public MidasSword getMidasSword() {
      return this.midasSword;
   }

   public Mjolnir getMjolnir() {
      return this.mjolnir;
   }

   public ShadowBlade getShadowBlade() {
      return this.shadowBlade;
   }

   public ReaperScythe getReaperScythe() {
      return this.reaperScythe;
   }

   public VillagerStaff getVillagerStaff() {
      return this.villagerStaff;
   }

   public DragonKatana getDragonKatana() {
      return this.dragonKatana;
   }

   public Excalibur getExcalibur() {
      return this.excalibur;
   }

   public VoidStaff getVoidStaff() {
      return this.voidStaff;
   }

   public AwakenedDragonKatana getAwakenedDragonKatana() {
      return this.awakenedDragonKatana;
   }

   public MagmaClub getMagmaClub() {
      return this.magmaClub;
   }

   public ShrinkRay getShrinkRay() {
      return this.shrinkRay;
   }

   public ChainsawSword getChainsawSword() {
      return this.chainsawSword;
   }

   public ChainsawSwordListener getChainsawSwordListener() {
      return this.chainsawSwordListener;
   }

   public WitherSickles getWitherSickles() {
      return this.witherSickles;
   }

   public WitherSicklesListener getWitherSicklesListener() {
      return this.witherSicklesListener;
   }

   public AwakenedWitherSickles getAwakenedWitherSickles() {
      return this.awakenedWitherSickles;
   }

   public AwakenedWitherSicklesListener getAwakenedWitherSicklesListener() {
      return this.awakenedWitherSicklesListener;
   }

   public CloudSword getCloudSword() {
      return this.cloudSword;
   }

   public CloudSwordListener getCloudSwordListener() {
      return this.cloudSwordListener;
   }

   public HypnosisStaff getHypnosisStaff() {
      return this.hypnosisStaff;
   }

   public HypnosisStaffListener getHypnosisStaffListener() {
      return this.hypnosisStaffListener;
   }

   public EmeraldBlade getEmeraldBlade() {
      return this.emeraldBlade;
   }

   public GolemHammer getGolemHammer() {
      return this.golemHammer;
   }

   public GolemHammerListener getGolemHammerListener() {
      return this.golemHammerListener;
   }

   public ToxicCrossbow getToxicCrossbow() {
      return this.toxicCrossbow;
   }

   public ToxicCrossbowListener getToxicCrossbowListener() {
      return this.toxicCrossbowListener;
   }

   public LichStaff getLichStaff() {
      return this.lichStaffListener.getLichStaff();
   }

   public ArtemisBow getArtemisBow() {
      return this.artemisBowListener.getArtemisBow();
   }

   public BeehiveBlaster getBeehiveBlaster() {
      return this.beehiveBlasterListener.getBeehiveBlaster();
   }

   public BeehiveBlasterListener getBeehiveBlasterListener() {
      return this.beehiveBlasterListener;
   }

   public FuguCannon getFuguCannon() {
      return this.fuguCannonListener.getFuguCannon();
   }

   public AdmonStick getAdmonStick() {
      return this.admonStickListener.getAdmonStick();
   }

   public ElementalStaff getElementalStaff() {
      return this.elementalStaffListener.getElementalStaff();
   }

   public boolean isLegendaryWeapon(ItemStack item) {
      return item != null && item.hasItemMeta()
         ? this.sculkCrossbow.isSculkCrossbow(item)
            || this.midasSword.isMidasSword(item)
            || this.mjolnir.isMjolnir(item)
            || this.shadowBlade.isShadowBlade(item)
            || this.reaperScythe.isReaperScythe(item)
            || this.villagerStaff.isVillagerStaff(item)
            || this.dragonKatana.isDragonKatana(item)
            || this.awakenedDragonKatana.isAwakenedDragonKatana(item)
            || this.excalibur.isExcalibur(item)
            || this.voidStaff.isVoidStaff(item)
            || this.magmaClub.isMagmaClub(item)
            || this.shrinkRay.isShrinkRay(item)
            || this.chainsawSword.isChainsawSword(item)
            || this.witherSickles.isWitherSickle(item)
            || this.awakenedWitherSickles.isAwakenedWitherSickle(item)
            || this.cloudSword.isCloudSword(item)
            || this.hypnosisStaff.isHypnosisStaff(item)
            || this.emeraldBlade.isEmeraldBlade(item)
            || this.golemHammer.isGolemHammer(item)
            || this.toxicCrossbow.isToxicCrossbow(item)
            || this.getLichStaff().isLichStaff(item)
            || this.getArtemisBow().isArtemisBow(item)
            || this.getPoseidonTrident().isPoseidonTrident(item)
            || this.getReinforcedElytra().isReinforcedElytra(item)
            || this.getReworkElytra().isReworkElytra(item)
            || this.ravagerHorn.isRavagerHorn(item)
            || this.freezingChakram.isFreezingChakram(item)
            || this.sculkweaversLantern.isSculkweaversLantern(item)
            || this.warPick.isWarPick(item)
            || this.getBeehiveBlaster().isBeehiveBlaster(item)
            || this.getFuguCannon().isFuguCannon(item)
            || this.getAdmonStick().isAdmonStick(item)
            || this.getElementalStaff().isElementalStaff(item)
         : false;
   }

   public PoseidonTrident getPoseidonTrident() {
      return this.poseidonTridentListener.getPoseidonTrident();
   }

   public ReinforcedElytra getReinforcedElytra() {
      return this.reinforcedElytraListener.getReinforcedElytra();
   }

   public ReworkElytra getReworkElytra() {
      return this.reworkElytraListener.getReworkElytra();
   }

   public RavagerHorn getRavagerHorn() {
      return this.ravagerHorn;
   }

   public RavagerHornListener getRavagerHornListener() {
      return this.ravagerHornListener;
   }

   public FreezingChakram getFreezingChakram() {
      return this.freezingChakram;
   }

   public FreezingChakramListener getFreezingChakramListener() {
      return this.freezingChakramListener;
   }

   public SculkweaversLantern getSculkweaversLantern() {
      return this.sculkweaversLantern;
   }

   public SculkweaversLanternListener getSculkweaversLanternListener() {
      return this.sculkweaversLanternListener;
   }

   public WarPick getWarPick() {
      return this.warPick;
   }

   public WarPickListener getWarPickListener() {
      return this.warPickListener;
   }

   public Object getMjolnirInputListener() {
      return this.mjolnirInputListener;
   }

   public LanguageManager getLanguageManager() {
      return this.languageManager;
   }

   public VersionManager getVersionManager() {
      return this.versionManager;
   }

   public WorldGuardHook getWorldGuardHook() {
      return this.worldGuardHook;
   }

   public boolean isRegionProtected(Location location) {
      return this.worldGuardHook != null && this.worldGuardHook.isProtected(location);
   }

   public int getWeaponCooldownSeconds(String weaponId) {
      switch (weaponId) {
         case "sculk_crossbow":
            return this.getConfig().getInt("weapons.sculk_crossbow.cooldown", 20);
         case "mjolnir":
            return this.getConfig().getInt("weapons.mjolnir.cooldown.throw_duration", 10);
         case "shadow_blade":
            return this.getConfig().getInt("weapons.shadow_blade.cooldown", 30);
         case "reaper_scythe":
            return this.getConfig().getInt("weapons.reaper_scythe.cooldown", 40);
         case "villager_staff":
            return this.getConfig().getInt("weapons.villager_staff.cooldown.time", 40);
         case "dragon_katana":
            return this.getConfig().getInt("weapons.dragon_katana.cooldown-seconds", 30);
         case "excalibur":
            return this.getConfig().getInt("weapons.excalibur.cooldown", 20);
         case "void_staff":
            return this.getConfig().getInt("weapons.void_staff.portal_cooldown", 40);
         case "magma_club":
            return this.getConfig().getInt("weapons.magma_club.cooldown", 15);
         case "shrink_ray":
            return this.getConfig().getInt("weapons.shrink_ray.cooldown", 40);
         case "chainsaw_sword":
            return this.getConfig().getInt("weapons.chainsaw_sword.curse-cooldown-seconds", 20);
         case "wither_sickles":
            return this.getConfig().getInt("weapons.wither_sickles.throw_cooldown", 10);
         case "cloud_sword":
            return this.getConfig().getInt("weapons.cloud_sword.cooldown", 25);
         case "hypnosis_staff":
            return this.getConfig().getInt("weapons.hypnosis_staff.cooldown", 5);
         case "golem_hammer":
            return this.getConfig().getInt("weapons.golem_hammer.cooldown", 40);
         case "toxic_crossbow":
            return this.getConfig().getInt("weapons.toxic_crossbow.cooldown", 30);
         case "lich_staff":
            return this.getConfig().getInt("weapons.lich_staff.cooldown_seconds", 30);
         case "poseidon_trident":
            return this.getConfig().getInt("weapons.poseidon_trident.throw_cooldown", 10);
         case "reinforced_elytra":
            return this.getConfig().getInt("weapons.reinforced_elytra.boost_cooldown_seconds", 50);
         case "freezing_chakram":
            return this.getConfig().getInt("weapons.freezing_chakram.cooldown_seconds", 8);
         case "sculkweavers_lantern":
            return this.getConfig().getInt("weapons.sculkweavers_lantern.cooldown_seconds", 60);
         case "war_pick":
            return this.getConfig().getInt("weapons.war_pick.cooldown_seconds", 5);
         case "beehive_blaster":
            return this.getConfig().getInt("weapons.beehive_blaster.cooldown_seconds", 25);
         case "fugu_cannon":
            return this.getConfig().getInt("weapons.fugu_cannon.cooldown_seconds", 12);
         case "ravager_horn":
            return this.getConfig().getInt("weapons.ravager_horn.cooldown_seconds", 40);
         case "awakened_dragon_katana":
            return this.getReworkConfig().getInt("awakened_dragon_katana.teleport_cooldown", 20);
         case "awakened_wither_sickles":
            return this.getReworkConfig().getInt("awakened_wither_sickles.throw_cooldown_seconds", 12);
         case "rework_elytra":
            return this.getReworkConfig().getInt("rework_elytra.boost_cooldown_seconds", 50);
         default:
            return 0;
      }
   }

   public void applyWeaponLore(ItemMeta meta, String weaponId) {
      if (meta != null) {
         List<String> loreLines = this.languageManager.getWeaponLore(weaponId);
         if (!loreLines.isEmpty()) {
            String cooldown = String.valueOf(this.getWeaponCooldownSeconds(weaponId));
            List<String> existing = meta.getLore();
            List<String> result = existing != null ? new ArrayList<>(existing) : new ArrayList<>();

            for (String line : loreLines) {
               result.add(line.replace("{cooldown}", cooldown));
            }

            meta.setLore(result);
         }
      }
   }
}
