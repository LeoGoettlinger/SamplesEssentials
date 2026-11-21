package su.nightexpress.nightcore.util.wrapper;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.Particle.Spell;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.Version;

public class UniParticle implements Writeable {
   private final Particle particle;
   private Object data;

   public UniParticle(@Nullable Particle particle, @Nullable Object data) {
      this.particle = particle;
      this.data = data;
   }

   @NonNull
   public static UniParticle of(@Nullable Particle particle) {
      return of(particle, null);
   }

   @NonNull
   public static UniParticle of(@Nullable Particle particle, @Nullable Object data) {
      return new UniParticle(particle, data);
   }

   @NonNull
   public static UniParticle itemCrack(@NonNull ItemStack item) {
      return new UniParticle(Particle.ITEM, new ItemStack(item));
   }

   @NonNull
   public static UniParticle itemCrack(@NonNull Material material) {
      return new UniParticle(Particle.ITEM, new ItemStack(material));
   }

   @NonNull
   public static UniParticle blockCrack(@NonNull Material material) {
      return new UniParticle(Particle.BLOCK, material.createBlockData());
   }

   @NonNull
   public static UniParticle blockDust(@NonNull Material material) {
      return blockCrack(material);
   }

   @NonNull
   public static UniParticle blockMarker(@NonNull Material material) {
      return new UniParticle(Particle.BLOCK_MARKER, material.createBlockData());
   }

   @NonNull
   public static UniParticle fallingDust(@NonNull Material material) {
      return new UniParticle(Particle.FALLING_DUST, material.createBlockData());
   }

   @NonNull
   public static UniParticle redstone(@NonNull Color color, float size) {
      return new UniParticle(Particle.DUST, new DustOptions(color, size));
   }

   @NonNull
   public static UniParticle read(@NonNull FileConfig config, @NonNull String path) {
      String name = config.getString(path + ".Name", "");
      Particle particle = (Particle)StringUtil.getEnum(name, Particle.class).orElse(null);
      if (particle == null) {
         return of(null);
      } else {
         Class<?> dataType = particle.getDataType();
         Object data = null;
         if (dataType == BlockData.class) {
            Material material = Material.getMaterial(config.getString(path + ".Material", ""));
            data = material != null ? material.createBlockData() : Material.STONE.createBlockData();
         } else if (dataType == DustOptions.class) {
            Color color = StringUtil.getColor(config.getString(path + ".Color", ""));
            double size = config.getDouble(path + ".Size", 1.0);
            data = new DustOptions(color, (float)size);
         } else if (Version.isAtLeast(Version.MC_1_21_10) && dataType == Spell.class) {
            Color color = StringUtil.getColor(config.getString(path + ".Color", ""));
            float power = (float)config.getDouble(path + ".Power", 1.0);
            data = new Spell(color, power);
         } else if (dataType == Color.class) {
            data = StringUtil.getColor(config.getString(path + ".Color", ""));
         } else if (dataType == DustTransition.class) {
            Color colorStart = StringUtil.getColor(config.getString(path + ".Color_From", ""));
            Color colorEnd = StringUtil.getColor(config.getString(path + ".Color_To", ""));
            double size = config.getDouble(path + ".Size", 1.0);
            data = new DustTransition(colorStart, colorEnd, (float)size);
         } else if (dataType == ItemStack.class) {
            ItemStack item = config.getItem(path + ".Item");
            data = item.getType().isAir() ? new ItemStack(Material.STONE) : item;
         } else if (dataType == Float.class) {
            data = (float)config.getDouble(path + ".floatValue", 1.0);
         } else if (dataType == Integer.class) {
            data = config.getInt(path + ".intValue", 1);
         } else if (dataType != Void.class) {
            return of(Particle.CLOUD);
         }

         return of(particle, data);
      }
   }

   @Override
   public void write(@NonNull FileConfig config, @NonNull String path) {
      config.set(path + ".Name", this.isEmpty() ? "null" : this.getParticle().name());
      Object data = this.getData();
      if (data instanceof BlockData blockData) {
         config.set(path + ".Material", blockData.getMaterial().name());
      } else if (data instanceof DustTransition dustTransition) {
         Color colorStart = dustTransition.getColor();
         Color colorEnd = dustTransition.getToColor();
         config.set(path + ".Color_From", colorStart.getRed() + "," + colorStart.getGreen() + "," + colorStart.getBlue());
         config.set(path + ".Color_To", colorEnd.getRed() + "," + colorEnd.getGreen() + "," + colorEnd.getBlue());
         config.set(path + ".Size", dustTransition.getSize());
      } else if (data instanceof DustOptions dustOptions) {
         Color color = dustOptions.getColor();
         config.set(path + ".Color", color.getRed() + "," + color.getGreen() + "," + color.getBlue());
         config.set(path + ".Size", dustOptions.getSize());
      } else if (Version.isAtLeast(Version.MC_1_21_10) && data instanceof Spell spell) {
         Color color = spell.getColor();
         float power = spell.getPower();
         config.set(path + ".Color", color.getRed() + "," + color.getGreen() + "," + color.getBlue());
         config.set(path + ".Power", power);
      } else if (data instanceof Color color) {
         config.set(path + ".Color", color.getRed() + "," + color.getGreen() + "," + color.getBlue());
      } else if (data instanceof ItemStack item) {
         config.setItem(path + ".Item", item);
      } else if (data instanceof Float f) {
         config.set(path + ".floatValue", f);
      } else if (data instanceof Integer i) {
         config.set(path + ".intValue", i);
      }
   }

   public boolean isEmpty() {
      return this.particle == null;
   }

   public Particle getParticle() {
      return this.particle;
   }

   @Nullable
   public Object getData() {
      return this.data;
   }

   public void validateData() {
      if (this.particle != null) {
         Class<?> dataType = this.particle.getDataType();
         if (this.data == null || !dataType.isAssignableFrom(this.data.getClass())) {
            if (dataType == BlockData.class) {
               this.data = Material.STONE.createBlockData();
            } else if (dataType == DustOptions.class) {
               this.data = new DustOptions(Color.WHITE, 1.0F);
            } else if (dataType == Color.class) {
               this.data = Color.WHITE;
            } else if (dataType == DustTransition.class) {
               this.data = new DustTransition(Color.BLACK, Color.WHITE, 1.0F);
            } else if (dataType == ItemStack.class) {
               this.data = new ItemStack(Material.STONE);
            } else if (dataType == Float.class) {
               this.data = 1.0F;
            } else if (dataType == Integer.class) {
               this.data = 1;
            }
         }
      }
   }

   public void play(@NonNull Location location, double speed, int amount) {
      this.play(location, 0.0, speed, amount);
   }

   public void play(@NonNull Location location, double offsetAll, double speed, int amount) {
      this.play(location, offsetAll, offsetAll, offsetAll, speed, amount);
   }

   public void play(@NonNull Location location, double xOffset, double yOffset, double zOffset, double speed, int amount) {
      this.play(null, location, xOffset, yOffset, zOffset, speed, amount);
   }

   public void play(@NonNull Player player, @NonNull Location location, double speed, int amount) {
      this.play(player, location, 0.0, speed, amount);
   }

   public void play(@NonNull Player player, @NonNull Location location, double offsetAll, double speed, int amount) {
      this.play(player, location, offsetAll, offsetAll, offsetAll, speed, amount);
   }

   public void play(@Nullable Player player, @NonNull Location location, double xOffset, double yOffset, double zOffset, double speed, int amount) {
      if (!this.isEmpty()) {
         if (this.particle != null && (this.particle.getDataType() == Void.class || this.data != null)) {
            if (player == null) {
               World world = location.getWorld();
               if (world == null) {
                  return;
               }

               world.spawnParticle(this.getParticle(), location, amount, xOffset, yOffset, zOffset, speed, this.getData());
            } else {
               player.spawnParticle(this.getParticle(), location, amount, xOffset, yOffset, zOffset, speed, this.getData());
            }
         }
      }
   }
}
