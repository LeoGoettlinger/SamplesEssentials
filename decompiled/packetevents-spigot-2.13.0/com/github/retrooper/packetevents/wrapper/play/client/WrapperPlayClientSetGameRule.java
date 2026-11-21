package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.List;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WrapperPlayClientSetGameRule extends PacketWrapper<WrapperPlayClientSetGameRule> {
   @MonotonicNonNull
   private List<WrapperPlayClientSetGameRule.Entry> entries;

   public WrapperPlayClientSetGameRule(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientSetGameRule(List<WrapperPlayClientSetGameRule.Entry> entries) {
      super(PacketType.Play.Client.SET_GAME_RULE);
      this.entries = entries;
   }

   @Override
   public void read() {
      this.entries = this.readList(WrapperPlayClientSetGameRule.Entry::read);
   }

   @Override
   public void write() {
      this.writeList(this.entries, WrapperPlayClientSetGameRule.Entry::write);
   }

   public void copy(WrapperPlayClientSetGameRule wrapper) {
      this.entries = wrapper.entries;
   }

   public List<WrapperPlayClientSetGameRule.Entry> getEntries() {
      return this.entries;
   }

   public void setEntries(List<WrapperPlayClientSetGameRule.Entry> entries) {
      this.entries = entries;
   }

   public static final class Entry {
      private final ResourceLocation gameRule;
      private final String value;

      public Entry(ResourceLocation gameRule, String value) {
         this.gameRule = gameRule;
         this.value = value;
      }

      public static WrapperPlayClientSetGameRule.Entry read(PacketWrapper<?> wrapper) {
         ResourceLocation gameRule = ResourceLocation.read(wrapper);
         String value = wrapper.readString();
         return new WrapperPlayClientSetGameRule.Entry(gameRule, value);
      }

      public static void write(PacketWrapper<?> wrapper, WrapperPlayClientSetGameRule.Entry entry) {
         ResourceLocation.write(wrapper, entry.gameRule);
         wrapper.writeString(entry.value);
      }

      public ResourceLocation getGameRule() {
         return this.gameRule;
      }

      public String getValue() {
         return this.value;
      }
   }
}
