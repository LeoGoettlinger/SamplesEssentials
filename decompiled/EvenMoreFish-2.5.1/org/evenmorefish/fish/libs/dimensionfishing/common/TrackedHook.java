package org.evenmorefish.fish.libs.dimensionfishing.common;

import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.skills.fishing.FishingManager;
import com.gmail.nossr50.util.player.UserManager;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.evenmorefish.fish.libs.dimensionfishing.DimensionFishing;
import org.evenmorefish.fish.libs.dimensionfishing.LureTracker;
import org.evenmorefish.fish.libs.dimensionfishing.config.DimensionFishingConfigProvider;
import org.evenmorefish.fish.libs.dimensionfishing.events.LavaFishCaughtEvent;
import org.evenmorefish.fish.libs.dimensionfishing.events.VoidFishCaughtEvent;
import org.evenmorefish.fish.libs.dimensionfishing.state.CatchState;
import org.evenmorefish.fish.libs.dimensionfishing.state.FishingState;
import org.evenmorefish.fish.libs.dimensionfishing.state.impl.NoneFishingState;
import org.evenmorefish.fish.libs.dimensionfishing.util.Keys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class TrackedHook {
   private static final Random RANDOM = new Random();
   private final FishHook hook;
   private final Player player;
   private LureTracker lureTracker;
   private FishingState fishingState = FishingState.NONE;
   private CatchState catchState = CatchState.WAIT;
   private ArmorStand stand;
   private boolean shouldCustomTick;
   private final int voidRequiredLevel;
   private int waitTime = 0;
   private int lureTime = 0;
   private int catchTime = 40;
   private boolean pulled = false;
   private int pullTime = 5;
   private final boolean canLava;
   private final boolean canVoid;

   public TrackedHook(@NotNull Player player, @NotNull FishHook hook, int lureLevel) {
      this.player = player;
      this.hook = hook;
      this.voidRequiredLevel = player.getLocation().getBlockY() - RANDOM.nextInt(4, 6);
      this.waitTime = this.fetchWaitTime(lureLevel);
      this.lureTime = RANDOM.nextInt(hook.getMinLureTime(), hook.getMaxLureTime());
      this.canLava = this.canLavaFish();
      this.canVoid = this.canVoidFish();
      this.shouldCustomTick = this.canLava || this.canVoid;
   }

   @NotNull
   public FishHook getFishHook() {
      return this.hook;
   }

   @NotNull
   public Player getPlayer() {
      return this.player;
   }

   @Nullable
   public ArmorStand getStand() {
      return this.stand;
   }

   @NotNull
   public FishingState getFishingState() {
      return this.fishingState;
   }

   @NotNull
   public CatchState getCatchState() {
      return this.catchState;
   }

   public void tick() {
      if (this.shouldCustomTick) {
         if (this.hook.getOwnerUniqueId() != null && this.hook.isValid() && !this.hook.isOnGround() && !this.isHookedToNormalEntity()) {
            if (this.pulled) {
               if (this.pullTime > 0) {
                  this.pullTime--;
               } else {
                  this.stand.teleport(this.stand.getLocation().add(0.0, 0.3, 0.0));
                  this.pulled = false;
               }
            }

            if (this.fishingState instanceof NoneFishingState) {
               if (this.isLava()) {
                  this.attachToStand();
                  this.fishingState = FishingState.LAVA;
                  this.lureTracker = new LureTracker(this);
               } else if (this.isVoid()) {
                  this.attachToStand();
                  this.fishingState = FishingState.VOID;
                  this.lureTracker = new LureTracker(this);
               } else if (this.isWater()) {
                  this.shouldCustomTick = false;
               }
            } else {
               switch (this.catchState) {
                  case WAIT:
                     this.waitTime--;
                     if (this.waitTime <= 0) {
                        this.catchState = CatchState.LURE;
                     }
                     break;
                  case LURE:
                     if (this.lureTracker != null) {
                        this.lureTracker.tick();
                     }

                     this.lureTime--;
                     if (this.lureTime <= 0) {
                        this.fishingState.playBiteSound(this.player);
                        this.stand.teleport(this.stand.getLocation().add(0.0, -0.3, 0.0));
                        this.pulled = true;
                        this.catchState = CatchState.CATCH;
                     }
                     break;
                  case CATCH:
                     this.catchTime--;
                     if (this.catchTime <= 0) {
                        this.fishingState.playSwallowSound(this.player);
                        this.invalidate();
                     }
               }
            }
         } else {
            this.shouldCustomTick = false;
         }
      }
   }

   public void reel() {
      this.shouldCustomTick = false;
      this.invalidate();
      if (this.catchState == CatchState.CATCH) {
         if (!this.checkMcMMOOverfishing()) {
            if (!this.fishingState.callEvent(this)) {
               this.fishingState.playSwallowSound(this.player);
               this.invalidate();
            }
         }
      }
   }

   private boolean isHookedToNormalEntity() {
      Entity hooked = this.hook.getHookedEntity();
      return hooked == null ? false : !hooked.equals(this.stand);
   }

   private boolean isLava() {
      return this.canLava && this.hook.getLocation().getBlock().getType() == Material.LAVA;
   }

   private boolean isWater() {
      return this.hook.getLocation().getBlock().getType() == Material.WATER;
   }

   private boolean isVoid() {
      return this.canVoid && this.hook.getLocation().getBlockY() <= this.voidRequiredLevel;
   }

   private boolean canLavaFish() {
      DimensionFishingConfigProvider config = DimensionFishing.getInstance().getConfigProvider();
      return config.isLavaEnabled()
         && LavaFishCaughtEvent.hasListeners()
         && config.getLavaPredicate().test(this.player)
         && FishingState.LAVA.checkPermission(this.player)
         && FishingState.LAVA.checkWorld(this.hook.getWorld());
   }

   private boolean canVoidFish() {
      DimensionFishingConfigProvider config = DimensionFishing.getInstance().getConfigProvider();
      return config.isVoidEnabled()
         && VoidFishCaughtEvent.hasListeners()
         && config.getVoidPredicate().test(this.player)
         && FishingState.VOID.checkPermission(this.player)
         && FishingState.VOID.checkWorld(this.hook.getWorld());
   }

   public void invalidate() {
      this.shouldCustomTick = false;
      this.stand.remove();
      this.hook.remove();
   }

   public boolean isShouldCustomTick() {
      return this.shouldCustomTick;
   }

   public int getRemainingWaitTime() {
      return this.waitTime;
   }

   public int getRemainingLureTime() {
      return this.lureTime;
   }

   public int getRemainingCatchTime() {
      return this.catchTime;
   }

   private void attachToStand() {
      if (this.stand == null) {
         ArmorStand stand = this.createNewStand(this.hook.getLocation());
         this.hook.setHookedEntity(stand);
         stand.addPassenger(this.hook);
         this.hook.setVisualFire(false);
         this.hook.setFireTicks(0);
         this.stand = stand;
      }
   }

   private ArmorStand createNewStand(@NonNull Location location) {
      ArmorStand stand = (ArmorStand)location.getWorld().spawn(location, ArmorStand.class);
      stand.setInvisible(true);
      stand.setInvulnerable(true);
      stand.setVisualFire(false);
      stand.setPersistent(false);
      stand.setGravity(false);
      stand.setAI(false);
      stand.getPersistentDataContainer().set(Keys.STAND_KEY, PersistentDataType.BOOLEAN, true);
      AttributeInstance scale = stand.getAttribute(Attribute.GENERIC_SCALE);
      if (scale != null) {
         scale.setBaseValue(0.1);
      }

      return stand;
   }

   private int fetchWaitTime(int lureLevel) {
      int ticksPerLureLevel = 100;
      int reduceTicks = lureLevel * ticksPerLureLevel;
      int maxWaitTime = Math.max(0, this.hook.getMaxWaitTime() - reduceTicks);
      int minWaitTime = Math.max(0, this.hook.getMinWaitTime() - reduceTicks);
      return minWaitTime == maxWaitTime ? minWaitTime : RANDOM.nextInt(minWaitTime, maxWaitTime);
   }

   private boolean checkMcMMOOverfishing() {
      if (!Bukkit.getPluginManager().isPluginEnabled("mcMMO")) {
         return false;
      } else if (!DimensionFishing.getInstance().hasMcMMODependency()) {
         return false;
      } else if (!ExperienceConfig.getInstance().isFishingExploitingPrevented()) {
         return false;
      } else {
         McMMOPlayer mmoPlayer = UserManager.getPlayer(this.player);
         if (mmoPlayer == null) {
            return false;
         } else {
            FishingManager manager = mmoPlayer.getFishingManager();
            manager.processExploiting(this.hook.getLocation().toVector());
            if (manager.isExploitingFishing()) {
               this.player
                  .sendMessage(
                     LocaleLoader.getString("Fishing.ScarcityTip", new Object[]{ExperienceConfig.getInstance().getFishingExploitingOptionMoveRange()})
                  );
               return true;
            } else {
               return false;
            }
         }
      }
   }
}
