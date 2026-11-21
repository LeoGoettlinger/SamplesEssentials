package com.artillexstudios.axafkzone.libs.axapi.hologram;

import com.artillexstudios.axafkzone.libs.axapi.collections.ThreadSafeList;
import com.artillexstudios.axafkzone.libs.axapi.events.PacketEntityInteractEvent;
import com.artillexstudios.axafkzone.libs.axapi.hologram.page.HologramPage;
import com.artillexstudios.axafkzone.libs.axapi.utils.UncheckedUtils;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Hologram {
   private final WeakHashMap<Player, Integer> playerPages = new WeakHashMap<>();
   private final ThreadSafeList<HologramPage<?, ?>> pages = new ThreadSafeList<>();
   private Consumer<PacketEntityInteractEvent> clickHandler;
   private Location location;

   public Hologram(Location location) {
      this.location = location;
   }

   public void hide(Player player) {
      for (int i = 0; i < this.pages.size(); i++) {
         HologramPage<?, ?> page = this.pages.get(i);
         page.hide(player);
      }
   }

   public void show(Player player) {
      HologramPage<?, ?> page = this.pages.getFirst();
      page.show(player);
   }

   public void teleport(Location location) {
      this.location = location;

      for (int i = 0; i < this.pages.size(); i++) {
         HologramPage<?, ?> page = this.pages.get(i);
         page.teleport(location);
      }
   }

   public void remove() {
      for (int i = 0; i < this.pages.size(); i++) {
         HologramPage<?, ?> page = this.pages.get(i);
         page.remove();
      }
   }

   public <T> void spawnWithContent(HologramType<T> type, T content) {
      HologramPage<T, HologramType<T>> page = this.createPage(type);
      page.setContent(content);
      page.spawn();
   }

   public <T> void setContent(int pageIndex, T content) {
      HologramPage<T, ?> page = (HologramPage<T, ?>)this.pages.get(pageIndex);
      page.setContent(content);
   }

   public <T, Z extends HologramType<T>> HologramPage<T, Z> createPage(Z type) {
      HologramPage<T, Z> page = UncheckedUtils.unsafeCast(type.create(this, this.pages.isEmpty(), this.location));
      this.addPage(page);
      return page;
   }

   public void addPage(HologramPage<?, ?> page) {
      this.pages.add(page);
   }

   public void removePage(HologramPage<?, ?> page) {
      this.pages.remove(page);
   }

   public HologramPage<?, ?> page(int pageIndex) {
      return this.pages.get(pageIndex);
   }

   public void changePage(Player player, Hologram.PageChangeDirection direction) {
      if (!this.pages.isEmpty() && this.pages.size() != 1) {
         Integer page = this.playerPages.get(player);
         if (direction == Hologram.PageChangeDirection.BACK) {
            int current = page == null ? 0 : page;
            if (current - 1 < 0) {
               return;
            }

            HologramPage<?, ?> previousPage = this.pages.get(current);
            previousPage.hide(player);
            HologramPage<?, ?> newPage = this.pages.get(current - 1);
            newPage.show(player);
            this.playerPages.put(player, current - 1);
         } else {
            int current = page == null ? 0 : page;
            if (current + 1 >= this.pages.size()) {
               return;
            }

            HologramPage<?, ?> previousPage = this.pages.get(current);
            previousPage.hide(player);
            HologramPage<?, ?> newPage = this.pages.get(current + 1);
            newPage.show(player);
            this.playerPages.put(player, current + 1);
         }
      }
   }

   public void event(Consumer<PacketEntityInteractEvent> clickHandler) {
      this.clickHandler = clickHandler;

      for (int i = 0; i < this.pages.size(); i++) {
         HologramPage<?, ?> page = this.pages.get(i);
         if (page.getClickHandler() == null) {
            page.setClickHandler(this.clickHandler);
         }
      }
   }

   public Consumer<PacketEntityInteractEvent> event() {
      return this.clickHandler;
   }

   public Location location() {
      return this.location.clone();
   }

   public static enum PageChangeDirection {
      BACK,
      FORWARD;
   }
}
