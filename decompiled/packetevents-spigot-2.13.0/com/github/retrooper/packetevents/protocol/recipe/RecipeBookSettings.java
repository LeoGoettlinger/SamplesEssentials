package com.github.retrooper.packetevents.protocol.recipe;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class RecipeBookSettings {
   private final Map<RecipeBookType, RecipeBookSettings.TypeState> states;

   public RecipeBookSettings(Map<RecipeBookType, RecipeBookSettings.TypeState> states) {
      this.states = states;
   }

   public static RecipeBookSettings read(PacketWrapper<?> wrapper) {
      Map<RecipeBookType, RecipeBookSettings.TypeState> state = new EnumMap<>(RecipeBookType.class);

      for (RecipeBookType bookType : RecipeBookType.values()) {
         state.put(bookType, RecipeBookSettings.TypeState.read(wrapper));
      }

      return new RecipeBookSettings(state);
   }

   public static void write(PacketWrapper<?> wrapper, RecipeBookSettings settings) {
      for (RecipeBookType bookType : RecipeBookType.values()) {
         RecipeBookSettings.TypeState.write(wrapper, settings.getState(bookType));
      }
   }

   public RecipeBookSettings.TypeState getState(RecipeBookType type) {
      return this.states.computeIfAbsent(type, $ -> new RecipeBookSettings.TypeState());
   }

   public void setState(RecipeBookType type, RecipeBookSettings.TypeState state) {
      this.states.put(type, state);
   }

   public Map<RecipeBookType, RecipeBookSettings.TypeState> getStates() {
      return this.states;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof RecipeBookSettings)) {
         return false;
      } else {
         RecipeBookSettings that = (RecipeBookSettings)obj;
         return this.states.equals(that.states);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.states);
   }

   @Override
   public String toString() {
      return "RecipeBookSettings{states=" + this.states + '}';
   }

   public static final class TypeState {
      private static final boolean DEFAULT_OPEN = false;
      private static final boolean DEFAULT_FILTERING = false;
      private boolean open;
      private boolean filtering;

      public TypeState() {
         this(false, false);
      }

      public TypeState(boolean open, boolean filtering) {
         this.open = open;
         this.filtering = filtering;
      }

      public static RecipeBookSettings.TypeState read(PacketWrapper<?> wrapper) {
         boolean open = wrapper.readBoolean();
         boolean filtering = wrapper.readBoolean();
         return new RecipeBookSettings.TypeState(open, filtering);
      }

      public static void write(PacketWrapper<?> wrapper, RecipeBookSettings.TypeState state) {
         wrapper.writeBoolean(state.open);
         wrapper.writeBoolean(state.filtering);
      }

      public boolean isOpen() {
         return this.open;
      }

      public void setOpen(boolean open) {
         this.open = open;
      }

      public boolean isFiltering() {
         return this.filtering;
      }

      public void setFiltering(boolean filtering) {
         this.filtering = filtering;
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (!(obj instanceof RecipeBookSettings.TypeState)) {
            return false;
         } else {
            RecipeBookSettings.TypeState typeState = (RecipeBookSettings.TypeState)obj;
            return this.open != typeState.open ? false : this.filtering == typeState.filtering;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.open, this.filtering);
      }

      @Override
      public String toString() {
         return "TypeState{open=" + this.open + ", filtering=" + this.filtering + '}';
      }
   }
}
