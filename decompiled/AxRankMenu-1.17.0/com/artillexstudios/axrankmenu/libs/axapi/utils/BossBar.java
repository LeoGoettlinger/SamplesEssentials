package com.artillexstudios.axrankmenu.libs.axapi.utils;

import com.artillexstudios.axrankmenu.libs.axapi.nms.wrapper.ServerPlayerWrapper;
import com.artillexstudios.axrankmenu.libs.axapi.packet.wrapper.PacketWrapper;
import com.artillexstudios.axrankmenu.libs.axapi.packet.wrapper.clientbound.ClientboundBossEventWrapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Validate;
import org.bukkit.entity.Player;

public final class BossBar {
   private final UUID uuid = UUID.randomUUID();
   private final Set<BossBar.Flag> flags = EnumSet.noneOf(BossBar.Flag.class);
   private final Set<Player> viewers = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
   private final ClientboundBossEventWrapper removePacket = new ClientboundBossEventWrapper(this.uuid, ClientboundBossEventWrapper.REMOVE_ACTION);
   private Component title;
   private BossBar.Color color;
   private BossBar.Style style;
   private float progress;
   private ClientboundBossEventWrapper addPacket;

   public BossBar(Component title, float progress, BossBar.Color color, BossBar.Style style, BossBar.Flag... flags) {
      Validate.inclusiveBetween(0.0, 1.0, progress);
      this.title = title;
      this.progress = progress;
      this.color = color;
      this.style = style;
      this.flags.addAll(Arrays.asList(flags));
      this.updatePacket();
   }

   public static BossBar create(Component title) {
      return new BossBar(title, 1.0F, BossBar.Color.PINK, BossBar.Style.PROGRESS);
   }

   public static BossBar create(Component title, float progress) {
      return new BossBar(title, progress, BossBar.Color.PINK, BossBar.Style.PROGRESS);
   }

   public static BossBar create(Component title, float progress, BossBar.Color color) {
      return new BossBar(title, progress, color, BossBar.Style.PROGRESS);
   }

   public static BossBar create(Component title, float progress, BossBar.Color color, BossBar.Style style) {
      return new BossBar(title, progress, color, style);
   }

   public static BossBar create(Component title, float progress, BossBar.Color color, BossBar.Style style, BossBar.Flag... flags) {
      return new BossBar(title, progress, color, style, flags);
   }

   public void show(Player player) {
      if (!this.viewers.contains(player)) {
         ServerPlayerWrapper wrapper = ServerPlayerWrapper.wrap(player);
         wrapper.sendPacket(this.addPacket);
         this.viewers.add(player);
      }
   }

   public void hide(Player player) {
      if (this.viewers.remove(player)) {
         ServerPlayerWrapper wrapper = ServerPlayerWrapper.wrap(player);
         wrapper.sendPacket(this.removePacket);
      }
   }

   public void style(BossBar.Style style) {
      this.style = style;
      this.updatePacket();
      this.broadcast(new ClientboundBossEventWrapper(this.uuid, new ClientboundBossEventWrapper.UpdateStyleAction(this.color, this.style)));
   }

   public void color(BossBar.Color color) {
      this.color = color;
      this.updatePacket();
      this.broadcast(new ClientboundBossEventWrapper(this.uuid, new ClientboundBossEventWrapper.UpdateStyleAction(this.color, this.style)));
   }

   public void addFlags(BossBar.Flag... flag) {
      this.flags.addAll(Arrays.asList(flag));
      this.updatePacket();
      this.broadcast(new ClientboundBossEventWrapper(this.uuid, new ClientboundBossEventWrapper.UpdateFlagsAction(this.flags)));
   }

   public void removeFlags(BossBar.Flag... flag) {
      Arrays.asList(flag).forEach(this.flags::remove);
      this.updatePacket();
      this.broadcast(new ClientboundBossEventWrapper(this.uuid, new ClientboundBossEventWrapper.UpdateFlagsAction(this.flags)));
   }

   public Set<BossBar.Flag> flags() {
      return this.flags;
   }

   public Component title() {
      return this.title;
   }

   public void title(Component title) {
      this.title = title;
      this.updatePacket();
      this.broadcast(new ClientboundBossEventWrapper(this.uuid, new ClientboundBossEventWrapper.UpdateNameAction(this.title)));
   }

   public float progress() {
      return this.progress;
   }

   public void progress(float progress) {
      this.progress = progress;
      this.updatePacket();
      this.broadcast(new ClientboundBossEventWrapper(this.uuid, new ClientboundBossEventWrapper.UpdateProgressAction(this.progress)));
   }

   public void remove() {
      this.broadcast(this.removePacket);
      this.viewers.clear();
   }

   private void updatePacket() {
      this.addPacket = new ClientboundBossEventWrapper(
         this.uuid, new ClientboundBossEventWrapper.AddAction(this.title, this.progress, this.color, this.style, this.flags)
      );
   }

   private void broadcast(PacketWrapper packetWrapper) {
      synchronized (this.viewers) {
         for (Player viewer : this.viewers) {
            ServerPlayerWrapper wrapper = ServerPlayerWrapper.wrap(viewer);
            wrapper.sendPacket(packetWrapper);
         }
      }
   }

   public static enum Color {
      PINK("pink"),
      BLUE("blue"),
      RED("red"),
      GREEN("green"),
      YELLOW("yellow"),
      PURPLE("purple"),
      WHITE("white");

      private final String internalName;

      private Color(String internalName) {
         this.internalName = internalName;
      }

      public static BossBar.Color parse(String color) {
         return valueOf(color.toUpperCase(Locale.ENGLISH));
      }

      public String getInternalName() {
         return this.internalName;
      }
   }

   public static enum Flag {
      DARKEN_SCREEN("darken_screen"),
      PLAY_BOSS_MUSIC("play_boss_music"),
      CREATE_WORLD_FOG("create_world_fog");

      private final String internalName;

      private Flag(final String internalName) {
         this.internalName = internalName;
      }

      public static BossBar.Flag parse(String flag) {
         return valueOf(flag.toUpperCase(Locale.ENGLISH));
      }

      public String getInternalName() {
         return this.internalName;
      }
   }

   public static enum Style {
      PROGRESS(Arrays.asList("progress", "solid"), "progress"),
      NOTCHED_6(Arrays.asList("notched_6", "segmented_6"), "notched_6"),
      NOTCHED_10(Arrays.asList("notched_10", "segmented_10"), "notched_10"),
      NOTCHED_12(Arrays.asList("notched_12", "segmented_12"), "notched_12"),
      NOTCHED_20(Arrays.asList("notched_20", "segmented_20"), "notched_20");

      private static final HashMap<List<String>, BossBar.Style> STYLES = new HashMap<>();
      private final List<String> styleNames;
      private final String internalName;

      private Style(List<String> styleNames, String internalName) {
         this.styleNames = styleNames;
         this.internalName = internalName;
      }

      public static BossBar.Style parse(String style) {
         for (Entry<List<String>, BossBar.Style> listStyleEntry : STYLES.entrySet()) {
            for (String name : listStyleEntry.getKey()) {
               if (name.equalsIgnoreCase(style)) {
                  return listStyleEntry.getValue();
               }
            }
         }

         return null;
      }

      public String getInternalName() {
         return this.internalName;
      }

      static {
         for (BossBar.Style value : values()) {
            STYLES.put(value.styleNames, value);
         }
      }
   }
}
