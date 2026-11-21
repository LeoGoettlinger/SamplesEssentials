package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.recipe.display;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;

public abstract class RecipeDisplay<T extends RecipeDisplay<?>> {
   protected final RecipeDisplayType<T> type;

   public RecipeDisplay(RecipeDisplayType<T> type) {
      this.type = type;
   }

   public static RecipeDisplay<?> read(PacketWrapper<?> wrapper) {
      return wrapper.readMappedEntity(RecipeDisplayTypes.getRegistry()).read(wrapper);
   }

   public static <T extends RecipeDisplay<?>> void write(PacketWrapper<?> wrapper, T display) {
      wrapper.writeMappedEntity(display.getType());
      ((RecipeDisplayType<T>)display.getType()).write(wrapper, display);
   }

   public RecipeDisplayType<T> getType() {
      return this.type;
   }
}
