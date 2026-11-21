package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.chunk.palette;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.stream.NetStreamInput;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class MapPalette implements Palette {
   private final int bits;
   private final int[] idToState;
   private final Int2IntHashMap stateToId;
   private int nextId = 0;

   public MapPalette(int bitsPerEntry) {
      this.bits = bitsPerEntry;
      this.idToState = new int[1 << bitsPerEntry];
      this.stateToId = new Int2IntHashMap(1 << bitsPerEntry);
   }

   @Deprecated
   public MapPalette(int bitsPerEntry, NetStreamInput in) {
      this(bitsPerEntry);
      int paletteLength = in.readVarInt();

      for (int i = 0; i < paletteLength; i++) {
         int state = in.readVarInt();
         this.idToState[i] = state;
         this.stateToId.putIfAbsent(state, i);
      }

      this.nextId = paletteLength;
   }

   public MapPalette(int bitsPerEntry, PacketWrapper<?> wrapper) {
      this(bitsPerEntry);
      int paletteLength = wrapper.readVarInt();

      for (int i = 0; i < paletteLength; i++) {
         int state = wrapper.readVarInt();
         this.idToState[i] = state;
         this.stateToId.putIfAbsent(state, i);
      }

      this.nextId = paletteLength;
   }

   @Override
   public int size() {
      return this.nextId;
   }

   @Override
   public int stateToId(int state) {
      int id = this.stateToId.get(state);
      if (id == -1 && this.size() < this.idToState.length) {
         id = this.nextId++;
         this.idToState[id] = state;
         this.stateToId.put(state, id);
      }

      return id;
   }

   @Override
   public int idToState(int id) {
      return id >= 0 && id < this.size() ? this.idToState[id] : 0;
   }

   @Override
   public int getBits() {
      return this.bits;
   }
}
