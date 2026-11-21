package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.component.builtin;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.ItemStack;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.ItemStackSerialization;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class SulfurCubeContentComponent {
   private ItemStack stack;

   public SulfurCubeContentComponent(ItemStack stack) {
      this.stack = stack;
   }

   public static SulfurCubeContentComponent read(PacketWrapper<?> wrapper) {
      ItemStack stack = ItemStackSerialization.readTemplate(wrapper);
      return new SulfurCubeContentComponent(stack);
   }

   public static void write(PacketWrapper<?> wrapper, SulfurCubeContentComponent component) {
      ItemStackSerialization.writeTemplate(wrapper, component.stack);
   }

   public ItemStack getStack() {
      return this.stack;
   }

   public void setStack(ItemStack stack) {
      this.stack = stack;
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         SulfurCubeContentComponent that = (SulfurCubeContentComponent)obj;
         return this.stack.equals(that.stack);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.stack);
   }
}
