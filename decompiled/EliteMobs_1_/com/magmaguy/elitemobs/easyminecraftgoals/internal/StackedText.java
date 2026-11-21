package com.magmaguy.elitemobs.easyminecraftgoals.internal;

import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.easyminecraftgoals.thirdparty.BedrockChecker;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class StackedText {
   private static final double TEXT_HEIGHT = 0.225;
   private final Map<UUID, StackedText.Viewer> viewers = new HashMap<>();
   private List<String> lines = List.of();
   private float scale = 1.0F;
   private double gap = 0.1;
   private boolean visible = true;
   private boolean removed;
   private Location anchor;

   public synchronized void setLines(List<String> text) {
      List<String> normalized = new ArrayList<>();

      for (String block : text) {
         String carry = "";

         for (String line : Objects.requireNonNullElse(block, "").split("\\R", -1)) {
            String formatted = carry + line;
            normalized.add(formatted);
            carry = ChatColor.getLastColors(formatted);
         }
      }

      this.lines = List.copyOf(normalized);
   }

   public synchronized void setScale(float scale) {
      if (Float.isFinite(scale) && !(scale <= 0.0F)) {
         this.scale = scale;
      } else {
         throw new IllegalArgumentException("Text scale must be positive and finite");
      }
   }

   public synchronized void setLineGap(double gap) {
      if (Double.isFinite(gap) && !(gap < 0.0)) {
         this.gap = gap;
      } else {
         throw new IllegalArgumentException("Text line gap must be nonnegative and finite");
      }
   }

   public synchronized void setVisible(boolean visible) {
      this.visible = visible;
      if (!visible) {
         this.clearViewers();
      }
   }

   public synchronized void move(Location anchor) {
      if (!this.removed && anchor != null && anchor.getWorld() != null) {
         if (this.anchor != null && !this.anchor.getWorld().equals(anchor.getWorld())) {
            this.clearViewers();
         }

         this.anchor = anchor.clone();

         for (StackedText.Viewer viewer : this.viewers.values()) {
            this.moveRows(viewer);
         }
      }
   }

   public synchronized void syncViewers(Collection<? extends Player> candidates) {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("Text viewers must be reconciled on the primary thread");
      } else if (!this.removed
         && this.visible
         && this.anchor != null
         && !this.lines.isEmpty()
         && !this.lines.stream().allMatch(line -> Objects.requireNonNullElse(ChatColor.stripColor(line), "").isBlank())) {
         Set<UUID> wanted = new HashSet<>();

         for (Player player : candidates) {
            if (player != null && player.isOnline() && this.anchor.getWorld().equals(player.getWorld())) {
               UUID id = player.getUniqueId();
               wanted.add(id);
               boolean bedrock = BedrockChecker.isBedrock(player);
               float effectiveScale = bedrock ? 1.0F : this.scale;
               StackedText.Viewer viewer = this.viewers.get(id);
               if (viewer != null && (viewer.bedrock != bedrock || viewer.scale != effectiveScale || viewer.rows.size() != this.lines.size())) {
                  viewer.remove();
                  this.viewers.remove(id);
                  viewer = null;
               }

               if (viewer == null) {
                  viewer = new StackedText.Viewer(bedrock, effectiveScale);

                  try {
                     for (int i = 0; i < this.lines.size(); i++) {
                        FakeText row = NMSManager.getAdapter()
                           .createFakeText(
                              this.rowLocation(viewer, i),
                              new FakeTextSettings()
                                 .setText(this.lines.get(i))
                                 .setScale(effectiveScale)
                                 .setBackgroundArgb(0)
                                 .setShadow(false)
                                 .setLineWidth(Integer.MAX_VALUE)
                           );
                        viewer.rows.add(row);
                        row.displayTo(player);
                     }

                     this.viewers.put(id, viewer);
                  } catch (Error | RuntimeException var11) {
                     viewer.remove();
                     throw var11;
                  }
               } else {
                  for (int i = 0; i < this.lines.size(); i++) {
                     if (!Objects.equals(viewer.rows.get(i).getText(), this.lines.get(i))) {
                        viewer.rows.get(i).setText(this.lines.get(i));
                     }
                  }
               }

               this.moveRows(viewer);
            }
         }

         Iterator<Entry<UUID, StackedText.Viewer>> iterator = this.viewers.entrySet().iterator();

         while (iterator.hasNext()) {
            Entry<UUID, StackedText.Viewer> entry = iterator.next();
            if (!wanted.contains(entry.getKey())) {
               entry.getValue().remove();
               iterator.remove();
            }
         }
      } else {
         this.clearViewers();
      }
   }

   public synchronized void hideFrom(UUID id) {
      StackedText.Viewer viewer = this.viewers.remove(id);
      if (viewer != null) {
         viewer.remove();
      }
   }

   public synchronized void remove() {
      this.removed = true;
      this.clearViewers();
      this.anchor = null;
   }

   private Location rowLocation(StackedText.Viewer viewer, int index) {
      double up = (this.lines.size() - 1 - index) * (0.225 + this.gap) * viewer.scale;
      return this.anchor.clone().add(0.0, up - (viewer.bedrock ? 0.5 : 0.0), 0.0);
   }

   private void moveRows(StackedText.Viewer viewer) {
      if (viewer.rows.size() == this.lines.size()) {
         for (int i = 0; i < viewer.rows.size(); i++) {
            Location target = this.rowLocation(viewer, i);
            Location previous = viewer.rows.get(i).getLocation();
            if (!previous.getWorld().equals(target.getWorld()) || previous.distanceSquared(target) > 1.0E-8) {
               viewer.rows.get(i).teleport(target);
            }
         }
      }
   }

   private void clearViewers() {
      this.viewers.values().forEach(StackedText.Viewer::remove);
      this.viewers.clear();
   }

   private static final class Viewer {
      private final boolean bedrock;
      private final float scale;
      private final List<FakeText> rows = new ArrayList<>();

      private Viewer(boolean bedrock, float scale) {
         this.bedrock = bedrock;
         this.scale = scale;
      }

      private void remove() {
         this.rows.forEach(FakeText::remove);
      }
   }
}
