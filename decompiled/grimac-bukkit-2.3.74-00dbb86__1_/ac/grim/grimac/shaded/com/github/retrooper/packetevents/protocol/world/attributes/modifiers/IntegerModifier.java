package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.attributes.modifiers;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtCodec;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtCodecs;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.attributes.EnvironmentAttribute;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface IntegerModifier<A> extends AttributeModifier<Integer, A> {
   IntegerModifier<Integer> ADD = Integer::sum;
   IntegerModifier<Integer> SUBTRACT = (IntegerModifier.Simple)(a, b) -> a - b;
   IntegerModifier<Integer> MULTIPLY = (IntegerModifier.Simple)(a, b) -> a * b;
   IntegerModifier<Integer> MINIMUM = Math::min;
   IntegerModifier<Integer> MAXIMUM = Math::max;

   @FunctionalInterface
   public interface Simple extends IntegerModifier<Integer> {
      @Override
      default NbtCodec<Integer> argumentCodec(EnvironmentAttribute<Integer> attribute) {
         return NbtCodecs.INT;
      }
   }
}
