package com.artillexstudios.axafkzone.libs.axapi.nms.wrapper;

import com.artillexstudios.axafkzone.libs.axapi.selection.BlockSetter;
import com.artillexstudios.axafkzone.libs.axapi.selection.ParallelBlockSetter;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;

public interface WorldWrapper extends Wrapper<World> {
   static WorldWrapper wrap(Object world) {
      return WrapperRegistry.WORLD.map(world);
   }

   BlockSetter setter();

   ParallelBlockSetter parallelSetter();

   List<ServerPlayerWrapper> players();

   void setBlockOwner(Location location, String texture);
}
