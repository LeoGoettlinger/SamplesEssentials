package ac.grim.grimac.utils.lists;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import java.util.Collection;
import lombok.Generated;

public final class ArrayUtils {
   public static StateType[] combine(Collection<StateType> tagStates, StateType... manualStates) {
      StateType[] result = new StateType[tagStates.size() + manualStates.length];
      int i = 0;

      for (StateType state : tagStates) {
         result[i++] = state;
      }

      System.arraycopy(manualStates, 0, result, tagStates.size(), manualStates.length);
      return result;
   }

   @Generated
   private ArrayUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
