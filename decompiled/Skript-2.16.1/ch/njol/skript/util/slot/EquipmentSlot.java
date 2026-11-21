package ch.njol.skript.util.slot;

import ch.njol.skript.bukkitutil.BukkitUtils;
import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.registrations.Classes;
import com.google.common.base.Preconditions;
import java.util.Locale;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EquipmentSlot extends SlotWithIndex {
   private final EntityEquipment entityEquipment;
   private EquipmentSlot.EquipSlot skriptSlot;
   private final int slotIndex;
   private final boolean slotToString;
   private org.bukkit.inventory.EquipmentSlot bukkitSlot;

   @Deprecated(
      since = "2.11.0",
      forRemoval = true
   )
   public EquipmentSlot(@NotNull EntityEquipment entityEquipment, @NotNull EquipmentSlot.EquipSlot skriptSlot, boolean slotToString) {
      Preconditions.checkNotNull(entityEquipment, "entityEquipment cannot be null");
      Preconditions.checkNotNull(skriptSlot, "skriptSlot cannot be null");
      this.entityEquipment = entityEquipment;
      int slotIndex = -1;
      if (skriptSlot == EquipmentSlot.EquipSlot.TOOL && entityEquipment.getHolder() instanceof Player player) {
         slotIndex = player.getInventory().getHeldItemSlot();
      }

      this.slotIndex = slotIndex;
      this.skriptSlot = skriptSlot;
      this.slotToString = slotToString;
   }

   @Deprecated(
      since = "2.11.0",
      forRemoval = true
   )
   public EquipmentSlot(@NotNull EntityEquipment entityEquipment, @NotNull EquipmentSlot.EquipSlot skriptSlot) {
      this(entityEquipment, skriptSlot, false);
   }

   public EquipmentSlot(@NotNull EntityEquipment entityEquipment, @NotNull org.bukkit.inventory.EquipmentSlot bukkitSlot, boolean slotToString) {
      Preconditions.checkNotNull(entityEquipment, "entityEquipment cannot be null");
      Preconditions.checkNotNull(bukkitSlot, "bukkitSlot cannot be null");
      this.entityEquipment = entityEquipment;
      int slotIndex = -1;
      if (bukkitSlot == org.bukkit.inventory.EquipmentSlot.HAND && entityEquipment.getHolder() instanceof Player player) {
         slotIndex = player.getInventory().getHeldItemSlot();
      }

      this.slotIndex = slotIndex;
      this.bukkitSlot = bukkitSlot;
      this.slotToString = slotToString;
   }

   public EquipmentSlot(@NotNull EntityEquipment equipment, @NotNull org.bukkit.inventory.EquipmentSlot bukkitSlot) {
      this(equipment, bukkitSlot, false);
   }

   public EquipmentSlot(@NotNull HumanEntity holder, int index) {
      this(holder.getEquipment(), BukkitUtils.getEquipmentSlotFromIndex(index), true);
   }

   public EquipmentSlot(@NotNull HumanEntity holder, @NotNull org.bukkit.inventory.EquipmentSlot bukkitSlot) {
      this(holder.getEquipment(), bukkitSlot, true);
   }

   @Nullable
   @Override
   public ItemStack getItem() {
      return this.skriptSlot != null ? this.skriptSlot.get(this.entityEquipment) : this.entityEquipment.getItem(this.bukkitSlot);
   }

   @Override
   public void setItem(@Nullable ItemStack item) {
      if (this.skriptSlot != null) {
         this.skriptSlot.set(this.entityEquipment, item);
      } else {
         this.entityEquipment.setItem(this.bukkitSlot, item);
      }

      if (this.entityEquipment.getHolder() instanceof Player player) {
         PlayerUtils.updateInventory(player);
      }
   }

   @Override
   public int getAmount() {
      ItemStack item = this.getItem();
      return item != null ? item.getAmount() : 0;
   }

   @Override
   public void setAmount(int amount) {
      ItemStack item = this.getItem();
      if (item != null) {
         item.setAmount(amount);
      }

      this.setItem(item);
   }

   @Deprecated(
      since = "2.11.0",
      forRemoval = true
   )
   public EquipmentSlot.EquipSlot getEquipSlot() {
      return this.skriptSlot;
   }

   public org.bukkit.inventory.EquipmentSlot getEquipmentSlot() {
      return this.bukkitSlot;
   }

   @Override
   public int getIndex() {
      if (this.slotIndex != -1) {
         return this.slotIndex;
      } else if (this.skriptSlot != null) {
         return this.skriptSlot.slotNumber;
      } else {
         return BukkitUtils.getEquipmentSlotIndex(this.bukkitSlot) != null ? BukkitUtils.getEquipmentSlotIndex(this.bukkitSlot) : -1;
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      if (this.slotToString) {
         SyntaxStringBuilder syntaxBuilder = new SyntaxStringBuilder(event, debug);
         syntaxBuilder.append("the ");
         if (this.skriptSlot != null) {
            syntaxBuilder.append(this.skriptSlot.name().toLowerCase(Locale.ENGLISH));
         } else {
            syntaxBuilder.append(this.bukkitSlot.name().replace('_', ' ').toLowerCase(Locale.ENGLISH));
         }

         syntaxBuilder.append(" of ").append(Classes.toString(this.entityEquipment.getHolder()));
         return syntaxBuilder.toString();
      } else {
         return Classes.toString(this.getItem());
      }
   }

   @Deprecated(
      since = "2.11.0",
      forRemoval = true
   )
   public static enum EquipSlot {
      TOOL {
         @Override
         public ItemStack get(EntityEquipment entityEquipment) {
            return entityEquipment.getItemInMainHand();
         }

         @Override
         public void set(EntityEquipment entityEquipment, @Nullable ItemStack item) {
            entityEquipment.setItemInMainHand(item);
         }
      },
      OFF_HAND(40) {
         @Override
         public ItemStack get(EntityEquipment entityEquipment) {
            return entityEquipment.getItemInOffHand();
         }

         @Override
         public void set(EntityEquipment entityEquipment, @Nullable ItemStack item) {
            entityEquipment.setItemInOffHand(item);
         }
      },
      HELMET(39) {
         @Nullable
         @Override
         public ItemStack get(EntityEquipment entityEquipment) {
            return entityEquipment.getHelmet();
         }

         @Override
         public void set(EntityEquipment entityEquipment, @Nullable ItemStack item) {
            entityEquipment.setHelmet(item);
         }
      },
      CHESTPLATE(38) {
         @Nullable
         @Override
         public ItemStack get(EntityEquipment entityEquipment) {
            return entityEquipment.getChestplate();
         }

         @Override
         public void set(EntityEquipment entityEquipment, @Nullable ItemStack item) {
            entityEquipment.setChestplate(item);
         }
      },
      LEGGINGS(37) {
         @Nullable
         @Override
         public ItemStack get(EntityEquipment entityEquipment) {
            return entityEquipment.getLeggings();
         }

         @Override
         public void set(EntityEquipment entityEquipment, @Nullable ItemStack item) {
            entityEquipment.setLeggings(item);
         }
      },
      BOOTS(36) {
         @Nullable
         @Override
         public ItemStack get(EntityEquipment entityEquipment) {
            return entityEquipment.getBoots();
         }

         @Override
         public void set(EntityEquipment entityEquipment, @Nullable ItemStack item) {
            entityEquipment.setBoots(item);
         }
      },
      BODY {
         @Override
         public ItemStack get(EntityEquipment entityEquipment) {
            return entityEquipment.getItem(org.bukkit.inventory.EquipmentSlot.BODY);
         }

         @Override
         public void set(EntityEquipment entityEquipment, @Nullable ItemStack item) {
            entityEquipment.setItem(org.bukkit.inventory.EquipmentSlot.BODY, item);
         }
      };

      public final int slotNumber;

      private EquipSlot() {
         this.slotNumber = -1;
      }

      private EquipSlot(int number) {
         this.slotNumber = number;
      }

      @Nullable
      public abstract ItemStack get(EntityEquipment var1);

      public abstract void set(EntityEquipment var1, @Nullable ItemStack var2);
   }
}
