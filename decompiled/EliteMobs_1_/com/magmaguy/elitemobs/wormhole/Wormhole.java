package com.magmaguy.elitemobs.wormhole;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.Generated;
import org.bukkit.Color;
import org.bukkit.util.Vector;

public class Wormhole {
   private static final HashSet<Wormhole> wormholes = new HashSet<>();
   private final WormholeConfigFields wormholeConfigFields;
   private final WormholeEntry wormholeEntry1;
   private final WormholeEntry wormholeEntry2;
   private final Color particleColor;
   private List<List<Vector>> cachedRotations = new ArrayList<>();

   public Wormhole(WormholeConfigFields wormholeConfigFields) {
      this.wormholeConfigFields = wormholeConfigFields;
      this.particleColor = Color.fromRGB(wormholeConfigFields.getParticleColor());
      if (wormholeConfigFields.getStyle() != null && !wormholeConfigFields.getStyle().equals(Wormhole.WormholeStyle.NONE)) {
         this.cachedRotations = new ArrayList<>(new VisualEffects(wormholeConfigFields).getCachedRotations());
      }

      this.wormholeEntry1 = new WormholeEntry(this, this.getWormholeConfigFields().getLocation1(), 1);
      this.wormholeEntry2 = new WormholeEntry(this, this.getWormholeConfigFields().getLocation2(), 2);
      wormholes.add(this);
      WormholeManager.getInstance(false);
   }

   public static void shutdown() {
      WormholeManager.getInstance(true).shutdown();

      for (Wormhole wormhole : wormholes) {
         wormhole.stop();
      }

      wormholes.clear();
      WormholeEntry.getWormholeEntries().clear();
   }

   private void stop() {
      this.wormholeEntry1.stop();
      this.wormholeEntry2.stop();
   }

   public void onDungeonInstall(String dungeonFilename) {
      if (this.getWormholeEntry1() != null
         && this.getWormholeEntry1().getLocationString() != null
         && this.getWormholeEntry1().getLocationString().equals(dungeonFilename)) {
         this.getWormholeEntry1().onDungeonInstall();
      } else if (this.getWormholeEntry2() != null
         && this.getWormholeEntry2().getLocationString() != null
         && this.getWormholeEntry2().getLocationString().equals(dungeonFilename)) {
         this.getWormholeEntry2().onDungeonInstall();
      }
   }

   public void onDungeonUninstall(String dungeonFilename) {
      if (this.getWormholeEntry1().getWormhole() != null && this.getWormholeEntry1().getLocationString().equals(dungeonFilename)) {
         this.getWormholeEntry1().onDungeonUninstall();
      } else if (this.getWormholeEntry2().getWormhole() != null && this.getWormholeEntry2().getLocationString().equals(dungeonFilename)) {
         this.getWormholeEntry2().onDungeonUninstall();
      }
   }

   @Generated
   public static HashSet<Wormhole> getWormholes() {
      return wormholes;
   }

   @Generated
   public WormholeConfigFields getWormholeConfigFields() {
      return this.wormholeConfigFields;
   }

   @Generated
   public WormholeEntry getWormholeEntry1() {
      return this.wormholeEntry1;
   }

   @Generated
   public WormholeEntry getWormholeEntry2() {
      return this.wormholeEntry2;
   }

   @Generated
   public Color getParticleColor() {
      return this.particleColor;
   }

   @Generated
   public List<List<Vector>> getCachedRotations() {
      return this.cachedRotations;
   }

   public static enum WormholeStyle {
      NONE,
      CRYSTAL,
      ICOSAHEDRON,
      CUBE;
   }
}
