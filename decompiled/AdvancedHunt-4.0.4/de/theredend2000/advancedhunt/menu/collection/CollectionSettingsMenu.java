package de.theredend2000.advancedhunt.menu.collection;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.Menu;
import de.theredend2000.advancedhunt.menu.act.ActRulesMenu;
import de.theredend2000.advancedhunt.menu.common.ConfirmationMenu;
import de.theredend2000.advancedhunt.menu.cron.CronEditorMenu;
import de.theredend2000.advancedhunt.menu.reward.RewardPresetListMenu;
import de.theredend2000.advancedhunt.menu.reward.RewardsMenu;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.model.CollectionRewardHolder;
import de.theredend2000.advancedhunt.model.Reward;
import de.theredend2000.advancedhunt.model.RewardPreset;
import de.theredend2000.advancedhunt.model.RewardPresetType;
import de.theredend2000.advancedhunt.model.TreasureCore;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import de.theredend2000.advancedhunt.util.MessageUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;

public class CollectionSettingsMenu extends Menu {
   private Collection collection;
   private boolean processing = false;

   public CollectionSettingsMenu(Player playerMenuUtility, Main plugin, Collection collection) {
      super(playerMenuUtility, plugin);
      this.collection = collection;
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.settings.title", "%collection%", this.collection.getName());
   }

   @Override
   public void handleMenu(InventoryClickEvent e) {
   }

   @Override
   public int getSlots() {
      return 54;
   }

   @Override
   public void open() {
      this.processing = false;
      super.open();
   }

   @Override
   public void refresh() {
      this.processing = false;
      super.refresh();
   }

   @Override
   public void setMenuItems() {
      this.fillBorders(super.FILLER_GLASS);
      this.addStaticItem(
         10,
         new ItemBuilder(XMaterial.PLAYER_HEAD)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.settings.categories.basic.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.settings.categories.basic.lore", false))
            .setSkullTexture(
               "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGJmOGI2Mjc3Y2QzNjI2NjI4M2NiNWE5ZTY5NDM5NTNjNzgzZTZmZjdkNmEyZDU5ZDE1YWQwNjk3ZTkxZDQzYyJ9fX0="
            )
            .build()
      );
      this.addStaticItem(11, super.EXTRA_GLASS);
      this.addButton(
         12,
         new ItemBuilder(XMaterial.NAME_TAG)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.settings.rename.name"))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.settings.rename.lore", "%name%", this.collection.getName()).toArray(new String[0]))
            .build(),
         e -> this.plugin
            .getChatInputListener()
            .requestInput(
               this.playerMenuUtility,
               input -> this.plugin
                  .getCollectionManager()
                  .renameCollection(this.collection.getName(), input)
                  .thenAccept(
                     success -> Bukkit.getScheduler()
                        .runTask(
                           this.plugin,
                           () -> {
                              if (success) {
                                 this.playerMenuUtility
                                    .sendMessage(
                                       this.plugin
                                          .getMessageManager()
                                          .getMessage("command.rename.success", "%old_name%", this.collection.getName(), "%new_name%", input)
                                    );
                                 this.collection.setName(input);
                              } else {
                                 this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("command.rename.failed"));
                              }

                              this.open();
                           }
                        )
                  )
            ),
         "advancedhunt.admin.collection.rename"
      );
      String status = this.collection.isEnabled()
         ? this.plugin.getMessageManager().getMessage("gui.common.enabled")
         : this.plugin.getMessageManager().getMessage("gui.common.disabled");
      this.addButton(
         13,
         new ItemBuilder(this.collection.isEnabled() ? XMaterial.LIME_DYE.get() : XMaterial.GRAY_DYE.get())
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.settings.status.name"))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.settings.status.lore", "%status%", status).toArray(new String[0]))
            .build(),
         e -> {
            if (!this.processing) {
               this.processing = true;
               this.collection.setEnabled(!this.collection.isEnabled());
               this.plugin.getCollectionManager().saveCollection(this.collection).thenRun(() -> Bukkit.getScheduler().runTask(this.plugin, () -> {
                  if (this.collection.isHideWhenNotAvailable()) {
                     this.plugin.getTreasureVisibilityManager().refreshCollectionVisibility(this.collection);
                  }

                  this.processing = false;
                  this.refresh();
               }));
            }
         }
      );
      String spfStatus = this.collection.isSinglePlayerFind()
         ? this.plugin.getMessageManager().getMessage("gui.common.enabled")
         : this.plugin.getMessageManager().getMessage("gui.common.disabled");
      this.addButton(
         14,
         new ItemBuilder(this.collection.isSinglePlayerFind() ? XMaterial.ENDER_PEARL.get() : XMaterial.ENDER_EYE.get())
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.settings.single_player_find.name"))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.settings.single_player_find.lore", "%status%", spfStatus).toArray(new String[0]))
            .build(),
         e -> {
            if (!this.processing) {
               this.processing = true;
               this.collection.setSinglePlayerFind(!this.collection.isSinglePlayerFind());
               this.plugin.getCollectionManager().saveCollection(this.collection).thenRun(() -> Bukkit.getScheduler().runTask(this.plugin, () -> {
                  if (this.collection.isHideAfterFound()) {
                     this.plugin.getTreasureVisibilityManager().refreshHideAfterFound(this.collection);
                  }

                  this.processing = false;
                  this.refresh();
               }));
            }
         }
      );
      this.addButton(
         15,
         new ItemBuilder(XMaterial.TNT)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.settings.delete.name"))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.settings.delete.lore").toArray(new String[0]))
            .build(),
         e -> new DeleteCollectionHandlingMenu(this.playerMenuUtility, this.plugin, this.collection, false).setPreviousMenu(this).open(),
         "advancedhunt.admin.collection.delete"
      );
      this.addStaticItem(
         19,
         new ItemBuilder(XMaterial.PLAYER_HEAD)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.settings.categories.rewards.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.settings.categories.rewards.lore", false))
            .setSkullTexture(
               "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGJmOGI2Mjc3Y2QzNjI2NjI4M2NiNWE5ZTY5NDM5NTNjNzgzZTZmZjdkNmEyZDU5ZDE1YWQwNjk3ZTkxZDQzYyJ9fX0="
            )
            .build()
      );
      this.addStaticItem(20, super.EXTRA_GLASS);
      this.addButton(
         21,
         new ItemBuilder(XMaterial.CHEST)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.settings.rewards.name"))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.settings.rewards.lore").toArray(new String[0]))
            .build(),
         e -> new RewardsMenu(this.playerMenuUtility, this.plugin, new CollectionRewardHolder(this.plugin, this.collection)).setPreviousMenu(this).open()
      );
      int ruleCount = this.collection.getActRules().size();
      this.addButton(
         22,
         new ItemBuilder(XMaterial.PLAYER_HEAD)
            .setSkullTexture(
               "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzY4N2EyYmNhZjgzZWE4YTMxNzRkMDNkMzkwMDk5MWI2N2U2NWM4Y2ExY2M0ZDk1YTBiMmNiNzE3OTY3YTYyNyJ9fX0="
            )
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.settings.act_rules.name", false))
            .setLore(
               this.plugin
                  .getMessageManager()
                  .getMessageList("gui.settings.act_rules.lore", false, "%count%", String.valueOf(ruleCount))
                  .toArray(new String[0])
            )
            .build(),
         e -> new ActRulesMenu(this.playerMenuUtility, this.plugin, this.collection).setPreviousMenu(this).open()
      );
      String cron = this.collection.getProgressResetCron() != null
         ? "§f" + this.collection.getProgressResetCron()
         : this.plugin.getMessageManager().getMessage("gui.common.none", false);
      this.addButton(
         23,
         new ItemBuilder(XMaterial.CLOCK)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.settings.progress_reset_cron.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.settings.progress_reset_cron.lore", false, "%cron%", cron).toArray(new String[0]))
            .build(),
         e -> new CollectionSettingsMenu.ProgressResetCronMenu(this.playerMenuUtility, this.plugin, this.collection).setPreviousMenu(this).open()
      );
      this.addStaticItem(
         28,
         new ItemBuilder(XMaterial.PLAYER_HEAD)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.settings.categories.presets.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.settings.categories.presets.lore", false))
            .setSkullTexture(
               "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGJmOGI2Mjc3Y2QzNjI2NjI4M2NiNWE5ZTY5NDM5NTNjNzgzZTZmZjdkNmEyZDU5ZDE1YWQwNjk3ZTkxZDQzYyJ9fX0="
            )
            .build()
      );
      this.addStaticItem(29, super.EXTRA_GLASS);
      String defaultPresetName = this.plugin.getMessageManager().getMessage("gui.common.none", false);
      UUID defaultPresetId = this.collection.getDefaultTreasureRewardPresetId();
      if (defaultPresetId != null) {
         defaultPresetName = this.plugin
            .getRewardPresetManager()
            .getPreset(RewardPresetType.TREASURE, defaultPresetId)
            .map(RewardPreset::getName)
            .orElse(this.plugin.getMessageManager().getMessage("gui.presets.unknown", false));
      }

      this.addButton(
         30,
         new ItemBuilder(XMaterial.WRITABLE_BOOK)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.settings.default_treasure_preset.name", false))
            .setLore(
               this.plugin
                  .getMessageManager()
                  .getMessageList("gui.settings.default_treasure_preset.lore", false, "%preset%", "§f" + defaultPresetName)
                  .toArray(new String[0])
            )
            .build(),
         e -> {
            if (!this.playerMenuUtility.hasPermission("advancedhunt.admin")) {
               this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.no_permission"));
            } else if (e.isRightClick()) {
               if (!this.processing) {
                  this.processing = true;
                  this.collection.setDefaultTreasureRewardPresetId(null);
                  this.plugin.getCollectionManager().saveCollection(this.collection).thenRun(() -> Bukkit.getScheduler().runTask(this.plugin, () -> {
                     this.processing = false;
                     this.refresh();
                  }));
               }
            } else {
               new RewardPresetListMenu(this.playerMenuUtility, this.plugin, RewardPresetType.TREASURE, selected -> {
                  if (!this.processing) {
                     this.processing = true;
                     this.collection.setDefaultTreasureRewardPresetId(selected.getId());
                     this.plugin.getCollectionManager().saveCollection(this.collection).thenRun(() -> Bukkit.getScheduler().runTask(this.plugin, () -> {
                        this.processing = false;
                        this.open();
                     }));
                  }
               }, this.collection, true).setPreviousMenu(this).open();
            }
         }
      );
      this.addButton(
         31,
         new ItemBuilder(XMaterial.ANVIL)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.settings.override_treasure_rewards.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.settings.override_treasure_rewards.lore", false).toArray(new String[0]))
            .build(),
         e -> {
            if (!this.playerMenuUtility.hasPermission("advancedhunt.admin")) {
               this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.no_permission"));
            } else {
               new RewardPresetListMenu(
                     this.playerMenuUtility,
                     this.plugin,
                     RewardPresetType.TREASURE,
                     selected -> new ConfirmationMenu(
                           this.playerMenuUtility,
                           this.plugin,
                           this.plugin
                              .getMessageManager()
                              .getMessage("gui.settings.override_treasure_rewards.confirm_title", false, "%name%", selected.getName()),
                           confirmEvent -> {
                              this.playerMenuUtility.closeInventory();
                              this.runBulkOverride(selected);
                           },
                           cancelEvent -> this.open()
                        )
                        .setPreviousMenu(this)
                        .open(),
                     this.collection
                  )
                  .setPreviousMenu(this)
                  .open();
            }
         }
      );
      this.addStaticItem(
         37,
         new ItemBuilder(Material.PLAYER_HEAD)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.settings.categories.visibility.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.settings.categories.visibility.lore", false))
            .setSkullTexture(
               "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGJmOGI2Mjc3Y2QzNjI2NjI4M2NiNWE5ZTY5NDM5NTNjNzgzZTZmZjdkNmEyZDU5ZDE1YWQwNjk3ZTkxZDQzYyJ9fX0="
            )
            .build()
      );
      this.addStaticItem(38, super.EXTRA_GLASS);
      String enabled = this.plugin.getMessageManager().getMessage("gui.common.enabled", false);
      String disabled = this.plugin.getMessageManager().getMessage("gui.common.disabled", false);
      String hafStatus = this.collection.isHideAfterFound() ? enabled : disabled;
      String block = this.plugin.getConfig().getString("treasure-settings.replace-block", "BARRIER").toUpperCase();
      if (!block.equals("BARRIER") && !block.equals("AIR")) {
         block = "BARRIER";
      }

      this.addButton(
         39,
         new ItemBuilder(this.collection.isHideAfterFound() ? Material.POTION : Material.GLASS_BOTTLE)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.settings.hide_after_found.name", false))
            .setLore(
               this.plugin
                  .getMessageManager()
                  .getMessageList("gui.settings.hide_after_found.lore", false, "%status%", hafStatus, "%block%", block)
                  .toArray(new String[0])
            )
            .addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
            .build(),
         e -> {
            if (!this.processing) {
               this.processing = true;
               this.collection.setHideAfterFound(!this.collection.isHideAfterFound());
               this.plugin.getCollectionManager().saveCollection(this.collection).thenRun(() -> Bukkit.getScheduler().runTask(this.plugin, () -> {
                  this.plugin.getTreasureVisibilityManager().refreshHideAfterFound(this.collection);
                  this.processing = false;
                  this.refresh();
               }));
            }
         }
      );
      String hideStatus = this.collection.isHideWhenNotAvailable() ? enabled : disabled;
      this.addButton(
         40,
         new ItemBuilder(this.collection.isHideWhenNotAvailable() ? Material.EXPERIENCE_BOTTLE : Material.GLASS_BOTTLE)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.settings.hide_when_not_available.name", false))
            .setLore(
               this.plugin
                  .getMessageManager()
                  .getMessageList("gui.settings.hide_when_not_available.lore", false, "%status%", hideStatus)
                  .toArray(new String[0])
            )
            .build(),
         e -> {
            if (!this.processing) {
               this.processing = true;
               this.collection.setHideWhenNotAvailable(!this.collection.isHideWhenNotAvailable());
               this.plugin.getCollectionManager().saveCollection(this.collection).thenRun(() -> Bukkit.getScheduler().runTask(this.plugin, () -> {
                  this.plugin.getTreasureVisibilityManager().refreshCollectionVisibility(this.collection);
                  this.processing = false;
                  this.refresh();
               }));
            }
         }
      );
   }

   private void runBulkOverride(RewardPreset preset) {
      List<TreasureCore> cores = this.plugin.getTreasureManager().getTreasureCoresInCollection(this.collection.getId());
      if (cores.isEmpty()) {
         this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.preset.override.none"));
         this.open();
      } else {
         this.playerMenuUtility.closeInventory();
         this.playerMenuUtility
            .sendMessage(
               this.plugin
                  .getMessageManager()
                  .getMessage("feedback.preset.override.started", "%count%", String.valueOf(cores.size()), "%name%", preset.getName())
            );
         List<Reward> rewardSnapshot = preset.getRewards() == null
            ? Collections.emptyList()
            : Collections.unmodifiableList(new ArrayList<>(preset.getRewards()));
         this.plugin
            .getTreasureManager()
            .overrideTreasureRewardsInCollection(
               this.collection.getId(),
               rewardSnapshot,
               50,
               (percentToSend, done) -> Bukkit.getScheduler()
                  .runTask(
                     this.plugin,
                     () -> MessageUtils.sendActionBar(
                        this.playerMenuUtility,
                        this.plugin
                           .getMessageManager()
                           .getMessage(
                              "feedback.preset.override.progress",
                              "%percent%",
                              String.valueOf(percentToSend),
                              "%current%",
                              String.valueOf(done),
                              "%total%",
                              String.valueOf(cores.size())
                           )
                     )
                  )
            )
            .whenComplete(
               (count, ex) -> Bukkit.getScheduler()
                  .runTask(
                     this.plugin,
                     () -> {
                        if (ex != null) {
                           this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.preset.override.failed"));
                        } else {
                           this.playerMenuUtility
                              .sendMessage(
                                 this.plugin
                                    .getMessageManager()
                                    .getMessage("feedback.preset.override.done", "%count%", String.valueOf(count), "%name%", preset.getName())
                              );
                        }
                     }
                  )
            );
      }
   }

   private static class ProgressResetCronMenu extends CronEditorMenu {
      public ProgressResetCronMenu(Player player, Main plugin, Collection collection) {
         super(player, plugin, collection);
      }

      @Override
      public String getMenuName() {
         return this.plugin.getMessageManager().getMessage("gui.settings.reset_schedule.title", false);
      }
   }
}
