package com.github.retrooper.packetevents.protocol.component.builtin.item;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentType;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import org.jetbrains.annotations.ApiStatus.Obsolete;

public class ItemEnchantments implements Iterable<Entry<EnchantmentType, Integer>> {
   public static final ItemEnchantments EMPTY = new ItemEnchantments(Collections.emptyMap(), true) {
      @Override
      public void setEnchantments(Map<EnchantmentType, Integer> enchantments) {
         throw new UnsupportedOperationException();
      }

      @Override
      public void setShowInTooltip(boolean showInTooltip) {
         throw new UnsupportedOperationException();
      }
   };
   private Map<EnchantmentType, Integer> enchantments;
   @Obsolete
   private boolean showInTooltip;

   public ItemEnchantments(Map<EnchantmentType, Integer> enchantments) {
      this(enchantments, true);
   }

   @Obsolete
   public ItemEnchantments(Map<EnchantmentType, Integer> enchantments, boolean showInTooltip) {
      this.enchantments = Collections.unmodifiableMap(enchantments);
      this.showInTooltip = showInTooltip;
   }

   public static ItemEnchantments read(PacketWrapper<?> wrapper) {
      Map<EnchantmentType, Integer> enchantments = wrapper.readMap(ew -> wrapper.readMappedEntity(EnchantmentTypes.getRegistry()), PacketWrapper::readVarInt);
      boolean showInTooltip = wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_5) || wrapper.readBoolean();
      return new ItemEnchantments(enchantments, showInTooltip);
   }

   public static void write(PacketWrapper<?> wrapper, ItemEnchantments enchantments) {
      ClientVersion version = wrapper.getServerVersion().toClientVersion();
      wrapper.writeMap(enchantments.getEnchantments(), (ew, enchantment) -> ew.writeVarInt(enchantment.getId(version)), PacketWrapper::writeVarInt);
      if (wrapper.getServerVersion().isOlderThan(ServerVersion.V_1_21_5)) {
         wrapper.writeBoolean(enchantments.isShowInTooltip());
      }
   }

   public int getEnchantmentLevel(EnchantmentType enchantment) {
      return this.enchantments.getOrDefault(enchantment, 0);
   }

   public void setEnchantmentLevel(EnchantmentType enchantment, int level) {
      if (level == 0) {
         this.enchantments.remove(enchantment);
      } else {
         this.enchantments.put(enchantment, level);
      }
   }

   public boolean isEmpty() {
      return this.getEnchantmentCount() < 1;
   }

   public int getEnchantmentCount() {
      return this.enchantments.size();
   }

   public Map<EnchantmentType, Integer> getEnchantments() {
      return this.enchantments;
   }

   public void setEnchantments(Map<EnchantmentType, Integer> enchantments) {
      this.enchantments = enchantments;
   }

   @Obsolete
   public boolean isShowInTooltip() {
      return this.showInTooltip;
   }

   @Obsolete
   public void setShowInTooltip(boolean showInTooltip) {
      this.showInTooltip = showInTooltip;
   }

   @Override
   public Iterator<Entry<EnchantmentType, Integer>> iterator() {
      return this.enchantments.entrySet().iterator();
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof ItemEnchantments)) {
         return false;
      } else {
         ItemEnchantments that = (ItemEnchantments)obj;
         return this.showInTooltip != that.showInTooltip ? false : this.enchantments.equals(that.enchantments);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.enchantments, this.showInTooltip);
   }

   @Override
   public String toString() {
      return "ItemEnchantments{enchantments=" + this.enchantments + ", showInTooltip=" + this.showInTooltip + '}';
   }
}
