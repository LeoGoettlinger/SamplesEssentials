package xyz.lychee.lagfixer.modules;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import lombok.Generated;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Flying;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.scheduler.BukkitTask;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.utils.ReflectionUtils;

public class MobAiReducerModule extends AbstractModule implements Listener {
   private final EnumSet<EntityType> list = EnumSet.noneOf(EntityType.class);
   private final EnumSet<SpawnReason> reasons = EnumSet.noneOf(SpawnReason.class);
   private final HashSet<String> ai_list = new HashSet<>();
   private final MobAiReducerModule.ErrorFilter filter = new MobAiReducerModule.ErrorFilter();
   private BukkitTask task;
   private MobAiReducerModule.NMS mobAiReducer;
   private boolean async;
   private boolean ignore_models;
   private boolean animals;
   private boolean monsters;
   private boolean villagers;
   private boolean tameable;
   private boolean birds;
   private boolean others;
   private boolean list_mode;
   private boolean force_load;
   private boolean click_event;
   private int purge_interval;
   private boolean collides;
   private boolean silent;
   private boolean keep_dedicated;
   private boolean ai_list_mode;
   private boolean temptEnabled;
   private double temptRange;
   private double temptSpeed;
   private int temptCooldown;
   private boolean temptTriggerBothHands;
   private boolean temptEvent;
   private boolean temptTeleport;
   private boolean breedEnabled;
   private double breedRange;
   private boolean breedEvent;
   private boolean breedTeleport;
   private double breedSpeed;

   public MobAiReducerModule(LagFixer plugin, ModuleManager manager) {
      super(
         plugin,
         manager,
         AbstractModule.Impact.VERY_HIGH,
         "MobAiReducer",
         new String[]{
            "Replaces creature movement to optimize and reduce behavior.",
            "Addresses inefficiencies caused by default animal behavior like unnecessary random movements or constant looking around.",
            "MobAiReducer intervenes by disabling unnecessary PathFinders or replacing them with more efficient ones.",
            "Crucial in scenarios with numerous animals as even minor movements can strain server resources."
         },
         "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGM3NTA1ZjIyNGQ1MTY0YTExN2Q4YzY5ZjAxNWY5OWVmZjQzNDQ3MWM4YTJkZjkwNzA5NmM0MjQyYzM1MjRlOCJ9fX0="
      );
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.HIGHEST
   )
   public void onSpawn(CreatureSpawnEvent e) {
      if (this.reasons.contains(e.getSpawnReason()) && this.isEnabled(e.getEntity()) && this.canContinue(e.getEntity().getWorld())) {
         if (this.async) {
            SupportManager.getInstance().getFork().runNow(true, e.getLocation(), () -> this.mobAiReducer.optimize(e.getEntity(), false));
         } else {
            this.mobAiReducer.optimize(e.getEntity(), false);
         }
      }
   }

   public boolean isEnabled(Entity ent) {
      if ((this.ignore_models || !HookManager.getInstance().hasModel(ent)) && this.list.contains(ent.getType()) == this.list_mode) {
         return switch (ent) {
            case Villager ignored -> this.villagers;
            case Tameable ignoredx -> this.tameable;
            case Flying ignoredxx -> this.birds;
            case Animals ignoredxxx -> this.animals;
            case Monster ignoredxxxx -> this.monsters;
            default -> this.others;
         };
      } else {
         return false;
      }
   }

   public boolean containsFilter(Logger logger) {
      Iterator<Filter> it = logger.getFilters();

      while (it.hasNext()) {
         Filter filter = it.next();
         if (filter instanceof MobAiReducerModule.ErrorFilter) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void load() {
      Logger logger = (Logger)LogManager.getRootLogger();
      if (!this.containsFilter(logger)) {
         logger.addFilter(this.filter);
      }

      Bukkit.getPluginManager().registerEvents(this, this.getPlugin());
      Bukkit.getPluginManager().registerEvents(this.mobAiReducer, this.getPlugin());
      if (this.force_load) {
         this.getAllowedWorlds()
            .forEach(
               w -> SupportManager.getInstance()
                  .getFork()
                  .runNow(
                     false,
                     new Location(w, 0.0, 100.0, 0.0),
                     () -> w.getLivingEntities().stream().filter(this::isEnabled).forEach(ent -> this.mobAiReducer.optimize(ent, true))
                  )
            );
      }

      this.task = SupportManager.getInstance().getFork().runTimer(true, () -> this.mobAiReducer.purge(), 60L, this.purge_interval, TimeUnit.SECONDS);
   }

   @Override
   public boolean loadConfig() {
      this.mobAiReducer = ReflectionUtils.createInstance("MobAiReducer", this);
      this.async = this.getSection().getBoolean("async");
      this.ignore_models = HookManager.getInstance().getModelHook() == null || this.getSection().getBoolean("ignore_models");
      this.animals = this.getSection().getBoolean("entities.animals");
      this.monsters = this.getSection().getBoolean("entities.monsters");
      this.villagers = this.getSection().getBoolean("entities.villagers");
      this.tameable = this.getSection().getBoolean("entities.tameable");
      this.birds = this.getSection().getBoolean("entities.birds");
      this.others = this.getSection().getBoolean("entities.others");
      this.force_load = this.getSection().getBoolean("force_load");
      this.click_event = this.getSection().getBoolean("click_event");
      this.purge_interval = this.getSection().getInt("purge_interval");
      this.list_mode = this.getSection().getBoolean("list_mode");
      ReflectionUtils.convertEnums(EntityType.class, this.list, this.getSection().getStringList("list"));
      ReflectionUtils.convertEnums(SpawnReason.class, this.reasons, this.getSection().getStringList("spawn_reasons"));
      this.collides = this.getSection().getBoolean("collides");
      this.silent = this.getSection().getBoolean("silent");
      this.keep_dedicated = this.getSection().getBoolean("pathfinder.keep_dedicated");
      this.ai_list_mode = this.getSection().getBoolean("pathfinder.list_mode");
      this.ai_list.clear();
      this.ai_list.addAll(this.getSection().getStringList("pathfinder.list"));
      this.temptEnabled = this.getSection().getBoolean("animals.tempt.enabled");
      this.temptRange = this.getSection().getDouble("animals.tempt.range");
      this.temptSpeed = this.getSection().getDouble("animals.tempt.speed");
      this.temptCooldown = this.getSection().getInt("animals.tempt.cooldown");
      this.temptTriggerBothHands = this.getSection().getBoolean("animals.tempt.trigger_two_hands");
      this.temptEvent = this.getSection().getBoolean("animals.tempt.event");
      this.temptTeleport = this.getSection().getBoolean("animals.tempt.teleport");
      this.breedEnabled = this.getSection().getBoolean("animals.breed.enabled");
      this.breedRange = this.getSection().getDouble("animals.breed.range");
      this.breedSpeed = this.getSection().getDouble("animals.breed.speed");
      this.breedEvent = this.getSection().getBoolean("animals.breed.event");
      this.breedTeleport = this.getSection().getBoolean("animals.breed.teleport");
      if (this.mobAiReducer != null) {
         this.mobAiReducer.load();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void disable() {
      HandlerList.unregisterAll(this);
      if (this.task != null && !this.task.isCancelled()) {
         this.task.cancel();
      }

      if (this.mobAiReducer != null) {
         HandlerList.unregisterAll(this.mobAiReducer);
      }
   }

   @Generated
   public EnumSet<EntityType> getList() {
      return this.list;
   }

   @Generated
   public EnumSet<SpawnReason> getReasons() {
      return this.reasons;
   }

   @Generated
   public HashSet<String> getAi_list() {
      return this.ai_list;
   }

   @Generated
   public MobAiReducerModule.ErrorFilter getFilter() {
      return this.filter;
   }

   @Generated
   public BukkitTask getTask() {
      return this.task;
   }

   @Generated
   public MobAiReducerModule.NMS getMobAiReducer() {
      return this.mobAiReducer;
   }

   @Generated
   public boolean isAsync() {
      return this.async;
   }

   @Generated
   public boolean isIgnore_models() {
      return this.ignore_models;
   }

   @Generated
   public boolean isAnimals() {
      return this.animals;
   }

   @Generated
   public boolean isMonsters() {
      return this.monsters;
   }

   @Generated
   public boolean isVillagers() {
      return this.villagers;
   }

   @Generated
   public boolean isTameable() {
      return this.tameable;
   }

   @Generated
   public boolean isBirds() {
      return this.birds;
   }

   @Generated
   public boolean isOthers() {
      return this.others;
   }

   @Generated
   public boolean isList_mode() {
      return this.list_mode;
   }

   @Generated
   public boolean isForce_load() {
      return this.force_load;
   }

   @Generated
   public boolean isClick_event() {
      return this.click_event;
   }

   @Generated
   public int getPurge_interval() {
      return this.purge_interval;
   }

   @Generated
   public boolean isCollides() {
      return this.collides;
   }

   @Generated
   public boolean isSilent() {
      return this.silent;
   }

   @Generated
   public boolean isKeep_dedicated() {
      return this.keep_dedicated;
   }

   @Generated
   public boolean isAi_list_mode() {
      return this.ai_list_mode;
   }

   @Generated
   public boolean isTemptEnabled() {
      return this.temptEnabled;
   }

   @Generated
   public double getTemptRange() {
      return this.temptRange;
   }

   @Generated
   public double getTemptSpeed() {
      return this.temptSpeed;
   }

   @Generated
   public int getTemptCooldown() {
      return this.temptCooldown;
   }

   @Generated
   public boolean isTemptTriggerBothHands() {
      return this.temptTriggerBothHands;
   }

   @Generated
   public boolean isTemptEvent() {
      return this.temptEvent;
   }

   @Generated
   public boolean isTemptTeleport() {
      return this.temptTeleport;
   }

   @Generated
   public boolean isBreedEnabled() {
      return this.breedEnabled;
   }

   @Generated
   public double getBreedRange() {
      return this.breedRange;
   }

   @Generated
   public boolean isBreedEvent() {
      return this.breedEvent;
   }

   @Generated
   public boolean isBreedTeleport() {
      return this.breedTeleport;
   }

   @Generated
   public double getBreedSpeed() {
      return this.breedSpeed;
   }

   private static class ErrorFilter extends AbstractFilter {
      public Result filter(LogEvent event) {
         return event.getThrown() instanceof NullPointerException npe && npe.getMessage() != null && npe.getMessage().contains(".isRunning()")
            ? Result.DENY
            : Result.NEUTRAL;
      }
   }

   public abstract static class NMS implements Listener {
      private final MobAiReducerModule module;

      public NMS(MobAiReducerModule module) {
         this.module = module;
      }

      public abstract void load();

      public abstract void optimize(Entity var1, boolean var2);

      public abstract void purge();

      @Generated
      public MobAiReducerModule getModule() {
         return this.module;
      }
   }
}
