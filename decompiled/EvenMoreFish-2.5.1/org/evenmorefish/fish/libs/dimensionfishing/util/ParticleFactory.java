package org.evenmorefish.fish.libs.dimensionfishing.util;

import com.destroystokyo.paper.ParticleBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Vibration;
import org.bukkit.Particle.DustTransition;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.libs.dimensionfishing.DimensionFishing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParticleFactory {
   private final List<ParticleBuilder> particles = new ArrayList<>();

   public ParticleFactory() {
   }

   public ParticleFactory(@NotNull List<Map<?, ?>> mapList) {
      this.loadParticles(mapList);
   }

   private void loadParticles(List<Map<?, ?>> mapList) {
      this.particles.clear();
      mapList.forEach(map -> {
         Particle particle = this.fetchParticle(map.get("particle"));
         if (particle == null) {
            DimensionFishing.getInstance().getLogger().warning("Particle was not configured properly.");
         } else if (this.isInvalidParticleData(particle)) {
            DimensionFishing.getInstance().getLogger().warning("Particle " + particle + " cannot be used in DimensionFishing.");
         } else {
            ParticleBuilder builder = particle.builder();
            this.applyAmount(map.get("amount"), builder);
            this.applyColor(map.get("color"), builder);
            this.applyColorTransition(map.get("color-transition"), builder);
            this.applyExtra(map.get("extra"), builder);
            this.applyDataType(map.get("data"), builder);
            this.particles.add(builder);
         }
      });
   }

   public void show(@NotNull Location location) {
      Iterator<ParticleBuilder> iterator = this.particles.iterator();

      while (iterator.hasNext()) {
         ParticleBuilder particle = iterator.next();

         try {
            particle.clone().location(location).spawn();
         } catch (IllegalArgumentException var5) {
            DimensionFishing.getInstance().getLogger().warning("Failed to spawn particle. It could be missing required data. It will no longer be spawned.");
            iterator.remove();
         }
      }
   }

   @Nullable
   private Particle fetchParticle(@Nullable Object name) {
      if (name == null) {
         return null;
      } else {
         try {
            return Particle.valueOf(name.toString().toUpperCase());
         } catch (IllegalArgumentException var3) {
            DimensionFishing.getInstance().getLogger().warning(name + " is not a valid particle.");
            return null;
         }
      }
   }

   private void applyColor(@Nullable Object color, @NotNull ParticleBuilder builder) {
      if (color != null) {
         Color finalColor = this.fetchColorFromHex(color.toString());
         if (finalColor != null) {
            try {
               builder.color(finalColor);
            } catch (IllegalStateException var5) {
               DimensionFishing.getInstance().getLogger().warning("Color is not supported on this particle.");
            }
         }
      }
   }

   private void applyColorTransition(@Nullable Object transition, @NotNull ParticleBuilder builder) {
      if (transition != null) {
         if (builder.particle().getDataType() != DustTransition.class) {
            DimensionFishing.getInstance().getLogger().warning("Color Transition is not supported on this particle.");
         } else {
            String string = transition.toString();
            String[] split = string.split(",");
            if (split.length != 3) {
               DimensionFishing.getInstance().getLogger().warning(string + " is not a valid transition.");
            } else {
               Color from = this.fetchColorFromHex(split[0]);
               Color to = this.fetchColorFromHex(split[1]);
               if (from != null && to != null) {
                  try {
                     float size = Float.parseFloat(split[2]);
                     builder.colorTransition(from, to, size);
                  } catch (NumberFormatException var8) {
                     DimensionFishing.getInstance().getLogger().warning(split[2] + " is not a valid float value.");
                  }
               }
            }
         }
      }
   }

   private void applyAmount(@Nullable Object amount, @NotNull ParticleBuilder builder) {
      if (amount == null) {
         builder.count(1);
      } else {
         try {
            builder.count(Integer.parseInt(amount.toString()));
         } catch (NumberFormatException var4) {
            DimensionFishing.getInstance().getLogger().warning(amount + " is not a valid amount.");
         }
      }
   }

   private void applyExtra(@Nullable Object extra, @NotNull ParticleBuilder builder) {
      if (extra != null) {
         try {
            builder.extra(Double.parseDouble(extra.toString()));
         } catch (NumberFormatException var4) {
            DimensionFishing.getInstance().getLogger().warning(extra + " is not a valid double value.");
         }
      }
   }

   @Nullable
   private Color fetchColorFromHex(@NotNull String hex) {
      try {
         java.awt.Color awtColor = java.awt.Color.decode(hex);
         int r = awtColor.getRed();
         int g = awtColor.getGreen();
         int b = awtColor.getBlue();
         return Color.fromRGB(r, g, b);
      } catch (NumberFormatException var6) {
         DimensionFishing.getInstance().getLogger().warning(hex + " is not a valid color.");
         return null;
      }
   }

   private void applyDataType(@Nullable Object object, @NotNull ParticleBuilder builder) {
      if (object != null) {
         String string = object.toString();
         Class<?> clazz = builder.particle().getDataType();

         try {
            if (clazz == Float.class) {
               builder.data(Float.parseFloat(string));
            } else if (clazz == Integer.class) {
               builder.data(Integer.parseInt(string));
            } else if (clazz == BlockData.class) {
               Material material = Material.valueOf(string.toUpperCase());
               if (material.isBlock()) {
                  builder.data(material.createBlockData());
               }
            } else if (clazz == ItemStack.class) {
               Material material = Material.valueOf(string.toUpperCase());
               if (material.isItem()) {
                  builder.data(ItemStack.of(material));
               }
            }
         } catch (IllegalArgumentException var6) {
            DimensionFishing.getInstance().getLogger().log(Level.WARNING, string + " is not valid particle data.", (Throwable)var6);
         }
      }
   }

   private boolean isInvalidParticleData(@NotNull Particle particle) {
      Class<?> clazz = particle.getDataType();
      return clazz == Vibration.class || clazz.getName().equals("org.bukkit.Particle.Trail");
   }
}
