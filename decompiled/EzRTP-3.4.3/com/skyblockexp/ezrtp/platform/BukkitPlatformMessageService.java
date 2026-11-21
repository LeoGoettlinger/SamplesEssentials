package com.skyblockexp.ezrtp.platform;

import com.skyblockexp.ezrtp.util.MessageUtil;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class BukkitPlatformMessageService implements PlatformMessageService {
   private final Plugin plugin;
   private final BukkitAudiences audiences;

   public BukkitPlatformMessageService(Plugin plugin) {
      this.plugin = plugin;
      BukkitAudiences tmp = null;

      try {
         tmp = BukkitAudiences.create(plugin);
      } catch (Throwable var4) {
      }

      this.audiences = tmp;
   }

   @Override
   public String resolvePlaceholders(Object player, String text, Logger logger) {
      if (text == null) {
         return null;
      } else {
         try {
            return player instanceof Player ? PlaceholderAPI.setPlaceholders((Player)player, text) : PlaceholderAPI.setPlaceholders(null, text);
         } catch (Throwable var5) {
            logger.fine("PlaceholderAPI resolution failed: " + var5.getMessage());
            return text;
         }
      }
   }

   @Override
   public Component resolvePlaceholdersComponent(Object player, Component component, Logger logger) {
      if (component == null) {
         return Component.empty();
      } else {
         try {
            String playerName = null;
            if (player instanceof Player) {
               playerName = ((Player)player).getName();
            } else {
               try {
                  Method m = player.getClass().getMethod("getName");
                  Object nm = m.invoke(player);
                  if (nm != null) {
                     playerName = nm.toString();
                  }
               } catch (Throwable var12) {
               }
            }

            if (playerName == null) {
               return component;
            } else {
               try {
                  Class<?> trcClass = Class.forName("net.kyori.adventure.text.replace.TextReplacementConfig");
                  Method builderMeth = trcClass.getMethod("builder");
                  Object builder = builderMeth.invoke(null);
                  builder.getClass().getMethod("matchLiteral", String.class).invoke(builder, "<player>");
                  builder.getClass().getMethod("replacement", Component.class).invoke(builder, Component.text(playerName));
                  Object trc = builder.getClass().getMethod("build").invoke(builder);
                  Method replaceMeth = component.getClass().getMethod("replaceText", trcClass);
                  Object replaced = replaceMeth.invoke(component, trc);
                  if (replaced instanceof Component) {
                     return (Component)replaced;
                  }
               } catch (Throwable var11) {
               }

               return this.fastReplace(component, playerName);
            }
         } catch (Throwable var13) {
            logger.fine("Component placeholder resolution failed: " + var13.getMessage());
            return component;
         }
      }
   }

   private Component fastReplace(Component c, String name) {
      if (c instanceof TextComponent tc) {
         String content = tc.content();
         if (!content.contains("<player>")) {
            List<Component> newChildren = new ArrayList<>();
            boolean changed = false;

            for (Component child : c.children()) {
               Component nc = this.fastReplace(child, name);
               newChildren.add(nc);
               if (nc != child) {
                  changed = true;
               }
            }

            if (changed) {
               try {
                  TextComponent.Builder b = tc.toBuilder();

                  for (Component nch : newChildren) {
                     b.append(nch);
                  }

                  return b.build();
               } catch (Throwable var10) {
                  return c;
               }
            } else {
               return c;
            }
         } else {
            String replaced = content.replace("<player>", name);

            try {
               TextComponent.Builder b = tc.toBuilder().content(replaced);

               for (Component childx : c.children()) {
                  b.append(this.fastReplace(childx, name));
               }

               return b.build();
            } catch (Throwable var11) {
               Component base = Component.text(replaced);
               if (!c.children().isEmpty()) {
                  base = base.toBuilder().append(c.children().toArray(new Component[0])).build();
               }

               return base;
            }
         }
      } else {
         List<Component> newChildren = new ArrayList<>();
         boolean changed = false;

         for (Component childx : c.children()) {
            Component nc = this.fastReplace(childx, name);
            newChildren.add(nc);
            if (nc != childx) {
               changed = true;
            }
         }

         if (changed) {
            try {
               ComponentBuilder<?, ? extends ComponentBuilder<?, ?>> b = (ComponentBuilder<?, ? extends ComponentBuilder<?, ?>>)c.toBuilder();

               for (Component nch : newChildren) {
                  b.append(nch);
               }

               return b.build();
            } catch (Throwable var12) {
               return c;
            }
         } else {
            return c;
         }
      }
   }

   @Override
   public boolean sendToSender(Object sender, Component component) {
      if (sender != null && component != null) {
         try {
            if (sender instanceof CommandSender commandSender) {
               if (this.audiences != null) {
                  this.audiences.sender((CommandSender)sender).sendMessage(component);
                  return true;
               }

               commandSender.sendMessage(MessageUtil.serializeComponent(component));
               return true;
            }

            try {
               sender.getClass().getMethod("sendMessage", String.class).invoke(sender, MessageUtil.serializeComponent(component));
               return true;
            } catch (Throwable var4) {
            }
         } catch (Throwable var5) {
         }

         return false;
      } else {
         return false;
      }
   }

   @Override
   public void close() {
      try {
         if (this.audiences != null) {
            this.audiences.close();
         }
      } catch (Throwable var2) {
      }
   }
}
