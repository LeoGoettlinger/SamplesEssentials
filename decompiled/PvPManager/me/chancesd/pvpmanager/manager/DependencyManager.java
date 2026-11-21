package me.chancesd.pvpmanager.manager;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.DependencyException;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.hook.EssentialsHook;
import me.chancesd.pvpmanager.integration.hook.GriefPreventionHook;
import me.chancesd.pvpmanager.integration.hook.HuskClaimsHook;
import me.chancesd.pvpmanager.integration.hook.HuskTownsHook;
import me.chancesd.pvpmanager.integration.hook.LibsDisguisesHook;
import me.chancesd.pvpmanager.integration.hook.SimpleClansHook;
import me.chancesd.pvpmanager.integration.hook.TABHook;
import me.chancesd.pvpmanager.integration.hook.TownyHook;
import me.chancesd.pvpmanager.integration.hook.VaultHook;
import me.chancesd.pvpmanager.integration.hook.placeholderapi.PlaceHolderAPIHook;
import me.chancesd.pvpmanager.integration.hook.worldguard.WorldGuardFlagHandler;
import me.chancesd.pvpmanager.integration.hook.worldguard.WorldGuardModernHook;
import me.chancesd.pvpmanager.integration.type.AFKDependency;
import me.chancesd.pvpmanager.integration.type.Dependency;
import me.chancesd.pvpmanager.integration.type.DisguiseDependency;
import me.chancesd.pvpmanager.integration.type.ForceToggleDependency;
import me.chancesd.pvpmanager.integration.type.GodDependency;
import me.chancesd.pvpmanager.integration.type.GroupDependency;
import me.chancesd.pvpmanager.integration.type.RegionDependency;
import me.chancesd.pvpmanager.integration.type.WorldGuardDependency;
import me.chancesd.pvpmanager.listener.MoveListener;
import me.chancesd.pvpmanager.listener.MoveListener1_9;
import me.chancesd.pvpmanager.player.ProtectionType;
import me.chancesd.pvpmanager.sdutils.scheduler.ScheduleUtils;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.sdutils.utils.MCVersion;
import me.chancesd.pvpmanager.sdutils.utils.Utils;
import me.chancesd.pvpmanager.setting.Conf;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class DependencyManager {
   private final Map<Hook, Dependency> dependencies = new EnumMap<>(Hook.class);
   private final ArrayList<GroupDependency> attackChecks = new ArrayList<>();
   private final ArrayList<RegionDependency> regionChecks = new ArrayList<>();
   private final ArrayList<GodDependency> godChecks = new ArrayList<>();
   private final ArrayList<DisguiseDependency> disguiseChecks = new ArrayList<>();
   private final ArrayList<ForceToggleDependency> togglePvPChecks = new ArrayList<>();
   private final ArrayList<AFKDependency> afkChecks = new ArrayList<>();

   public void onLoadSetup() {
      if (Hook.WORLDGUARD.getPlugin() != null
         && Utils.isVersionAtLeast(Utils.stripTags(Hook.WORLDGUARD.getVersion()), "7.0")
         && !PvPManager.getInstance().isReloading()) {
         WorldGuardFlagHandler.initializeFlags();
      }
   }

   public void onEnableSetup() {
      if (Bukkit.getPluginManager().getPlugin("TAB-Bridge") != null && (Conf.NAMETAG_COMBAT_ENABLED.asBool() || Conf.TOGGLE_NAMETAG_ENABLED.asBool())) {
         Log.info("TAB-Bridge detected. Nametags in combat disabled due to incompatibility. Use the prefix/suffix placeholders instead");
         Conf.NAMETAG_COMBAT_ENABLED.disable();
         Conf.TOGGLE_NAMETAG_ENABLED.disable();
      }

      List<Hook> failedHooks = this.setupHooks(Hook.values());
      ScheduleUtils.runPlatformTask(
         () -> {
            Hook[] hooks = Arrays.stream(Hook.values())
               .filter(hook -> hook.isEnabled() && !this.isDependencyEnabled(hook) && !failedHooks.contains(hook))
               .toArray(Hook[]::new);
            if (hooks.length != 0) {
               List<String> softDepend = PvPManager.getInstance().getDescription().getSoftDepend();
               Stream<Hook> missingHooks = Arrays.stream(hooks).filter(h -> softDepend.contains(h.toString()));
               if (missingHooks.count() > 0L) {
                  Log.severe(
                     "Found plugins that PvPManager declared as soft dependencies but were loaded out of order: " + missingHooks.map(Hook::toString).toList()
                  );
                  Log.severe("If you're not using any server flags that cause this, please report it to the server software developers.");
               }

               Log.infoColor(ChatColor.LIGHT_PURPLE + "Delayed checking for any missing hooks...");
               this.setupHooks(hooks);
            }
         }
      );
   }

   public void onDisableCleanup() {
      if (Hook.WORLDGUARD.isEnabled() && Utils.isVersionAtLeast(Utils.stripTags(Hook.WORLDGUARD.getVersion()), "7.0")) {
         WorldGuardFlagHandler.shutdown();
      }
   }

   private List<Hook> setupHooks(Hook... hooks) {
      List<Hook> failedHooks = new ArrayList<>();

      for (Hook hook : hooks) {
         try {
            if (!hook.isEnabled()) {
               if (hook.getDisabledWarning() != null) {
                  Log.warning(hook.getDisabledWarning());
               }

               hook.getDisabledAction().run();
            } else {
               this.attemptHookingInto(hook);
            }
         } catch (NoSuchMethodError | ClassCastException | NoSuchFieldError | NoClassDefFoundError var8) {
            Log.warning("Your " + hook + " version is currently unsupported: " + hook.getDescription().getFullName());
            Log.warning(hook + " support disabled");
            failedHooks.add(hook);
         } catch (DependencyException var9) {
            Log.warning(var9.getMessage());
            hook.getDisabledAction().run();
            failedHooks.add(hook);
         } catch (ExceptionInInitializerError var10) {
            Log.warning("Exception initializing " + hook);
         } catch (LinkageError | Exception var11) {
            Log.warning("Failed to enable support for " + hook.getDescription().getFullName() + ". Is " + hook + " up to date?", var11);
            failedHooks.add(hook);
         }
      }

      return failedHooks;
   }

   private boolean attemptHookingInto(Hook hook) {
      Dependency dependency = (Dependency)(switch (hook) {
         case SIMPLECLANS -> {
            SimpleClansHook var19 = new SimpleClansHook(hook);
            yield var19;
         }
         case VAULT -> {
            VaultHook var18 = new VaultHook(hook);
            yield var18;
         }
         case WORLDGUARD -> {
            if (Utils.isVersionAtLeast(Utils.stripTags(hook.getVersion()), "7.0")) {
               WorldGuardModernHook var16 = new WorldGuardModernHook(hook);
               yield var16;
            } else {
               WorldGuardDependency var17;
               try {
                  Class<?> clazz = Class.forName("me.chancesd.pvpmanager.integration.hook.worldguard.WorldGuardLegacyHook");
                  Constructor<?> constructor = clazz.getConstructor(Hook.class);
                  WorldGuardDependency legacyHook = (WorldGuardDependency)constructor.newInstance(hook);
                  var17 = legacyHook;
               } catch (Exception var7) {
                  throw new DependencyException("Failed to load WorldGuardLegacyHook", var7, hook);
               }

               yield var17;
            }
         }
         case ESSENTIALS -> {
            EssentialsHook var15 = new EssentialsHook(hook);
            yield var15;
         }
         case PLACEHOLDERAPI -> {
            PlaceHolderAPIHook var14 = new PlaceHolderAPIHook(hook);
            yield var14;
         }
         case LIBSDISGUISES -> {
            LibsDisguisesHook var13 = new LibsDisguisesHook(hook);
            yield var13;
         }
         case TOWNY -> {
            TownyHook var12 = new TownyHook(hook);
            yield var12;
         }
         case HUSKTOWNS -> {
            HuskTownsHook var11 = new HuskTownsHook(hook);
            yield var11;
         }
         case HUSKCLAIMS -> {
            HuskClaimsHook var10 = new HuskClaimsHook(hook);
            yield var10;
         }
         case GRIEFPREVENTION -> {
            GriefPreventionHook var9 = new GriefPreventionHook(hook);
            yield var9;
         }
         case TAB -> {
            TABHook var8 = new TABHook(hook);
            yield var8;
         }
         default -> {
            BaseDependency var3 = new BaseDependency(hook);
            yield var3;
         }
      });
      return this.registerDependency(dependency);
   }

   public final boolean canAttack(Player attacker, Player defender) {
      for (GroupDependency pvPlugin : this.attackChecks) {
         if (!pvPlugin.canAttack(attacker, defender)) {
            return false;
         }
      }

      return true;
   }

   public final boolean canAttackAt(Player p, Location l) {
      for (RegionDependency regionPlugin : this.regionChecks) {
         if (!regionPlugin.canAttackAt(p, l)) {
            return false;
         }
      }

      return true;
   }

   public final void disableGodMode(Player p) {
      for (GodDependency godPlugin : this.godChecks) {
         if (godPlugin.hasGodMode(p)) {
            godPlugin.disableGodMode(p);
         }
      }
   }

   public final void disableDisguise(Player p) {
      for (DisguiseDependency disguisePlugin : this.disguiseChecks) {
         if (disguisePlugin.isDisguised(p)) {
            disguisePlugin.unDisguise(p);
         }
      }
   }

   public final boolean shouldDisableProtection(@NotNull Player attacker, @NotNull Player defender, ProtectionType reason) {
      for (ForceToggleDependency togglePvPPlugin : this.togglePvPChecks) {
         if (togglePvPPlugin.shouldDisable(attacker, defender, reason)) {
            return true;
         }
      }

      return false;
   }

   public final boolean shouldDisableProtection(Player defender) {
      for (ForceToggleDependency togglePvPPlugin : this.togglePvPChecks) {
         if (togglePvPPlugin.shouldDisable(defender)) {
            return true;
         }
      }

      return false;
   }

   public final boolean shouldProtectAFK(Player player) {
      for (AFKDependency afkPlugin : this.afkChecks) {
         if (afkPlugin.isAFK(player)) {
            return true;
         }
      }

      return false;
   }

   public void startListeners(PvPManager plugin) {
      if (Conf.PUSHBACK_ENABLED.asBool() && !this.regionChecks.isEmpty()) {
         if (MCVersion.isAtLeast(MCVersion.V1_9)) {
            Bukkit.getPluginManager().registerEvents(new MoveListener1_9(plugin.getPlayerManager(), this), plugin);
         } else if (MCVersion.isAtLeast(MCVersion.V1_8)) {
            Bukkit.getPluginManager().registerEvents(new MoveListener(plugin.getPlayerManager()), plugin);
         } else {
            Log.warning("Pushback on border hopping not available for 1.7.10 or below! Feature disabled!");
            Conf.PUSHBACK_ENABLED.disable();
         }
      }

      if (this.isDependencyEnabled(Hook.WORLDGUARD)) {
         ((WorldGuardDependency)this.getDependency(Hook.WORLDGUARD)).startListener(plugin.getPlayerManager());
      }
   }

   public boolean isDependencyEnabled(Hook d) {
      return this.dependencies.containsKey(d);
   }

   public Dependency getDependency(Hook h) {
      return this.dependencies.get(h);
   }

   public boolean registerDependency(Dependency dep) {
      this.dependencies.put(dep.getHook(), dep);
      if (dep instanceof GroupDependency pvpHook) {
         this.attackChecks.add(pvpHook);
      }

      if (dep instanceof RegionDependency regionHook) {
         this.regionChecks.add(regionHook);
      }

      if (dep instanceof GodDependency godHook) {
         this.godChecks.add(godHook);
      }

      if (dep instanceof DisguiseDependency disguiseHook) {
         this.disguiseChecks.add(disguiseHook);
      }

      if (dep instanceof ForceToggleDependency togglePvPHook && togglePvPHook.shouldDisableProtection()) {
         this.togglePvPChecks.add(togglePvPHook);
      }

      if (dep instanceof AFKDependency afkHook && afkHook.shouldProtectAFK()) {
         this.afkChecks.add(afkHook);
      }

      return true;
   }

   public void unregisterDependency(Dependency dep) {
      this.dependencies.remove(dep.getHook());
      this.attackChecks.remove(dep);
      this.regionChecks.remove(dep);
      this.godChecks.remove(dep);
      this.disguiseChecks.remove(dep);
      this.togglePvPChecks.remove(dep);
   }

   public JavaPlugin getDependencyMainClass(Hook h) {
      return this.isDependencyEnabled(h) ? this.dependencies.get(h).getPlugin() : null;
   }

   public final Economy getEconomy() {
      return this.isDependencyEnabled(Hook.VAULT) ? ((VaultHook)this.dependencies.get(Hook.VAULT)).getEconomy() : null;
   }

   public Collection<Dependency> getDependencies() {
      return this.dependencies.values();
   }
}
