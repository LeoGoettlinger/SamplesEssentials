package ch.njol.skript.hooks;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Documentation;
import java.io.IOException;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class VaultHook extends Hook<Plugin> {
   public static final String NO_GROUP_SUPPORT = "The permissions plugin you are using does not support groups.";
   public static Economy economy;
   public static Chat chat;
   public static Permission permission;

   public VaultHook() throws IOException {
   }

   @Override
   protected boolean init() {
      economy = Bukkit.getServicesManager().getRegistration(Economy.class) == null
         ? null
         : (Economy)Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
      chat = Bukkit.getServicesManager().getRegistration(Chat.class) == null
         ? null
         : (Chat)Bukkit.getServicesManager().getRegistration(Chat.class).getProvider();
      permission = Bukkit.getServicesManager().getRegistration(Permission.class) == null
         ? null
         : (Permission)Bukkit.getServicesManager().getRegistration(Permission.class).getProvider();
      return economy != null || chat != null || permission != null;
   }

   @Override
   protected void loadClasses() throws IOException {
      if (economy != null || Documentation.canGenerateUnsafeDocs()) {
         Skript.getAddonInstance().loadClasses(this.getClass().getPackage().getName() + ".economy");
      }

      if (chat != null || Documentation.canGenerateUnsafeDocs()) {
         Skript.getAddonInstance().loadClasses(this.getClass().getPackage().getName() + ".chat");
      }

      if (permission != null || Documentation.canGenerateUnsafeDocs()) {
         Skript.getAddonInstance().loadClasses(this.getClass().getPackage().getName() + ".permission");
      }
   }

   @Override
   public String getName() {
      return "Vault";
   }
}
