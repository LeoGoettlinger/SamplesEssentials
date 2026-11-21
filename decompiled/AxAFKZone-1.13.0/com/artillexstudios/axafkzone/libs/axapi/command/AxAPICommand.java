package com.artillexstudios.axafkzone.libs.axapi.command;

import com.artillexstudios.axafkzone.libs.axapi.nms.wrapper.ServerPlayerWrapper;
import com.artillexstudios.axafkzone.libs.axapi.nms.wrapper.ServerWrapper;
import com.artillexstudios.axafkzone.libs.axapi.packet.ClientboundPacketTypes;
import com.artillexstudios.axafkzone.libs.axapi.packet.PacketSide;
import com.artillexstudios.axafkzone.libs.axapi.packet.ServerboundPacketTypes;
import com.artillexstudios.axafkzone.libs.axapi.reflection.FieldAccessor;
import com.artillexstudios.axafkzone.libs.axapi.utils.ItemBuilder;
import com.artillexstudios.axafkzone.libs.axapi.utils.MessageUtils;
import com.artillexstudios.axafkzone.libs.axapi.utils.StringUtils;
import com.artillexstudios.axafkzone.libs.axapi.utils.Version;
import com.artillexstudios.axafkzone.libs.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axafkzone.libs.axapi.utils.http.Requests;
import com.artillexstudios.axafkzone.libs.axapi.utils.logging.LogUtils;
import com.artillexstudios.axafkzone.libs.lamp.annotation.Command;
import com.artillexstudios.axafkzone.libs.lamp.annotation.Subcommand;
import com.artillexstudios.axafkzone.libs.lamp.bukkit.annotation.CommandPermission;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

@Command({"axapi"})
@CommandPermission("axapi.command")
public final class AxAPICommand {
   private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
   private static final String PREFIX = "<#00CCFF><b>AxAPI</b> <gray>» ";
   private static final Gson gson = new Gson();

   @Subcommand({"dump"})
   @CommandPermission("axapi.command.dump")
   public void dumpPlugins(CommandSender sender) {
      String dump = "AxAPI DUMP @ %time%\nServer software: %server-software%\nServer version: %server-version%\nMinecraft version: %minecraft-version%\nBukkit version: %bukkit-version%\nAxAPI NMS version: %version-axapi%\nAxAPI protocol version: %protocol-version%\nAxAPI build: %axapi-build%\n\nAx plugins:\n%axplugins%\n\nOther plugins:\n%otherplugins%\n";
      StringBuilder axPlugins = new StringBuilder();
      StringBuilder otherPlugins = new StringBuilder();

      for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
         String axPackage = this.findAxPackage(plugin);
         if (axPackage == null) {
            String otherPlugin = "----%name%----\nVersion: %version%\nEnabled: %enabled%\n"
               .replace("%name%", plugin.getName())
               .replace("%version%", plugin.getDescription().getVersion())
               .replace("%enabled%", String.valueOf(plugin.isEnabled()));
            otherPlugins.append(otherPlugin).append('\n');
         } else {
            FieldAccessor accessor = FieldAccessor.builder().withClass(axPackage.replace("/", ".") + "BuildConstants").withField("VERSION").build();
            if (FeatureFlags.DEBUG.get()) {
               LogUtils.debug("Getting accessor for class: {}", axPackage.replace("/", ".") + "BuildConstants");
            }

            String axPlugin = "----%name%----\nVersion: %version%\nAxAPI version: %axapi-version%\nEnabled: %enabled%\n"
               .replace("%name%", plugin.getName())
               .replace("%version%", plugin.getDescription().getVersion())
               .replace("%enabled%", String.valueOf(plugin.isEnabled()))
               .replace("%axapi-version%", (CharSequence)(accessor == null ? "UNKNOWN VERSION" : accessor.get(null, String.class)));
            axPlugins.append(axPlugin).append('\n');
         }
      }

      this.upload(
            dump.replace("%time%", formatter.format(LocalDateTime.now()))
               .replace("%server-software%", Bukkit.getName())
               .replace("%server-version%", Bukkit.getVersion())
               .replace("%minecraft-version%", String.join("/", Version.getServerVersion().getVersions()))
               .replace("%bukkit-version%", Bukkit.getBukkitVersion())
               .replace("%version-axapi%", Version.getServerVersion().getNMSVersion())
               .replace("%protocol-version%", String.valueOf(Version.getServerVersion().getProtocolId()))
               .replace("%axapi-build%", "2.2.0-DEV-NEW-VERSION-5")
               .replace("%axplugins%", axPlugins)
               .replace("%otherplugins%", otherPlugins)
         )
         .thenAccept(
            response -> {
               String body = response.body();
               if (body != null && !body.isBlank()) {
                  JsonObject object = (JsonObject)gson.fromJson(body, JsonObject.class);
                  String key = object.get("key").getAsString();
                  if (sender instanceof Player player) {
                     Component message = MiniMessage.miniMessage()
                        .deserialize(
                           "<#00CCFF><b>AxAPI</b> <gray>» "
                              + "<green>Successfully uploaded data to our paste service! URL: <click:open_url:'https://paste.artillex-studios.com/%key%'>https://paste.artillex-studios.com/%key%</click>"
                                 .replace("%key%", key)
                        );
                     ServerPlayerWrapper wrapper = ServerPlayerWrapper.wrap(player);
                     wrapper.message(message);
                  } else {
                     MessageUtils.sendMessage(
                        sender,
                        "<#00CCFF><b>AxAPI</b> <gray>» ",
                        "<green>Successfully uploaded data to our paste service! URL: <click:open_url:'https://paste.artillex-studios.com/%key%'>https://paste.artillex-studios.com/%key%</click>"
                           .replace("%key%", key)
                     );
                  }
               } else {
                  MessageUtils.sendMessage(sender, "<#00CCFF><b>AxAPI</b> <gray>» ", "<red>Failed to upload data!");
               }
            }
         );
   }

   @Subcommand({"print"})
   @CommandPermission("axapi.command.print")
   public void printPlugins(CommandSender sender) {
      String dump = "AxAPI DUMP @ %time%\nServer software: %server-software%\nServer version: %server-version%\nMinecraft version: %minecraft-version%\nBukkit version: %bukkit-version%\nAxAPI NMS version: %version-axapi%\nAxAPI protocol version: %protocol-version%\nAxAPI build: %axapi-build%\n\nAx plugins:\n%axplugins%\n\nPackets:\n%packets%\n";
      StringBuilder axPlugins = new StringBuilder();

      for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
         String axPackage = this.findAxPackage(plugin);
         if (axPackage != null) {
            FieldAccessor accessor = FieldAccessor.builder().withClass(axPackage.replace("/", ".") + "BuildConstants").withField("VERSION").build();
            if (FeatureFlags.DEBUG.get()) {
               LogUtils.debug("Getting accessor for class: {}", axPackage.replace("/", ".") + "BuildConstants");
            }

            String axPlugin = "----%name%----\nVersion: %version%\nAxAPI version: %axapi-version%\nEnabled: %enabled%\n"
               .replace("%name%", plugin.getName())
               .replace("%version%", plugin.getDescription().getVersion())
               .replace("%enabled%", String.valueOf(plugin.isEnabled()))
               .replace("%axapi-version%", (CharSequence)(accessor == null ? "UNKNOWN VERSION" : accessor.get(null, String.class)));
            axPlugins.append(axPlugin).append('\n');
         }
      }

      String packets = "AXAPI Registered:\nClientbound:\n%clientbound%\n\nServerbound:\n%serverbound%\n\nServer registered:\nClientbound:\n%server-clientbound%\n\nServerbound:\n%server-serverbound%\n"
         .replace("%clientbound%", ClientboundPacketTypes.dump())
         .replace("%serverbound%", ServerboundPacketTypes.dump())
         .replace("%server-clientbound%", ServerWrapper.INSTANCE.listPackets(PacketSide.CLIENT_BOUND))
         .replace("%server-serverbound%", ServerWrapper.INSTANCE.listPackets(PacketSide.SERVER_BOUND));
      this.upload(
            dump.replace("%time%", formatter.format(LocalDateTime.now()))
               .replace("%server-software%", Bukkit.getName())
               .replace("%server-version%", Bukkit.getVersion())
               .replace("%minecraft-version%", String.join("/", Version.getServerVersion().getVersions()))
               .replace("%bukkit-version%", Bukkit.getBukkitVersion())
               .replace("%version-axapi%", Version.getServerVersion().getNMSVersion())
               .replace("%protocol-version%", String.valueOf(Version.getServerVersion().getProtocolId()))
               .replace("%axapi-build%", "2.2.0-DEV-NEW-VERSION-5")
               .replace("%axplugins%", axPlugins)
               .replace("%packets%", packets.toString())
         )
         .thenAccept(
            response -> {
               String body = response.body();
               if (body != null && !body.isBlank()) {
                  JsonObject object = (JsonObject)gson.fromJson(body, JsonObject.class);
                  String key = object.get("key").getAsString();
                  if (sender instanceof Player player) {
                     Component message = MiniMessage.miniMessage()
                        .deserialize(
                           "<#00CCFF><b>AxAPI</b> <gray>» "
                              + "<green>Successfully uploaded data to our paste service! URL: <click:open_url:'https://paste.artillex-studios.com/%key%'>https://paste.artillex-studios.com/%key%</click>"
                                 .replace("%key%", key)
                        );
                     ServerPlayerWrapper wrapper = ServerPlayerWrapper.wrap(player);
                     wrapper.message(message);
                  } else {
                     MessageUtils.sendMessage(
                        sender,
                        "<#00CCFF><b>AxAPI</b> <gray>» ",
                        "<green>Successfully uploaded data to our paste service! URL: <click:open_url:'https://paste.artillex-studios.com/%key%'>https://paste.artillex-studios.com/%key%</click>"
                           .replace("%key%", key)
                     );
                  }
               } else {
                  MessageUtils.sendMessage(sender, "<#00CCFF><b>AxAPI</b> <gray>» ", "<red>Failed to upload data!");
               }
            }
         );
   }

   private String findAxPackage(Plugin plugin) {
      try {
         String var5;
         try (ZipFile file = new ZipFile(Paths.get(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).toFile())) {
            Iterator<? extends ZipEntry> iterator = file.entries().asIterator();

            ZipEntry entry;
            do {
               if (!iterator.hasNext()) {
                  return null;
               }

               entry = iterator.next();
            } while (!entry.getName().contains("AxPlugin.class"));

            var5 = entry.getName().replace("AxPlugin.class", "");
         }

         return var5;
      } catch (IOException | URISyntaxException var8) {
         LogUtils.error("Failed to get plugins!", var8);
         return null;
      }
   }

   private CompletableFuture<HttpResponse<String>> upload(String data) {
      return Requests.postAsyncString(
         "https://paste.artillex-studios.com/api/post", Map.of("User-Agent", "axapi", "Bytebin-Api-Key", "axplugin", "Content-Type", "text/plain"), () -> data
      );
   }

   @Subcommand({"snbt"})
   @CommandPermission("axapi.command.snbt")
   public void dumpSNBT(Player player) {
      ItemStack stack = player.getInventory().getItemInMainHand();
      if (stack != null && !stack.getType().isAir()) {
         ItemBuilder builder = ItemBuilder.create(stack);
         String serialized = (String)builder.serialize(true).get("snbt");
         String escaped = MiniMessage.miniMessage().escapeTags(serialized);
         Component message = StringUtils.format("<#00CCFF><b>AxAPI</b> <gray>» ")
            .append(
               ((TextComponent)Component.text("Dumped data! Click to copy to clipboard: " + escaped).color(NamedTextColor.GREEN))
                  .clickEvent(ClickEvent.copyToClipboard(escaped))
            );
         ServerPlayerWrapper wrapper = ServerPlayerWrapper.wrap(player);
         wrapper.message(message);
      } else {
         MessageUtils.sendMessage(player, "<#00CCFF><b>AxAPI</b> <gray>» ", "<red>You need to be holding an item in your main hand!");
      }
   }
}
