package com.github.retrooper.packetevents.protocol.component.builtin.item;

import com.github.retrooper.packetevents.protocol.color.DyeColor;
import com.github.retrooper.packetevents.protocol.item.banner.BannerPattern;
import com.github.retrooper.packetevents.protocol.item.banner.BannerPatterns;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.List;
import java.util.Objects;

public class BannerLayers {
   private List<BannerLayers.Layer> layers;

   public BannerLayers(List<BannerLayers.Layer> layers) {
      this.layers = layers;
   }

   public static BannerLayers read(PacketWrapper<?> wrapper) {
      List<BannerLayers.Layer> layers = wrapper.readList(BannerLayers.Layer::read);
      return new BannerLayers(layers);
   }

   public static void write(PacketWrapper<?> wrapper, BannerLayers patterns) {
      wrapper.writeList(patterns.layers, BannerLayers.Layer::write);
   }

   public void addLayer(BannerLayers.Layer layer) {
      this.layers.add(layer);
   }

   public List<BannerLayers.Layer> getLayers() {
      return this.layers;
   }

   public void setLayers(List<BannerLayers.Layer> layers) {
      this.layers = layers;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof BannerLayers)) {
         return false;
      } else {
         BannerLayers that = (BannerLayers)obj;
         return this.layers.equals(that.layers);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.layers);
   }

   public static class Layer {
      private BannerPattern pattern;
      private DyeColor color;

      public Layer(BannerPattern pattern, DyeColor color) {
         this.pattern = pattern;
         this.color = color;
      }

      public static BannerLayers.Layer read(PacketWrapper<?> wrapper) {
         BannerPattern pattern = wrapper.readMappedEntityOrDirect(BannerPatterns.getRegistry(), BannerPattern::readDirect);
         DyeColor color = DyeColor.read(wrapper);
         return new BannerLayers.Layer(pattern, color);
      }

      public static void write(PacketWrapper<?> wrapper, BannerLayers.Layer layer) {
         wrapper.writeMappedEntityOrDirect(layer.pattern, BannerPattern::writeDirect);
         DyeColor.write(wrapper, layer.color);
      }

      public BannerPattern getPattern() {
         return this.pattern;
      }

      public void setPattern(BannerPattern pattern) {
         this.pattern = pattern;
      }

      public DyeColor getColor() {
         return this.color;
      }

      public void setColor(DyeColor color) {
         this.color = color;
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (!(obj instanceof BannerLayers.Layer)) {
            return false;
         } else {
            BannerLayers.Layer layer = (BannerLayers.Layer)obj;
            return !this.pattern.equals(layer.pattern) ? false : this.color == layer.color;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.pattern, this.color);
      }
   }
}
