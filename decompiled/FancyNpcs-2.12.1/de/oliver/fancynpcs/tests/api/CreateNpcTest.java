package de.oliver.fancynpcs.tests.api;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcData;
import de.oliver.fancynpcs.api.NpcManager;
import de.oliver.plugintests.Expectable;
import de.oliver.plugintests.annotations.FPAfterEach;
import de.oliver.plugintests.annotations.FPBeforeEach;
import de.oliver.plugintests.annotations.FPTest;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class CreateNpcTest {
   private static final NpcManager NPC_MANAGER = FancyNpcsPlugin.get().getNpcManager();
   private String npcName;
   private UUID creatorUUID;
   private Location location;
   private Npc createdNpc;

   @FPBeforeEach
   public void setUp(Player player) {
      this.npcName = "test-" + UUID.randomUUID().toString().substring(0, 8);
      this.creatorUUID = player.getUniqueId();
      this.location = player.getLocation().clone();
      this.createdNpc = null;
   }

   @FPAfterEach
   public void tearDown(Player player) {
      if (this.createdNpc != null) {
         NPC_MANAGER.removeNpc(this.createdNpc);
      }

      Expectable.expect(NPC_MANAGER.getNpc(this.npcName)).toBeNull();
      this.createdNpc = null;
      this.npcName = null;
      this.creatorUUID = null;
      this.location = null;
   }

   @FPTest(
      name = "Create and register npc"
   )
   public void createAndRegisterNpc(Player player) {
      NpcData data = new NpcData(this.npcName, this.creatorUUID, this.location);
      this.createdNpc = FancyNpcsPlugin.get().getNpcAdapter().apply(data);
      Expectable.expect(this.createdNpc).toBeDefined();
      this.createdNpc.create();
      Expectable.expect(this.createdNpc.getEntityId()).toBeGreaterThan(-1);
      Expectable.expect(this.createdNpc.getData().getName()).toEqual(this.npcName);
      Expectable.expect(this.createdNpc.getData().getCreator()).toEqual(this.creatorUUID);
      Expectable.expect(this.createdNpc.getData().getLocation()).toEqual(this.location);
      Expectable.expect(this.createdNpc.getData().getType()).toEqual(EntityType.PLAYER);
      NPC_MANAGER.registerNpc(this.createdNpc);
      Expectable.expect(NPC_MANAGER.getNpc(this.npcName)).toBeDefined();
   }
}
