package com.artillexstudios.axtrade.libs.axapi.hologram.page;

import com.artillexstudios.axtrade.libs.axapi.events.PacketEntityInteractEvent;
import com.artillexstudios.axtrade.libs.axapi.hologram.Hologram;
import com.artillexstudios.axtrade.libs.axapi.hologram.HologramType;
import com.artillexstudios.axtrade.libs.axapi.packetentity.meta.EntityMeta;
import com.artillexstudios.axtrade.libs.axapi.placeholders.PlaceholderParameters;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class HologramPage<T, Z extends HologramType<T>> {
   private final PlaceholderParameters parameters = new PlaceholderParameters();
   private Consumer<PacketEntityInteractEvent> clickHandler;
   private Consumer<EntityMeta> metaHandler;
   private final Hologram hologram;
   private final boolean firstPage;
   private Location location;

   public HologramPage(Hologram hologram, boolean firstPage, Location location) {
      this.hologram = hologram;
      this.firstPage = firstPage;
      this.location = location;
   }

   public abstract void setContent(T content);

   public abstract T getContent();

   public abstract Z getType();

   public abstract boolean containsPlaceholders();

   public abstract boolean contentEquals(HologramPage<?, ?> other);

   public abstract void hide(Player player);

   public abstract void show(Player player);

   public abstract void teleport(Location location);

   public abstract void remove();

   public abstract void update();

   public abstract EntityMeta getEntityMeta();

   public void setClickHandler(Consumer<PacketEntityInteractEvent> clickHandler) {
      this.clickHandler = clickHandler;
   }

   public Consumer<PacketEntityInteractEvent> getClickHandler() {
      return this.clickHandler;
   }

   public void setEntityMetaHandler(Consumer<EntityMeta> metaHandler) {
      this.metaHandler = metaHandler;
   }

   public Consumer<EntityMeta> getEntityMetaHandler() {
      return this.metaHandler;
   }

   public abstract boolean spawn();

   public PlaceholderParameters getParameters() {
      return this.parameters;
   }

   public void setLocation(Location location) {
      this.location = location;
   }

   public boolean isFirstPage() {
      return this.firstPage;
   }

   public Location getLocation() {
      return this.location;
   }

   protected Hologram getHologram() {
      return this.hologram;
   }
}
