package com.github.retrooper.packetevents.protocol.world.chunk.palette;

public interface Palette {
   int size();

   int stateToId(int state);

   int idToState(int id);

   int getBits();
}
