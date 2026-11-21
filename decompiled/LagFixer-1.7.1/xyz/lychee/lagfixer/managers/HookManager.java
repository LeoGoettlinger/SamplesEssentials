package xyz.lychee.lagfixer.managers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.hooks.BetterModelHook;
import xyz.lychee.lagfixer.hooks.ModelEngineHook;
import xyz.lychee.lagfixer.hooks.MythicMobsHook;
import xyz.lychee.lagfixer.hooks.PacketEventsHook;
import xyz.lychee.lagfixer.hooks.PlaceholderAPIHook;
import xyz.lychee.lagfixer.hooks.RoseStackerHook;
import xyz.lychee.lagfixer.hooks.SparkHook;
import xyz.lychee.lagfixer.hooks.StackMobHook;
import xyz.lychee.lagfixer.hooks.UltimateStackerHook;
import xyz.lychee.lagfixer.hooks.WildStackerHook;
import xyz.lychee.lagfixer.objects.AbstractHook;
import xyz.lychee.lagfixer.objects.AbstractManager;
import xyz.lychee.lagfixer.utils.TimingUtil;

public class HookManager extends AbstractManager {
   private static HookManager instance;
   private final Map<String, AbstractHook> hooks = new HashMap<>();
   private final Map<Class<? extends AbstractHook>, AbstractHook> loadedHooks = new HashMap<>();
   private HookManager.StackerContainer stackerHook;
   private HookManager.ModelContainer modelHook;

   public HookManager(LagFixer plugin) {
      super(plugin);
      instance = this;
      this.add(
         new ModelEngineHook(plugin, this),
         new MythicMobsHook(plugin, this),
         new BetterModelHook(plugin, this),
         new WildStackerHook(plugin, this),
         new RoseStackerHook(plugin, this),
         new UltimateStackerHook(plugin, this),
         new StackMobHook(plugin, this),
         new PlaceholderAPIHook(plugin, this),
         new SparkHook(plugin, this),
         new PacketEventsHook(plugin, this)
      );
   }

   protected void add(AbstractHook... hooks) {
      for (AbstractHook hook : hooks) {
         this.hooks.put(hook.getName(), hook);
      }
   }

   @Nullable
   public <T extends AbstractHook> T getHookIfLoaded(Class<T> clazz) {
      return this.loadedHooks.containsKey(clazz) ? clazz.cast(this.loadedHooks.get(clazz)) : null;
   }

   public boolean hasModel(Entity entity) {
      HookManager.ModelContainer model = this.getModelHook();
      return model != null && model.hasModel(entity);
   }

   public void loadHook(AbstractHook hook) throws Exception {
      hook.load();
      if (hook instanceof HookManager.ModelContainer model) {
         this.modelHook = model;
      } else if (hook instanceof HookManager.StackerContainer stacker) {
         this.stackerHook = stacker;
      } else {
         this.loadedHooks.put((Class<? extends AbstractHook>)hook.getClass(), hook);
      }
   }

   public void unloadHook(AbstractHook hook) {
      hook.disable();
      if (hook instanceof HookManager.ModelContainer) {
         this.modelHook = null;
      } else if (hook instanceof HookManager.StackerContainer) {
         this.stackerHook = null;
      } else {
         this.loadedHooks.remove(hook.getClass());
      }
   }

   @Override
   public void load() {
      for (AbstractHook hook : this.hooks.values()) {
         if (hook.isSupported()) {
            try {
               TimingUtil t = TimingUtil.startNew();
               this.loadHook(hook);
               this.getPlugin().getLogger().info(" &8• &rSuccessfully loaded hook " + hook.getName() + " in " + t.stop() + "!");
            } catch (Throwable var4) {
               this.unloadHook(hook);
               this.getPlugin().getLogger().info(" &8• &cError with enabling hook " + hook.getName() + ", reason: " + var4.getMessage());
               this.getPlugin().printError(var4);
            }
         }
      }
   }

   @Override
   public void disable() {
      for (AbstractHook hook : this.hooks.values()) {
         if (hook.isSupported()) {
            TimingUtil t = TimingUtil.startNew();
            this.unloadHook(hook);
            this.getPlugin().getLogger().info(" &8• &rSuccessfully disabled hook " + hook.getName() + " in " + t.stop() + "!");
         }
      }
   }

   @Override
   public boolean isEnabled() {
      return true;
   }

   @Generated
   public Map<String, AbstractHook> getHooks() {
      return this.hooks;
   }

   @Generated
   public Map<Class<? extends AbstractHook>, AbstractHook> getLoadedHooks() {
      return this.loadedHooks;
   }

   @Generated
   public HookManager.StackerContainer getStackerHook() {
      return this.stackerHook;
   }

   @Generated
   public HookManager.ModelContainer getModelHook() {
      return this.modelHook;
   }

   @Generated
   public static HookManager getInstance() {
      return instance;
   }

   public interface ModelContainer {
      boolean hasModel(Entity var1);
   }

   public interface StackerContainer {
      void addItemsToList(Item var1, Collection<ItemStack> var2);

      boolean isStacked(LivingEntity var1);
   }
}
