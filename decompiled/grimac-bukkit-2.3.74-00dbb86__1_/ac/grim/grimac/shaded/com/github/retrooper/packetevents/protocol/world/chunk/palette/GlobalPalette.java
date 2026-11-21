package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.chunk.palette;

public class GlobalPalette implements Palette {
   public static final int BITS_PER_ENTRY = 15;
   public static final GlobalPalette INSTANCE = new GlobalPalette();

   @Override
   public int size() {
      return Integer.MAX_VALUE;
   }

   @Override
   public int stateToId(int state) {
      return state;
   }

   @Override
   public int idToState(int id) {
      return id;
   }

   @Override
   public int getBits() {
      return 15;
   }
}
