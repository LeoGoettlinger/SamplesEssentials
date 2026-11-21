package de.oliver.fancynpcs.tests.api;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcData;
import de.oliver.plugintests.Expectable;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class NpcTestEnv {
   public static Npc givenDefaultNpcIsCreated() {
      String name = "test-" + UUID.randomUUID().toString().substring(0, 8);
      UUID creator = UUID.randomUUID();
      Location location = new Location(Bukkit.getWorld("world"), 100.0, 100.0, -100.0);
      NpcData data = new NpcData(name, creator, location);
      Npc npc = FancyNpcsPlugin.get().getNpcAdapter().apply(data);
      Expectable.expect(npc).toBeDefined();
      npc.create();
      Expectable.expect(npc.getEntityId()).toBeGreaterThan(-1);
      return npc;
   }

   public static void givenNpcIsRegistered(Npc npc) {
      Expectable.expect(npc).toBeDefined();
      FancyNpcsPlugin.get().getNpcManager().registerNpc(npc);
      Expectable.expect(FancyNpcsPlugin.get().getNpcManager().getNpc(npc.getData().getName())).toBeDefined();
   }

   public static void givenNpcIsUnregistered(Npc npc) {
      Expectable.expect(npc).toBeDefined();
      FancyNpcsPlugin.get().getNpcManager().removeNpc(npc);
      Expectable.expect(FancyNpcsPlugin.get().getNpcManager().getNpc(npc.getData().getName())).toBeNull();
   }
}
