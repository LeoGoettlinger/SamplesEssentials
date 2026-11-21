package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.commands.ReloadCommand;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.InitializeConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.magmacore.config.ConfigurationEngine;
import com.magmaguy.elitemobs.magmacore.config.CustomConfigFields;
import com.magmaguy.elitemobs.magmacore.menus.ContentPackage;
import com.magmaguy.elitemobs.magmacore.menus.NightbreakSetupIcons;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakAccount;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakContentManager;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakManagedContent;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.SpigotMessage;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

public abstract class EMPackage extends ContentPackage implements NightbreakManagedContent {
   private static final String ELITEMOBS_CONTENT_PAGE = "https://nightbreak.io/plugin/elitemobs/";
   protected static final Map<String, EMPackage> content = new ConcurrentHashMap<>();
   private static final Map<String, EMPackage> emPackages = new ConcurrentHashMap<>();
   protected final ContentPackagesConfigFields contentPackagesConfigFields;
   protected volatile boolean isDownloaded;
   protected volatile boolean isInstalled;
   protected volatile boolean outOfDate = false;
   protected volatile NightbreakAccount.AccessInfo cachedAccessInfo = null;
   protected List<CustomBossEntity> customBossEntityList = new ArrayList<>();
   protected List<TreasureChest> treasureChestList = new ArrayList<>();
   protected List<NPCEntity> npcEntities = new ArrayList<>();
   private static EMPackage.BulkToggle bulkToggle;

   public EMPackage(ContentPackagesConfigFields contentPackagesConfigFields) {
      this.contentPackagesConfigFields = contentPackagesConfigFields;
      emPackages.put(contentPackagesConfigFields.getFilename(), this);
      this.baseInitialization();
   }

   public static EMPackage getContent(String data) {
      return content.get(data);
   }

   public static void shutdown() {
      EMPackage.BulkToggle batch = bulkToggle;
      bulkToggle = null;
      if (batch != null) {
         if (batch.flushTask != null) {
            batch.flushTask.cancel();
         }

         if (batch.worker != null) {
            reportSaveFailures(batch.worker.join());
         }
      }

      content.clear();
      emPackages.clear();
   }

   protected static boolean bulkMemberTogglesLocked() {
      return bulkToggle != null && bulkToggle.worker != null;
   }

   protected static void submitBulkMemberSaves(Player player, Collection<? extends CustomConfigFields> members, boolean enabled, String reloadingMessage) {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("Content toggles require the server thread");
      } else if (bulkMemberTogglesLocked()) {
         throw new IllegalStateException("Content toggle already saving");
      } else {
         Map<Path, EMPackage.MemberIntent> incoming = new LinkedHashMap<>();

         for (CustomConfigFields fields : members) {
            try {
               incoming.put(fields.getFile().getCanonicalFile().toPath(), new EMPackage.MemberIntent(fields, enabled));
            } catch (IOException var9) {
               throw new UncheckedIOException("Cannot identify content file " + fields.getFile(), var9);
            }
         }

         if (bulkToggle == null) {
            EMPackage.BulkToggle batch = new EMPackage.BulkToggle(player, reloadingMessage);
            bulkToggle = batch;

            try {
               batch.flushTask = Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> flushBulkMemberSaves(batch));
            } catch (RuntimeException var8) {
               bulkToggle = null;
               throw var8;
            }
         }

         bulkToggle.intents.putAll(incoming);
      }
   }

   private static void flushBulkMemberSaves(EMPackage.BulkToggle batch) {
      if (bulkToggle == batch && !MetadataHandler.shutdownRequested) {
         List<EMPackage.MemberSave> saves = new ArrayList<>();

         try {
            for (Entry<Path, EMPackage.MemberIntent> entry : batch.intents.entrySet()) {
               EMPackage.MemberIntent intent = entry.getValue();
               if (intent.fields().isEnabled() != intent.enabled()) {
                  YamlConfiguration snapshot = new YamlConfiguration();
                  snapshot.loadFromString(intent.fields().getWritableFileConfiguration().saveToString());
                  snapshot.set("isEnabled", intent.enabled());
                  saves.add(new EMPackage.MemberSave(intent, entry.getKey().toFile(), snapshot.saveToString()));
               }
            }
         } catch (Exception var6) {
            bulkToggle = null;
            Logger.warn("Content toggle could not be prepared; no member was saved: " + var6);
            if (batch.player != null && batch.player.isOnline()) {
               Logger.sendMessage(batch.player, DungeonsConfig.getContentConfigurationSaveFailedMessage());
            }

            return;
         }

         if (saves.isEmpty()) {
            bulkToggle = null;
         } else {
            batch.worker = CompletableFuture.supplyAsync(() -> {
               List<EMPackage.MemberResult> results = new ArrayList<>();

               for (EMPackage.MemberSave save : saves) {
                  try {
                     ConfigurationEngine.fileSaverSerialized(save.contents(), save.file());
                     results.add(new EMPackage.MemberResult(save, null));
                  } catch (RuntimeException var5x) {
                     results.add(new EMPackage.MemberResult(save, var5x));
                  }
               }

               return List.copyOf(results);
            });
            batch.worker.thenAccept(results -> {
               if (!MetadataHandler.shutdownRequested) {
                  Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
                     if (bulkToggle == batch && !MetadataHandler.shutdownRequested) {
                        bulkToggle = null;
                        boolean failed = reportSaveFailures((List<EMPackage.MemberResult>)results);
                        boolean changed = false;

                        for (EMPackage.MemberResult result : results) {
                           if (result.failure() == null) {
                              EMPackage.MemberIntent intentx = result.save().intent();
                              intentx.fields().setEnabled(intentx.enabled());
                              intentx.fields().getWritableFileConfiguration().set("isEnabled", intentx.enabled());
                              changed = true;
                           }
                        }

                        if (batch.player != null && batch.player.isOnline()) {
                           Logger.sendMessage(batch.player, failed ? DungeonsConfig.getContentConfigurationSaveFailedMessage() : batch.reloadingMessage);
                        }

                        if (changed) {
                           ReloadCommand.reload((CommandSender)(batch.player != null && batch.player.isOnline() ? batch.player : Bukkit.getConsoleSender()));
                        }
                     }
                  });
               }
            });
         }
      }
   }

   private static boolean reportSaveFailures(List<EMPackage.MemberResult> results) {
      boolean failed = false;

      for (EMPackage.MemberResult result : results) {
         if (result.failure() != null) {
            failed = true;
            Logger.warn("Content member was not changed: " + result.save().file() + ": " + result.failure());
         }
      }

      if (failed) {
         Logger.warn("Content toggle partially failed; successful members remain saved. Failed files are listed above.");
      }

      return failed;
   }

   public static Set<String> getMetaPackageChildFilenames() {
      Set<String> childFilenames = new HashSet<>();

      for (EMPackage pkg : emPackages.values()) {
         List<String> contained = pkg.getContentPackagesConfigFields().getContainedPackages();
         if (contained != null) {
            childFilenames.addAll(contained);
         }
      }

      return childFilenames;
   }

   public static void initialize(ContentPackagesConfigFields contentPackagesConfigFields) {
      if (contentPackagesConfigFields.getContentType() != null) {
         switch (contentPackagesConfigFields.getContentType()) {
            case INSTANCED_DUNGEON:
               new WorldInstancedDungeonPackage(contentPackagesConfigFields);
               break;
            case DYNAMIC_DUNGEON:
               new DynamicDungeonPackage(contentPackagesConfigFields);
               break;
            case OPEN_DUNGEON:
               new WorldDungeonPackage(contentPackagesConfigFields);
               break;
            case HUB:
               new WorldPackage(contentPackagesConfigFields);
               break;
            case SCHEMATIC_DUNGEON:
               Logger.warn(
                  "Tried to load schematic dungeon "
                     + contentPackagesConfigFields.getFilename()
                     + "! This will not work because schematic dungeons have been removed as of EliteMobs 9.0 and replaced with world dungeons. If you want the schematic dungeon experience, I recommend you use BetterStructures with the elite shrines packages, which work better than schematics ever could. Fix this by deleting it from the dungeonpackager file."
               );
               break;
            case META_PACKAGE:
               new MetaPackage(contentPackagesConfigFields);
               break;
            case ITEMS_PACKAGE:
               new ItemsPackage(contentPackagesConfigFields);
               break;
            case EVENTS_PACKAGE:
               new EventsPackage(contentPackagesConfigFields);
               break;
            case MODELS_PACKAGE:
               new ModelsPackage(contentPackagesConfigFields);
         }
      }
   }

   protected static void notify(Player player, String message) {
      if (player != null) {
         player.sendMessage(message);
      } else {
         Logger.info(message);
      }
   }

   public void setupMenuToggle(Player player) {
      switch (this.getContentState()) {
         case INSTALLED:
            this.doUninstall(player);
            break;
         case NOT_INSTALLED:
            this.doInstall(player);
            break;
         case NEEDS_ACCESS:
         case OUT_OF_DATE_NO_ACCESS:
            this.doShowAccessInfo(player);
            break;
         default:
            this.doDownload(player);
      }
   }

   @Override
   protected ItemStack getInstalledItemStack() {
      return this.generateItemStackWithIcon(this.installedLore(), Material.GREEN_STAINED_GLASS_PANE, "nightbreak:ui/checkmark");
   }

   @Override
   protected ItemStack getNotInstalledItemStack() {
      return this.generateItemStackWithIcon(
         List.of(DungeonsConfig.getContentNotInstalledLine1(), DungeonsConfig.getContentNotInstalledLine2()),
         Material.YELLOW_STAINED_GLASS_PANE,
         "nightbreak:ui/gray_x"
      );
   }

   @Override
   protected ItemStack getPartiallyInstalledItemStack() {
      return this.generateItemStackWithIcon(
         List.of(
            DungeonsConfig.getContentPartialLine1(),
            DungeonsConfig.getContentPartialLine2(),
            DungeonsConfig.getContentPartialLine3(),
            DungeonsConfig.getContentPartialLine4()
         ),
         Material.ORANGE_STAINED_GLASS_PANE,
         "nightbreak:ui/gray_x"
      );
   }

   @Override
   protected ItemStack getNotDownloadedItemStack() {
      String slug = this.contentPackagesConfigFields.getNightbreakSlug();
      String modelId;
      if (slug == null || slug.isEmpty()) {
         modelId = "nightbreak:ui/unlocked";
      } else if (!NightbreakAccount.hasToken()) {
         modelId = "nightbreak:ui/lockedunlinked";
      } else if (this.cachedAccessInfo != null && this.cachedAccessInfo.hasAccess) {
         modelId = "nightbreak:ui/unlocked";
      } else {
         modelId = "nightbreak:ui/lockedunpaid";
      }

      return this.generateItemStackWithIcon(
         List.of(DungeonsConfig.getContentNotDownloadedLine1(), DungeonsConfig.getContentNotDownloadedLine2()), Material.YELLOW_STAINED_GLASS_PANE, modelId
      );
   }

   @Override
   protected ItemStack getNeedsAccessItemStack() {
      List<String> tooltip = new ArrayList<>();
      tooltip.add(DungeonsConfig.getContentNeedAccessMessage());
      tooltip.add(DungeonsConfig.getContentAccessClickMessage());
      if (this.cachedAccessInfo != null) {
         if (this.cachedAccessInfo.patreonLink != null && !this.cachedAccessInfo.patreonLink.isEmpty()) {
            tooltip.add(DungeonsConfig.getContentAvailablePatreon());
         }

         if (this.cachedAccessInfo.itchLink != null && !this.cachedAccessInfo.itchLink.isEmpty()) {
            tooltip.add(DungeonsConfig.getContentAvailableItch());
         }
      }

      tooltip.addAll(this.contentPackagesConfigFields.getSetupMenuDescription());
      ItemStack itemStack = ItemStackGenerator.generateItemStack(Material.PURPLE_STAINED_GLASS_PANE, this.contentPackagesConfigFields.getName(), tooltip);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
      if (ItemSettingsConfig.isHideEnchants()) {
         itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      }

      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ADDITIONAL_TOOLTIP});
      itemStack.setItemMeta(itemMeta);
      NightbreakSetupIcons.applyItemModel(itemStack, "nightbreak:ui/lockedunpaid");
      return itemStack;
   }

   @Override
   protected ItemStack getOutOfDateUpdatableItemStack() {
      String slug = this.contentPackagesConfigFields.getNightbreakSlug();
      String modelId;
      if (slug == null || slug.isEmpty()) {
         modelId = "nightbreak:ui/update";
      } else if (!NightbreakAccount.hasToken()) {
         modelId = "nightbreak:ui/updateunlinked";
      } else {
         modelId = "nightbreak:ui/update";
      }

      return this.generateItemStackWithIcon(
         List.of(DungeonsConfig.getContentUpdateAvailable(), DungeonsConfig.getContentUpdateClickMessage()), Material.YELLOW_STAINED_GLASS_PANE, modelId
      );
   }

   @Override
   protected ItemStack getOutOfDateNoAccessItemStack() {
      List<String> tooltip = new ArrayList<>();
      tooltip.add(DungeonsConfig.getContentUpdateAvailable());
      tooltip.add(DungeonsConfig.getContentUpdateNeedAccess());
      tooltip.add(DungeonsConfig.getContentUpdateAccessClick());
      tooltip.addAll(this.contentPackagesConfigFields.getSetupMenuDescription());
      ItemStack itemStack = ItemStackGenerator.generateItemStack(Material.ORANGE_STAINED_GLASS_PANE, this.contentPackagesConfigFields.getName(), tooltip);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
      if (ItemSettingsConfig.isHideEnchants()) {
         itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      }

      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ADDITIONAL_TOOLTIP});
      itemStack.setItemMeta(itemMeta);
      NightbreakSetupIcons.applyItemModel(itemStack, "nightbreak:ui/updateunpaid");
      return itemStack;
   }

   private ItemStack generateItemStack(List<String> specificTooltip, Material material) {
      List<String> tooltip = new ArrayList<>(specificTooltip);
      tooltip.addAll(this.contentPackagesConfigFields.getSetupMenuDescription());
      ItemStack itemStack = ItemStackGenerator.generateItemStack(material, this.contentPackagesConfigFields.getName(), tooltip);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
      if (ItemSettingsConfig.isHideEnchants()) {
         itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      }

      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ADDITIONAL_TOOLTIP});
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   protected ItemStack generateItemStackWithIcon(List<String> specificTooltip, Material material, String modelId) {
      List<String> tooltip = new ArrayList<>(specificTooltip);
      tooltip.addAll(this.contentPackagesConfigFields.getSetupMenuDescription());
      ItemStack itemStack = ItemStackGenerator.generateItemStack(material, this.contentPackagesConfigFields.getName(), tooltip);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
      if (ItemSettingsConfig.isHideEnchants()) {
         itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      }

      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ADDITIONAL_TOOLTIP});
      itemStack.setItemMeta(itemMeta);
      NightbreakSetupIcons.applyItemModel(itemStack, modelId);
      return itemStack;
   }

   private List<String> installedLore() {
      return List.of(
         normalizedInstalledLoreLine(DungeonsConfig.getContentInstalledLine1(), "Content is installed!", "Content is installed and up to date.", "&a"),
         normalizedInstalledLoreLine(DungeonsConfig.getContentInstalledLine2(), "Click to uninstall!", "Click to uninstall.", "&7")
      );
   }

   private static String normalizedInstalledLoreLine(String configuredLine, String oldDefault, String newDefault, String fallbackColor) {
      String line = configuredLine != null && !configuredLine.isBlank() ? configuredLine : newDefault;
      if (line.trim().equalsIgnoreCase(oldDefault)) {
         line = newDefault;
      }

      return hasFormatting(line) ? line : fallbackColor + line;
   }

   private static boolean hasFormatting(String line) {
      return line.contains("&") || line.contains("§") || line.contains("<");
   }

   @Override
   public abstract void doInstall(Player var1);

   @Override
   public abstract void doUninstall(Player var1);

   @Override
   public void doDownload(Player player) {
      String slug = this.contentPackagesConfigFields.getNightbreakSlug();
      if (slug == null || slug.isEmpty()) {
         String downloadLink = this.getContentPackagesConfigFields().getDownloadLink();
         if (downloadLink != null && !downloadLink.isEmpty()) {
            Logger.sendSimpleMessage(player, DungeonsConfig.getContentDownloadSeparator());
            player.spigot()
               .sendMessage(
                  new BaseComponent[]{
                     SpigotMessage.simpleMessage(DungeonsConfig.getContentDownloadLegacyMessage().replace("$link", "")),
                     SpigotMessage.hoverLinkMessage("&9&n" + downloadLink, "&7Click to download", downloadLink)
                  }
               );
            Logger.sendSimpleMessage(player, DungeonsConfig.getContentDownloadSeparator());
         } else {
            Logger.sendSimpleMessage(player, "&cNo download link available for this content pack.");
         }
      } else if (!NightbreakAccount.hasToken()) {
         Logger.sendSimpleMessage(player, DungeonsConfig.getContentDownloadSeparator());
         Logger.sendSimpleMessage(player, DungeonsConfig.getContentNightbreakPromptLine1());
         player.spigot()
            .sendMessage(
               new BaseComponent[]{
                  SpigotMessage.simpleMessage(DungeonsConfig.getContentNightbreakPromptLine2()),
                  SpigotMessage.hoverLinkMessage(
                     InitializeConfig.getAccountLinkDisplay(), InitializeConfig.getAccountLinkHover(), "https://nightbreak.io/account/"
                  )
               }
            );
         Logger.sendSimpleMessage(player, DungeonsConfig.getContentNightbreakPromptLine3());
         player.spigot()
            .sendMessage(
               new BaseComponent[]{
                  SpigotMessage.simpleMessage(DungeonsConfig.getContentNightbreakPromptLine4()),
                  SpigotMessage.hoverLinkMessage(InitializeConfig.getContentLinkDisplay(), InitializeConfig.getContentLinkHover(), this.getContentPageUrl())
               }
            );
         Logger.sendSimpleMessage(player, DungeonsConfig.getContentDownloadSeparator());
      } else {
         player.sendMessage(DungeonsConfig.getContentCheckingAccessMessage().replace("$name", this.contentPackagesConfigFields.getName()));
         NightbreakContentManager.checkAccessAsync(MetadataHandler.PLUGIN, slug, accessInfo -> {
            this.cachedAccessInfo = accessInfo;
            if (player.isOnline()) {
               if (accessInfo == null) {
                  if (NightbreakAccount.hasAuthFailure()) {
                     this.sendInvalidTokenMessage(player);
                  } else {
                     player.sendMessage(DungeonsConfig.getContentAccessFailedMessage());
                     player.sendMessage(DungeonsConfig.getContentAccessFailedLink());
                  }
               } else if (!accessInfo.hasAccess) {
                  this.doShowAccessInfo(player);
               } else {
                  File importsFolder = new File(MetadataHandler.PLUGIN.getDataFolder(), "imports");
                  if (!importsFolder.exists()) {
                     importsFolder.mkdirs();
                  }

                  NightbreakContentManager.downloadAsync(MetadataHandler.PLUGIN, slug, importsFolder, player, success -> {
                     if (success && player.isOnline()) {
                        player.sendMessage(DungeonsConfig.getContentDownloadedReloadMessage());
                        Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> ReloadCommand.reload(player), 20L);
                     }
                  });
               }
            }
         });
      }
   }

   @Override
   public void doShowAccessInfo(Player player) {
      Logger.sendSimpleMessage(player, DungeonsConfig.getContentDownloadSeparator());
      Logger.sendSimpleMessage(player, DungeonsConfig.getContentNoAccessMessage().replace("$name", this.contentPackagesConfigFields.getName()));
      Logger.sendSimpleMessage(player, "");
      Logger.sendSimpleMessage(player, DungeonsConfig.getContentGetAccessMessage());
      player.spigot()
         .sendMessage(
            new BaseComponent[]{
               SpigotMessage.simpleMessage(DungeonsConfig.getContentNightbreakLink()),
               SpigotMessage.hoverLinkMessage(InitializeConfig.getContentLinkDisplay(), InitializeConfig.getContentLinkHover(), this.getContentPageUrl())
            }
         );
      if (this.cachedAccessInfo != null) {
         if (this.cachedAccessInfo.patreonLink != null && !this.cachedAccessInfo.patreonLink.isEmpty()) {
            player.spigot()
               .sendMessage(
                  new BaseComponent[]{
                     SpigotMessage.simpleMessage(DungeonsConfig.getContentPatreonLink()),
                     SpigotMessage.hoverLinkMessage("&9&n" + this.cachedAccessInfo.patreonLink, "&7Click to open Patreon", this.cachedAccessInfo.patreonLink)
                  }
               );
         }

         if (this.cachedAccessInfo.itchLink != null && !this.cachedAccessInfo.itchLink.isEmpty()) {
            player.spigot()
               .sendMessage(
                  new BaseComponent[]{
                     SpigotMessage.simpleMessage(DungeonsConfig.getContentItchLink()),
                     SpigotMessage.hoverLinkMessage("&9&n" + this.cachedAccessInfo.itchLink, "&7Click to open itch.io", this.cachedAccessInfo.itchLink)
                  }
               );
         }
      }

      Logger.sendSimpleMessage(player, "");
      Logger.sendSimpleMessage(player, DungeonsConfig.getContentLinkAccountMessage());
      Logger.sendSimpleMessage(player, DungeonsConfig.getContentDownloadSeparator());
   }

   private void sendInvalidTokenMessage(Player player) {
      player.sendMessage(DungeonsConfig.getContentInvalidTokenMessage());
      player.spigot()
         .sendMessage(
            new BaseComponent[]{
               SpigotMessage.simpleMessage(DungeonsConfig.getContentInvalidTokenInstructions()),
               SpigotMessage.hoverLinkMessage(
                  InitializeConfig.getAccountLinkDisplay(), InitializeConfig.getAccountLinkHover(), "https://nightbreak.io/account/"
               )
            }
         );
   }

   @Override
   protected ContentPackage.ContentState getContentState() {
      if (this.isInstalled && this.outOfDate) {
         String slug = this.contentPackagesConfigFields.getNightbreakSlug();
         if (slug == null || slug.isEmpty() || !NightbreakAccount.hasToken()) {
            return ContentPackage.ContentState.OUT_OF_DATE_UPDATABLE;
         } else if (this.cachedAccessInfo != null && this.cachedAccessInfo.hasAccess) {
            return ContentPackage.ContentState.OUT_OF_DATE_UPDATABLE;
         } else {
            return this.cachedAccessInfo != null && !this.cachedAccessInfo.hasAccess
               ? ContentPackage.ContentState.OUT_OF_DATE_NO_ACCESS
               : ContentPackage.ContentState.OUT_OF_DATE_UPDATABLE;
         }
      } else if (this.isInstalled) {
         return ContentPackage.ContentState.INSTALLED;
      } else if (this.isDownloaded) {
         return ContentPackage.ContentState.NOT_INSTALLED;
      } else {
         String slug = this.contentPackagesConfigFields.getNightbreakSlug();
         return slug != null && !slug.isEmpty() && NightbreakAccount.hasToken() && this.cachedAccessInfo != null && !this.cachedAccessInfo.hasAccess
            ? ContentPackage.ContentState.NEEDS_ACCESS
            : ContentPackage.ContentState.NOT_DOWNLOADED;
      }
   }

   @Override
   public boolean isOutOfDate() {
      return !this.isInstalled ? false : this.outOfDate;
   }

   @Override
   public String getNightbreakSlug() {
      return this.contentPackagesConfigFields.getNightbreakSlug();
   }

   @Override
   public String getDisplayName() {
      return this.contentPackagesConfigFields.getName();
   }

   @Override
   public String getDownloadLink() {
      return this.contentPackagesConfigFields.getDownloadLink();
   }

   public String getContentPageUrl() {
      String downloadLink = this.contentPackagesConfigFields.getDownloadLink();
      if (downloadLink != null && !downloadLink.isBlank()) {
         return downloadLink;
      } else {
         String slug = this.contentPackagesConfigFields.getNightbreakSlug();
         return slug != null && !slug.isBlank() ? "https://nightbreak.io/plugin/elitemobs/#" + slug : "https://nightbreak.io/plugin/elitemobs/";
      }
   }

   @Override
   public int getLocalVersion() {
      return this.contentPackagesConfigFields.getDungeonVersion();
   }

   public void refreshState() {
      this.getContentState();
   }

   public abstract void baseInitialization();

   public abstract void initializeContent();

   @Generated
   public static Map<String, EMPackage> getEmPackages() {
      return emPackages;
   }

   @Generated
   public ContentPackagesConfigFields getContentPackagesConfigFields() {
      return this.contentPackagesConfigFields;
   }

   @Generated
   @Override
   public boolean isDownloaded() {
      return this.isDownloaded;
   }

   @Generated
   public void setDownloaded(boolean isDownloaded) {
      this.isDownloaded = isDownloaded;
   }

   @Generated
   @Override
   public boolean isInstalled() {
      return this.isInstalled;
   }

   @Generated
   public void setInstalled(boolean isInstalled) {
      this.isInstalled = isInstalled;
   }

   @Generated
   @Override
   public void setOutOfDate(boolean outOfDate) {
      this.outOfDate = outOfDate;
   }

   @Generated
   @Override
   public NightbreakAccount.AccessInfo getCachedAccessInfo() {
      return this.cachedAccessInfo;
   }

   @Generated
   @Override
   public void setCachedAccessInfo(NightbreakAccount.AccessInfo cachedAccessInfo) {
      this.cachedAccessInfo = cachedAccessInfo;
   }

   @Generated
   public List<CustomBossEntity> getCustomBossEntityList() {
      return this.customBossEntityList;
   }

   private static final class BulkToggle {
      private final Map<Path, EMPackage.MemberIntent> intents = new LinkedHashMap<>();
      private final Player player;
      private final String reloadingMessage;
      private BukkitTask flushTask;
      private CompletableFuture<List<EMPackage.MemberResult>> worker;

      private BulkToggle(Player player, String reloadingMessage) {
         this.player = player;
         this.reloadingMessage = reloadingMessage;
      }
   }

   private record MemberIntent(CustomConfigFields fields, boolean enabled) {
   }

   private record MemberResult(EMPackage.MemberSave save, RuntimeException failure) {
   }

   private record MemberSave(EMPackage.MemberIntent intent, File file, String contents) {
   }
}
