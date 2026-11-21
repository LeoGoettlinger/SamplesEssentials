package com.earth2me.essentials.commands.essentials;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.adventure.ComponentHolder;
import com.earth2me.essentials.commands.EssentialsTreeNode;
import com.earth2me.essentials.economy.EconomyLayer;
import com.earth2me.essentials.economy.EconomyLayers;
import com.earth2me.essentials.utils.VersionUtil;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

public class VersionCommand extends EssentialsTreeNode {
   private static final List<String> versionPlugins = Arrays.asList(
      "Vault",
      "Reserve",
      "PlaceholderAPI",
      "CMI",
      "Towny",
      "ChestShop",
      "Citizens",
      "LuckPerms",
      "UltraPermissions",
      "PermissionsEx",
      "GroupManager",
      "bPermissions",
      "DiscordSRV",
      "AntiPopup",
      "NoChatReports",
      "NoEncryption"
   );

   public VersionCommand() {
      super("version", "ver");
   }

   @Override
   protected void run(CommandSource sender, String commandLabel, String[] args) throws Exception {
      if (!sender.isPlayer() || this.ess.getUser(sender.getPlayer()).isAuthorized("essentials.version")) {
         boolean isMismatched = false;
         boolean isVaultInstalled = false;
         boolean isUnsupported = false;
         VersionUtil.SupportStatus supportStatus = VersionUtil.getServerSupportStatus();
         PluginManager pm = Bukkit.getPluginManager();
         String essVer = pm.getPlugin("Essentials").getDescription().getVersion();
         String serverMessageKey;
         if (supportStatus.isSupported()) {
            serverMessageKey = "versionOutputFine";
         } else if (supportStatus == VersionUtil.SupportStatus.UNSTABLE) {
            serverMessageKey = "versionOutputUnsupported";
         } else {
            serverMessageKey = "versionOutputWarn";
         }

         sender.sendTl(serverMessageKey, "Server", Bukkit.getBukkitVersion() + " " + Bukkit.getVersion());
         sender.sendTl(serverMessageKey, "Brand", Bukkit.getName());
         sender.sendTl("versionOutputFine", "EssentialsX", essVer);

         for (Plugin plugin : pm.getPlugins()) {
            PluginDescriptionFile desc = plugin.getDescription();
            String name = desc.getName();
            String version = desc.getVersion();
            if (name.startsWith("Essentials") && !name.equalsIgnoreCase("Essentials")) {
               if (VersionUtil.officialPlugins.contains(name)) {
                  name = name.replace("Essentials", "EssentialsX");
                  if (!version.equalsIgnoreCase(essVer)) {
                     isMismatched = true;
                     sender.sendTl("versionOutputWarn", name, version);
                  } else {
                     sender.sendTl("versionOutputFine", name, version);
                  }
               } else {
                  sender.sendTl("versionOutputUnsupported", name, version);
                  isUnsupported = true;
               }
            }

            if (versionPlugins.contains(name)) {
               if (VersionUtil.warnPlugins.contains(name)) {
                  sender.sendTl("versionOutputUnsupported", name, version);
                  isUnsupported = true;
               } else {
                  sender.sendTl("versionOutputFine", name, version);
               }
            }

            if (name.equals("Vault")) {
               isVaultInstalled = true;
            }
         }

         String layer;
         if (this.ess.getSettings().isEcoDisabled()) {
            layer = "Disabled";
         } else if (EconomyLayers.isLayerSelected()) {
            EconomyLayer economyLayer = EconomyLayers.getSelectedLayer();
            layer = economyLayer.getName() + " (" + economyLayer.getBackendName() + ")";
         } else {
            layer = "None";
         }

         sender.sendTl("versionOutputEconLayer", layer);
         if (isMismatched) {
            sender.sendTl("versionMismatchAll");
         }

         if (!isVaultInstalled) {
            sender.sendTl("versionOutputVaultMissing");
         }

         if (isUnsupported) {
            sender.sendTl("versionOutputUnsupportedPlugins");
         }

         switch (supportStatus) {
            case NMS_CLEANROOM:
               sender.sendTl("serverUnsupportedCleanroom");
               break;
            case DANGEROUS_FORK:
               sender.sendTl("serverUnsupportedDangerous");
               break;
            case STUPID_PLUGIN:
               sender.sendTl("serverUnsupportedDumbPlugins");
               break;
            case UNSTABLE:
               sender.sendTl("serverUnsupportedMods");
               break;
            case OUTDATED:
               sender.sendTl("serverUnsupported");
               break;
            case LIMITED:
               sender.sendTl("serverUnsupportedLimitedApi");
         }

         if (VersionUtil.getSupportStatusClass() != null) {
            sender.sendTl("serverUnsupportedClass");
         }

         sender.sendTl("versionFetching");
         this.ess.runTaskAsynchronously(() -> {
            for (ComponentHolder component : this.ess.getUpdateChecker().getVersionMessages(true, true, sender)) {
               sender.sendComponent(component);
            }
         });
      }
   }
}
