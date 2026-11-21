package github.nighter.smartspawner.commands.hologram;

import github.nighter.smartspawner.Scheduler;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.language.LanguageManager;
import github.nighter.smartspawner.language.format.ColorUtil;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.TextDisplay.TextAlignment;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public class SpawnerHologram {
   private final SmartSpawner plugin;
   private final LanguageManager languageManager;
   private final AtomicReference<TextDisplay> textDisplay = new AtomicReference<>(null);
   private final Location spawnerLocation;
   private int stackSize;
   private EntityType entityType;
   private Material spawnedItemMaterial;
   private String displayName;
   private long currentExp;
   private long maxExp;
   private int currentItems;
   private int maxSlots;
   private static final String HOLOGRAM_IDENTIFIER = "SmartSpawner-Holo";
   private final String uniqueIdentifier;
   private static final Vector3f SCALE = new Vector3f(1.0F, 1.0F, 1.0F);
   private static final Vector3f TRANSLATION = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final AxisAngle4f ROTATION = new AxisAngle4f(0.0F, 0.0F, 0.0F, 0.0F);
   private String cachedProcessedTemplate = null;
   private String cachedEntityName = null;
   private String cachedEntitySmallCaps = null;

   public SpawnerHologram(Location location) {
      this.plugin = SmartSpawner.getInstance();
      this.spawnerLocation = location;
      this.languageManager = this.plugin.getLanguageManager();
      this.uniqueIdentifier = this.generateUniqueIdentifier(location);
   }

   private String generateUniqueIdentifier(Location location) {
      return "SmartSpawner-Holo-" + location.getWorld().getName() + "-" + location.getBlockX() + "-" + location.getBlockY() + "-" + location.getBlockZ();
   }

   public void createHologram() {
      if (this.spawnerLocation != null && this.spawnerLocation.getWorld() != null) {
         this.cleanupExistingHologram();
         double offsetX = this.plugin.getConfig().getDouble("hologram.offset_x", 0.5);
         double offsetY = this.plugin.getConfig().getDouble("hologram.offset_y", 0.5);
         double offsetZ = this.plugin.getConfig().getDouble("hologram.offset_z", 0.5);
         Location holoLoc = this.spawnerLocation.clone().add(offsetX, offsetY, offsetZ);
         Scheduler.runLocationTask(holoLoc, () -> {
            try {
               TextDisplay display = (TextDisplay)this.spawnerLocation.getWorld().spawn(holoLoc, TextDisplay.class, td -> {
                  td.setBillboard(Billboard.CENTER);
                  String alignmentStr = this.plugin.getConfig().getString("hologram.alignment", "CENTER");

                  TextAlignment alignment;
                  try {
                     alignment = TextAlignment.valueOf(alignmentStr.toUpperCase());
                  } catch (IllegalArgumentException var5x) {
                     alignment = TextAlignment.CENTER;
                     this.plugin.getLogger().warning("Invalid hologram alignment in config: " + alignmentStr + ". Using CENTER as default.");
                  }

                  td.setAlignment(alignment);
                  td.setViewRange(16.0F);
                  td.setShadowed(this.plugin.getConfig().getBoolean("hologram.shadowed_text", true));
                  td.setDefaultBackground(false);
                  td.setTransformation(new Transformation(TRANSLATION, ROTATION, SCALE, ROTATION));
                  td.setSeeThrough(this.plugin.getConfig().getBoolean("hologram.see_through", false));
                  boolean transparentBg = this.plugin.getConfig().getBoolean("hologram.transparent_background", false);
                  if (transparentBg) {
                     td.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
                  }

                  td.setCustomName(this.uniqueIdentifier);
                  td.setCustomNameVisible(false);
                  td.setPersistent(false);
               });
               this.textDisplay.set(display);
               this.updateText();
            } catch (Exception var3x) {
               this.plugin.getLogger().severe("Error creating hologram: " + var3x.getMessage());
               var3x.printStackTrace();
            }
         });
      }
   }

   private String getProcessedTemplate() {
      if (this.cachedProcessedTemplate == null) {
         this.cachedProcessedTemplate = ColorUtil.translateHexColorCodes(this.languageManager.getHologramText());
      }

      return this.cachedProcessedTemplate;
   }

   public void invalidateTemplateCache() {
      this.cachedProcessedTemplate = null;
   }

   private String computeText() {
      if (this.cachedEntityName == null || !this.cachedEntityName.equals(this.displayName)) {
         this.cachedEntityName = this.displayName != null ? this.displayName : this.languageManager.getFormattedMobName(this.entityType);
         this.cachedEntitySmallCaps = this.languageManager.getSmallCaps(this.cachedEntityName);
      }

      double pctStorage = this.maxSlots > 0 ? (double)this.currentItems / this.maxSlots * 100.0 : 0.0;
      double pctExp = this.maxExp > 0L ? (double)this.currentExp / this.maxExp * 100.0 : 0.0;
      return this.getProcessedTemplate()
         .replace("{entity}", this.cachedEntityName)
         .replace("{ᴇɴᴛɪᴛʏ}", this.cachedEntitySmallCaps)
         .replace("{stack_size}", String.valueOf(this.stackSize))
         .replace("{current_exp}", this.languageManager.formatNumber(this.currentExp))
         .replace("{max_exp}", this.languageManager.formatNumber(this.maxExp))
         .replace("{used_slots}", this.languageManager.formatNumber(this.currentItems))
         .replace("{max_slots}", this.languageManager.formatNumber(this.maxSlots))
         .replace("{percent_storage_decimal}", formatOneDecimal(pctStorage))
         .replace("{percent_storage_rounded}", String.valueOf((int)Math.round(pctStorage)))
         .replace("{percent_exp_decimal}", formatOneDecimal(pctExp))
         .replace("{percent_exp_rounded}", String.valueOf((int)Math.round(pctExp)));
   }

   private static String formatOneDecimal(double value) {
      long scaled = Math.round(value * 10.0);
      return scaled / 10L + "." + scaled % 10L;
   }

   public void updateText() {
      TextDisplay display = this.textDisplay.get();
      if (display != null && this.entityType != null) {
         String finalText = this.computeText();
         Scheduler.runEntityTask(display, () -> {
            if (display.isValid()) {
               display.setText(finalText);
            }
         });
      }
   }

   public void updateData(
      int stackSize, EntityType entityType, Material spawnedItemMaterial, String displayName, long currentExp, long maxExp, int currentItems, int maxSlots
   ) {
      TextDisplay display = this.textDisplay.get();
      if (display == null
         || this.stackSize != stackSize
         || this.entityType != entityType
         || this.spawnedItemMaterial != spawnedItemMaterial
         || !Objects.equals(this.displayName, displayName)
         || this.currentExp != currentExp
         || this.maxExp != maxExp
         || this.currentItems != currentItems
         || this.maxSlots != maxSlots) {
         this.stackSize = stackSize;
         this.entityType = entityType;
         this.spawnedItemMaterial = spawnedItemMaterial;
         this.displayName = displayName;
         this.currentExp = currentExp;
         this.maxExp = maxExp;
         this.currentItems = currentItems;
         this.maxSlots = maxSlots;
         if (display == null) {
            this.createHologram();
         } else {
            String finalText = this.computeText();
            Scheduler.runEntityTask(display, () -> {
               if (!display.isValid()) {
                  this.textDisplay.set(null);
                  this.createHologram();
               } else {
                  display.setText(finalText);
               }
            });
         }
      }
   }

   public void remove() {
      TextDisplay display = this.textDisplay.get();
      if (display != null) {
         Scheduler.runEntityTask(display, () -> {
            if (display.isValid()) {
               display.remove();
            }
         });
         this.textDisplay.set(null);
      }

      this.cleanupExistingHologram();
   }

   public void cleanupExistingHologram() {
      if (this.spawnerLocation != null && this.spawnerLocation.getWorld() != null) {
         TextDisplay display = this.textDisplay.get();
         if (display != null) {
            Scheduler.runEntityTask(display, () -> {
               if (display.isValid()) {
                  display.remove();
               }
            });
            this.textDisplay.set(null);
         }

         Scheduler.runLocationTask(
            this.spawnerLocation,
            () -> {
               double searchRadius = 2.0;
               this.spawnerLocation
                  .getWorld()
                  .getNearbyEntities(this.spawnerLocation, searchRadius, searchRadius, searchRadius)
                  .stream()
                  .filter(entity -> entity instanceof TextDisplay && entity.getCustomName() != null)
                  .filter(entity -> entity.getCustomName().equals(this.uniqueIdentifier))
                  .forEach(entity -> Scheduler.runEntityTask(entity, entity::remove));
            }
         );
      }
   }
}
