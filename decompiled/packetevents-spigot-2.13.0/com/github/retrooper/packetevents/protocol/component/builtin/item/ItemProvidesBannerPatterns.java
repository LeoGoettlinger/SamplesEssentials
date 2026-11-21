package com.github.retrooper.packetevents.protocol.component.builtin.item;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.item.banner.BannerPattern;
import com.github.retrooper.packetevents.protocol.item.banner.BannerPatterns;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntitySet;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ItemProvidesBannerPatterns {
   private MappedEntitySet<BannerPattern> set;

   public ItemProvidesBannerPatterns(MappedEntitySet<BannerPattern> set) {
      this.set = set;
   }

   public ItemProvidesBannerPatterns(ResourceLocation tagKey) {
      this(new MappedEntitySet<>(tagKey));
   }

   public static ItemProvidesBannerPatterns read(PacketWrapper<?> wrapper) {
      if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_26_1)) {
         MappedEntitySet<BannerPattern> set = MappedEntitySet.read(wrapper, BannerPatterns.getRegistry());
         return new ItemProvidesBannerPatterns(set);
      } else {
         ResourceLocation tagKey = wrapper.readIdentifier();
         return new ItemProvidesBannerPatterns(tagKey);
      }
   }

   public static void write(PacketWrapper<?> wrapper, ItemProvidesBannerPatterns patterns) {
      if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_26_1)) {
         MappedEntitySet.write(wrapper, patterns.set);
      } else {
         wrapper.writeIdentifier(patterns.getTagKey());
      }
   }

   public MappedEntitySet<BannerPattern> getSet() {
      return this.set;
   }

   public void setSet(MappedEntitySet<BannerPattern> set) {
      this.set = set;
   }

   public ResourceLocation getTagKey() {
      ResourceLocation tagKey = this.set.getTagKey();
      if (tagKey == null) {
         throw new IllegalStateException("No tag key present");
      } else {
         return tagKey;
      }
   }

   public void setTagKey(ResourceLocation tagKey) {
      this.set = new MappedEntitySet<>(tagKey);
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof ItemProvidesBannerPatterns)) {
         return false;
      } else {
         ItemProvidesBannerPatterns that = (ItemProvidesBannerPatterns)obj;
         return this.set.equals(that.set);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.set);
   }
}
