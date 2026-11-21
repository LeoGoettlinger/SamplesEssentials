package com.magmaguy.elitemobs.magmacore.nightbreak;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.SpigotMessage;
import java.util.function.BooleanSupplier;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class NightbreakFirstTimeSetupWarner implements Listener {
   private final JavaPlugin ownerPlugin;
   private final NightbreakFirstTimeSetupSpec spec;
   private final BooleanSupplier setupDoneSupplier;
   private final NightbreakPluginSpec pluginSpec;

   public NightbreakFirstTimeSetupWarner(
      JavaPlugin ownerPlugin, NightbreakFirstTimeSetupSpec spec, BooleanSupplier setupDoneSupplier, NightbreakPluginSpec pluginSpec
   ) {
      this.ownerPlugin = ownerPlugin;
      this.spec = spec;
      this.setupDoneSupplier = setupDoneSupplier;
      this.pluginSpec = pluginSpec;
   }

   public NightbreakFirstTimeSetupWarner(JavaPlugin ownerPlugin, NightbreakFirstTimeSetupSpec spec, BooleanSupplier setupDoneSupplier) {
      this(ownerPlugin, spec, setupDoneSupplier, null);
   }

   @EventHandler
   public void onPlayerLogin(final PlayerJoinEvent event) {
      if (!this.setupDoneSupplier.getAsBoolean()) {
         if (event.getPlayer().hasPermission(this.spec.adminPermission())) {
            (new BukkitRunnable() {
                  public void run() {
                     if (event.getPlayer().isOnline()) {
                        Logger.sendSimpleMessage(event.getPlayer(), "&8&m----------------------------------------------------");
                        Logger.sendMessage(event.getPlayer(), "&fInitial setup message:");
                        Logger.sendSimpleMessage(
                           event.getPlayer(),
                           "&7Welcome to " + NightbreakFirstTimeSetupWarner.this.spec.pluginDisplayName() + "! &c&lIt looks like setup is still pending."
                        );
                        boolean showContentPackages = NightbreakFirstTimeSetupWarner.this.pluginSpec == null
                           || NightbreakFirstTimeSetupWarner.this.pluginSpec.hasContentPackages();
                        if (!NightbreakFirstTimeSetupWarner.this.spec.initializeCommand().isEmpty()) {
                           String wording = showContentPackages ? " &7to walk through the guided content setup." : " &7to walk through the first-time setup.";
                           event.getPlayer()
                              .spigot()
                              .sendMessage(
                                 new BaseComponent[]{
                                    SpigotMessage.simpleMessage("&7Use "),
                                    SpigotMessage.commandHoverMessage(
                                       "&a" + NightbreakFirstTimeSetupWarner.this.spec.initializeCommand(),
                                       "&7Click to open the " + NightbreakFirstTimeSetupWarner.this.spec.pluginDisplayName() + " first-time setup flow.",
                                       NightbreakFirstTimeSetupWarner.this.spec.initializeCommand()
                                    ),
                                    SpigotMessage.simpleMessage(wording)
                                 }
                              );
                        }

                        event.getPlayer()
                           .spigot()
                           .sendMessage(
                              new BaseComponent[]{
                                 SpigotMessage.simpleMessage("&7You can browse content any time with "),
                                 SpigotMessage.commandHoverMessage(
                                    "&a" + NightbreakFirstTimeSetupWarner.this.spec.setupCommand(),
                                    "&7Click to open the " + NightbreakFirstTimeSetupWarner.this.spec.pluginDisplayName() + " setup menu.",
                                    NightbreakFirstTimeSetupWarner.this.spec.setupCommand()
                                 ),
                                 SpigotMessage.simpleMessage("&7.")
                              }
                           );
                        if (NightbreakFirstTimeSetupWarner.this.spec.supportUrl() != null && !NightbreakFirstTimeSetupWarner.this.spec.supportUrl().isEmpty()) {
                           event.getPlayer()
                              .spigot()
                              .sendMessage(
                                 new BaseComponent[]{
                                    SpigotMessage.simpleMessage("&7Support: "),
                                    SpigotMessage.hoverLinkMessage(
                                       "&9&n" + NightbreakFirstTimeSetupWarner.this.spec.supportUrl(),
                                       "&7Click to open the support link.",
                                       NightbreakFirstTimeSetupWarner.this.spec.supportUrl()
                                    )
                                 }
                              );
                        }

                        for (String warningNote : NightbreakFirstTimeSetupWarner.this.spec.warningNotes()) {
                           Logger.sendSimpleMessage(event.getPlayer(), warningNote);
                        }

                        Logger.sendSimpleMessage(event.getPlayer(), "&8&m----------------------------------------------------");
                     }
                  }
               })
               .runTaskLater(this.ownerPlugin, 200L);
         }
      }
   }
}
