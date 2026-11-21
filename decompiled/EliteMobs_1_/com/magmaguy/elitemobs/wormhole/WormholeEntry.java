package com.magmaguy.elitemobs.wormhole;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.WormholesConfig;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.DrawLine;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.ChunkLocationChecker;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.PersistentObject;
import com.magmaguy.elitemobs.mobconstructor.PersistentObjectHandler;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModel;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.freeminecraftmodels.customentity.StaticEntity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import org.bukkit.Color;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class WormholeEntry implements PersistentObject {
   private static final Set<WormholeEntry> wormholeEntries = ConcurrentHashMap.newKeySet();
   private static final float LINE_WIDTH = 0.04F;
   private static final Material[] CONCRETE_COLORS_BY_RGB = new Material[]{
      Material.WHITE_CONCRETE,
      Material.ORANGE_CONCRETE,
      Material.MAGENTA_CONCRETE,
      Material.LIGHT_BLUE_CONCRETE,
      Material.YELLOW_CONCRETE,
      Material.LIME_CONCRETE,
      Material.PINK_CONCRETE,
      Material.GRAY_CONCRETE,
      Material.LIGHT_GRAY_CONCRETE,
      Material.CYAN_CONCRETE,
      Material.PURPLE_CONCRETE,
      Material.BLUE_CONCRETE,
      Material.BROWN_CONCRETE,
      Material.GREEN_CONCRETE,
      Material.RED_CONCRETE,
      Material.BLACK_CONCRETE
   };
   private static final int[] CONCRETE_RGB_VALUES = new int[]{
      16777215, 16738816, 16711935, 6724095, 16776960, 8453888, 16738740, 8421504, 12632256, 65535, 8388736, 255, 9127187, 65280, 16711680, 0
   };
   private final Wormhole wormhole;
   private final int wormholeNumber;
   private final List<DrawLine.LineData> lineDataList = new ArrayList<>();
   private final boolean isInitializing = false;
   private Location location;
   private String locationString;
   private String armorStandText;
   private TextDisplay textDisplay = null;
   private StaticEntity staticModel = null;
   private BedrockWormholeMarker bedrockWormholeMarker = null;
   private String worldName;
   private String portalMissingMessage = null;
   private String opMessage = null;
   private boolean showDownloadHint = false;
   private PersistentObjectHandler persistentObjectHandler = null;
   private long[] cachedEdges = null;
   private Material concreteColor = null;
   private boolean linesInitialized = false;
   private int rotationIndex = 0;
   private static final long LINES_RECREATION_COOLDOWN_MS = 500L;
   private static final int TICKS_WITHOUT_LOS_BEFORE_CLEAR = 100;
   private volatile boolean isProcessingChunkEvent = false;
   private long lastLinesClearTime = 0L;
   private int particleTickCounter = 0;
   private int ticksWithoutLOS = 0;
   private final Set<UUID> playersViewingLines = new HashSet<>();

   public WormholeEntry(Wormhole wormhole, String locationString, int wormholeNumber) {
      this.wormhole = wormhole;
      this.wormholeNumber = wormholeNumber;
      this.locationString = locationString;
      if (locationString == null) {
         Logger.warn("Wormhole " + wormhole.getWormholeConfigFields().getFilename() + " is missing a wormhole location! Fix this!");
      } else {
         this.setLocationFromConfiguration();
         if (wormholeNumber == 1) {
            this.armorStandText = wormhole.getWormholeConfigFields().getLocation1Text();
         } else {
            this.armorStandText = wormhole.getWormholeConfigFields().getLocation2Text();
         }

         this.persistentObjectHandler = new PersistentObjectHandler(this);
         wormholeEntries.add(this);
      }
   }

   private String getCustomModelName() {
      return this.wormholeNumber == 1 ? this.wormhole.getWormholeConfigFields().getCustomModel1() : this.wormhole.getWormholeConfigFields().getCustomModel2();
   }

   private boolean shouldUseCustomModel() {
      String modelName = this.getCustomModelName();
      return modelName != null
         && !modelName.isEmpty()
         && CustomModel.customModelsEnabled()
         && CustomModel.getModelPlugin() == CustomModel.ModelPlugin.FREE_MINECRAFT_MODELS
         && CustomModel.modelExists(modelName);
   }

   private void initializeCustomModel() {
      String modelName = this.getCustomModelName();
      if (modelName != null && !modelName.isEmpty()) {
         if (this.location != null && this.location.getWorld() != null) {
            if (CustomModel.customModelsEnabled()) {
               if (CustomModel.getModelPlugin() == CustomModel.ModelPlugin.FREE_MINECRAFT_MODELS) {
                  if (CustomModel.modelExists(modelName)) {
                     if (this.staticModel != null) {
                        this.staticModel.remove();
                     }

                     this.staticModel = StaticEntity.create(modelName, this.location.clone());
                     if (this.staticModel != null) {
                        this.staticModel.setLeftClickCallback((player, entity) -> {});
                        this.staticModel.setRightClickCallback((player, entity) -> {});
                     }
                  }
               }
            }
         }
      }
   }

   private static long encodeEdge(int p1, int p2) {
      return p1 < p2 ? (long)p1 << 32 | p2 : (long)p2 << 32 | p1;
   }

   private Location getDungeonLocation() {
      EMPackage emPackage = EMPackage.getEmPackages().get(this.locationString);
      if (emPackage == null) {
         if (!EMPackage.getEmPackages().isEmpty()) {
            Logger.warn(
               "Dungeon "
                  + this.locationString
                  + " is not a valid dungeon packager name! Wormhole "
                  + this.wormhole.getWormholeConfigFields().getFilename()
                  + " will not lead anywhere."
            );
         }

         this.setPortalMissingMessage(WormholesConfig.getDefaultPortalMissingMessage());
         return null;
      } else {
         if (!emPackage.isDownloaded() || !emPackage.isInstalled()) {
            this.setPortalMissingMessage(
               WormholesConfig.getDungeonNotInstalledMessage().replace("$dungeonID", emPackage.getContentPackagesConfigFields().getName())
            );
            this.showDownloadHint = true;
         }

         Location teleportLocation = emPackage.getContentPackagesConfigFields().getTeleportLocation();
         if (teleportLocation == null) {
            return null;
         } else {
            this.worldName = emPackage.getContentPackagesConfigFields().getWorldName();
            return teleportLocation.clone();
         }
      }
   }

   private void setLocationFromConfiguration() {
      if (this.locationString.contains(",")) {
         this.worldName = ConfigurationLocation.worldName(this.locationString);
         this.location = ConfigurationLocation.serialize(this.locationString);
         if (this.location == null || this.location.getWorld() == null) {
            this.setPortalMissingMessage(WormholesConfig.getDefaultPortalMissingMessage());
            String filename = this.wormhole.getWormholeConfigFields().getFilename();
            String setupMsg = CommandMessagesConfig.getWormholeOpSetupMessage();
            if (setupMsg != null) {
               setupMsg = setupMsg.replace("$filename", filename).replace("$number", String.valueOf(this.wormholeNumber));
               if (filename.equals("adventurers_guild_wormhole")) {
                  String agMsg = CommandMessagesConfig.getWormholeAgSpawnRecommendation();
                  if (agMsg != null) {
                     setupMsg = setupMsg + "\n" + agMsg;
                  }
               }

               this.setOpMessage(setupMsg);
            }
         }
      } else {
         this.location = this.getDungeonLocation();
      }
   }

   public void onDungeonInstall() {
      this.location = this.getDungeonLocation();
      if (this.persistentObjectHandler != null) {
         this.persistentObjectHandler.remove();
      }

      this.persistentObjectHandler = new PersistentObjectHandler(this);
      this.chunkLoad();
   }

   public void onDungeonUninstall() {
      this.clearLines();
      if (this.staticModel != null) {
         this.staticModel.remove();
         this.staticModel = null;
      }

      this.location = null;
   }

   @Override
   public void chunkLoad() {
   }

   @Override
   public void chunkUnload() {
      this.isProcessingChunkEvent = true;

      try {
         this.clearLines();
         this.playersViewingLines.clear();
         if (this.textDisplay != null && this.textDisplay.isValid()) {
            this.textDisplay.remove();
            this.textDisplay = null;
         }

         if (this.staticModel != null) {
            this.staticModel.remove();
            this.staticModel = null;
         }

         this.removeBedrockMarker();
      } finally {
         this.isProcessingChunkEvent = false;
      }
   }

   @Override
   public void worldLoad(World world) {
      this.setLocationFromConfiguration();
      if (this.location != null && this.location.getWorld() != null && ChunkLocationChecker.chunkAtLocationIsLoaded(this.location)) {
         this.chunkLoad();
      }
   }

   @Override
   public void worldUnload() {
      if (this.location != null) {
         this.location.setWorld(null);
      }

      this.remove();
   }

   @Override
   public Location getPersistentLocation() {
      return this.location;
   }

   @Override
   public String getWorldName() {
      return this.worldName;
   }

   public void stop() {
      this.remove();
   }

   private void remove() {
      if (this.textDisplay != null && this.textDisplay.isValid()) {
         this.textDisplay.remove();
         this.textDisplay = null;
      }

      if (this.staticModel != null) {
         this.staticModel.remove();
         this.staticModel = null;
      }

      this.removeBedrockMarker();
      this.clearLines();
   }

   public void updateLocation(Player player) {
      Location replacement = player.getLocation().add(0.0, this.wormhole.getWormholeConfigFields().getSizeMultiplier(), 0.0);
      this.wormhole.getWormholeConfigFields().setWormholeEntryLocation(replacement, this.wormholeNumber);
      this.remove();
      if (this.persistentObjectHandler != null) {
         this.persistentObjectHandler.remove();
      }

      this.location = replacement;
      this.locationString = ConfigurationLocation.deserialize(replacement);
      this.worldName = replacement.getWorld().getName();
      this.portalMissingMessage = null;
      this.opMessage = null;
      this.showDownloadHint = false;
      this.persistentObjectHandler = new PersistentObjectHandler(this);
   }

   public void tick(List<Player> nearbyPlayers) {
      if (!this.isProcessingChunkEvent) {
         if (this.location != null && this.location.getWorld() != null && ChunkLocationChecker.chunkAtLocationIsLoaded(this.location)) {
            if (this.shouldUseCustomModel() && this.staticModel == null) {
               this.initializeCustomModel();
            }

            if (this.armorStandText != null && (this.textDisplay == null || !this.textDisplay.isValid())) {
               this.initializeTextDisplay();
            }

            if (this.staticModel != null) {
               if (this.linesInitialized || !this.lineDataList.isEmpty()) {
                  this.clearLines();
               }

               this.removeBedrockMarker();
            } else {
               this.tickBedrockMarker(nearbyPlayers);
               Set<UUID> currentNearbyPlayerUUIDs = new HashSet<>();

               for (Player player : nearbyPlayers) {
                  currentNearbyPlayerUUIDs.add(player.getUniqueId());
               }

               if (this.linesInitialized && !this.playersViewingLines.isEmpty()) {
                  Set<UUID> playersWhoLeft = new HashSet<>(this.playersViewingLines);
                  playersWhoLeft.removeAll(currentNearbyPlayerUUIDs);
                  if (!playersWhoLeft.isEmpty() && this.ticksWithoutLOS > 20) {
                     this.clearLines();
                  }
               }

               this.playersViewingLines.clear();
               this.playersViewingLines.addAll(currentNearbyPlayerUUIDs);
               if (!WormholesConfig.isNoParticlesMode() && this.wormhole.getWormholeConfigFields().getStyle() != Wormhole.WormholeStyle.NONE) {
                  if (this.anyPlayerHasLineOfSight(nearbyPlayers)) {
                     this.ticksWithoutLOS = 0;
                     this.updateVisualEffects();
                  } else {
                     this.ticksWithoutLOS++;
                     if (this.ticksWithoutLOS >= 100 && this.linesInitialized) {
                        this.clearLines();
                        this.ticksWithoutLOS = 0;
                     }
                  }
               }
            }
         }
      }
   }

   private boolean anyPlayerHasLineOfSight(List<Player> players) {
      if (!players.isEmpty() && this.location != null && this.location.getWorld() != null) {
         World world = this.location.getWorld();

         for (Player player : players) {
            Location playerEyes = player.getEyeLocation();
            Vector direction = this.location.toVector().subtract(playerEyes.toVector());
            double distance = direction.length();
            if (distance < 0.1) {
               return true;
            }

            direction.normalize();
            RayTraceResult result = world.rayTraceBlocks(playerEyes, direction, distance, FluidCollisionMode.NEVER, true);
            if (result == null) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public void initializeTextDisplay() {
      if (this.armorStandText != null) {
         if (this.location != null && this.location.getWorld() != null) {
            if (this.textDisplay != null && this.textDisplay.isValid()) {
               this.textDisplay.remove();
            }

            double sizeMultiplier = this.wormhole.getWormholeConfigFields().getSizeMultiplier();
            double yOffset = this.staticModel != null ? 2.0 * sizeMultiplier : 1.2 * sizeMultiplier;
            Location textLocation = this.location.clone().add(0.0, yOffset, 0.0);
            this.textDisplay = (TextDisplay)textLocation.getWorld().spawn(textLocation, TextDisplay.class, display -> {
               display.setText(ChatColorConverter.convert(this.armorStandText));
               display.setBillboard(Billboard.CENTER);
               display.setShadowed(false);
               display.setPersistent(false);
            });
            EntityTracker.registerVisualEffects(this.textDisplay);
         }
      }
   }

   private void updateVisualEffects() {
      List<List<Vector>> cachedRotations = this.wormhole.getCachedRotations();
      if (!cachedRotations.isEmpty()) {
         if (this.rotationIndex >= cachedRotations.size()) {
            this.rotationIndex = 0;
         }

         this.updateLines(cachedRotations.get(this.rotationIndex));
         this.particleTickCounter++;
         if (this.particleTickCounter >= 5) {
            this.spawnMinimalParticles(cachedRotations.get(this.rotationIndex));
            this.particleTickCounter = 0;
         }

         this.rotationIndex++;
      }
   }

   private void updateLines(List<Vector> currentFrame) {
      if (currentFrame.size() >= 2) {
         World world = this.location.getWorld();
         if (world != null) {
            boolean needsRecreation = !this.linesInitialized || this.cachedEdges == null || this.areLinesDeleted();
            if (needsRecreation) {
               if (System.currentTimeMillis() - this.lastLinesClearTime < 500L) {
                  if (!this.lineDataList.isEmpty()) {
                     this.updateExistingLines(currentFrame);
                  }

                  return;
               }

               List<DrawLine.LineData> oldLines = new ArrayList<>(this.lineDataList);
               this.lineDataList.clear();
               this.linesInitialized = false;
               this.concreteColor = this.getConcreteColorForWormhole();
               if (this.cachedEdges == null) {
                  this.cachedEdges = this.getEdgesForShape(this.wormhole.getCachedRotations().getFirst());
               }

               try {
                  for (long edge : this.cachedEdges) {
                     int p1 = (int)(edge >>> 32);
                     int p2 = (int)(edge & 4294967295L);
                     if (p1 < currentFrame.size() && p2 < currentFrame.size()) {
                        Vector v1 = currentFrame.get(p1);
                        Vector v2 = currentFrame.get(p2);
                        Location loc1 = this.location.clone().add(v1);
                        Location loc2 = this.location.clone().add(v2);
                        DrawLine.LineData line = DrawLine.drawLine(loc1, loc2, 0.04F, this.concreteColor, -1);
                        if (line != null) {
                           this.lineDataList.add(line);
                        }
                     }
                  }

                  this.linesInitialized = true;

                  for (DrawLine.LineData oldLine : oldLines) {
                     if (oldLine != null) {
                        oldLine.remove();
                     }
                  }

                  this.lastLinesClearTime = System.currentTimeMillis();
               } catch (Exception var17) {
                  for (DrawLine.LineData newLine : this.lineDataList) {
                     if (newLine != null) {
                        newLine.remove();
                     }
                  }

                  this.lineDataList.clear();
                  this.lineDataList.addAll(oldLines);
                  this.linesInitialized = !this.lineDataList.isEmpty();
                  this.lastLinesClearTime = System.currentTimeMillis();
                  return;
               }
            } else {
               int lineIndex = 0;

               for (long edgex : this.cachedEdges) {
                  int p1 = (int)(edgex >>> 32);
                  int p2 = (int)(edgex & 4294967295L);
                  if (p1 < currentFrame.size() && p2 < currentFrame.size()) {
                     if (lineIndex >= this.lineDataList.size()) {
                        break;
                     }

                     Vector v1 = currentFrame.get(p1);
                     Vector v2 = currentFrame.get(p2);
                     Location loc1 = this.location.clone().add(v1);
                     Location loc2 = this.location.clone().add(v2);
                     DrawLine.LineData line = this.lineDataList.get(lineIndex);
                     if (line != null) {
                        DrawLine.updateLine(line, loc1, loc2);
                     }

                     lineIndex++;
                  }
               }
            }
         }
      }
   }

   private boolean areLinesDeleted() {
      if (this.lineDataList.isEmpty()) {
         return true;
      } else if (this.location != null && this.location.getWorld() != null && ChunkLocationChecker.chunkAtLocationIsLoaded(this.location)) {
         for (DrawLine.LineData line : this.lineDataList) {
            if (line == null || !line.getDisplay().isValid()) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private void updateExistingLines(List<Vector> currentFrame) {
      if (this.cachedEdges != null) {
         int lineIndex = 0;

         for (long edge : this.cachedEdges) {
            int p1 = (int)(edge >>> 32);
            int p2 = (int)(edge & 4294967295L);
            if (p1 < currentFrame.size() && p2 < currentFrame.size()) {
               if (lineIndex >= this.lineDataList.size()) {
                  break;
               }

               Vector v1 = currentFrame.get(p1);
               Vector v2 = currentFrame.get(p2);
               Location loc1 = this.location.clone().add(v1);
               Location loc2 = this.location.clone().add(v2);
               DrawLine.LineData line = this.lineDataList.get(lineIndex);
               if (line != null && line.getDisplay().isValid()) {
                  DrawLine.updateLine(line, loc1, loc2);
               }

               lineIndex++;
            }
         }
      }
   }

   private void spawnMinimalParticles(List<Vector> currentFrame) {
      World world = this.location.getWorld();
      if (world != null) {
         int particleInterval = Math.max(1, currentFrame.size() / 8);

         for (int i = 0; i < currentFrame.size(); i += particleInterval) {
            Vector v = currentFrame.get(i);
            Location particleLoc = this.location.clone().add(v);
            world.spawnParticle(Particle.DUST, particleLoc, 1, 0.25, 0.25, 0.25, 0.0, new DustOptions(this.wormhole.getParticleColor(), 0.5F));
         }
      }
   }

   private long[] getEdgesForShape(List<Vector> allPoints) {
      Wormhole.WormholeStyle style = this.wormhole.getWormholeConfigFields().getStyle();
      double sizeMultiplier = this.wormhole.getWormholeConfigFields().getSizeMultiplier();
      switch (style) {
         case CUBE:
            return this.getCubeEdges(allPoints, sizeMultiplier);
         case CRYSTAL:
            return this.getCrystalEdges(allPoints, sizeMultiplier);
         case ICOSAHEDRON:
            return this.getIcosahedronEdges(allPoints, sizeMultiplier);
         default:
            return new long[0];
      }
   }

   private long[] getCubeEdges(List<Vector> allPoints, double sizeMultiplier) {
      Set<Long> edgeSet = new HashSet<>();
      double tolerance = 0.1 * sizeMultiplier;
      Integer topIdx = this.findVertexIndex(allPoints, new Vector(0, 1, 0).multiply(sizeMultiplier), tolerance);
      Integer bottomIdx = this.findVertexIndex(allPoints, new Vector(0, -1, 0).multiply(sizeMultiplier), tolerance);
      Integer frontIdx = this.findVertexIndex(allPoints, new Vector(1, 0, 0).multiply(sizeMultiplier), tolerance);
      Integer sideIdx = this.findVertexIndex(allPoints, new Vector(0, 0, 1).multiply(sizeMultiplier), tolerance);
      Integer backIdx = this.findVertexIndex(allPoints, new Vector(-1, 0, 0).multiply(sizeMultiplier), tolerance);
      Integer leftIdx = this.findVertexIndex(allPoints, new Vector(0, 0, -1).multiply(sizeMultiplier), tolerance);
      if (topIdx != null && frontIdx != null) {
         edgeSet.add(encodeEdge(topIdx, frontIdx));
      }

      if (bottomIdx != null && frontIdx != null) {
         edgeSet.add(encodeEdge(bottomIdx, frontIdx));
      }

      if (frontIdx != null && sideIdx != null) {
         edgeSet.add(encodeEdge(frontIdx, sideIdx));
      }

      if (topIdx != null && backIdx != null) {
         edgeSet.add(encodeEdge(topIdx, backIdx));
      }

      if (bottomIdx != null && backIdx != null) {
         edgeSet.add(encodeEdge(bottomIdx, backIdx));
      }

      if (backIdx != null && leftIdx != null) {
         edgeSet.add(encodeEdge(backIdx, leftIdx));
      }

      if (frontIdx != null && leftIdx != null) {
         edgeSet.add(encodeEdge(frontIdx, leftIdx));
      }

      if (topIdx != null && sideIdx != null) {
         edgeSet.add(encodeEdge(topIdx, sideIdx));
      }

      if (topIdx != null && leftIdx != null) {
         edgeSet.add(encodeEdge(topIdx, leftIdx));
      }

      if (bottomIdx != null && sideIdx != null) {
         edgeSet.add(encodeEdge(bottomIdx, sideIdx));
      }

      if (bottomIdx != null && leftIdx != null) {
         edgeSet.add(encodeEdge(bottomIdx, leftIdx));
      }

      if (frontIdx != null && backIdx != null) {
         edgeSet.add(encodeEdge(frontIdx, backIdx));
      }

      if (sideIdx != null && leftIdx != null) {
         edgeSet.add(encodeEdge(sideIdx, leftIdx));
      }

      return edgeSet.stream().mapToLong(Long::longValue).toArray();
   }

   private long[] getCrystalEdges(List<Vector> allPoints, double sizeMultiplier) {
      Set<Long> edgeSet = new HashSet<>();
      double tolerance = 0.1 * sizeMultiplier;
      Integer topIdx = this.findVertexIndex(allPoints, new Vector(0, 1, 0).multiply(sizeMultiplier), tolerance);
      Integer bottomIdx = this.findVertexIndex(allPoints, new Vector(0, -1, 0).multiply(sizeMultiplier), tolerance);
      Integer frontIdx = this.findVertexIndex(allPoints, new Vector(0.5, 0.0, 0.0).multiply(sizeMultiplier), tolerance);
      Integer sideIdx = this.findVertexIndex(allPoints, new Vector(0.0, 0.0, 0.5).multiply(sizeMultiplier), tolerance);
      if (topIdx != null && frontIdx != null) {
         edgeSet.add(encodeEdge(topIdx, frontIdx));
      }

      if (bottomIdx != null && frontIdx != null) {
         edgeSet.add(encodeEdge(bottomIdx, frontIdx));
      }

      if (frontIdx != null && sideIdx != null) {
         edgeSet.add(encodeEdge(frontIdx, sideIdx));
      }

      Integer backIdx = this.findVertexIndex(allPoints, new Vector(-0.5, 0.0, 0.0).multiply(sizeMultiplier), tolerance);
      Integer leftIdx = this.findVertexIndex(allPoints, new Vector(0.0, 0.0, -0.5).multiply(sizeMultiplier), tolerance);
      if (topIdx != null && backIdx != null) {
         edgeSet.add(encodeEdge(topIdx, backIdx));
      }

      if (bottomIdx != null && backIdx != null) {
         edgeSet.add(encodeEdge(bottomIdx, backIdx));
      }

      if (backIdx != null && leftIdx != null) {
         edgeSet.add(encodeEdge(backIdx, leftIdx));
      }

      if (frontIdx != null && leftIdx != null) {
         edgeSet.add(encodeEdge(frontIdx, leftIdx));
      }

      if (topIdx != null && sideIdx != null) {
         edgeSet.add(encodeEdge(topIdx, sideIdx));
      }

      if (topIdx != null && leftIdx != null) {
         edgeSet.add(encodeEdge(topIdx, leftIdx));
      }

      if (bottomIdx != null && sideIdx != null) {
         edgeSet.add(encodeEdge(bottomIdx, sideIdx));
      }

      if (bottomIdx != null && leftIdx != null) {
         edgeSet.add(encodeEdge(bottomIdx, leftIdx));
      }

      if (frontIdx != null && backIdx != null) {
         edgeSet.add(encodeEdge(frontIdx, backIdx));
      }

      if (sideIdx != null && leftIdx != null) {
         edgeSet.add(encodeEdge(sideIdx, leftIdx));
      }

      return edgeSet.stream().mapToLong(Long::longValue).toArray();
   }

   private long[] getIcosahedronEdges(List<Vector> allPoints, double sizeMultiplier) {
      Set<Long> edgeSet = new HashSet<>();
      double tolerance = 0.2 * sizeMultiplier;
      Integer topIdx = this.findVertexIndex(allPoints, new Vector(0, 1, 0).multiply(sizeMultiplier), tolerance);
      Integer bottomIdx = this.findVertexIndex(allPoints, new Vector(0, -1, 0).multiply(sizeMultiplier), tolerance);
      List<Integer> topPentagonIndices = new ArrayList<>();
      List<Integer> bottomPentagonIndices = new ArrayList<>();
      Vector topOffset = new Vector(1.0, 0.5, 0.0).multiply(sizeMultiplier);
      Vector bottomOffset = new Vector(1.0, -0.5, 0.0).multiply(sizeMultiplier);
      bottomOffset.rotateAroundY(Math.PI / 5);

      for (int i = 0; i < 5; i++) {
         Vector topVec = topOffset.clone().rotateAroundY((Math.PI * 2.0 / 5.0) * i);
         Vector bottomVec = bottomOffset.clone().rotateAroundY((Math.PI * 2.0 / 5.0) * i);
         Integer topPentIdx = this.findVertexIndex(allPoints, topVec, tolerance);
         Integer bottomPentIdx = this.findVertexIndex(allPoints, bottomVec, tolerance);
         if (topPentIdx != null) {
            topPentagonIndices.add(topPentIdx);
         }

         if (bottomPentIdx != null) {
            bottomPentagonIndices.add(bottomPentIdx);
         }
      }

      if (topIdx != null) {
         for (Integer pentIdx : topPentagonIndices) {
            edgeSet.add(encodeEdge(topIdx, pentIdx));
         }
      }

      if (bottomIdx != null) {
         for (Integer pentIdx : bottomPentagonIndices) {
            edgeSet.add(encodeEdge(bottomIdx, pentIdx));
         }
      }

      for (int i = 0; i < topPentagonIndices.size(); i++) {
         int next = (i + 1) % topPentagonIndices.size();
         edgeSet.add(encodeEdge(topPentagonIndices.get(i), topPentagonIndices.get(next)));
      }

      for (int i = 0; i < bottomPentagonIndices.size(); i++) {
         int next = (i + 1) % bottomPentagonIndices.size();
         edgeSet.add(encodeEdge(bottomPentagonIndices.get(i), bottomPentagonIndices.get(next)));
      }

      for (int i = 0; i < Math.min(topPentagonIndices.size(), bottomPentagonIndices.size()); i++) {
         edgeSet.add(encodeEdge(topPentagonIndices.get(i), bottomPentagonIndices.get(i)));
         if (i + 1 < topPentagonIndices.size() - 1) {
            edgeSet.add(encodeEdge(topPentagonIndices.get(i + 1), bottomPentagonIndices.get(i)));
         }
      }

      if (!topPentagonIndices.isEmpty() && !bottomPentagonIndices.isEmpty()) {
         edgeSet.add(encodeEdge(topPentagonIndices.get(0), bottomPentagonIndices.get(4 % bottomPentagonIndices.size())));
      }

      return edgeSet.stream().mapToLong(Long::longValue).toArray();
   }

   private Integer findVertexIndex(List<Vector> points, Vector target, double tolerance) {
      for (int i = 0; i < points.size(); i++) {
         if (points.get(i).distance(target) <= tolerance) {
            return i;
         }
      }

      return null;
   }

   private void clearLines() {
      for (DrawLine.LineData line : this.lineDataList) {
         if (line != null) {
            line.remove();
         }
      }

      this.lineDataList.clear();
      this.linesInitialized = false;
      this.playersViewingLines.clear();
      this.lastLinesClearTime = System.currentTimeMillis();
   }

   public void onNoNearbyPlayers() {
      this.removeBedrockMarker();
      if (this.linesInitialized || !this.lineDataList.isEmpty()) {
         this.clearLines();
      }
   }

   private void tickBedrockMarker(List<Player> nearbyPlayers) {
      if (!WormholesConfig.isReducedParticlesMode() && !WormholesConfig.isNoParticlesMode()) {
         this.removeBedrockMarker();
      } else {
         if (this.bedrockWormholeMarker == null) {
            this.bedrockWormholeMarker = new BedrockWormholeMarker();
         }

         this.bedrockWormholeMarker.tick(this.wormhole, this.location, nearbyPlayers);
      }
   }

   private void removeBedrockMarker() {
      if (this.bedrockWormholeMarker != null) {
         this.bedrockWormholeMarker.remove();
         this.bedrockWormholeMarker = null;
      }
   }

   private Material getConcreteColorForWormhole() {
      Color wormholeColor = this.wormhole.getParticleColor();
      int targetRGB = wormholeColor.getRed() << 16 | wormholeColor.getGreen() << 8 | wormholeColor.getBlue();
      int closestIndex = 0;
      double minDistance = Double.MAX_VALUE;

      for (int i = 0; i < CONCRETE_RGB_VALUES.length; i++) {
         int concreteRGB = CONCRETE_RGB_VALUES[i];
         double distance = this.colorDistance(targetRGB, concreteRGB);
         if (distance < minDistance) {
            minDistance = distance;
            closestIndex = i;
         }
      }

      return CONCRETE_COLORS_BY_RGB[closestIndex];
   }

   private double colorDistance(int rgb1, int rgb2) {
      int r1 = rgb1 >> 16 & 0xFF;
      int g1 = rgb1 >> 8 & 0xFF;
      int b1 = rgb1 & 0xFF;
      int r2 = rgb2 >> 16 & 0xFF;
      int g2 = rgb2 >> 8 & 0xFF;
      int b2 = rgb2 & 0xFF;
      int dr = r1 - r2;
      int dg = g1 - g2;
      int db = b1 - b2;
      return Math.sqrt(dr * dr + dg * dg + db * db);
   }

   @Generated
   public static Set<WormholeEntry> getWormholeEntries() {
      return wormholeEntries;
   }

   @Generated
   public Wormhole getWormhole() {
      return this.wormhole;
   }

   @Generated
   public Location getLocation() {
      return this.location;
   }

   @Generated
   public String getLocationString() {
      return this.locationString;
   }

   @Generated
   public String getArmorStandText() {
      return this.armorStandText;
   }

   @Generated
   public String getPortalMissingMessage() {
      return this.portalMissingMessage;
   }

   @Generated
   public void setPortalMissingMessage(String portalMissingMessage) {
      this.portalMissingMessage = portalMissingMessage;
   }

   @Generated
   public String getOpMessage() {
      return this.opMessage;
   }

   @Generated
   public void setOpMessage(String opMessage) {
      this.opMessage = opMessage;
   }

   @Generated
   public boolean isShowDownloadHint() {
      return this.showDownloadHint;
   }
}
