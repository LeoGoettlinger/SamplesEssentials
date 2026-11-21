package de.oliver.fancynpcs.api;

import de.oliver.fancylib.RandomUtils;
import de.oliver.fancylib.serverSoftware.ServerSoftware;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.api.actions.ActionTrigger;
import de.oliver.fancynpcs.api.actions.NpcAction;
import de.oliver.fancynpcs.api.actions.executor.ActionExecutor;
import de.oliver.fancynpcs.api.events.NpcInteractEvent;
import de.oliver.fancynpcs.api.utils.Interval;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

public abstract class Npc {
   private static final NpcAttribute INVISIBLE_ATTRIBUTE = FancyNpcsPlugin.get().getAttributeManager().getAttributeByName(EntityType.PLAYER, "invisible");
   private static final char[] localNameChars = new char[]{
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'k', 'l', 'm', 'n', 'o', 'r'
   };
   protected final Map<UUID, Boolean> isTeamCreated = new ConcurrentHashMap<>();
   protected final Map<UUID, Boolean> isVisibleForPlayer = new ConcurrentHashMap<>();
   protected final Map<UUID, Boolean> isLookingAtPlayer = new ConcurrentHashMap<>();
   protected final Map<UUID, Long> lastPlayerInteraction = new ConcurrentHashMap<>();
   private final Translator translator = FancyNpcsPlugin.get().getTranslator();
   protected NpcData data;
   protected boolean saveToFile;

   public Npc(NpcData data) {
      this.data = data;
      this.saveToFile = true;
   }

   protected String generateLocalName() {
      StringBuilder localName = new StringBuilder();

      for (int i = 0; i < 8; i++) {
         localName.append('&').append(localNameChars[(int)RandomUtils.randomInRange(0.0, localNameChars.length)]);
      }

      return ChatColor.translateAlternateColorCodes('&', localName.toString());
   }

   public abstract void create();

   public abstract void spawn(Player var1);

   public void spawnForAll() {
      FancyNpcsPlugin.get().getNpcThread().submit(() -> {
         for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            this.spawn(onlinePlayer);
         }
      });
   }

   public abstract void remove(Player var1);

   public void removeForAll() {
      for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
         this.remove(onlinePlayer);
      }
   }

   protected boolean shouldBeVisible(Player player) {
      if (!this.data.getVisibility().canSee(player, this)) {
         return false;
      } else {
         int visibilityDistance = this.data.getVisibilityDistance() > -1
            ? this.data.getVisibilityDistance()
            : FancyNpcsPlugin.get().getFancyNpcConfig().getVisibilityDistance();
         if (visibilityDistance == 0) {
            return false;
         } else if (!this.data.isSpawnEntity()) {
            return false;
         } else if (this.data.getLocation() == null) {
            return false;
         } else if (player.getLocation().getWorld() != this.data.getLocation().getWorld()) {
            return false;
         } else {
            return visibilityDistance != Integer.MAX_VALUE
                  && this.data.getLocation().distanceSquared(player.getLocation()) > visibilityDistance * visibilityDistance
               ? false
               : !FancyNpcsPlugin.get().getFancyNpcConfig().isSkipInvisibleNpcs()
                  || !this.data.getAttributes().getOrDefault(INVISIBLE_ATTRIBUTE, "false").equalsIgnoreCase("true")
                  || this.data.isGlowing()
                  || !this.data.getEquipment().isEmpty();
         }
      }
   }

   public void checkAndUpdateVisibility(Player player) {
      FancyNpcsPlugin.get().getNpcThread().submit(() -> {
         boolean shouldBeVisible = this.shouldBeVisible(player);
         boolean wasVisible = this.isVisibleForPlayer.getOrDefault(player.getUniqueId(), false);
         if (shouldBeVisible && !wasVisible) {
            this.spawn(player);
            if (ServerSoftware.isFolia() && FancyNpcsPlugin.get().getFeatureFlagConfig().getFeatureFlag("enable-folia-visibility-fix").isEnabled()) {
               FancyNpcsPlugin.get().getNpcThread().schedule(() -> {
                  this.remove(player);
                  this.spawn(player);
               }, 100L, TimeUnit.MILLISECONDS);
            }
         } else if (!shouldBeVisible && wasVisible) {
            this.remove(player);
         }
      });
   }

   public void checkAndUpdateVisibilityForAll() {
      for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
         this.checkAndUpdateVisibility(onlinePlayer);
      }
   }

   public abstract void lookAt(Player var1, Location var2);

   public abstract void update(Player var1, boolean var2);

   public void update(Player player) {
      this.update(player, FancyNpcsPlugin.get().getFancyNpcConfig().isSwingArmOnUpdate());
   }

   public void updateForAll(boolean swingArm) {
      for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
         this.update(onlinePlayer, swingArm);
      }
   }

   public void updateForAll() {
      this.updateForAll(FancyNpcsPlugin.get().getFancyNpcConfig().isSwingArmOnUpdate());
   }

   public abstract void move(Player var1, boolean var2);

   public void move(Player player) {
      this.move(player, FancyNpcsPlugin.get().getFancyNpcConfig().isSwingArmOnUpdate());
   }

   public void moveForAll(boolean swingArm) {
      for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
         this.move(onlinePlayer, swingArm);
      }
   }

   public void moveForAll() {
      this.moveForAll(FancyNpcsPlugin.get().getFancyNpcConfig().isSwingArmOnUpdate());
   }

   public void interact(Player player) {
      this.interact(player, ActionTrigger.CUSTOM);
   }

   public void interact(Player player, ActionTrigger actionTrigger) {
      if (this.data.getInteractionCooldown() > 0.0F) {
         long interactionCooldownMillis = (long)(this.data.getInteractionCooldown() * 1000.0F);
         long lastInteractionMillis = this.lastPlayerInteraction.getOrDefault(player.getUniqueId(), 0L);
         Interval interactionCooldownLeft = Interval.between(
            lastInteractionMillis + interactionCooldownMillis, System.currentTimeMillis(), Interval.Unit.MILLISECONDS
         );
         if (interactionCooldownLeft.as(Interval.Unit.MILLISECONDS) > 0.0) {
            if (!FancyNpcsPlugin.get().getFancyNpcConfig().isInteractionCooldownMessageDisabled()) {
               this.translator.translate("interaction_on_cooldown").replace("time", interactionCooldownLeft.toString()).send(player);
            }

            return;
         }

         this.lastPlayerInteraction.put(player.getUniqueId(), System.currentTimeMillis());
      }

      List<NpcAction.NpcActionData> actions = this.data.getActions(actionTrigger);
      if (new NpcInteractEvent(this, this.data.getOnClick(), actions, player, actionTrigger).callEvent()) {
         if (this.data.getOnClick() != null) {
            this.data.getOnClick().accept(player);
         }

         ActionExecutor.execute(actionTrigger, this, player);
         if (actionTrigger == ActionTrigger.LEFT_CLICK || actionTrigger == ActionTrigger.RIGHT_CLICK) {
            ActionExecutor.execute(ActionTrigger.ANY_CLICK, this, player);
         }
      }
   }

   protected abstract void refreshEntityData(Player var1);

   public abstract int getEntityId();

   public NpcData getData() {
      return this.data;
   }

   public abstract float getEyeHeight();

   public Map<UUID, Boolean> getIsTeamCreated() {
      return this.isTeamCreated;
   }

   public Map<UUID, Boolean> getIsVisibleForPlayer() {
      return this.isVisibleForPlayer;
   }

   public boolean isShownFor(Player player) {
      return this.isVisibleForPlayer.getOrDefault(player.getUniqueId(), false);
   }

   public Map<UUID, Boolean> getIsLookingAtPlayer() {
      return this.isLookingAtPlayer;
   }

   public Map<UUID, Long> getLastPlayerInteraction() {
      return this.lastPlayerInteraction;
   }

   public boolean isDirty() {
      return this.data.isDirty();
   }

   public void setDirty(boolean dirty) {
      this.data.setDirty(dirty);
   }

   public boolean isSaveToFile() {
      return this.saveToFile;
   }

   public void setSaveToFile(boolean saveToFile) {
      this.saveToFile = saveToFile;
   }

   @ApiStatus.Internal
   protected void runOnPlayerScheduler(Player player, Runnable task) {
      if (ServerSoftware.isFolia()) {
         player.getScheduler().run(FancyNpcsPlugin.get().getPlugin(), t -> task.run(), null);
      } else {
         task.run();
      }
   }
}
