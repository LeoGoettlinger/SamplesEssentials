package com.skyblockexp.ezrtp.util.compat;

import com.skyblockexp.ezrtp.util.MessageUtil;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public final class BossBarCompat {
   private BossBarCompat() {
   }

   public static BossBarCompat.Wrapper create(String title, BarColor color, BarStyle style) {
      try {
         BossBar bossBar = Bukkit.createBossBar(title, color, style, new BarFlag[0]);
         return new BossBarCompat.Wrapper(bossBar, true);
      } catch (Exception | NoSuchMethodError var7) {
         try {
            Method create = Bukkit.class.getMethod("createBossBar", String.class, BarColor.class, BarStyle.class);
            Object bossBarx = create.invoke(null, title, color, style);
            return new BossBarCompat.Wrapper(bossBarx, true);
         } catch (Exception var6) {
            return new BossBarCompat.Wrapper(null, false);
         }
      }
   }

   public static final class Wrapper {
      private final Object bossBarInstance;
      private final BossBar directBossBar;
      private final boolean supported;

      private Wrapper(Object bossBarInstance, boolean supported) {
         this.bossBarInstance = bossBarInstance;
         this.directBossBar = bossBarInstance instanceof BossBar ? (BossBar)bossBarInstance : null;
         this.supported = supported && bossBarInstance != null;
      }

      public boolean isSupported() {
         return this.supported && this.bossBarInstance != null;
      }

      public void setTitle(Component component) {
         if (this.isSupported()) {
            if (this.directBossBar != null) {
               this.directBossBar.setTitle(MessageUtil.serializeComponent(component));
            } else {
               try {
                  Method setTitleStr = this.bossBarInstance.getClass().getMethod("setTitle", String.class);
                  setTitleStr.invoke(this.bossBarInstance, MessageUtil.serializeComponent(component));
               } catch (ReflectiveOperationException var3) {
               }
            }
         }
      }

      public void addPlayer(Player player) {
         if (this.isSupported()) {
            if (this.directBossBar != null) {
               this.directBossBar.addPlayer(player);
            } else {
               try {
                  Method add = this.bossBarInstance.getClass().getMethod("addPlayer", Player.class);
                  add.invoke(this.bossBarInstance, player);
               } catch (ReflectiveOperationException var3) {
               }
            }
         }
      }

      public void removeAll() {
         if (this.isSupported()) {
            if (this.directBossBar != null) {
               this.directBossBar.removeAll();
            } else {
               try {
                  Method rem = this.bossBarInstance.getClass().getMethod("removeAll");
                  rem.invoke(this.bossBarInstance);
               } catch (ReflectiveOperationException var2) {
               }
            }
         }
      }

      public void setProgress(double progress) {
         if (this.isSupported()) {
            if (this.directBossBar != null) {
               this.directBossBar.setProgress(progress);
            } else {
               try {
                  Method set = this.bossBarInstance.getClass().getMethod("setProgress", double.class);
                  set.invoke(this.bossBarInstance, progress);
               } catch (ReflectiveOperationException var4) {
               }
            }
         }
      }

      public void setColor(BarColor color) {
         if (this.isSupported()) {
            if (this.directBossBar != null) {
               this.directBossBar.setColor(color);
            } else {
               try {
                  Method set = this.bossBarInstance.getClass().getMethod("setColor", BarColor.class);
                  set.invoke(this.bossBarInstance, color);
               } catch (ReflectiveOperationException var3) {
               }
            }
         }
      }

      public void setStyle(BarStyle style) {
         if (this.isSupported()) {
            if (this.directBossBar != null) {
               this.directBossBar.setStyle(style);
            } else {
               try {
                  Method set = this.bossBarInstance.getClass().getMethod("setStyle", BarStyle.class);
                  set.invoke(this.bossBarInstance, style);
               } catch (ReflectiveOperationException var3) {
               }
            }
         }
      }

      public void setVisible(boolean visible) {
         if (this.isSupported()) {
            if (this.directBossBar != null) {
               this.directBossBar.setVisible(visible);
            } else {
               try {
                  Method set = this.bossBarInstance.getClass().getMethod("setVisible", boolean.class);
                  set.invoke(this.bossBarInstance, visible);
               } catch (ReflectiveOperationException var3) {
               }
            }
         }
      }

      public Collection<Player> getPlayers() {
         if (!this.isSupported()) {
            return Collections.emptyList();
         } else if (this.directBossBar != null) {
            return this.directBossBar.getPlayers();
         } else {
            try {
               Method get = this.bossBarInstance.getClass().getMethod("getPlayers");
               Object out = get.invoke(this.bossBarInstance);
               return (Collection<Player>)out;
            } catch (ClassCastException | ReflectiveOperationException var3) {
               return Collections.emptyList();
            }
         }
      }
   }
}
