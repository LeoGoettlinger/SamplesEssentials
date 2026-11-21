package de.oliver.fancynpcs.api;

import java.util.Collection;
import java.util.UUID;
import org.jetbrains.annotations.ApiStatus;

public interface NpcManager {
   void registerNpc(Npc var1);

   void removeNpc(Npc var1);

   @ApiStatus.Internal
   Npc getNpc(int var1);

   Npc getNpc(String var1);

   Npc getNpcById(String var1);

   Npc getNpc(String var1, UUID var2);

   Collection<Npc> getAllNpcs();

   void saveNpcs(boolean var1);

   void loadNpcs();

   boolean isLoaded();

   void reloadNpcs();
}
