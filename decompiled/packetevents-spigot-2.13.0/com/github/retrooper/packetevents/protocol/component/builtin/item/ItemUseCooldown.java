package com.github.retrooper.packetevents.protocol.component.builtin.item;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetCooldown;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public class ItemUseCooldown {
   private float seconds;
   private Optional<ResourceLocation> cooldownGroup;

   public ItemUseCooldown(float seconds, @Nullable ResourceLocation cooldownGroup) {
      this(seconds, Optional.ofNullable(cooldownGroup));
   }

   public ItemUseCooldown(float seconds, Optional<ResourceLocation> cooldownGroup) {
      this.seconds = seconds;
      this.cooldownGroup = cooldownGroup;
   }

   public static ItemUseCooldown read(PacketWrapper<?> wrapper) {
      float seconds = wrapper.readFloat();
      ResourceLocation cooldownGroup = wrapper.readOptional(PacketWrapper::readIdentifier);
      return new ItemUseCooldown(seconds, cooldownGroup);
   }

   public static void write(PacketWrapper<?> wrapper, ItemUseCooldown cooldown) {
      wrapper.writeFloat(cooldown.seconds);
      wrapper.writeOptional(cooldown.cooldownGroup.orElse(null), PacketWrapper::writeIdentifier);
   }

   public WrapperPlayServerSetCooldown buildWrapper(ItemStack fallbackStack) {
      return this.buildWrapper(fallbackStack.getType());
   }

   public WrapperPlayServerSetCooldown buildWrapper(ItemType fallbackItem) {
      int ticks = (int)(this.seconds * 20.0F);
      return this.cooldownGroup
         .<WrapperPlayServerSetCooldown>map(resourceLocation -> new WrapperPlayServerSetCooldown(resourceLocation, ticks))
         .orElseGet(() -> new WrapperPlayServerSetCooldown(fallbackItem, ticks));
   }

   public float getSeconds() {
      return this.seconds;
   }

   public void setSeconds(float seconds) {
      this.seconds = seconds;
   }

   public Optional<ResourceLocation> getCooldownGroup() {
      return this.cooldownGroup;
   }

   public void setCooldownGroup(Optional<ResourceLocation> cooldownGroup) {
      this.cooldownGroup = cooldownGroup;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof ItemUseCooldown)) {
         return false;
      } else {
         ItemUseCooldown that = (ItemUseCooldown)obj;
         return Float.compare(that.seconds, this.seconds) != 0 ? false : this.cooldownGroup.equals(that.cooldownGroup);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.seconds, this.cooldownGroup);
   }

   @Override
   public String toString() {
      return "ItemUseCooldown{seconds=" + this.seconds + ", cooldownGroup=" + this.cooldownGroup + '}';
   }
}
