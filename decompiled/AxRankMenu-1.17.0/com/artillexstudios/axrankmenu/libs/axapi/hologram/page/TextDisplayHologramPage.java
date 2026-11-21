package com.artillexstudios.axrankmenu.libs.axapi.hologram.page;

import com.artillexstudios.axrankmenu.libs.axapi.hologram.Hologram;
import com.artillexstudios.axrankmenu.libs.axapi.hologram.HologramType;
import com.artillexstudios.axrankmenu.libs.axapi.hologram.HologramTypes;
import com.artillexstudios.axrankmenu.libs.axapi.hologram.Holograms;
import com.artillexstudios.axrankmenu.libs.axapi.nms.NMSHandlers;
import com.artillexstudios.axrankmenu.libs.axapi.packetentity.PacketEntity;
import com.artillexstudios.axrankmenu.libs.axapi.packetentity.meta.EntityMeta;
import com.artillexstudios.axrankmenu.libs.axapi.packetentity.meta.entity.TextDisplayMeta;
import com.artillexstudios.axrankmenu.libs.axapi.utils.ComponentSerializer;
import com.artillexstudios.axrankmenu.libs.axapi.utils.StringUtils;
import com.artillexstudios.axrankmenu.libs.axapi.utils.featureflags.FeatureFlags;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class TextDisplayHologramPage extends HologramPage<String, HologramType<String>> {
   private static final Pattern NEW_LINE = Pattern.compile("\n");
   private final PacketEntity textDisplay;
   private boolean containsPlaceholders;
   private String content;
   private boolean spawned = false;
   private LineData[] data;

   public TextDisplayHologramPage(Hologram hologram, boolean firstPage, Location location) {
      super(hologram, firstPage, location);
      this.textDisplay = NMSHandlers.getNmsHandler().createEntity(EntityType.TEXT_DISPLAY, this.getLocation());
   }

   public void setContent(String content) {
      this.content = content;
      this.containsPlaceholders = false;
      String[] split = NEW_LINE.split(content);
      this.data = new LineData[split.length];

      for (int i = 0; i < split.length; i++) {
         String line = split[i];
         boolean containsPlaceholders = false;
         Component formatted = StringUtils.format(line + (i == split.length - 1 ? "" : "\n"));
         String legacy = PlainTextComponentSerializer.plainText().serialize(formatted);

         for (Pattern pattern : FeatureFlags.PLACEHOLDER_PATTERNS.get()) {
            Matcher matcher = pattern.matcher(legacy);
            if (matcher.find()) {
               containsPlaceholders = true;
               this.containsPlaceholders = true;
               break;
            }
         }

         if (legacy.isBlank()) {
            formatted = Component.newline();
         }

         this.data[i] = new LineData(
            line, ComponentSerializer.INSTANCE.toVanilla((Component)(containsPlaceholders ? Component.empty() : formatted)), containsPlaceholders
         );
      }

      this.create();
   }

   private void create() {
      TextDisplayMeta meta = (TextDisplayMeta)this.textDisplay.meta();
      meta.component(StringUtils.format(this.content));
      if (this.getEntityMetaHandler() != null) {
         this.getEntityMetaHandler().accept(meta);
      }

      if (!this.isFirstPage()) {
         this.textDisplay.setVisibleByDefault(false);
      }

      this.textDisplay.onInteract(event -> {
         if (this.getClickHandler() != null) {
            this.getClickHandler().accept(event);
         }

         this.getHologram().changePage(event.getPlayer(), event.isAttack() ? Hologram.PageChangeDirection.BACK : Hologram.PageChangeDirection.FORWARD);
      });
   }

   public String getContent() {
      return this.content;
   }

   @Override
   public HologramType<String> getType() {
      return HologramTypes.TEXT;
   }

   @Override
   public boolean containsPlaceholders() {
      return this.containsPlaceholders;
   }

   @Override
   public boolean contentEquals(HologramPage<?, ?> other) {
      return other instanceof TextDisplayHologramPage textPage ? textPage.getContent().equals(this.getContent()) : false;
   }

   @Override
   public void hide(Player player) {
      this.textDisplay.hide(player);
   }

   @Override
   public void show(Player player) {
      this.textDisplay.show(player);
   }

   @Override
   public void teleport(Location location) {
      this.setLocation(location);
      if (this.textDisplay != null) {
         this.textDisplay.teleport(location);
      }
   }

   @Override
   public void remove() {
      if (this.textDisplay != null) {
         this.spawned = false;
         Holograms.remove(this.textDisplay.id());
         this.textDisplay.remove();
      }
   }

   @Override
   public void update() {
      if (this.textDisplay != null) {
         this.textDisplay.update();
      }
   }

   public LineData[] getData() {
      return this.data;
   }

   @Override
   public EntityMeta getEntityMeta() {
      return this.textDisplay.meta();
   }

   @Override
   public synchronized boolean spawn() {
      if (this.spawned) {
         return false;
      } else {
         Holograms.put(this.textDisplay.id(), this);
         this.textDisplay.spawn();
         this.spawned = true;
         return true;
      }
   }
}
