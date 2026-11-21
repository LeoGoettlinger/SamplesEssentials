package com.earth2me.essentials.commands.essentials;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.commands.EssentialsTreeNode;
import com.earth2me.essentials.economy.EconomyLayer;
import com.earth2me.essentials.economy.EconomyLayers;
import com.earth2me.essentials.utils.CommandMapUtil;
import com.earth2me.essentials.utils.DateUtil;
import com.earth2me.essentials.utils.PasteUtil;
import com.earth2me.essentials.utils.VersionUtil;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.ess3.provider.KnownCommandsProvider;
import net.ess3.provider.OnlineModeProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

public class DumpCommand extends EssentialsTreeNode {
   public DumpCommand() {
      super("dump");
   }

   @Override
   protected void run(CommandSource sender, String commandLabel, String[] args) throws Exception {
      sender.sendTl("dumpCreating");
      JsonObject dump = new JsonObject();
      JsonObject meta = new JsonObject();
      meta.addProperty("timestamp", Instant.now().toEpochMilli());
      meta.addProperty("sender", sender.getPlayer() != null ? sender.getPlayer().getName() : null);
      meta.addProperty("senderUuid", sender.getPlayer() != null ? sender.getPlayer().getUniqueId().toString() : null);
      dump.add("meta", meta);
      JsonObject serverData = new JsonObject();
      serverData.addProperty("bukkit-version", Bukkit.getBukkitVersion());
      serverData.addProperty("server-version", Bukkit.getVersion());
      serverData.addProperty("server-brand", Bukkit.getName());
      serverData.addProperty("online-mode", this.ess.provider(OnlineModeProvider.class).getOnlineModeString());
      JsonObject supportStatus = new JsonObject();
      VersionUtil.SupportStatus status = VersionUtil.getServerSupportStatus();
      supportStatus.addProperty("status", status.name());
      supportStatus.addProperty("supported", status.isSupported());
      supportStatus.addProperty("trigger", VersionUtil.getSupportStatusClass());
      serverData.add("support-status", supportStatus);
      dump.add("server-data", serverData);
      JsonObject environment = new JsonObject();
      environment.addProperty("java-version", System.getProperty("java.version"));
      environment.addProperty("operating-system", System.getProperty("os.name"));
      environment.addProperty("uptime", DateUtil.formatDateDiff(ManagementFactory.getRuntimeMXBean().getStartTime()));
      environment.addProperty("allocated-memory", Runtime.getRuntime().totalMemory() / 1024L / 1024L + "MB");
      dump.add("environment", environment);
      JsonObject essData = new JsonObject();
      essData.addProperty("version", this.ess.getDescription().getVersion());
      JsonObject updateData = new JsonObject();
      updateData.addProperty("id", this.ess.getUpdateChecker().getVersionIdentifier());
      updateData.addProperty("branch", this.ess.getUpdateChecker().getVersionBranch());
      updateData.addProperty("dev", this.ess.getUpdateChecker().isDevBuild());
      essData.add("update-data", updateData);
      JsonObject econLayer = new JsonObject();
      econLayer.addProperty("enabled", !this.ess.getSettings().isEcoDisabled());
      econLayer.addProperty("selected-layer", EconomyLayers.isLayerSelected());
      EconomyLayer layer = EconomyLayers.getSelectedLayer();
      econLayer.addProperty("name", layer == null ? "null" : layer.getName());
      econLayer.addProperty("layer-version", layer == null ? "null" : layer.getPluginVersion());
      econLayer.addProperty("backend-name", layer == null ? "null" : layer.getBackendName());
      essData.add("economy-layer", econLayer);
      JsonArray addons = new JsonArray();
      JsonArray plugins = new JsonArray();
      ArrayList<Plugin> alphabetical = new ArrayList<>();
      Collections.addAll(alphabetical, Bukkit.getPluginManager().getPlugins());
      alphabetical.sort(Comparator.comparing(o -> o.getName().toUpperCase(Locale.ENGLISH)));

      for (Plugin plugin : alphabetical) {
         JsonObject pluginData = new JsonObject();
         PluginDescriptionFile info = plugin.getDescription();
         String name = info.getName();
         pluginData.addProperty("name", name);
         pluginData.addProperty("version", info.getVersion());
         pluginData.addProperty("description", info.getDescription());
         pluginData.addProperty("main", info.getMain());
         pluginData.addProperty("enabled", plugin.isEnabled());
         pluginData.addProperty("official", plugin == this.ess || VersionUtil.officialPlugins.contains(name));
         pluginData.addProperty("unsupported", VersionUtil.warnPlugins.contains(name));
         JsonArray authors = new JsonArray();

         for (String author : info.getAuthors()) {
            authors.add((JsonElement)(author == null ? JsonNull.INSTANCE : new JsonPrimitive(author)));
         }

         pluginData.add("authors", authors);
         if (name.startsWith("Essentials") && !name.equals("Essentials")) {
            addons.add(pluginData);
         }

         plugins.add(pluginData);
      }

      essData.add("addons", addons);
      dump.add("ess-data", essData);
      dump.add("plugins", plugins);
      List<PasteUtil.PasteFile> files = new ArrayList<>();
      files.add(new PasteUtil.PasteFile("dump.json", dump.toString()));
      Plugin essDiscord = Bukkit.getPluginManager().getPlugin("EssentialsDiscord");
      Plugin essDiscordLink = Bukkit.getPluginManager().getPlugin("EssentialsDiscordLink");
      Plugin essSpawn = Bukkit.getPluginManager().getPlugin("EssentialsSpawn");
      Map<String, Command> knownCommandsCopy = new HashMap<>(this.ess.provider(KnownCommandsProvider.class).getKnownCommands());
      Map<String, String> disabledCommandsCopy = new HashMap<>(this.ess.getAlternativeCommandsHandler().disabledCommands());
      this.ess
         .runTaskAsynchronously(
            () -> {
               boolean config = false;
               boolean discord = false;
               boolean kits = false;
               boolean log = false;
               boolean worth = false;
               boolean tpr = false;
               boolean spawns = false;
               boolean commands = false;

               for (String arg : args) {
                  if (arg.equals("*") || arg.equalsIgnoreCase("all")) {
                     config = true;
                     discord = true;
                     kits = true;
                     log = true;
                     worth = true;
                     tpr = true;
                     spawns = true;
                     commands = true;
                     break;
                  }

                  if (arg.equalsIgnoreCase("config")) {
                     config = true;
                  } else if (arg.equalsIgnoreCase("discord")) {
                     discord = true;
                  } else if (arg.equalsIgnoreCase("kits")) {
                     kits = true;
                  } else if (arg.equalsIgnoreCase("log")) {
                     log = true;
                  } else if (arg.equalsIgnoreCase("worth")) {
                     worth = true;
                  } else if (arg.equalsIgnoreCase("tpr")) {
                     tpr = true;
                  } else if (arg.equalsIgnoreCase("spawns")) {
                     spawns = true;
                  } else if (arg.equalsIgnoreCase("commands")) {
                     commands = true;
                  }
               }

               if (config) {
                  try {
                     files.add(
                        new PasteUtil.PasteFile(
                           "config.yml", new String(Files.readAllBytes(this.ess.getSettings().getConfigFile().toPath()), StandardCharsets.UTF_8)
                        )
                     );
                  } catch (IOException var29x) {
                     sender.sendTl("dumpErrorUpload", "config.yml", var29x.getMessage());
                  }
               }

               if (discord && essDiscord != null) {
                  try {
                     files.add(
                        new PasteUtil.PasteFile(
                           "discord-config.yml",
                           new String(Files.readAllBytes(essDiscord.getDataFolder().toPath().resolve("config.yml")), StandardCharsets.UTF_8)
                              .replaceAll("[A-Za-z\\d]{24}\\.[\\w-]{6}\\.[\\w-]{27}", "<censored token>")
                        )
                     );
                  } catch (IOException var28x) {
                     sender.sendTl("dumpErrorUpload", "discord-config.yml", var28x.getMessage());
                  }

                  if (essDiscordLink != null) {
                     try {
                        files.add(
                           new PasteUtil.PasteFile(
                              "discord-link-config.yml",
                              new String(Files.readAllBytes(essDiscordLink.getDataFolder().toPath().resolve("config.yml")), StandardCharsets.UTF_8)
                           )
                        );
                     } catch (IOException var27x) {
                        sender.sendTl("dumpErrorUpload", "discord-link-config.yml", var27x.getMessage());
                     }
                  }
               }

               if (kits) {
                  try {
                     files.add(
                        new PasteUtil.PasteFile("kits.yml", new String(Files.readAllBytes(this.ess.getKits().getFile().toPath()), StandardCharsets.UTF_8))
                     );
                  } catch (IOException var26x) {
                     sender.sendTl("dumpErrorUpload", "kits.yml", var26x.getMessage());
                  }
               }

               if (log) {
                  try {
                     files.add(
                        new PasteUtil.PasteFile(
                           "latest.log",
                           new String(Files.readAllBytes(Paths.get("logs", "latest.log")), StandardCharsets.UTF_8)
                              .replaceAll(
                                 "(?m)^\\[\\d\\d:\\d\\d:\\d\\d] \\[.+/(?:DEBUG|TRACE)]: .+\\s(?:[A-Za-z.]+:.+\\s(?:\\t.+\\s)*)?\\s*(?:\"[A-Za-z]+\" : .+[\\s}\\]]+)*",
                                 ""
                              )
                              .replaceAll("(?:[0-9]{1,3}\\.){3}[0-9]{1,3}", "<censored ip address>")
                        )
                     );
                  } catch (IOException var25x) {
                     sender.sendTl("dumpErrorUpload", "latest.log", var25x.getMessage());
                  }
               }

               if (worth) {
                  try {
                     files.add(
                        new PasteUtil.PasteFile("worth.yml", new String(Files.readAllBytes(this.ess.getWorth().getFile().toPath()), StandardCharsets.UTF_8))
                     );
                  } catch (IOException var24x) {
                     sender.sendTl("dumpErrorUpload", "worth.yml", var24x.getMessage());
                  }
               }

               if (tpr) {
                  try {
                     files.add(
                        new PasteUtil.PasteFile(
                           "tpr.yml", new String(Files.readAllBytes(this.ess.getRandomTeleport().getFile().toPath()), StandardCharsets.UTF_8)
                        )
                     );
                  } catch (IOException var23) {
                     sender.sendTl("dumpErrorUpload", "tpr.yml", var23.getMessage());
                  }
               }

               if (spawns && essSpawn != null) {
                  try {
                     files.add(
                        new PasteUtil.PasteFile(
                           "spawn.yml", new String(Files.readAllBytes(this.ess.getDataFolder().toPath().resolve("spawn.yml")), StandardCharsets.UTF_8)
                        )
                     );
                  } catch (IOException var22x) {
                     sender.sendTl("dumpErrorUpload", "spawn.yml", var22x.getMessage());
                  }
               }

               if (commands) {
                  try {
                     files.add(new PasteUtil.PasteFile("commands.yml", new String(Files.readAllBytes(Paths.get("commands.yml")), StandardCharsets.UTF_8)));
                     files.add(new PasteUtil.PasteFile("commandmap.json", CommandMapUtil.toJsonPretty(this.ess, knownCommandsCopy)));
                     files.add(new PasteUtil.PasteFile("commandoverride.json", disabledCommandsCopy.toString()));
                  } catch (IOException var21x) {
                     sender.sendTl("dumpErrorUpload", "commands.yml", var21x.getMessage());
                  }
               }

               CompletableFuture<PasteUtil.PasteResult> future = PasteUtil.createPaste(files);
               future.thenAccept(result -> {
                  if (result != null) {
                     String dumpUrl = "https://essentialsx.net/dump?bytebin=" + result.getPasteId();
                     sender.sendTl("dumpUrl", dumpUrl);
                     if (sender.isPlayer()) {
                        this.ess.getLogger().info(this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("dumpConsoleUrl", dumpUrl)));
                     }
                  }

                  files.clear();
               });
               future.exceptionally(throwable -> {
                  sender.sendTl("dumpError", throwable.getMessage());
                  return null;
               });
            }
         );
   }

   @Override
   protected List<String> tabComplete(CommandSource sender, String commandLabel, String[] args) {
      List<String> list = Lists.newArrayList(new String[]{"config", "kits", "log", "discord", "worth", "tpr", "spawns", "commands", "all"});

      for (String arg : args) {
         if (arg.equals("*") || arg.equalsIgnoreCase("all")) {
            list.clear();
            return list;
         }

         list.remove(arg.toLowerCase(Locale.ENGLISH));
      }

      return list;
   }
}
