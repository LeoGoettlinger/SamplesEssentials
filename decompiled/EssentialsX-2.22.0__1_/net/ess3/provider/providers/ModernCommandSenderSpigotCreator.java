package net.ess3.provider.providers;

import java.util.UUID;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender.Spigot;

public final class ModernCommandSenderSpigotCreator {
   private ModernCommandSenderSpigotCreator() {
   }

   public static Spigot stupidDumbHackToMakeTheJvmHappy(BukkitSenderProvider provider) {
      return new Spigot() {
         public void sendMessage(BaseComponent component) {
            provider.sendMessage(component.toLegacyText());
         }

         public void sendMessage(BaseComponent... components) {
            this.sendMessage(new TextComponent(components));
         }

         public void sendMessage(UUID sender, BaseComponent... components) {
            this.sendMessage(components);
         }

         public void sendMessage(UUID sender, BaseComponent component) {
            this.sendMessage(component);
         }
      };
   }
}
