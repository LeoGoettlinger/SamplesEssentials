package com.skyblockexp.ezrtp.integration;

import com.skyblockexp.ezrtp.gui.FactionClaimSelectionGuiManager;
import java.util.logging.Level;
import org.bukkit.plugin.Plugin;

public final class TeamsApiSubcommandBridge {
   private final Plugin plugin;
   private final FactionClaimSelectionGuiManager factionClaimGuiManager;
   private Object registeredSubcommand;

   public TeamsApiSubcommandBridge(Plugin plugin, FactionClaimSelectionGuiManager factionClaimGuiManager) {
      this.plugin = plugin;
      this.factionClaimGuiManager = factionClaimGuiManager;
   }

   public void register() {
      if (this.plugin.getServer().getPluginManager().isPluginEnabled("TeamsAPI")) {
         try {
            this.registeredSubcommand = TeamsApiSubcommandOps.registerSubcommand(this.plugin, this.factionClaimGuiManager);
            this.plugin.getLogger().info("TeamsAPI integration: registered '/f rtp' subcommand.");
         } catch (NoClassDefFoundError var2) {
         } catch (Throwable var3) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to register TeamsAPI '/f rtp' subcommand.", var3);
         }
      }
   }

   public void unregister() {
      if (this.registeredSubcommand != null) {
         try {
            TeamsApiSubcommandOps.unregisterSubcommand(this.registeredSubcommand);
         } catch (Throwable var5) {
            this.plugin.getLogger().log(Level.FINE, "Failed to unregister TeamsAPI subcommand cleanly.", var5);
         } finally {
            this.registeredSubcommand = null;
         }
      }
   }
}
