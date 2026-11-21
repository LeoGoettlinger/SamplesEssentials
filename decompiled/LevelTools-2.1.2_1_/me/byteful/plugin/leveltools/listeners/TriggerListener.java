package me.byteful.plugin.leveltools.listeners;

import java.util.List;
import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.block.BlockDataManager;
import me.byteful.plugin.leveltools.api.block.BlockPosition;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.api.trigger.Trigger;
import me.byteful.plugin.leveltools.api.trigger.TriggerContext;
import me.byteful.plugin.leveltools.api.trigger.TriggerRegistry;
import me.byteful.plugin.leveltools.api.trigger.TriggerSlot;
import me.byteful.plugin.leveltools.profile.ProfileManager;
import me.byteful.plugin.leveltools.profile.item.ItemProfile;
import me.byteful.plugin.leveltools.profile.trigger.TriggerProfile;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TriggerListener implements Listener {
   private final ProfileManager profileManager;
   private final TriggerRegistry triggerRegistry;
   private final XPHandler xpHandler;
   private final BlockDataManager blockDataManager;

   public TriggerListener(@NotNull ProfileManager profileManager, @NotNull TriggerRegistry triggerRegistry, @NotNull BlockDataManager blockDataManager) {
      this.profileManager = profileManager;
      this.triggerRegistry = triggerRegistry;
      this.xpHandler = new XPHandler(profileManager);
      this.blockDataManager = blockDataManager;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onBlockBreak(BlockBreakEvent event) {
      Player player = event.getPlayer();
      Block block = event.getBlock();
      if (player.hasPermission("leveltools.enabled")) {
         if (LevelToolsPlugin.getInstance().getConfig().getBoolean("playerPlacedBlocks")
            || !this.blockDataManager.isPlacedBlock(BlockPosition.fromBukkit(block))) {
            this.handleTrigger("block_break", player, player.getItemInHand(), TriggerSlot.HAND, block, event);
            this.handleTrigger("farming", player, player.getItemInHand(), TriggerSlot.HAND, block, event);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onEntityDeath(EntityDeathEvent event) {
      Player killer = event.getEntity().getKiller();
      if (killer != null) {
         if (killer.hasPermission("leveltools.enabled")) {
            this.handleTrigger("entity_kill", killer, killer.getItemInHand(), TriggerSlot.HAND, event.getEntity(), event);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerFish(PlayerFishEvent event) {
      if (event.getState() == State.CAUGHT_FISH) {
         Player player = event.getPlayer();
         if (player.hasPermission("leveltools.enabled")) {
            Entity caught = event.getCaught();
            ItemStack item;
            TriggerSlot slot;
            if (LevelToolsUtil.supportsDualWielding() && event.getHand() != null) {
               item = event.getPlayer().getInventory().getItem(event.getHand());
               slot = TriggerSlot.fromBukkit(event.getHand());
            } else {
               item = event.getPlayer().getItemInHand();
               slot = TriggerSlot.HAND;
            }

            this.handleTrigger("fishing", player, item, slot, caught, event);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      if (player.hasPermission("leveltools.enabled")) {
         Action action = event.getAction();
         Block clickedBlock = event.getClickedBlock();
         TriggerSlot slot = LevelToolsUtil.supportsDualWielding() && event.getHand() != null ? TriggerSlot.fromBukkit(event.getHand()) : TriggerSlot.HAND;
         ItemStack item = event.getItem();
         if (item != null) {
            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
               this.handleTrigger("right_click", player, item, slot, clickedBlock, event);
               this.handleTrigger("farming", player, item, slot, clickedBlock, event);
            } else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
               this.handleTrigger("left_click", player, item, slot, clickedBlock, event);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
      Player player = event.getPlayer();
      if (player.hasPermission("leveltools.enabled")) {
         TriggerSlot slot = !LevelToolsUtil.supportsDualWielding() ? TriggerSlot.HAND : TriggerSlot.fromBukkit(event.getHand());
         ItemStack consumedItem = event.getItem();
         this.handleTrigger("consume", player, consumedItem, slot, consumedItem, event);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerItemDamage(PlayerItemDamageEvent event) {
      Player player = event.getPlayer();
      if (player.hasPermission("leveltools.enabled")) {
         ItemStack damagedItem = event.getItem();
         if (damagedItem.getType() != Material.AIR) {
            TriggerSlot armorSlot = this.findArmorSlot(player, damagedItem);
            if (armorSlot != null) {
               this.handleTrigger("armor_durability", player, damagedItem, armorSlot, damagedItem.getType(), event);
            }
         }
      }
   }

   private void handleTrigger(
      @NotNull String triggerId, @NotNull Player player, @NotNull ItemStack item, @Nullable TriggerSlot slot, @Nullable Object source, @NotNull Event event
   ) {
      if (item.getType() != Material.AIR) {
         ItemProfile itemProfile = this.profileManager.getProfileForMaterial(item.getType());
         if (itemProfile != null) {
            List<TriggerProfile> triggerProfiles = this.profileManager.getTriggerProfilesFor(itemProfile);
            if (!triggerProfiles.isEmpty()) {
               Trigger trigger = this.triggerRegistry.get(triggerId);
               if (trigger != null) {
                  double totalModifier = 0.0;

                  for (TriggerProfile triggerProfile : triggerProfiles) {
                     if (triggerId.equals(triggerProfile.getTriggerId()) && triggerProfile.getSlotFilter().matches(slot)) {
                        TriggerContext context = TriggerContext.builder()
                           .player(player)
                           .item(item)
                           .triggerId(triggerId)
                           .slot(slot)
                           .source(source)
                           .originalEvent(event)
                           .itemProfile(itemProfile)
                           .triggerProfile(triggerProfile)
                           .build();
                        if (trigger.canHandle(context)) {
                           totalModifier += trigger.calculateXpModifier(context);
                        }
                     }
                  }

                  if (!(totalModifier <= 0.0)) {
                     LevelToolsItem tool = LevelToolsUtil.createLevelToolsItem(item);
                     this.xpHandler.handle(player, itemProfile, slot, tool, totalModifier);
                  }
               }
            }
         }
      }
   }

   @Nullable
   private TriggerSlot findArmorSlot(@NotNull Player player, @NotNull ItemStack itemStack) {
      if (this.matchesItem(player.getInventory().getHelmet(), itemStack)) {
         return TriggerSlot.HELMET;
      } else if (this.matchesItem(player.getInventory().getChestplate(), itemStack)) {
         return TriggerSlot.CHESTPLATE;
      } else if (this.matchesItem(player.getInventory().getLeggings(), itemStack)) {
         return TriggerSlot.LEGGINGS;
      } else {
         return this.matchesItem(player.getInventory().getBoots(), itemStack) ? TriggerSlot.BOOTS : null;
      }
   }

   private boolean matchesItem(@Nullable ItemStack item, @NotNull ItemStack check) {
      return item != null && item.equals(check);
   }
}
