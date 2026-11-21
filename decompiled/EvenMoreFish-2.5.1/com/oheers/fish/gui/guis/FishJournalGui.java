package com.oheers.fish.gui.guis;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.api.sort.SortType;
import com.oheers.fish.api.utils.Scheduling;
import com.oheers.fish.config.gui.GuiConfig;
import com.oheers.fish.config.gui.impl.JournalFishGuiConfig;
import com.oheers.fish.config.gui.impl.JournalRaritiesGuiConfig;
import com.oheers.fish.database.Database;
import com.oheers.fish.database.data.FishRarityKey;
import com.oheers.fish.database.data.UserFishRarityKey;
import com.oheers.fish.database.model.fish.FishStats;
import com.oheers.fish.database.model.user.UserFishStats;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.gui.ConfigGui;
import com.oheers.fish.items.ItemFactory;
import com.oheers.fish.items.config.DisplayNameItemConfig;
import com.oheers.fish.items.config.ItemConfig;
import com.oheers.fish.items.config.LoreItemConfig;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.plugin.PluginDataManager;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.inventorygui.GuiElement;
import org.evenmorefish.fish.libs.inventorygui.GuiElementGroup;
import org.evenmorefish.fish.libs.inventorygui.InventoryGui;
import org.evenmorefish.fish.libs.inventorygui.StaticGuiElement;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class FishJournalGui extends ConfigGui {
   private final int userId;
   private final IRarity rarity;
   private final SortType sortType;
   private final boolean usePreloadedStatsOnly;

   public static void openAsync(@NonNull Player player, @Nullable IRarity rarity) {
      openAsync(player, rarity, null);
   }

   public static void openAsync(@NonNull Player player, @Nullable IRarity rarity, @Nullable InventoryGui expectedOpenGui) {
      EvenMoreFish plugin = EvenMoreFish.getInstance();
      plugin.debug("Preparing fish journal for %s.".formatted(player.getName()));
      plugin.getPluginDataManager().preloadUserDataAsync(player.getUniqueId()).whenComplete((userId, throwable) -> {
         if (throwable != null) {
            Logging.warn("Could not prepare fish journal for " + player.getName() + ".", throwable);
         } else {
            plugin.debug("Fish journal data prepared for %s with user id %d.".formatted(player.getName(), userId));
            Scheduling.getInstance().runTask(player, () -> {
               try {
                  if (!player.isOnline()) {
                     plugin.debug("Skipping fish journal open for %s because the player is offline.".formatted(player.getName()));
                     return;
                  }

                  if (expectedOpenGui != null && InventoryGui.getOpen(player) != expectedOpenGui) {
                     plugin.debug("Skipping fish journal open for %s because the open GUI changed while data loaded.".formatted(player.getName()));
                     return;
                  }

                  new FishJournalGui(player, rarity, userId, true).open();
               } catch (Exception var6) {
                  Logging.warn("Could not open fish journal for " + player.getName() + ".", var6);
               }
            });
         }
      });
   }

   public FishJournalGui(@NonNull HumanEntity player, @Nullable IRarity rarity) {
      this(player, rarity, EvenMoreFish.getInstance().getPluginDataManager().getUserManager().getUserId(player.getUniqueId()), false);
   }

   private FishJournalGui(@NonNull HumanEntity player, @Nullable IRarity rarity, int userId, boolean usePreloadedStatsOnly) {
      super((GuiConfig)(rarity == null ? JournalRaritiesGuiConfig.getInstance() : JournalFishGuiConfig.getInstance()), player);
      this.rarity = rarity;
      if (rarity != null) {
         this.addReplacement("{rarity}", rarity.getDisplayName());
      }

      this.userId = userId;
      this.usePreloadedStatsOnly = usePreloadedStatsOnly;
      this.createGui();
      Section config = this.getGuiConfig();
      if (config != null) {
         this.sortType = FishUtils.getEnumValue(SortType.class, config.getString("sort-type"), SortType.ALPHABETICAL);
         this.getGui().addElement(this.getGroup(config));
      } else {
         this.sortType = SortType.ALPHABETICAL;
      }
   }

   private GuiElement getGroup(Section section) {
      return this.rarity == null ? this.getRarityGroup(section) : this.getFishGroup(section);
   }

   private GuiElement getFishGroup(Section section) {
      char character = FishUtils.getCharFromString(section.getString("fish-character"), 'f');
      GuiElementGroup group = new GuiElementGroup(character);
      this.sortType.sort(this.rarity.getFishList()).forEach(fish -> {
         if (fish.getShowInJournal()) {
            ItemStack item = this.getFishItem(fish, section);
            if (!item.isEmpty()) {
               group.addElement(new StaticGuiElement(character, item));
            }
         }
      });
      return group;
   }

   @NonNull
   private String getUnknownMessage() {
      return this.getGuiConfig().getString("unknown-message", "Unknown");
   }

   private ItemStack getFishItem(IFish fish, Section section) {
      Database database = this.requireDatabase("Can not show fish in the Journal Menu, please enable the database!");
      if (database == null) {
         return ItemFactory.itemFactory(section, "undiscovered-fish").createItem(this.player.getUniqueId());
      } else {
         boolean hideUndiscovered = section.getBoolean("hide-undiscovered-fish", true);
         if (hideUndiscovered && !this.userHasFish(database, fish)) {
            return ItemFactory.itemFactory(section, "undiscovered-fish").createItem(this.player.getUniqueId());
         } else {
            ItemStack item = fish.give();
            item.editMeta(meta -> {
               ItemFactory factory = ItemFactory.itemFactory(section, "fish-item");
               EMFSingleMessage display = this.prepareDisplay(factory, fish);
               if (display != null) {
                  meta.displayName(display.getComponentMessage(this.player));
               }

               meta.lore(this.prepareLore(factory, fish).getComponentListMessage(this.player));
            });
            return item;
         }
      }
   }

   @Nullable
   private EMFSingleMessage prepareDisplay(@NonNull ItemFactory factory, @NonNull IFish fish) {
      DisplayNameItemConfig displayConfig = factory.getItemConfig(DisplayNameItemConfig.class);
      if (displayConfig == null) {
         return null;
      } else {
         String display = displayConfig.getConfiguredValue();
         if (display == null) {
            return null;
         } else {
            EMFSingleMessage displayMessage = EMFSingleMessage.fromString(display);
            displayMessage.setVariable("{fishname}", fish.getDisplayName());
            return displayMessage;
         }
      }
   }

   private boolean userHasFish(@NonNull Database database, @NonNull IFish fish) {
      PluginDataManager dataManager = EvenMoreFish.getInstance().getPluginDataManager();
      return !this.usePreloadedStatsOnly && !dataManager.isUserFishStatsPreloaded(this.userId)
         ? database.userHasFish(fish.getRarity().getId(), fish.getId(), this.userId)
         : dataManager.getUserFishStatsDataManager().peek(UserFishRarityKey.of(this.userId, fish).toString()) != null;
   }

   @NonNull
   private EMFListMessage prepareLore(@NonNull ItemFactory factory, @NonNull IFish fish) {
      PluginDataManager dataManager = EvenMoreFish.getInstance().getPluginDataManager();
      UserFishStats userFishStats = !this.usePreloadedStatsOnly && !dataManager.isUserFishStatsPreloaded(this.userId)
         ? dataManager.getUserFishStatsDataManager().get(UserFishRarityKey.of(this.userId, fish).toString())
         : dataManager.getUserFishStatsDataManager().peek(UserFishRarityKey.of(this.userId, fish).toString());
      FishStats fishStats = dataManager.isFishStatsPreloaded()
         ? dataManager.getFishStatsDataManager().peek(FishRarityKey.of(fish).toString())
         : dataManager.getFishStatsDataManager().get(FishRarityKey.of(fish).toString());
      String discoverDate = getDiscoverDate(userFishStats, this.getUnknownMessage());
      String discoverer = getDiscoverer(fishStats, this.getUnknownMessage());
      EMFListMessage lore = EMFListMessage.ofList(
         Optional.ofNullable(factory.getItemConfig(LoreItemConfig.class)).map(ItemConfig::getConfiguredValue).orElse(Collections.emptyList())
      );
      lore.setVariable("{times-caught}", this.getValueOrDefault(() -> userFishStats == null ? null : Integer.toString(userFishStats.getQuantity()), "0"));
      lore.setVariable("{largest-size}", this.getValueOrDefault(() -> userFishStats == null ? null : String.valueOf(userFishStats.getLongestLength()), "0"));
      lore.setVariable("{smallest-size}", this.getValueOrDefault(() -> userFishStats == null ? null : String.valueOf(userFishStats.getShortestLength()), "0"));
      lore.setVariable("{discover-date}", discoverDate);
      lore.setVariable("{discoverer}", discoverer);
      lore.setVariable("{server-largest}", this.getValueOrDefault(() -> fishStats == null ? null : String.valueOf(fishStats.getLongestLength()), "0"));
      lore.setVariable("{server-smallest}", this.getValueOrDefault(() -> fishStats == null ? null : String.valueOf(fishStats.getShortestLength()), "0"));
      lore.setVariable("{server-caught}", this.getValueOrDefault(() -> fishStats == null ? null : String.valueOf(fishStats.getQuantity()), "0"));
      return lore;
   }

   @NonNull
   static String getDiscoverDate(@Nullable UserFishStats userFishStats, @NonNull String unknownMessage) {
      return userFishStats == null ? unknownMessage : userFishStats.getFirstCatchTime().format(DateTimeFormatter.ISO_DATE);
   }

   @NonNull
   static String getDiscoverer(@Nullable FishStats fishStats, @NonNull String unknownMessage) {
      return fishStats == null ? unknownMessage : Optional.ofNullable(fishStats.getDiscovererName()).orElse(unknownMessage);
   }

   @NonNull
   private String getValueOrDefault(@NonNull Supplier<String> supplier, @NonNull String def) {
      try {
         return Optional.ofNullable(supplier.get()).orElse(def);
      } catch (Exception var4) {
         EvenMoreFish.getInstance().debug("An exception occurred while getting a value. Defaulting to " + def, var4);
         return def;
      }
   }

   private GuiElement getRarityGroup(Section section) {
      char character = FishUtils.getCharFromString(section.getString("rarity-character"), 'r');
      GuiElementGroup group = new GuiElementGroup(character);
      this.sortType.sort(FishManager.getInstance().getRarityMap().values()).forEach(rarity -> {
         if (rarity.getShowInJournal()) {
            ItemStack item = this.getRarityItem(rarity, section);
            if (!item.isEmpty()) {
               group.addElement(new StaticGuiElement(character, item, click -> {
                  openAsync(this.player, rarity, click.getGui());
                  return true;
               }));
            }
         }
      });
      return group;
   }

   private ItemStack getRarityItem(IRarity rarity, Section section) {
      Database database = this.requireDatabase("Can not show rarities in the Journal Menu, please enable the database!");
      if (database == null) {
         return ItemFactory.itemFactory(section, "undiscovered-rarity").createItem(this.player.getUniqueId());
      } else {
         boolean hideUndiscovered = section.getBoolean("hide-undiscovered-rarity", true);
         if (hideUndiscovered && !this.userHasRarity(database, rarity)) {
            return ItemFactory.itemFactory(section, "undiscovered-rarity").createItem(this.player.getUniqueId());
         } else {
            ItemStack rarityItem = rarity.getJournalItem();
            ItemStack configuredItem = ItemFactory.itemFactory(section, "rarity-item").createItem(this.player.getUniqueId());
            ItemMeta configuredMeta = configuredItem.getItemMeta();
            if (configuredMeta != null) {
               rarityItem.editMeta(meta -> {
                  Component configuredDisplay = configuredMeta.displayName();
                  if (configuredDisplay != null) {
                     EMFSingleMessage display = EMFSingleMessage.of(configuredDisplay);
                     display.setRarity(rarity.getDisplayName());
                     meta.displayName(display.getComponentMessage(this.player));
                  }

                  meta.lore(configuredMeta.lore());
                  if (configuredMeta.hasCustomModelData()) {
                     meta.setCustomModelData(configuredMeta.getCustomModelData());
                  }
               });
            }

            return rarityItem;
         }
      }
   }

   private boolean userHasRarity(@NonNull Database database, @NonNull IRarity rarity) {
      PluginDataManager dataManager = EvenMoreFish.getInstance().getPluginDataManager();
      return !this.usePreloadedStatsOnly && !dataManager.isUserFishStatsPreloaded(this.userId)
         ? database.userHasRarity(rarity.getId(), this.userId)
         : rarity.getFishList()
            .stream()
            .anyMatch(fish -> dataManager.getUserFishStatsDataManager().peek(UserFishRarityKey.of(this.userId, fish).toString()) != null);
   }

   @Override
   public void doRescue() {
   }

   @Nullable
   private Database requireDatabase(String logMessage) {
      Database db = EvenMoreFish.getInstance().getPluginDataManager().getDatabase();
      if (db == null) {
         Logging.warn(logMessage);
      }

      return db;
   }
}
