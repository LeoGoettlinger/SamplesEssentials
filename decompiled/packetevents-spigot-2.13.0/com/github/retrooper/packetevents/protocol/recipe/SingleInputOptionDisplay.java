package com.github.retrooper.packetevents.protocol.recipe;

import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntitySet;
import com.github.retrooper.packetevents.protocol.recipe.display.slot.SlotDisplay;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class SingleInputOptionDisplay {
   private MappedEntitySet<ItemType> input;
   private SlotDisplay<?> optionDisplay;

   public SingleInputOptionDisplay(MappedEntitySet<ItemType> input, SlotDisplay<?> optionDisplay) {
      this.input = input;
      this.optionDisplay = optionDisplay;
   }

   public static SingleInputOptionDisplay read(PacketWrapper<?> wrapper) {
      MappedEntitySet<ItemType> ingredient = MappedEntitySet.read(wrapper, ItemTypes.getRegistry());
      SlotDisplay<?> optionDisplay = SlotDisplay.read(wrapper);
      return new SingleInputOptionDisplay(ingredient, optionDisplay);
   }

   public static void write(PacketWrapper<?> wrapper, SingleInputOptionDisplay recipe) {
      MappedEntitySet.write(wrapper, recipe.input);
      SlotDisplay.write(wrapper, recipe.optionDisplay);
   }

   public MappedEntitySet<ItemType> getInput() {
      return this.input;
   }

   public void setInput(MappedEntitySet<ItemType> input) {
      this.input = input;
   }

   public SlotDisplay<?> getOptionDisplay() {
      return this.optionDisplay;
   }

   public void setOptionDisplay(SlotDisplay<?> optionDisplay) {
      this.optionDisplay = optionDisplay;
   }
}
