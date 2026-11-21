package com.blaxk.spawnelytra;

import com.blaxk.spawnelytra.bedrock.TempElytraManager;
import com.blaxk.spawnelytra.bedrock.TempElytraTrackListener;
import com.blaxk.spawnelytra.command.CommandHandler;
import com.blaxk.spawnelytra.config.ConfigUpdater;
import com.blaxk.spawnelytra.config.LanguageUpdater;
import com.blaxk.spawnelytra.data.PlayerDataManager;
import com.blaxk.spawnelytra.integration.BedrockSupport;
import com.blaxk.spawnelytra.integration.PlaceholderAPIIntegration;
import com.blaxk.spawnelytra.listener.SpawnElytra;
import com.blaxk.spawnelytra.metrics.bukkit.Metrics;
import com.blaxk.spawnelytra.metrics.charts.DrilldownPie;
import com.blaxk.spawnelytra.setup.SetupManager;
import com.blaxk.spawnelytra.util.BackupUtil;
import com.blaxk.spawnelytra.util.DisplayNames;
import com.blaxk.spawnelytra.util.MessageUtil;
import com.blaxk.spawnelytra.util.SchedulerUtil;
import com.blaxk.spawnelytra.util.UpdateUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class Main extends JavaPlugin implements Listener {
   public static Main plugin;
   private static final String CURRENT_VERSION = "1.5";
   private static final String MODRINTH_PROJECT_ID = "Egw2R8Fj";
   private static final String MIGRATION_NOTICE_FILENAME = "MIGRATED_TO_SPAWN_ELYTRA.txt";
   private PlayerDataManager playerDataManager;
   private TempElytraManager tempElytraManager;
   private final Map<String, SpawnElytra> worldInstances = new HashMap<>();
   private final Map<String, String> lastMenuSent = new HashMap<>();
   private int remainingFirstInstallShows = 5;
   private SetupManager setupManager;
   private String latestVersion;
   private boolean updateAvailable;
   private SchedulerUtil.TaskHandle versionCheckTask;

   public void onEnable() {
      plugin = this;
      this.initializeConfiguration();
      this.showFirstInstallWelcomeIfNeeded();
      this.setupBStats();
      this.registerListenersAndCommands();
      this.registerPlaceholders();
      new Main.VersionChecker().start();
   }

   private void initializeConfiguration() {
      boolean migrated = this.migrateFromOldDataFolderIfPresent();
      this.saveDefaultConfig();
      this.playerDataManager = new PlayerDataManager(this);
      this.playerDataManager.initialize();
      MessageUtil.initialize(this);
      boolean migratedFromV13 = ConfigUpdater.updateConfig(this);
      if (migrated) {
         this.getConfig().set("first_install_completed", true);
         this.saveConfig();
      }

      this.saveLanguageFiles();
      LanguageUpdater.updateLanguages(this, migratedFromV13);
      MessageUtil.loadMessages(this);
      this.loadWorldConfigurations();
   }

   private void showFirstInstallWelcomeIfNeeded() {
      boolean firstInstallPending = !this.getConfig().getBoolean("first_install_completed", false);
      if (firstInstallPending) {
         for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isOp()) {
               SchedulerUtil.runAtEntityLater(this, p, 40L, () -> this.sendFirstInstallWelcome(p));
            }
         }
      }
   }

   private void setupBStats() {
      int pluginId = 25081;
      Metrics metrics = new Metrics(this, 25081);
      this.setupMetrics(metrics);
   }

   private void registerListenersAndCommands() {
      Bukkit.getPluginManager().registerEvents(this, this);
      BedrockSupport.initialize(this);
      this.tempElytraManager = new TempElytraManager(this);
      Bukkit.getPluginManager().registerEvents(this.tempElytraManager, this);

      try {
         Class.forName("io.papermc.paper.event.player.PlayerTrackEntityEvent");
         Bukkit.getPluginManager().registerEvents(new TempElytraTrackListener(this, this.tempElytraManager), this);
      } catch (ClassNotFoundException var2) {
      }

      this.setupManager = new SetupManager(this);
      Bukkit.getPluginManager().registerEvents(this.setupManager, this);
      CommandHandler commandHandler = new CommandHandler(this);
      Objects.requireNonNull(this.getCommand("spawnelytra")).setExecutor(commandHandler);
      Objects.requireNonNull(this.getCommand("spawnelytra")).setTabCompleter(commandHandler);
   }

   private void registerPlaceholders() {
      if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
         boolean registered = new PlaceholderAPIIntegration(this, this.playerDataManager).register();
         if (!registered) {
            this.getLogger().warning("Failed to register Spawn Elytra placeholders");
         }
      }
   }

   public void onDisable() {
      if (this.tempElytraManager != null) {
         this.tempElytraManager.restoreAll();
      }

      if (this.playerDataManager != null) {
         this.playerDataManager.saveAllPlayerData();
      }

      if (this.versionCheckTask != null) {
         this.versionCheckTask.cancel();
         this.versionCheckTask = null;
      }

      if (this.setupManager != null) {
         this.setupManager.stopAll();
      }

      for (SpawnElytra instance : this.worldInstances.values()) {
         if (instance != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
               instance.cleanupPlayer(player);
               instance.stopVisualization(player);
            }
         }
      }

      this.worldInstances.clear();
      MessageUtil.shutdown();
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      if (player.isOp() && !this.getConfig().getBoolean("first_install_completed", false)) {
         this.sendFirstInstallWelcome(player);
      }

      if (player.isOp() && this.updateAvailable && this.latestVersion != null) {
         SchedulerUtil.runAtEntityLater(this, player, 20L, () -> this.sendUpdateNotification(player));
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      this.lastMenuSent.remove(player.getUniqueId().toString());

      for (SpawnElytra instance : this.worldInstances.values()) {
         if (instance != null) {
            instance.cleanupPlayer(player);
         }
      }

      BedrockSupport.forget(player.getUniqueId());
   }

   private void sendUpdateNotification(Player player) {
      this.sendUpdateNotification((CommandSender)player);
   }

   private void sendUpdateNotification(CommandSender recipient) {
      if (recipient != null && this.latestVersion != null) {
         String language = this.getConfig().getString("language", "en").toLowerCase(Locale.ROOT);
         MessageUtil.sendRaw(recipient, this.getNewVersionMessage(language));
         MessageUtil.sendRaw(recipient, this.getUpdateToVersionMessage(language, this.latestVersion));
         if (recipient instanceof Player p && BedrockSupport.isBedrockPlayer(p)) {
            MessageUtil.sendRaw(recipient, this.getDownloadCommandsMessage(language));
         } else {
            MessageUtil.sendRaw(recipient, this.getDownloadButtonsMessage(language, this.latestVersion));
         }
      }
   }

   private Component getDownloadCommandsMessage(String language) {
      String modrinthUrl = this.buildUpdateLink();

      String text = switch (language) {
         case "de" -> "<#91f251>Auto-Update: <#ffd166>/spawnelytra update</#ffd166> <#aaa8a8>• Download: <#5db3ff>" + modrinthUrl;
         case "es" -> "<#91f251>Actualización automática: <#ffd166>/spawnelytra update</#ffd166> <#aaa8a8>• Descarga: <#5db3ff>" + modrinthUrl;
         case "fr" -> "<#91f251>Mise à jour auto : <#ffd166>/spawnelytra update</#ffd166> <#aaa8a8>• Téléchargement : <#5db3ff>" + modrinthUrl;
         case "pl" -> "<#91f251>Automatyczna aktualizacja: <#ffd166>/spawnelytra update</#ffd166> <#aaa8a8>• Pobieranie: <#5db3ff>" + modrinthUrl;
         default -> "<#91f251>Auto update: <#ffd166>/spawnelytra update</#ffd166> <#aaa8a8>• Download: <#5db3ff>" + modrinthUrl;
      };
      return MiniMessage.miniMessage().deserialize(text);
   }

   private String buildUpdateLink() {
      return this.latestVersion != null ? "https://modrinth.com/plugin/spawn-elytra/version/" + this.latestVersion : "https://modrinth.com/plugin/spawn-elytra";
   }

   private Component getNewVersionMessage(String language) {
      String text = switch (language) {
         case "de" -> "<#5db3ff>Eine neue Version von Spawn Elytra ist verfügbar!";
         case "es" -> "<#5db3ff>¡Una nueva versión de Spawn Elytra está disponible!";
         case "fr" -> "<#5db3ff>Une nouvelle version de Spawn Elytra est disponible !";
         case "pl" -> "<#5db3ff>Dostępna jest aktualizacja pluginu SpawnElytra!";
         default -> "<#5db3ff>A new version of Spawn Elytra is available!";
      };
      return MiniMessage.miniMessage().deserialize(text);
   }

   private Component getUpdateToVersionMessage(String language, String latestVersion) {
      String text = switch (language) {
         case "de" -> "<#fdba5e>Bitte aktualisiere auf Version <#91f251>" + latestVersion + "</#91f251> <#aaa8a8>(aktuell: <#fd5e5e>1.5</#fd5e5e>)</#aaa8a8>";
         case "es" -> "<#fdba5e>Por favor, actualiza a la versión <#91f251>" + latestVersion + "</#91f251> <#aaa8a8>(actual: <#fd5e5e>1.5</#fd5e5e>)</#aaa8a8>";
         case "fr" -> "<#fdba5e>Veuillez mettre à jour vers la version <#91f251>"
            + latestVersion
            + "</#91f251> <#aaa8a8>(actuelle : <#fd5e5e>1.5</#fd5e5e>)</#aaa8a8>";
         case "pl" -> "<#fdba5e>Zaaktualizuj do wersji <#91f251>" + latestVersion + "</#91f251> <#aaa8a8>(obecna: <#fd5e5e>1.5</#fd5e5e>)</#aaa8a8>";
         default -> "<#fdba5e>Please update to version <#91f251>" + latestVersion + "</#91f251> <#aaa8a8>(current: <#fd5e5e>1.5</#fd5e5e>)</#aaa8a8>";
      };
      return MiniMessage.miniMessage().deserialize(text);
   }

   private Component getDownloadButtonsMessage(String language, String version) {
      String modrinthUrl = this.buildUpdateLink();
      String githubUrl = "https://github.com/blax-k/SpawnElytra/releases";

      String text = switch (language) {
         case "de" -> "<#91f251>[<click:run_command:'/spawnelytra update'><hover:show_text:'<#91f251>Automatisch auf Version "
            + version
            + " aktualisieren\n<#ffd166>⚠ Server-Neustart erforderlich\n<#FF7F50>    ⏩ Experimentelle Funktion'>⏷ Auto Update</hover></click>] [<click:open_url:'https://github.com/blax-k/SpawnElytra/releases'><hover:show_text:'<#91f251>GitHub-Releases öffnen'>⏷ GitHub</hover></click>] [<click:open_url:'"
            + modrinthUrl
            + "'><hover:show_text:'<#91f251>Modrinth-Seite öffnen'>⏷ Modrinth</hover></click>]";
         case "es" -> "<#91f251>[<click:run_command:'/spawnelytra update'><hover:show_text:'<#91f251>Actualizar automáticamente a la versión "
            + version
            + "\n<#ffd166>⚠ Se requiere reinicio del servidor\n<#FF7F50>    ⏩ Función experimental'>⏷ Auto Actualización</hover></click>] [<click:open_url:'https://github.com/blax-k/SpawnElytra/releases'><hover:show_text:'<#91f251>Abrir GitHub releases'>⏷ GitHub</hover></click>] [<click:open_url:'"
            + modrinthUrl
            + "'><hover:show_text:'<#91f251>Abrir página de Modrinth'>⏷ Modrinth</hover></click>]";
         case "fr" -> "<#91f251>[<click:run_command:'/spawnelytra update'><hover:show_text:'<#91f251>Mettre à jour automatiquement vers la version "
            + version
            + "\n<#ffd166>⚠ Redémarrage du serveur requis\n<#FF7F50>    ⏩ Fonction expérimentale'>⏷ Mise à Jour Automatique</hover></click>] [<click:open_url:'https://github.com/blax-k/SpawnElytra/releases'><hover:show_text:'<#91f251>Ouvrir les publications GitHub'>⏷ GitHub</hover></click>] [<click:open_url:'"
            + modrinthUrl
            + "'><hover:show_text:'<#91f251>Ouvrir la page Modrinth'>⏷ Modrinth</hover></click>]";
         case "pl" -> "<#91f251>[<click:run_command:'/spawnelytra update'><hover:show_text:'<#91f251>Automatycznie zaaktualizuj do wersji "
            + version
            + "\n<#ffd166>⚠ Wymagany jest restart serwera\n<#FF7F50>    ⏩ Funkcja eksperymentalna'>⏷ Zaaktualizuj automatycznie</hover></click>] [<click:open_url:'https://github.com/blax-k/SpawnElytra/releases'><hover:show_text:'<#91f251>Otwórz zakładkę 'Releases' na GitHubie'>⏷ GitHub</hover></click>] [<click:open_url:'"
            + modrinthUrl
            + "'><hover:show_text:'<#91f251>Otwórz stronę pluginu na Modrinth'>⏷ Modrinth</hover></click>]";
         default -> "<#91f251>[<click:run_command:'/spawnelytra update'><hover:show_text:'<#91f251>Automatically update to version "
            + version
            + "\n<#ffd166>⚠ Server restart required\n<#FF7F50>    ⏩ Experimental feature'>⏷ Auto Update</hover></click>] [<click:open_url:'https://github.com/blax-k/SpawnElytra/releases'><hover:show_text:'<#91f251>Open GitHub releases'>⏷ GitHub</hover></click>] [<click:open_url:'"
            + modrinthUrl
            + "'><hover:show_text:'<#91f251>Open Modrinth release page'>⏷ Modrinth</hover></click>]";
      };
      return MiniMessage.miniMessage().deserialize(text);
   }

   public void performAutoUpdate(CommandSender sender) {
      if (this.latestVersion == null) {
         this.sendAutoUpdateMessage(sender, "update_no_version_available");
      } else {
         this.sendAutoUpdateMessage(sender, "update_starting", this.latestVersion);
         SchedulerUtil.runAsync(
            this,
            () -> {
               try {
                  this.getLogger().info("Starting auto-update to version " + this.latestVersion);
                  boolean success = UpdateUtil.downloadAndInstallUpdate(this, this.latestVersion);
                  if (success) {
                     this.getLogger().info("Auto-update completed successfully. Please restart the server.");
                     SchedulerUtil.runNow(
                        this,
                        () -> {
                           Bukkit.getOnlinePlayers()
                              .stream()
                              .filter(ServerOperator::isOp)
                              .forEach(p -> this.sendAutoUpdateMessage(p, "update_success", this.latestVersion));
                           if (!(sender instanceof Player) || !sender.isOp()) {
                              this.sendAutoUpdateMessage(sender, "update_success", this.latestVersion);
                           }
                        }
                     );
                  } else {
                     this.getLogger().warning("Auto-update failed");
                     SchedulerUtil.runNow(this, () -> this.sendAutoUpdateMessage(sender, "update_failed"));
                  }
               } catch (Exception var4) {
                  this.getLogger().severe("Auto-update failed: " + var4.getMessage());
                  String errorMsg = var4.getMessage() != null ? var4.getMessage() : var4.getClass().getSimpleName();
                  SchedulerUtil.runNow(this, () -> this.sendAutoUpdateMessage(sender, "update_error", errorMsg));
               }
            }
         );
      }
   }

   private void sendAutoUpdateMessage(CommandSender sender, String messageKey, String... args) {
      if (sender != null) {
         String language = this.getConfig().getString("language", "en").toLowerCase(Locale.ROOT);
         Component message = this.getAutoUpdateStatusMessage(language, messageKey, args);
         MessageUtil.sendRaw(sender, message);
      }
   }

   private Component getAutoUpdateStatusMessage(String language, String messageKey, String... args) {
      String version = args.length > 0 ? args[0] : "";
      String errorMsg = args.length > 0 ? args[0] : "";

      String unknownError = switch (language) {
         case "de" -> "Unbekannter Fehler";
         case "es" -> "Error desconocido";
         case "fr" -> "Erreur inconnue";
         case "pl" -> "Nieznany błąd";
         default -> "Unknown error";
      };

      String text = switch (messageKey) {
         case "update_no_version_available" -> {
            switch (language) {
               case "de":
                  yield "<#fd5e5e>Keine neue Version verfügbar zum Aktualisieren.";
               case "es":
                  yield "<#fd5e5e>No hay una nueva versión disponible para actualizar.";
               case "fr":
                  yield "<#fd5e5e>Aucune nouvelle version disponible pour la mise à jour.";
               case "pl":
                  yield "<#fd5e5e>Nie ma dostępnych aktualizacji.";
               default:
                  yield "<#fd5e5e>No new version available to update.";
            }
         }
         case "update_starting" -> {
            switch (language) {
               case "de":
                  yield "<#91f251>Starte automatisches Update auf Version <#ffd166>" + version + "</#ffd166>...";
               case "es":
                  yield "<#91f251>Iniciando actualización automática a la versión <#ffd166>" + version + "</#ffd166>...";
               case "fr":
                  yield "<#91f251>Démarrage de la mise à jour automatique vers la version <#ffd166>" + version + "</#ffd166>...";
               case "pl":
                  yield "<#91f251>Rozpoczęto automatyczną aktualizację do wersji <#ffd166>" + version + "</#ffd166>...";
               default:
                  yield "<#91f251>Starting automatic update to version <#ffd166>" + version + "</#ffd166>...";
            }
         }
         case "update_success" -> {
            switch (language) {
               case "de":
                  yield "<#91f251>✔ Update erfolgreich heruntergeladen und installiert!\n<#ffd166>⚠ Bitte starte den Server neu, um Version <#91f251>"
                     + version
                     + "</#91f251> zu laden.";
               case "es":
                  yield "<#91f251>✔ ¡Actualización descargada e instalada con éxito!\n<#ffd166>⚠ Por favor, reinicia el servidor para cargar la versión <#91f251>"
                     + version
                     + "</#91f251>.";
               case "fr":
                  yield "<#91f251>✔ Mise à jour téléchargée et installée avec succès !\n<#ffd166>⚠ Veuillez redémarrer le serveur pour charger la version <#91f251>"
                     + version
                     + "</#91f251>.";
               case "pl":
                  yield "<#91f251>✔ Aktualizacja została pobrana i zainstalowana!\n<#ffd166>⚠ Uruchom ponownie serwer aby załadować wersję <#91f251>"
                     + version
                     + "</#91f251>.";
               default:
                  yield "<#91f251>✔ Update downloaded and installed successfully!\n<#ffd166>⚠ Please restart the server to load version <#91f251>"
                     + version
                     + "</#91f251>.";
            }
         }
         case "update_failed" -> {
            switch (language) {
               case "de":
                  yield "<#fd5e5e>✗ Automatisches Update fehlgeschlagen. Bitte aktualisiere manuell.";
               case "es":
                  yield "<#fd5e5e>✗ La actualización automática falló. Por favor, actualiza manualmente.";
               case "fr":
                  yield "<#fd5e5e>✗ La mise à jour automatique a échoué. Veuillez mettre à jour manuellement.";
               case "pl":
                  yield "<#fd5e5e>✗ Nie udało się przeprowadzić aktualizacji. Zainstaluj aktualizację ręcznie.";
               default:
                  yield "<#fd5e5e>✗ Automatic update failed. Please update manually.";
            }
         }
         case "update_error" -> {
            switch (language) {
               case "de":
                  yield "<#fd5e5e>✗ Fehler beim Update: <#aaa8a8>" + (errorMsg.isEmpty() ? unknownError : errorMsg);
               case "es":
                  yield "<#fd5e5e>✗ Error durante la actualización: <#aaa8a8>" + (errorMsg.isEmpty() ? unknownError : errorMsg);
               case "fr":
                  yield "<#fd5e5e>✗ Erreur lors de la mise à jour : <#aaa8a8>" + (errorMsg.isEmpty() ? unknownError : errorMsg);
               case "pl":
                  yield "<#fd5e5e>✗ Błąd aktualizacji: <#aaa8a8>" + (errorMsg.isEmpty() ? unknownError : errorMsg);
               default:
                  yield "<#fd5e5e>✗ Update error: <#aaa8a8>" + (errorMsg.isEmpty() ? unknownError : errorMsg);
            }
         }
         default -> "<#aaa8a8>Unknown message: " + messageKey;
      };
      return MiniMessage.miniMessage().deserialize(text);
   }

   public SpawnElytra getSpawnElytraInstance() {
      return this.worldInstances.isEmpty() ? null : this.worldInstances.values().iterator().next();
   }

   public void markFirstInstallCompleted() {
      if (!this.getConfig().getBoolean("first_install_completed", false)) {
         this.getConfig().set("first_install_completed", true);
         this.saveConfig();
      }
   }

   public SpawnElytra getSpawnElytraInstance(String worldName) {
      return this.worldInstances.get(worldName);
   }

   public Map<String, SpawnElytra> getAllWorldInstances() {
      return new HashMap<>(this.worldInstances);
   }

   public void applyLanguageSetting(CommandSender actor, String langCode) {
      this.getConfig().set("language", langCode.toLowerCase(Locale.ROOT));
      this.saveConfig();
      MessageUtil.loadMessages(this);
      if (actor instanceof Player p) {
         String ctx = this.lastMenuSent.get(p.getUniqueId().toString());
         if ("first_install".equals(ctx)) {
            this.markFirstInstallCompleted();
         }
      }

      Component confirmation = MiniMessage.miniMessage().deserialize("<#91f251>Language set to <#ffd166>" + langCode + "</#ffd166>.");
      if (actor instanceof Player px) {
         MessageUtil.sendRaw(px, confirmation);
      } else {
         actor.sendMessage(MessageUtil.plain("info_header"));
      }
   }

   public void applyStyleSetting(CommandSender actor, String style) {
      String normalized = "small_caps".equalsIgnoreCase(style) ? "small_caps" : "classic";
      this.getConfig().set("messages.style", normalized);
      this.saveConfig();
      MessageUtil.loadMessages(this);
      if (actor instanceof Player p) {
         String ctx = this.lastMenuSent.get(p.getUniqueId().toString());
         if ("first_install".equals(ctx)) {
            this.markFirstInstallCompleted();
         }
      }

      Component confirmation = MiniMessage.miniMessage().deserialize("<#91f251>Style set to <#ffd166>" + normalized + "</#ffd166>.");
      if (actor instanceof Player px) {
         MessageUtil.sendRaw(px, confirmation);
      } else {
         actor.sendMessage(MessageUtil.plain("info_header"));
      }
   }

   public void sendFirstInstallWelcome(Player player) {
      if (player != null) {
         if (!this.getConfig().getBoolean("first_install_completed", false)) {
            if (this.remainingFirstInstallShows <= 0) {
               this.markFirstInstallCompleted();
            } else {
               this.remainingFirstInstallShows--;
               boolean bedrock = BedrockSupport.isBedrockPlayer(player);
               Component header = MiniMessage.miniMessage().deserialize("<#ffcc33>Welcome to Spawn Elytra!");
               MessageUtil.sendRaw(player, header);
               this.lastMenuSent.put(player.getUniqueId().toString(), "first_install");
               Component languagePrompt = MiniMessage.miniMessage().deserialize("<#fdba5e>✎ Choose your preferred language below:");
               MessageUtil.sendRaw(player, languagePrompt);
               Component languageButtons;
               if (bedrock) {
                  languageButtons = MiniMessage.miniMessage()
                     .deserialize(
                        "   <#91f251>/spawnelytra set language <#ffd166>en<#aaa8a8> | <#ffd166>de<#aaa8a8> | <#ffd166>es<#aaa8a8> | <#ffd166>fr<#aaa8a8> | <#ffd166>pl"
                     );
               } else {
                  languageButtons = MiniMessage.miniMessage()
                     .deserialize(
                        "   <#91f251>[<click:run_command:'/spawnelytra set language en'><hover:show_text:'<#91f251>Set language to English'>English</hover></click>] [<click:run_command:'/spawnelytra set language de'><hover:show_text:'<#91f251>Set language to German'>Deutsch</hover></click>] [<click:run_command:'/spawnelytra set language es'><hover:show_text:'<#91f251>Set language to Spanish'>Español</hover></click>] [<click:run_command:'/spawnelytra set language fr'><hover:show_text:'<#91f251>Set language to French'>Français</hover></click>] [<click:run_command:'/spawnelytra set language pl'><hover:show_text:'<#91f251>Set language to Polish'>Polski</hover></click>]"
                     );
               }

               MessageUtil.sendRaw(player, languageButtons);
               Component setupPrompt = MiniMessage.miniMessage().deserialize("<#fdba5e>⚐ Set up the spawn area where the Elytra spawn will work:");
               MessageUtil.sendRaw(player, setupPrompt);
               Component setupButton;
               if (bedrock) {
                  setupButton = MiniMessage.miniMessage().deserialize("   <#91f251>/spawnelytra setup");
               } else {
                  setupButton = MiniMessage.miniMessage()
                     .deserialize(
                        "   <#91f251>[<click:run_command:'/spawnelytra setup'><hover:show_text:'<#91f251>Currently, the Elytra spawn works 100 blocks in every direction from the world spawn point.\nTo define a specific area, you can either use the Setup Help or configure it yourself in config.yml.'>Start Setup</hover></click>]"
                     );
               }

               MessageUtil.sendRaw(player, setupButton);
               Component dismiss;
               if (bedrock) {
                  dismiss = MiniMessage.miniMessage().deserialize("<#aaa8a8>Dismiss this message forever: /spawnelytra dismiss");
               } else {
                  dismiss = MiniMessage.miniMessage()
                     .deserialize(
                        "<#aaa8a8>[<click:run_command:'/spawnelytra dismiss'><hover:show_text:'<#aaa8a8>Hide this message forever'>Dismiss this message</hover></click>]"
                     );
               }

               MessageUtil.sendRaw(player, dismiss);
               if (this.remainingFirstInstallShows == 0) {
                  this.markFirstInstallCompleted();
               }
            }
         }
      }
   }

   public void sendSettingsMenu(Player player) {
      if (player != null) {
         MessageUtil.send(player, "settings_menu_header");
         String currentLanguage = this.getConfig().getString("language", "en");
         String currentStyle = this.getConfig().getString("messages.style", "classic");
         String version = this.getDescription().getVersion();
         if (this.getDescription().getAuthors().isEmpty()) {
            String var10000 = "Unknown";
         } else {
            String var7 = (String)this.getDescription().getAuthors().getFirst();
         }

         MessageUtil.send(player, "settings_current_language", Placeholder.unparsed("value", DisplayNames.language(currentLanguage)));
         MessageUtil.send(player, "settings_current_style", Placeholder.unparsed("value", this.prettyStyle(currentStyle)));
         String activeWorlds = this.worldInstances.isEmpty() ? "-" : String.join(", ", this.worldInstances.keySet());
         MessageUtil.send(player, "settings_active_worlds", Placeholder.unparsed("value", activeWorlds));
         MessageUtil.sendRaw(player, Component.text(" "));
         this.lastMenuSent.put(player.getUniqueId().toString(), "settings");
         this.sendLanguageAndStyleChoices(player);
      }
   }

   public void sendOptionsMenu(Player player) {
      if (player != null) {
         MessageUtil.send(player, "settings_menu_header");
         MessageUtil.sendRaw(player, Component.text(" "));
         this.lastMenuSent.put(player.getUniqueId().toString(), "options");
         this.sendLanguageAndStyleChoices(player);
      }
   }

   private String prettyStyle(String style) {
      if (style == null) {
         return "-";
      } else {
         String lang = this.getConfig().getString("language", "en").toLowerCase(Locale.ROOT);
         return this.localizedStyleName(style.toLowerCase(Locale.ROOT), lang);
      }
   }

   private void sendLanguageAndStyleChoices(Player player) {
      String currentLanguage = this.getConfig().getString("language", "en").toLowerCase(Locale.ROOT);
      String rawStyle = this.getConfig().getString("messages.style", "classic");
      String currentStyle = (rawStyle == null ? "classic" : rawStyle).toLowerCase(Locale.ROOT);
      if (BedrockSupport.isBedrockPlayer(player)) {
         MessageUtil.send(player, "settings_change_language");
         MessageUtil.sendRaw(
            player,
            MiniMessage.miniMessage()
               .deserialize(
                  "<#91f251>/spawnelytra set language <#ffd166>en<#aaa8a8> | <#ffd166>de<#aaa8a8> | <#ffd166>es<#aaa8a8> | <#ffd166>fr<#aaa8a8> | <#ffd166>pl <#aaa8a8>(current: <#91f251>"
                     + currentLanguage
                     + "<#aaa8a8>)"
               )
         );
         MessageUtil.send(player, "settings_change_style");
         MessageUtil.sendRaw(
            player,
            MiniMessage.miniMessage()
               .deserialize(
                  "<#91f251>/spawnelytra set style <#ffd166>classic<#aaa8a8> | <#ffd166>small_caps <#aaa8a8>(current: <#91f251>" + currentStyle + "<#aaa8a8>)"
               )
         );
      } else {
         MessageUtil.send(player, "settings_change_language");
         StringBuilder langBuilder = new StringBuilder("<#91f251>");
         String[] languages = new String[]{"de", "en", "es", "fr", "pl"};
         String[] langNames = new String[]{"Deutsch", "English", "Español", "Français", "Polski"};

         for (int i = 0; i < languages.length; i++) {
            String lang = languages[i];
            String langName = langNames[i];
            boolean isSelected = currentLanguage.equals(lang);
            String hoverKey = "language_hover_" + lang;
            String hoverText = MessageUtil.plain(hoverKey);
            langBuilder.append("[");
            langBuilder.append("<click:run_command:'/spawnelytra set language ").append(lang).append("'>");
            langBuilder.append("<hover:show_text:'").append(hoverText).append("'>");
            if (isSelected) {
               langBuilder.append("<underlined>");
            }

            langBuilder.append(langName);
            if (isSelected) {
               langBuilder.append("</underlined>");
            }

            langBuilder.append("</hover></click>").append("] ");
         }

         Component langs = MiniMessage.miniMessage().deserialize(langBuilder.toString().trim());
         MessageUtil.sendRaw(player, langs);
         MessageUtil.send(player, "settings_change_style");
         StringBuilder styleBuilder = new StringBuilder("<#91f251>");
         String[] styles = new String[]{"classic", "small_caps"};
         String[] styleNames = new String[]{this.localizedStyleName("classic", currentLanguage), this.localizedStyleName("small_caps", currentLanguage)};

         for (int i = 0; i < styles.length; i++) {
            String style = styles[i];
            String styleName = styleNames[i];
            boolean isSelectedx = currentStyle.equals(style);
            String hoverKeyx = "style_hover_" + style;
            String hoverTextx = MessageUtil.plain(hoverKeyx);
            styleBuilder.append("[");
            styleBuilder.append("<click:run_command:'/spawnelytra set style ").append(style).append("'>");
            styleBuilder.append("<hover:show_text:'").append(hoverTextx).append("'>");
            if (isSelectedx) {
               styleBuilder.append("<underlined>");
            }

            styleBuilder.append(styleName);
            if (isSelectedx) {
               styleBuilder.append("</underlined>");
            }

            styleBuilder.append("</hover></click>").append("] ");
         }

         Component styleComponents = MiniMessage.miniMessage().deserialize(styleBuilder.toString().trim());
         MessageUtil.sendRaw(player, styleComponents);
      }
   }

   private void loadWorldConfigurations() {
      ConfigurationSection worldsSection = this.getConfig().getConfigurationSection("worlds");
      if (worldsSection == null) {
         this.getLogger().warning("No worlds config found! Creating default for 'world'...");
         this.createDefaultWorldConfig();
      } else {
         for (String worldName : worldsSection.getKeys(false)) {
            ConfigurationSection worldConfig = worldsSection.getConfigurationSection(worldName);
            if (worldConfig != null && worldConfig.getBoolean("enabled", true) && this.validateWorldConfiguration(worldName, worldConfig)) {
               World world = Bukkit.getWorld(worldName);
               if (world != null) {
                  SpawnElytra instance = new SpawnElytra(this, worldName, worldConfig);
                  this.worldInstances.put(worldName, instance);
                  Bukkit.getPluginManager().registerEvents(instance, this);
               } else {
                  this.getLogger().warning("World '" + worldName + "' not found, skipping.");
               }
            }
         }

         if (this.worldInstances.isEmpty()) {
            this.getLogger().warning("No valid worlds configured for Spawn Elytra!");
         }
      }
   }

   private boolean validateWorldConfiguration(String worldName, ConfigurationSection worldConfig) {
      boolean valid = true;
      int radius = worldConfig.getInt("radius", 100);
      if (radius <= 0) {
         this.getLogger().warning("[" + worldName + "] Invalid radius (" + radius + "), using default: 100");
         worldConfig.set("radius", 100);
         valid = false;
      }

      int boostStrength = worldConfig.getInt("boost.strength", 2);
      if (boostStrength <= 0) {
         this.getLogger().warning("[" + worldName + "] Invalid boost strength (" + boostStrength + "), using default: 2");
         worldConfig.set("boost.strength", 2);
         valid = false;
      }

      String activationMode = worldConfig.getString("activation_mode", "double_jump");
      List<String> validModes = List.of("double_jump", "auto", "sneak_jump", "f_key");
      if (!validModes.contains(activationMode)) {
         this.getLogger().warning("[" + worldName + "] Invalid activation mode '" + activationMode + "', using default: double_jump");
         worldConfig.set("activation_mode", "double_jump");
         valid = false;
      }

      String boostDirection = worldConfig.getString("boost.direction", "forward");
      if (!"forward".equals(boostDirection) && !"upward".equals(boostDirection)) {
         this.getLogger().warning("[" + worldName + "] Invalid boost direction '" + boostDirection + "', using default: forward");
         worldConfig.set("boost.direction", "forward");
         valid = false;
      }

      try {
         String soundName = worldConfig.getString("boost.sound", "ENTITY_BAT_TAKEOFF");
         Sound.valueOf(soundName.toUpperCase());
      } catch (IllegalArgumentException var14) {
         this.getLogger().warning("[" + worldName + "] Invalid boost sound, using default: ENTITY_BAT_TAKEOFF");
         worldConfig.set("boost.sound", "ENTITY_BAT_TAKEOFF");
         valid = false;
      }

      double fKeyLaunchStrength = worldConfig.getDouble("f_key.launch_strength", 1.5);
      if (fKeyLaunchStrength <= 0.0) {
         this.getLogger().warning("[" + worldName + "] Invalid F-key launch strength (" + fKeyLaunchStrength + "), using default: 1.5");
         worldConfig.set("f_key.launch_strength", 1.5);
         valid = false;
      }

      int maxBoosts = worldConfig.getInt("boost.max_boosts", 1);
      if (maxBoosts < 1) {
         this.getLogger().warning("[" + worldName + "] Invalid max_boosts (" + maxBoosts + "), using default: 1");
         worldConfig.set("boost.max_boosts", 1);
         valid = false;
      }

      double boostCooldown = worldConfig.getDouble("boost.boost_cooldown", 0.0);
      if (boostCooldown < 0.0) {
         this.getLogger().warning("[" + worldName + "] Invalid boost_cooldown (" + boostCooldown + "), using default: 0");
         worldConfig.set("boost.boost_cooldown", 0);
         valid = false;
      }

      if (!valid) {
         this.saveConfig();
      }

      return true;
   }

   private void createDefaultWorldConfig() {
      this.getConfig().set("worlds.world.enabled", true);
      this.getConfig().set("worlds.world.activation_mode", "double_jump");
      this.getConfig().set("worlds.world.radius", 100);
      this.getConfig().set("worlds.world.spawn_area.mode", "auto");
      this.getConfig().set("worlds.world.spawn_area.area_type", "circular");
      this.getConfig().set("worlds.world.spawn_area.x", 0);
      this.getConfig().set("worlds.world.spawn_area.y", 64);
      this.getConfig().set("worlds.world.spawn_area.z", 0);
      this.getConfig().set("worlds.world.spawn_area.x2", 0);
      this.getConfig().set("worlds.world.spawn_area.y2", 0);
      this.getConfig().set("worlds.world.spawn_area.z2", 0);
      this.getConfig().set("worlds.world.boost.enabled", true);
      this.getConfig().set("worlds.world.boost.strength", 2);
      this.getConfig().set("worlds.world.boost.direction", "forward");
      this.getConfig().set("worlds.world.boost.max_boosts", 1);
      this.getConfig().set("worlds.world.boost.boost_cooldown", 0);
      this.getConfig().set("worlds.world.boost.sound", "ENTITY_BAT_TAKEOFF");
      this.getConfig().set("worlds.world.f_key.launch_strength", 1.5);
      this.saveConfig();
      this.reloadConfig();
      this.loadWorldConfigurations();
   }

   private void setupMetrics(Metrics metrics) {
      metrics.addCustomChart(new DrilldownPie("configured_language", () -> {
         String lang = this.getConfig().getString("language", "en");
         if (lang.isEmpty()) {
            lang = "en";
         }

         lang = lang.toLowerCase(Locale.ROOT);
         Map<String, Integer> inner = new HashMap<>();
         inner.put(lang, 1);
         Map<String, Map<String, Integer>> outer = new HashMap<>();
         outer.put(lang, inner);
         return outer;
      }));
      metrics.addCustomChart(new DrilldownPie("worlds_configured", () -> {
         Map<String, Integer> worldCount = new HashMap<>();
         worldCount.put(String.valueOf(this.worldInstances.size()), 1);
         Map<String, Map<String, Integer>> outer = new HashMap<>();
         outer.put("world_count", worldCount);
         return outer;
      }));
      metrics.addCustomChart(new DrilldownPie("activation_mode", () -> {
         Map<String, Integer> modeCounts = new HashMap<>();
         ConfigurationSection worldsSection = this.getConfig().getConfigurationSection("worlds");
         if (worldsSection != null) {
            for (String worldName : worldsSection.getKeys(false)) {
               ConfigurationSection worldConfig = worldsSection.getConfigurationSection(worldName);
               if (worldConfig != null && worldConfig.getBoolean("enabled", true)) {
                  String mode = worldConfig.getString("activation_mode", "double_jump");
                  modeCounts.put(mode, modeCounts.getOrDefault(mode, 0) + 1);
               }
            }
         }

         Map<String, Map<String, Integer>> outer = new HashMap<>();

         for (Entry<String, Integer> entry : modeCounts.entrySet()) {
            Map<String, Integer> inner = new HashMap<>();
            inner.put(entry.getKey(), entry.getValue());
            outer.put(entry.getKey(), inner);
         }

         return outer;
      }));
      metrics.addCustomChart(new DrilldownPie("boost_direction", () -> {
         Map<String, Integer> directionCounts = new HashMap<>();
         ConfigurationSection worldsSection = this.getConfig().getConfigurationSection("worlds");
         if (worldsSection != null) {
            for (String worldName : worldsSection.getKeys(false)) {
               ConfigurationSection worldConfig = worldsSection.getConfigurationSection(worldName);
               if (worldConfig != null && worldConfig.getBoolean("enabled", true)) {
                  ConfigurationSection boostSection = worldConfig.getConfigurationSection("boost");
                  if (boostSection != null) {
                     String direction = boostSection.getString("direction", "forward");
                     directionCounts.put(direction, directionCounts.getOrDefault(direction, 0) + 1);
                  }
               }
            }
         }

         Map<String, Map<String, Integer>> outer = new HashMap<>();

         for (Entry<String, Integer> entry : directionCounts.entrySet()) {
            Map<String, Integer> inner = new HashMap<>();
            inner.put(entry.getKey(), entry.getValue());
            outer.put(entry.getKey(), inner);
         }

         return outer;
      }));
   }

   private String localizedStyleName(String style, String language) {
      String lang = language == null ? "en" : language.toLowerCase(Locale.ROOT);
      String s = style == null ? "" : style.toLowerCase(Locale.ROOT);

      return switch (s) {
         case "classic" -> {
            switch (lang) {
               case "de":
                  yield "Klassisch";
               case "es":
                  yield "Clásico";
               case "fr":
                  yield "Classique";
               case "pl":
                  yield "Klasyczny";
               default:
                  yield "Classic";
            }
         }
         case "small_caps" -> "ꜱᴍᴀʟʟ ᴄᴀᴘꜱ";
         default -> style;
      };
   }

   private boolean migrateFromOldDataFolderIfPresent() {
      try {
         boolean preventMigration = this.getConfig().getBoolean("prevent-spawnelytra-from-migrating", false);
         if (preventMigration) {
            this.getLogger().info("Migration prevented by 'prevent-spawnelytra-from-migrating' flag.");
            return false;
         } else {
            File pluginsDir = this.getDataFolder().getParentFile();
            if (pluginsDir == null) {
               return false;
            } else {
               File oldDir = new File(pluginsDir, "CraftAttackSpawnElytra");
               if (!oldDir.exists() || !oldDir.isDirectory()) {
                  return false;
               } else if (this.isDirectoryEmpty(oldDir)) {
                  return false;
               } else {
                  File migrationNotice = new File(oldDir, "MIGRATED_TO_SPAWN_ELYTRA.txt");
                  if (migrationNotice.exists()) {
                     this.getLogger().warning("Legacy migration skipped: MIGRATED_TO_SPAWN_ELYTRA.txt already exists. Delete it to re-migrate.");
                     return false;
                  } else {
                     File newDir = this.getDataFolder();
                     if (!newDir.exists() && !newDir.mkdirs()) {
                        this.getLogger().warning("Failed to create data folder: " + newDir.getAbsolutePath());
                        return false;
                     } else {
                        boolean migrated = false;
                        File backupTimestampDir = BackupUtil.backupDirectory(this, oldDir, "CraftAttackSpawnElytra");
                        if (backupTimestampDir == null) {
                           this.getLogger().warning("Could not backup legacy folder. Aborting migration.");
                           return false;
                        } else {
                           File oldConfig = new File(oldDir, "config.yml");
                           if (oldConfig.exists()) {
                              File newConfig = new File(newDir, "config.yml");
                              if (!newConfig.exists()) {
                                 try {
                                    Files.copy(oldConfig.toPath(), newConfig.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                    migrated = true;
                                 } catch (IOException var12) {
                                    this.getLogger().warning("Failed to copy config.yml from old folder: " + var12.getMessage());
                                 }
                              }
                           }

                           if (new File(oldDir, "lang").exists()) {
                              this.getLogger().info("Skipping legacy lang files; fresh ones will be generated.");
                              migrated = true;
                           }

                           boolean cleaned = this.deleteDirectoryContents(oldDir);
                           if (!cleaned) {
                              this.getLogger().warning("Failed to clean legacy folder after backup.");
                           }

                           try {
                              this.writeMigrationNotice(oldDir, newDir, backupTimestampDir);
                           } catch (IOException var11) {
                              this.getLogger().warning("Failed to write migration notice file: " + var11.getMessage());
                           }

                           this.getLogger().info("Migrated legacy 'CraftAttackSpawnElytra' data to '" + newDir.getName() + "'.");
                           return true;
                        }
                     }
                  }
               }
            }
         }
      } catch (Exception var13) {
         this.getLogger().warning("Migration from legacy data folder failed: " + var13.getMessage());
         return false;
      }
   }

   private boolean isDirectoryEmpty(File dir) {
      if (null != dir && dir.exists() && dir.isDirectory()) {
         File[] children = dir.listFiles();
         return null != children && 0 != children.length ? children.length == 1 && "MIGRATED_TO_SPAWN_ELYTRA.txt".equals(children[0].getName()) : true;
      } else {
         return true;
      }
   }

   private boolean deleteDirectoryContents(File dir) {
      if (dir != null && dir.exists() && dir.isDirectory()) {
         File[] children = dir.listFiles();
         boolean ok = true;
         if (children != null) {
            for (File child : children) {
               ok &= this.deleteRecursively(child);
            }
         }

         return ok;
      } else {
         return false;
      }
   }

   private boolean deleteRecursively(File file) {
      if (file.isDirectory()) {
         File[] children = file.listFiles();
         if (children != null) {
            for (File c : children) {
               if (!this.deleteRecursively(c)) {
                  return false;
               }
            }
         }
      }

      return file.delete();
   }

   private void writeMigrationNotice(File oldDir, File newDataFolder, File backupTimestampDir) throws IOException {
      if (oldDir != null && newDataFolder != null && backupTimestampDir != null) {
         String timestampFolderName = backupTimestampDir.getName();
         String newFolderName = newDataFolder.getName();
         String en = "SpawnElytra Migration Notice\r\n\r\nAll previous data from CraftAttackSpawnElytra has been migrated.\r\nThis plugin now uses the folder: plugins/"
            + newFolderName
            + "\r\n\r\nBackup:\r\nA full backup of the old CraftAttackSpawnElytra folder was created here:\r\nplugins/"
            + newFolderName
            + "/backups/"
            + timestampFolderName
            + "/CraftAttackSpawnElytra/\r\n\r\nIf you have a conflicting plugin and want to prevent SpawnElytra from migrating data, add the following to plugins/"
            + newFolderName
            + "/config.yml:\r\nprevent-spawnelytra-from-migrating: true\r\nNote: This key is NOT present by default and must be added manually.";
         String de = "Hinweis zur SpawnElytra-Migration\r\n\r\nAlle bisherigen Daten von CraftAttackSpawnElytra wurden migriert.\r\nDas Plugin verwendet jetzt den Ordner: plugins/"
            + newFolderName
            + "\r\n\r\nBackup:\r\nEine vollständige Sicherung des alten CraftAttackSpawnElytra-Ordners wurde hier erstellt:\r\nplugins/"
            + newFolderName
            + "/backups/"
            + timestampFolderName
            + "/CraftAttackSpawnElytra/\r\n\r\nWenn du ein anderes, in Konflikt stehendes Plugin hast und die Migration durch SpawnElytra verhindern möchtest, füge Folgendes in plugins/"
            + newFolderName
            + "/config.yml hinzu:\r\nprevent-spawnelytra-from-migrating: true\r\nHinweis: Dieser Schlüssel ist standardmäßig nicht in der config.yml vorhanden und muss manuell hinzugefügt werden.";
         String pl = "Informacje na temat migracji SpawnElytra\r\n\r\nWszystkie poprzednie dane z pluginu CraftAttackSpawnElytra zostały zmigrowane.\r\nTen plugin używa folderu: plugins/"
            + newFolderName
            + "\r\n\r\nKopia zapasowa:\r\nPełna kopia zapasowa starego folderu CraftAttackSpawnElytra została stworzona tutaj:\r\nplugins/"
            + newFolderName
            + "/backups/"
            + timestampFolderName
            + "/CraftAttackSpawnElytra/\r\n\r\nJeśli masz plugin, który jest niekompatybilny i nie chcesz migrować danych SpawnElytra, dodaj poniższą opcję do plugins/"
            + newFolderName
            + "/config.yml:\r\nprevent-spawnelytra-from-migrating: true\r\nUwaga: Ta opcja domyślnie nie jest obecna w pliku konfiguracyjnym i musi zostać dodana ręcznie.";
         String content = en + "\r\n\r\n" + de + "\r\n\r\n" + pl + "\r\n";
         Files.writeString(new File(oldDir, "MIGRATED_TO_SPAWN_ELYTRA.txt").toPath(), content);
      }
   }

   private void saveLanguageFiles() {
      SchedulerUtil.runAsync(this, () -> {
         File langDir = new File(this.getDataFolder(), "lang");
         if (!langDir.exists() && !langDir.mkdirs()) {
            this.getLogger().warning("Failed to create language directory");
         } else {
            String[] languages = new String[]{"en", "de", "es", "fr", "pl"};

            for (String lang : languages) {
               File langFile = new File(langDir, lang + ".yml");
               if (!langFile.exists()) {
                  try (InputStream in = this.getResource("lang/" + lang + ".yml")) {
                     if (in != null) {
                        Files.write(langFile.toPath(), in.readAllBytes());
                     }
                  } catch (IOException var13) {
                     this.getLogger().warning("Failed to write language file " + lang + ".yml: " + var13.getMessage());
                  }
               }
            }
         }
      });
   }

   public PlayerDataManager getPlayerDataManager() {
      return this.playerDataManager;
   }

   public TempElytraManager getTempElytraManager() {
      return this.tempElytraManager;
   }

   public SetupManager getSetupManager() {
      return this.setupManager;
   }

   public void reload() {
      this.reloadConfig();
      MessageUtil.loadMessages(this);
      BedrockSupport.reloadSettings(this);
      if (this.tempElytraManager != null) {
         this.tempElytraManager.restoreAll();
      }

      if (this.setupManager != null) {
         this.setupManager.stopAll();
      }

      for (SpawnElytra instance : this.worldInstances.values()) {
         if (instance != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
               instance.cleanupPlayer(player);
               instance.stopVisualization(player);
            }
         }
      }

      this.worldInstances.clear();
      this.loadWorldConfigurations();
   }

   private String sanitizeMiniMessageValue(String value) {
      return value == null ? "" : value.replace('<', '[').replace('>', ']');
   }

   private String fetchLatestVersionNumber() throws IOException {
      URL url = new URL("https://api.modrinth.com/v2/project/Egw2R8Fj/version");
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("User-Agent", "SpawnElytra/1.5");
      conn.setConnectTimeout(5000);
      conn.setReadTimeout(10000);

      String var6;
      try {
         int status = conn.getResponseCode();
         if (200 != status) {
            throw new IOException("HTTP " + status + " " + conn.getResponseMessage());
         }

         try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            var6 = getString(root);
         }
      } finally {
         conn.disconnect();
      }

      return var6;
   }

   @NotNull
   static String getString(JsonElement root) throws IOException {
      if (!root.isJsonArray()) {
         throw new IOException("Unexpected response from Modrinth");
      } else {
         JsonArray versions = root.getAsJsonArray();
         if (versions.isEmpty()) {
            throw new IOException("No version data available");
         } else {
            String latestRelease = null;

            for (JsonElement elem : versions) {
               JsonObject obj = elem.getAsJsonObject();
               String type = obj.get("version_type").getAsString();
               if ("release".equalsIgnoreCase(type)) {
                  latestRelease = obj.get("version_number").getAsString();
                  break;
               }
            }

            return latestRelease;
         }
      }
   }

   private class VersionChecker {
      void start() {
         Main.this.versionCheckTask = SchedulerUtil.runAsyncRepeating(Main.this, this::tick, 1200L, 288000L);
      }

      private void tick() {
         try {
            String latest = Main.this.fetchLatestVersionNumber();
            if (!"1.5".equals(latest)) {
               Main.this.latestVersion = latest;
               Main.this.updateAvailable = true;
               Bukkit.getOnlinePlayers()
                  .stream()
                  .filter(ServerOperator::isOp)
                  .forEach(p -> SchedulerUtil.runAtEntityNow(Main.this, p, () -> Main.this.sendUpdateNotification(p)));
               String language = Main.this.getConfig().getString("language", "en").toLowerCase(Locale.ROOT);

               String newVersionText = switch (language) {
                  case "de" -> "Eine neue Version von Spawn Elytra ist verfügbar!";
                  case "es" -> "¡Una nueva versión de Spawn Elytra está disponible!";
                  case "fr" -> "Une nouvelle version de Spawn Elytra est disponible !";
                  case "pl" -> "Nowa wersja SpawnElytra jest dostępna!";
                  default -> "A new version of Spawn Elytra is available!";
               };

               String updateToVersionText = switch (language) {
                  case "de" -> "Bitte aktualisiere auf Version " + Main.this.latestVersion + " (aktuell: 1.5)";
                  case "es" -> "Por favor, actualiza a la versión " + Main.this.latestVersion + " (actual: 1.5)";
                  case "fr" -> "Veuillez mettre à jour vers la version " + Main.this.latestVersion + " (actuelle : 1.5)";
                  case "pl" -> "Zaaktualizuj do wersji " + Main.this.latestVersion + " (obecna: 1.5)";
                  default -> "Please update to version " + Main.this.latestVersion + " (current: 1.5)";
               };
               Main.this.getLogger().warning(newVersionText + " " + updateToVersionText);
               Main.this.getLogger().warning("Download: " + Main.this.buildUpdateLink());
            } else {
               Main.this.updateAvailable = false;
               Main.this.latestVersion = null;
            }
         } catch (Exception var7) {
            String errorMessage = var7.getMessage() != null ? var7.getMessage() : var7.getClass().getSimpleName();
            String sanitized = Main.this.sanitizeMiniMessageValue(errorMessage);
            Main.this.getLogger().warning(MessageUtil.plain("failed_update_check", Placeholder.unparsed("error_message", sanitized)));
         }
      }
   }
}
