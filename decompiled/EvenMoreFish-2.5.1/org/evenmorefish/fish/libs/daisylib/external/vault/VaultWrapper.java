package org.evenmorefish.fish.libs.daisylib.external.vault;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.evenmorefish.fish.libs.daisylib.DaisyLib;
import org.evenmorefish.fish.libs.daisylib.utils.CommonUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class VaultWrapper implements Listener {
   private static final VaultWrapper INSTANCE = new VaultWrapper();
   private static final boolean VAULT_AVAILABLE = CommonUtils.classExists("net.milkbowl.vault.economy.Economy");
   private static boolean LISTENER_REGISTERED = false;
   private Economy economy;
   private Permission permission;
   private Chat chat;

   private VaultWrapper() {
   }

   @NonNull
   public static VaultWrapper get() {
      return INSTANCE;
   }

   public void load() {
      if (VAULT_AVAILABLE) {
         this.setupEconomy();
         this.setupPermissions();
         this.setupChat();
         if (!LISTENER_REGISTERED) {
            Bukkit.getPluginManager().registerEvents(this, DaisyLib.get().getPlugin());
            LISTENER_REGISTERED = true;
         }
      }
   }

   public boolean isEconomyAvailable() {
      return this.economy != null;
   }

   public boolean isPermissionAvailable() {
      return this.permission != null;
   }

   public boolean isChatAvailable() {
      return this.chat != null;
   }

   @NonNull
   public Economy getEconomy() throws IllegalStateException {
      if (this.economy == null) {
         throw new IllegalStateException("Economy is not available.");
      } else {
         return this.economy;
      }
   }

   @Nullable
   public Economy getEconomyOrNull() {
      return this.economy;
   }

   @NonNull
   public Permission getPermission() throws IllegalStateException {
      if (this.permission == null) {
         throw new IllegalStateException("Permission is not available.");
      } else {
         return this.permission;
      }
   }

   @Nullable
   public Permission getPermissionOrNull() {
      return this.permission;
   }

   @NonNull
   public Chat getChat() throws IllegalStateException {
      if (this.chat == null) {
         throw new IllegalStateException("Chat is not available.");
      } else {
         return this.chat;
      }
   }

   @Nullable
   public Chat getChatOrNull() {
      return this.chat;
   }

   private void setupEconomy() {
      RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
      if (rsp != null) {
         this.economy = (Economy)rsp.getProvider();
      }
   }

   private void setupPermissions() {
      RegisteredServiceProvider<Permission> rsp = Bukkit.getServicesManager().getRegistration(Permission.class);
      if (rsp != null) {
         this.permission = (Permission)rsp.getProvider();
      }
   }

   private void setupChat() {
      RegisteredServiceProvider<Chat> rsp = Bukkit.getServicesManager().getRegistration(Chat.class);
      if (rsp != null) {
         this.chat = (Chat)rsp.getProvider();
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onRegister(@NonNull ServiceRegisterEvent event) {
      Class<?> clazz = event.getProvider().getService();
      if (clazz.equals(Economy.class)) {
         DaisyLib.get().getLogging().info("Detected a new economy provider. Reloading VaultWrapper.");
         this.setupEconomy();
      } else if (clazz.equals(Permission.class)) {
         DaisyLib.get().getLogging().info("Detected a new permission provider. Reloading VaultWrapper.");
         this.setupPermissions();
      } else if (clazz.equals(Chat.class)) {
         DaisyLib.get().getLogging().info("Detected a new chat provider. Reloading VaultWrapper.");
         this.setupChat();
      }
   }
}
