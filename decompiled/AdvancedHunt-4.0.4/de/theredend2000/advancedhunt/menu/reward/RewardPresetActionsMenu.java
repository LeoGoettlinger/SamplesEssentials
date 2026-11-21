package de.theredend2000.advancedhunt.menu.reward;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.Menu;
import de.theredend2000.advancedhunt.menu.common.ConfirmationMenu;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.model.PresetRewardHolder;
import de.theredend2000.advancedhunt.model.Reward;
import de.theredend2000.advancedhunt.model.RewardPreset;
import de.theredend2000.advancedhunt.model.RewardPresetType;
import de.theredend2000.advancedhunt.model.TreasureCore;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import de.theredend2000.advancedhunt.util.MessageUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class RewardPresetActionsMenu extends Menu {
   private final RewardPreset preset;
   private final Collection collectionContext;

   public RewardPresetActionsMenu(Player playerMenuUtility, Main plugin, RewardPreset preset, Collection collectionContext) {
      super(playerMenuUtility, plugin);
      this.preset = preset;
      this.collectionContext = collectionContext;
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.presets.actions.title", false, "%name%", this.preset.getName());
   }

   @Override
   public int getSlots() {
      return 27;
   }

   @Override
   public void handleMenu(InventoryClickEvent e) {
   }

   @Override
   public void setMenuItems() {
      this.fillBorders(this.FILLER_GLASS);
      this.addButton(
         10,
         new ItemBuilder(XMaterial.NAME_TAG)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.presets.actions.rename.name", false))
            .setLore(
               this.plugin
                  .getMessageManager()
                  .getMessageList("gui.presets.actions.rename.lore", false, "%current%", this.preset.getName())
                  .toArray(new String[0])
            )
            .build(),
         e -> this.handleRename()
      );
      this.addButton(
         12,
         this.buildActionItem(
            XMaterial.ANVIL.get(),
            this.isTreasurePreset() && this.hasCollectionContext(),
            "gui.presets.actions.load_all.name",
            "gui.presets.actions.load_all.lore",
            "gui.presets.actions.load_all.lore_disabled",
            new String[0]
         ),
         e -> this.handleLoadAll()
      );
      String defaultPresetName = this.plugin.getMessageManager().getMessage("gui.common.none", false);
      boolean isCurrentDefault;
      if (this.hasCollectionContext() && this.isTreasurePreset()) {
         UUID defaultPresetId = this.collectionContext.getDefaultTreasureRewardPresetId();
         defaultPresetName = this.plugin
            .getRewardPresetManager()
            .getPreset(RewardPresetType.TREASURE, defaultPresetId)
            .map(RewardPreset::getName)
            .orElse(this.plugin.getMessageManager().getMessage("gui.presets.unknown", false));
         isCurrentDefault = this.preset.getId().equals(defaultPresetId);
      } else {
         isCurrentDefault = false;
      }

      String optionKey = isCurrentDefault ? "gui.presets.actions.set_default.option.already_selected" : "gui.presets.actions.set_default.option.available";
      boolean enabled = this.isTreasurePreset() && this.hasCollectionContext();
      Material enabledMaterial = isCurrentDefault ? XMaterial.OAK_SAPLING.get() : XMaterial.DEAD_BUSH.get();
      this.addButton(
         13,
         this.buildActionItem(
            enabledMaterial,
            enabled,
            "gui.presets.actions.set_default.name",
            "gui.presets.actions.set_default.lore",
            "gui.presets.actions.set_default.lore_disabled",
            new String[]{"%option%", this.plugin.getMessageManager().getMessage(optionKey, false), "%current%", defaultPresetName}
         ),
         e -> {
            if (!isCurrentDefault) {
               this.handleSetDefault();
            }
         }
      );
      this.addButton(
         14,
         new ItemBuilder(XMaterial.WRITTEN_BOOK)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.presets.actions.edit_rewards.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.presets.actions.edit_rewards.lore", false).toArray(new String[0]))
            .build(),
         e -> new RewardsMenu(this.playerMenuUtility, this.plugin, new PresetRewardHolder(this.plugin, this.preset)).setPreviousMenu(this).open()
      );
      this.addButton(
         16,
         new ItemBuilder(XMaterial.TNT)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.presets.actions.delete.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.presets.actions.delete.lore", false).toArray(new String[0]))
            .build(),
         e -> this.handleDelete()
      );
      this.fillBackground(this.FILLER_GLASS);
   }

   private void handleRename() {
      if (!this.playerMenuUtility.hasPermission("advancedhunt.admin")) {
         this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.no_permission"));
      } else {
         this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.preset.prompt_rename", "%name%", this.preset.getName()));
         this.plugin
            .getChatInputListener()
            .requestInput(
               this.playerMenuUtility,
               input -> {
                  String trimmed = input == null ? null : input.trim();
                  if (trimmed == null || trimmed.isEmpty()) {
                     this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.preset.invalid_name"));
                     Bukkit.getScheduler().runTask(this.plugin, this::open);
                  } else if (this.isNameTaken(trimmed)) {
                     this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.preset.duplicate_name"));
                     Bukkit.getScheduler().runTask(this.plugin, this::open);
                  } else {
                     this.preset.setName(trimmed);
                     this.plugin
                        .getRewardPresetManager()
                        .savePreset(this.preset)
                        .whenComplete(
                           (v, ex) -> Bukkit.getScheduler()
                              .runTask(
                                 this.plugin,
                                 () -> {
                                    if (ex != null) {
                                       this.playerMenuUtility
                                          .sendMessage(
                                             this.plugin.getMessageManager().getMessage("error.preset.save_failed", "%type%", this.getPresetTypeName())
                                          );
                                       this.open();
                                    } else {
                                       this.playerMenuUtility
                                          .sendMessage(this.plugin.getMessageManager().getMessage("feedback.preset.renamed", "%name%", this.preset.getName()));
                                       this.openPreviousMenu();
                                    }
                                 }
                              )
                        );
                  }
               }
            );
      }
   }

   private void handleSetDefault() {
      if (this.isTreasurePreset() && this.hasCollectionContext()) {
         if (!this.playerMenuUtility.hasPermission("advancedhunt.admin")) {
            this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.no_permission"));
         } else {
            this.collectionContext.setDefaultTreasureRewardPresetId(this.preset.getId());
            this.plugin
               .getCollectionManager()
               .saveCollection(this.collectionContext)
               .thenRun(
                  () -> Bukkit.getScheduler()
                     .runTask(
                        this.plugin,
                        () -> {
                           this.playerMenuUtility
                              .sendMessage(
                                 this.plugin
                                    .getMessageManager()
                                    .getMessage(
                                       "feedback.preset.set_default", "%collection%", this.collectionContext.getName(), "%name%", this.preset.getName()
                                    )
                              );
                           this.refresh();
                        }
                     )
               );
         }
      }
   }

   private void handleLoadAll() {
      if (this.isTreasurePreset() && this.hasCollectionContext()) {
         if (!this.playerMenuUtility.hasPermission("advancedhunt.admin")) {
            this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.no_permission"));
         } else {
            new ConfirmationMenu(
                  this.playerMenuUtility,
                  this.plugin,
                  this.plugin
                     .getMessageManager()
                     .getMessage(
                        "gui.presets.actions.load_all.confirm_title", false, "%collection%", this.collectionContext.getName(), "%name%", this.preset.getName()
                     ),
                  confirmEvent -> {
                     this.playerMenuUtility.closeInventory();
                     this.runBulkOverride();
                  },
                  cancelEvent -> this.open()
               )
               .setPreviousMenu(this)
               .open();
         }
      }
   }

   private void runBulkOverride() {
      List<TreasureCore> cores = this.plugin.getTreasureManager().getTreasureCoresInCollection(this.collectionContext.getId());
      if (cores.isEmpty()) {
         this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.preset.override.none"));
         this.open();
      } else {
         this.playerMenuUtility
            .sendMessage(
               this.plugin
                  .getMessageManager()
                  .getMessage("feedback.preset.override.started", "%count%", String.valueOf(cores.size()), "%name%", this.preset.getName())
            );
         List<Reward> rewardSnapshot = this.preset.getRewards() == null
            ? Collections.emptyList()
            : Collections.unmodifiableList(new ArrayList<>(this.preset.getRewards()));
         this.plugin
            .getTreasureManager()
            .overrideTreasureRewardsInCollection(
               this.collectionContext.getId(),
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
                                    .getMessage("feedback.preset.override.done", "%count%", String.valueOf(count), "%name%", this.preset.getName())
                              );
                        }

                        this.openPreviousMenu();
                     }
                  )
            );
      }
   }

   private void handleDelete() {
      if (!this.playerMenuUtility.hasPermission("advancedhunt.admin")) {
         this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.no_permission"));
      } else {
         new ConfirmationMenu(
               this.playerMenuUtility,
               this.plugin,
               this.plugin.getMessageManager().getMessage("gui.presets.actions.delete.confirm_title", false, "%name%", this.preset.getName()),
               confirmEvent -> this.plugin
                  .getRewardPresetManager()
                  .deletePreset(this.preset.getType(), this.preset.getId())
                  .thenRun(() -> Bukkit.getScheduler().runTask(this.plugin, () -> {
                     this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.preset.deleted", "%name%", this.preset.getName()));
                     this.openPreviousMenu();
                  })),
               cancelEvent -> this.open()
            )
            .setPreviousMenu(this)
            .open();
      }
   }

   private boolean hasCollectionContext() {
      return this.collectionContext != null;
   }

   private boolean isTreasurePreset() {
      return this.preset.getType() == RewardPresetType.TREASURE;
   }

   private String getPresetTypeName() {
      return this.plugin.getMessageManager().getMessage(this.preset.getType() == RewardPresetType.COLLECTION ? "common.collection" : "common.treasure", false);
   }

   private boolean isNameTaken(String name) {
      String normalized = normalizeName(name);
      UUID currentId = this.preset.getId();

      for (RewardPreset p : this.plugin.getRewardPresetManager().getPresets(this.preset.getType())) {
         if (!p.getId().equals(currentId) && normalizeName(p.getName()).equals(normalized)) {
            return true;
         }
      }

      return false;
   }

   private static String normalizeName(String name) {
      return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
   }
}
