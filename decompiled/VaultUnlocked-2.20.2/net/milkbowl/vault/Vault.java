package net.milkbowl.vault;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
import net.milkbowl.libs.faststats.bukkit.BukkitMetrics;
import net.milkbowl.libs.faststats.core.data.Metric;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.metrics.bukkit.Metrics;
import net.milkbowl.vault.metrics.charts.SimplePie;
import net.milkbowl.vault.papi.EconomyPlaceholders;
import net.milkbowl.vault.permission.Permission;
import net.milkbowl.vault2.chat.ChatUnlocked;
import net.milkbowl.vault2.permission.PermissionUnlocked;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Vault extends JavaPlugin {
   private static final String VAULT_BUKKIT_URL = "https://www.spigotmc.org/resources/vaultunlocked.117277/";
   private Logger log;
   private String newVersionTitle = "";
   private double newVersion = 0.0;
   private double currentVersion = 0.0;
   private String currentVersionTitle = "";
   private ScheduledExecutorService asyncTaskTimer;
   private static Vault instance;
   private final BukkitMetrics.Factory fastStats = BukkitMetrics.factory().token("52b0541b7a48894156360f1d7a83e461");

   public void onDisable() {
      this.getServer().getServicesManager().unregisterAll(this);
      if (this.asyncTaskTimer != null) {
         this.asyncTaskTimer.shutdownNow();
      }
   }

   public void onEnable() {
      instance = this;
      this.log = this.getLogger();
      this.currentVersionTitle = this.getDescription().getVersion().split("-")[0];
      this.currentVersion = Double.parseDouble(this.currentVersionTitle.replaceFirst("\\.", ""));
      this.getConfig().addDefault("update-check", true);
      this.getConfig().options().copyDefaults(true);
      this.saveConfig();
      this.getCommand("vault-info").setExecutor(this);
      this.getCommand("vault-convert").setExecutor(this);
      this.getServer().getPluginManager().registerEvents(new Vault.VaultListener(), this);
      Metrics metrics = new Metrics(this, 22252);
      this.findCustomData(metrics);

      for (Metric<?> metric : this.customMetrics()) {
         this.fastStats.addMetric(metric);
      }

      this.fastStats.create((Plugin)this);
      if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
         new EconomyPlaceholders().register();
      } else {
         Bukkit.getPluginManager().registerEvents(new PluginEnableListener(), this);
      }

      this.log.info(String.format("Enabled Version %s", this.getDescription().getVersion()));
   }

   public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel, final String[] args) {
      if (command.getName().equalsIgnoreCase("vault-info")) {
         if (!sender.hasPermission("vault.admin.info")) {
            sender.sendMessage("You do not have permission to use that command!");
            return true;
         } else {
            this.infoCommand(sender);
            return true;
         }
      } else if (command.getName().equalsIgnoreCase("vault-convert")) {
         if (!sender.hasPermission("vault.admin.convert")) {
            sender.sendMessage("You do not have permission to use that command!");
            return true;
         } else {
            this.convertCommand(sender, args);
            return true;
         }
      } else {
         sender.sendMessage("VaultUnlocked Commands:");
         sender.sendMessage("  /vault-info - Displays information about Vault");
         sender.sendMessage("  /vault-convert [economy1] [economy2] - Converts from one Economy to another");
         return true;
      }
   }

   private void convertCommand(final CommandSender sender, final String[] args) {
      Collection<RegisteredServiceProvider<Economy>> econs = this.getServer().getServicesManager().getRegistrations(Economy.class);
      Collection<RegisteredServiceProvider<net.milkbowl.vault2.economy.Economy>> econs2 = this.getServer()
         .getServicesManager()
         .getRegistrations(net.milkbowl.vault2.economy.Economy.class);
      if (econs != null && (econs.size() >= 2 || !econs2.isEmpty())) {
         if (args.length != 2) {
            sender.sendMessage("You must specify only the economy to convert from and the economy to convert to. (names should not contain spaces)");
         } else {
            Economy econ1 = null;
            Economy econ2 = null;
            net.milkbowl.vault2.economy.Economy econ1Unlocked = null;
            net.milkbowl.vault2.economy.Economy econ2Unlocked = null;
            StringBuilder economies = new StringBuilder();

            for (RegisteredServiceProvider<Economy> econ : econs) {
               String econName = ((Economy)econ.getProvider()).getName().replace(" ", "");
               if (econName.equalsIgnoreCase(args[0])) {
                  econ1 = (Economy)econ.getProvider();
               } else if (econName.equalsIgnoreCase(args[1])) {
                  econ2 = (Economy)econ.getProvider();
               }

               if (economies.length() > 0) {
                  economies.append(", ");
               }

               economies.append(econName);
            }

            for (RegisteredServiceProvider<net.milkbowl.vault2.economy.Economy> econ : econs2) {
               String econNamex = ((net.milkbowl.vault2.economy.Economy)econ.getProvider()).getName().replace(" ", "");
               if (econNamex.equalsIgnoreCase(args[0])) {
                  econ1Unlocked = (net.milkbowl.vault2.economy.Economy)econ.getProvider();
               } else if (econNamex.equalsIgnoreCase(args[1])) {
                  econ2Unlocked = (net.milkbowl.vault2.economy.Economy)econ.getProvider();
               }

               if (economies.length() > 0) {
                  economies.append(", ");
               }

               economies.append(econNamex);
            }

            if (econ1 == null && econ1Unlocked == null) {
               sender.sendMessage("Could not find " + args[0] + " loaded on the server, check your spelling.");
               sender.sendMessage("Valid economies are: " + economies);
            } else if (econ2 == null && econ2Unlocked == null) {
               sender.sendMessage("Could not find " + args[1] + " loaded on the server, check your spelling.");
               sender.sendMessage("Valid economies are: " + economies);
            } else {
               sender.sendMessage("This may take some time to convert, expect server lag.");
               boolean useUnlocked1 = econ1Unlocked != null;
               boolean useUnlocked2 = econ2Unlocked != null;
               String pluginID = "vault conversion";

               for (OfflinePlayer op : Bukkit.getServer().getOfflinePlayers()) {
                  if (useUnlocked1) {
                     if (useUnlocked2) {
                        if (!econ2Unlocked.hasAccount(op.getUniqueId())) {
                           econ2Unlocked.createAccount(op.getUniqueId(), op.getName());
                           BigDecimal diff = econ1Unlocked.getBalance("vault conversion", op.getUniqueId())
                              .subtract(econ2Unlocked.getBalance("vault conversion", op.getUniqueId()));
                           if (diff.compareTo(BigDecimal.ZERO) > 0) {
                              econ2Unlocked.deposit("vault conversion", op.getUniqueId(), diff);
                           } else if (diff.compareTo(BigDecimal.ZERO) < 0) {
                              econ2Unlocked.withdraw("vault conversion", op.getUniqueId(), diff.negate());
                           }
                        }
                     } else if (!econ2.hasAccount(op)) {
                        econ2.createPlayerAccount(op);
                        BigDecimal diff = econ1Unlocked.getBalance("vault conversion", op.getUniqueId()).subtract(BigDecimal.valueOf(econ2.getBalance(op)));
                        if (diff.compareTo(BigDecimal.ZERO) > 0) {
                           econ2.depositPlayer(op, diff.doubleValue());
                        } else if (diff.compareTo(BigDecimal.ZERO) < 0) {
                           econ2.withdrawPlayer(op, diff.negate().doubleValue());
                        }
                     }
                  } else if (useUnlocked2) {
                     if (!econ2Unlocked.hasAccount(op.getUniqueId())) {
                        econ2Unlocked.createAccount(op.getUniqueId(), op.getName());
                        BigDecimal diff = BigDecimal.valueOf(econ1.getBalance(op)).subtract(econ2Unlocked.getBalance("vault conversion", op.getUniqueId()));
                        if (diff.compareTo(BigDecimal.ZERO) > 0) {
                           econ2Unlocked.deposit("vault conversion", op.getUniqueId(), diff);
                        } else if (diff.compareTo(BigDecimal.ZERO) < 0) {
                           econ2Unlocked.withdraw("vault conversion", op.getUniqueId(), diff.negate());
                        }
                     }
                  } else if (!econ2.hasAccount(op)) {
                     econ2.createPlayerAccount(op);
                     BigDecimal diff = BigDecimal.valueOf(econ1.getBalance(op)).subtract(BigDecimal.valueOf(econ2.getBalance(op)));
                     if (diff.compareTo(BigDecimal.ZERO) > 0) {
                        econ2.depositPlayer(op, diff.doubleValue());
                     } else if (diff.compareTo(BigDecimal.ZERO) < 0) {
                        econ2.withdrawPlayer(op, diff.negate().doubleValue());
                     }
                  }
               }

               sender.sendMessage("Conversion complete, please verify the data before using it.");
            }
         }
      } else {
         sender.sendMessage("You must have at least 2 economies loaded to convert.");
      }
   }

   private void infoCommand(final CommandSender sender) {
      StringBuilder registeredEcons = new StringBuilder();

      for (RegisteredServiceProvider<Economy> econ : this.getServer().getServicesManager().getRegistrations(Economy.class)) {
         if (registeredEcons.length() > 0) {
            registeredEcons.append(", ");
         }

         registeredEcons.append(((Economy)econ.getProvider()).getName());
      }

      StringBuilder registeredModernEcons = new StringBuilder();

      for (RegisteredServiceProvider<net.milkbowl.vault2.economy.Economy> econ : this.getServer()
         .getServicesManager()
         .getRegistrations(net.milkbowl.vault2.economy.Economy.class)) {
         if (registeredModernEcons.length() > 0) {
            registeredModernEcons.append(", ");
         }

         registeredModernEcons.append(((net.milkbowl.vault2.economy.Economy)econ.getProvider()).getName());
      }

      StringBuilder registeredPerms = new StringBuilder();

      for (RegisteredServiceProvider<Permission> perm : this.getServer().getServicesManager().getRegistrations(Permission.class)) {
         if (registeredPerms.length() > 0) {
            registeredPerms.append(", ");
         }

         registeredPerms.append(((Permission)perm.getProvider()).getName());
      }

      StringBuilder registeredModernPerms = new StringBuilder();

      for (RegisteredServiceProvider<PermissionUnlocked> perm : this.getServer().getServicesManager().getRegistrations(PermissionUnlocked.class)) {
         if (registeredModernPerms.length() > 0) {
            registeredModernPerms.append(", ");
         }

         registeredModernPerms.append(((PermissionUnlocked)perm.getProvider()).getName());
      }

      StringBuilder registeredChats = new StringBuilder();

      for (RegisteredServiceProvider<Chat> chat : this.getServer().getServicesManager().getRegistrations(Chat.class)) {
         if (registeredChats.length() > 0) {
            registeredChats.append(", ");
         }

         registeredChats.append(((Chat)chat.getProvider()).getName());
      }

      StringBuilder registeredModernChats = new StringBuilder();

      for (RegisteredServiceProvider<ChatUnlocked> chat : this.getServer().getServicesManager().getRegistrations(ChatUnlocked.class)) {
         if (registeredModernChats.length() > 0) {
            registeredModernChats.append(", ");
         }

         registeredModernChats.append(((ChatUnlocked)chat.getProvider()).getName());
      }

      Economy econ = null;
      RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
      if (rsp != null) {
         econ = (Economy)rsp.getProvider();
      }

      net.milkbowl.vault2.economy.Economy econ2 = null;
      RegisteredServiceProvider<net.milkbowl.vault2.economy.Economy> rsp2 = this.getServer()
         .getServicesManager()
         .getRegistration(net.milkbowl.vault2.economy.Economy.class);
      if (rsp2 != null) {
         econ2 = (net.milkbowl.vault2.economy.Economy)rsp2.getProvider();
      }

      Permission perm = null;
      RegisteredServiceProvider<Permission> rspp = this.getServer().getServicesManager().getRegistration(Permission.class);
      if (rspp != null) {
         perm = (Permission)rspp.getProvider();
      }

      PermissionUnlocked perm2 = null;
      RegisteredServiceProvider<PermissionUnlocked> rspp2 = this.getServer().getServicesManager().getRegistration(PermissionUnlocked.class);
      if (rspp2 != null) {
         perm2 = (PermissionUnlocked)rspp2.getProvider();
      }

      Chat chat = null;
      RegisteredServiceProvider<Chat> rspc = this.getServer().getServicesManager().getRegistration(Chat.class);
      if (rspc != null) {
         chat = (Chat)rspc.getProvider();
      }

      ChatUnlocked chat2 = null;
      RegisteredServiceProvider<ChatUnlocked> rspc2 = this.getServer().getServicesManager().getRegistration(ChatUnlocked.class);
      if (rspc2 != null) {
         chat2 = (ChatUnlocked)rspc2.getProvider();
      }

      sender.sendMessage(String.format("[%s] Vault v%s Information", this.getDescription().getName(), this.getDescription().getVersion()));
      sender.sendMessage(
         String.format(
            "[%s] Economy Legacy: %s%s",
            this.getDescription().getName(),
            econ == null ? "None" : econ.getName(),
            registeredEcons.length() == 0 ? "" : " [" + registeredEcons + "]"
         )
      );
      sender.sendMessage(
         String.format(
            "[%s] Economy Modern: %s%s",
            this.getDescription().getName(),
            econ2 == null ? "None" : econ2.getName(),
            registeredModernEcons.length() == 0 ? "" : " [" + registeredModernEcons + "]"
         )
      );
      sender.sendMessage(
         String.format(
            "[%s] Permission Legacy: %s%s",
            this.getDescription().getName(),
            perm == null ? "None" : perm.getName(),
            registeredPerms.length() == 0 ? "" : " [" + registeredPerms + "]"
         )
      );
      sender.sendMessage(
         String.format(
            "[%s] Permission Modern: %s%s",
            this.getDescription().getName(),
            perm2 == null ? "None" : perm2.getName(),
            registeredModernPerms.length() == 0 ? "" : " [" + registeredModernPerms + "]"
         )
      );
      sender.sendMessage(
         String.format(
            "[%s] Chat Legacy: %s%s",
            this.getDescription().getName(),
            chat == null ? "None" : chat.getName(),
            registeredChats.length() == 0 ? "" : " [" + registeredChats + "]"
         )
      );
      sender.sendMessage(
         String.format(
            "[%s] Chat Modern: %s%s",
            this.getDescription().getName(),
            chat2 == null ? "None" : chat2.getName(),
            registeredModernChats.length() == 0 ? "" : " [" + registeredChats + "]"
         )
      );
   }

   public Optional<net.milkbowl.vault2.economy.Economy> modernProvider() {
      RegisteredServiceProvider<net.milkbowl.vault2.economy.Economy> econ = this.getServer()
         .getServicesManager()
         .getRegistration(net.milkbowl.vault2.economy.Economy.class);
      return econ == null ? Optional.empty() : Optional.ofNullable((net.milkbowl.vault2.economy.Economy)econ.getProvider());
   }

   public Optional<Economy> legacyProvider() {
      RegisteredServiceProvider<Economy> econ = this.getServer().getServicesManager().getRegistration(Economy.class);
      return econ == null ? Optional.empty() : Optional.ofNullable((Economy)econ.getProvider());
   }

   private static boolean packagesExists(final String... packages) {
      try {
         for (String pkg : packages) {
            Class.forName(pkg);
         }

         return true;
      } catch (Exception var5) {
         return false;
      }
   }

   public double updateCheck(final double currentVersion) {
      try {
         URL url = new URL("https://api.curseforge.com/servermods/files?projectids=33184");
         URLConnection conn = url.openConnection();
         conn.setReadTimeout(5000);
         conn.addRequestProperty("User-Agent", "VaultUnlocked Update Checker");
         conn.setDoOutput(true);
         BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
         String response = reader.readLine();
         JSONArray array = (JSONArray)JSONValue.parse(response);
         if (array.isEmpty()) {
            this.getLogger().warning("No files found, or Feed URL is bad.");
            return currentVersion;
         } else {
            this.newVersionTitle = ((String)((JSONObject)array.get(array.size() - 1)).get("name")).replace("Vault", "").trim();
            return Double.parseDouble(this.newVersionTitle.replaceFirst("\\.", "").trim());
         }
      } catch (Exception var8) {
         this.log.info("There was an issue attempting to check for the latest version.");
         return currentVersion;
      }
   }

   private void findCustomData(final Metrics metrics) {
      RegisteredServiceProvider<Economy> rspEcon = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
      Economy econ = null;
      if (rspEcon != null) {
         econ = (Economy)rspEcon.getProvider();
      }

      String econName = econ != null ? econ.getName() : "No Economy";
      metrics.addCustomChart(new SimplePie("economy", () -> econName));
      RegisteredServiceProvider<Permission> rspPerm = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
      Permission perm = null;
      if (rspPerm != null) {
         perm = (Permission)rspPerm.getProvider();
      }

      String permName = perm != null ? perm.getName() : "No Economy";
      metrics.addCustomChart(new SimplePie("permission", () -> permName));
      RegisteredServiceProvider<Chat> rspChat = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
      Chat chat = null;
      if (rspChat != null) {
         chat = (Chat)rspChat.getProvider();
      }

      String chatName = chat != null ? chat.getName() : "No Chat";
      metrics.addCustomChart(new SimplePie("chat", () -> chatName));
   }

   private List<Metric<?>> customMetrics() {
      List<Metric<?>> metrics = new ArrayList<>();
      metrics.add(Metric.string("permission_provider", () -> {
         RegisteredServiceProvider<Permission> rspPerm = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
         RegisteredServiceProvider<PermissionUnlocked> rspPerm2 = Bukkit.getServer().getServicesManager().getRegistration(PermissionUnlocked.class);
         if (rspPerm == null && rspPerm2 == null) {
            return "None";
         } else {
            PermissionUnlocked perm2 = (PermissionUnlocked)rspPerm2.getProvider();
            if (perm2 != null) {
               return perm2.getName();
            } else {
               Permission perm = (Permission)rspPerm.getProvider();
               return perm != null ? perm.getName() : "None";
            }
         }
      }));
      metrics.add(
         Metric.string(
            "economy_provider",
            () -> {
               RegisteredServiceProvider<Economy> rspEconomy = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
               RegisteredServiceProvider<net.milkbowl.vault2.economy.Economy> rspEconomy2 = Bukkit.getServer()
                  .getServicesManager()
                  .getRegistration(net.milkbowl.vault2.economy.Economy.class);
               if (rspEconomy == null && rspEconomy2 == null) {
                  return "None";
               } else {
                  net.milkbowl.vault2.economy.Economy economy2 = (net.milkbowl.vault2.economy.Economy)rspEconomy2.getProvider();
                  if (economy2 != null) {
                     return economy2.getName();
                  } else {
                     Economy economy = (Economy)rspEconomy.getProvider();
                     return economy != null ? economy.getName() : "None";
                  }
               }
            }
         )
      );
      metrics.add(
         Metric.bool(
            "async_economy",
            () -> {
               RegisteredServiceProvider<net.milkbowl.vault2.economy.Economy> rspEconomy2 = Bukkit.getServer()
                  .getServicesManager()
                  .getRegistration(net.milkbowl.vault2.economy.Economy.class);
               if (rspEconomy2 == null) {
                  return false;
               } else {
                  net.milkbowl.vault2.economy.Economy economy = (net.milkbowl.vault2.economy.Economy)rspEconomy2.getProvider();
                  return economy == null ? false : economy.supportsAsync();
               }
            }
         )
      );
      metrics.add(Metric.string("chat_provider", () -> {
         RegisteredServiceProvider<Chat> rspChat = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
         RegisteredServiceProvider<ChatUnlocked> rspChat2 = Bukkit.getServer().getServicesManager().getRegistration(ChatUnlocked.class);
         if (rspChat == null && rspChat2 == null) {
            return "None";
         } else {
            ChatUnlocked chat2 = (ChatUnlocked)rspChat2.getProvider();
            if (chat2 != null) {
               return chat2.getName();
            } else {
               Chat chat = (Chat)rspChat.getProvider();
               return chat != null ? chat.getName() : "None";
            }
         }
      }));
      metrics.add(Metric.string("game_name", () -> "minecraft"));
      return metrics;
   }

   public static Vault instance() {
      return instance;
   }

   public class VaultListener implements Listener {
      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPlayerJoin(final PlayerJoinEvent event) {
         Player player = event.getPlayer();
         if (player.hasPermission("vault.update")) {
            try {
               if (Vault.this.newVersion > Vault.this.currentVersion) {
                  player.sendMessage("VaultUnlocked " + Vault.this.newVersionTitle + " is out! You are running " + Vault.this.currentVersionTitle);
                  player.sendMessage("Update VaultUnlocked at: https://www.spigotmc.org/resources/vaultunlocked.117277/");
               }
            } catch (Exception var4) {
            }
         }
      }
   }
}
