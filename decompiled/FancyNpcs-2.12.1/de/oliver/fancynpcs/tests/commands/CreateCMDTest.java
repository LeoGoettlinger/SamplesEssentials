package de.oliver.fancynpcs.tests.commands;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcManager;
import de.oliver.plugintests.Expectable;
import de.oliver.plugintests.annotations.FPAfterEach;
import de.oliver.plugintests.annotations.FPBeforeEach;
import de.oliver.plugintests.annotations.FPTest;
import de.oliver.plugintests.utils.Delay;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class CreateCMDTest {
   private static final NpcManager NPC_MANAGER = FancyNpcsPlugin.get().getNpcManager();
   private String npcName;
   private Npc createdNpc;

   @FPBeforeEach
   public void setUp(Player player) {
      this.npcName = "test-" + UUID.randomUUID().toString().substring(0, 8);
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
   }

   @FPTest(
      name = "Create npc"
   )
   public void createNpc(Player player) {
      Expectable.expect(player.performCommand("npc create " + this.npcName)).toBe(true);
      Delay.delay(() -> {
         this.createdNpc = NPC_MANAGER.getNpc(this.npcName);
         Expectable.expect(this.createdNpc).toBeDefined();
         Expectable.expect(this.createdNpc.getEntityId()).toBeGreaterThan(-1);
         Expectable.expect(this.createdNpc.getData().getName()).toEqual(this.npcName);
         Expectable.expect(this.createdNpc.getData().getType()).toEqual(EntityType.PLAYER);
         Expectable.expect(this.createdNpc.getData().getLocation()).toBeDefined();
         Expectable.expect(this.createdNpc.getData().getLocation().getWorld().getName()).toEqual(player.getWorld().getName());
         Expectable.expect(this.createdNpc.getData().getCreator()).toEqual(player.getUniqueId());
      });
   }

   @FPTest(
      name = "Create npc with type"
   )
   public void createNpcWithType(Player player) {
      Expectable.expect(player.performCommand("npc create " + this.npcName + " --type PIG")).toBe(true);
      Delay.delay(() -> {
         this.createdNpc = NPC_MANAGER.getNpc(this.npcName);
         Expectable.expect(this.createdNpc).toBeDefined();
         Expectable.expect(this.createdNpc.getEntityId()).toBeGreaterThan(-1);
         Expectable.expect(this.createdNpc.getData().getType()).toEqual(EntityType.PIG);
      });
   }

   @FPTest(
      name = "Create npc with location"
   )
   public void createNpcWithLocation(Player player) {
      Expectable.expect(player.performCommand("npc create " + this.npcName + " --location 12 154 842")).toBe(true);
      Delay.delay(() -> {
         this.createdNpc = NPC_MANAGER.getNpc(this.npcName);
         Expectable.expect(this.createdNpc).toBeDefined();
         Expectable.expect(this.createdNpc.getEntityId()).toBeGreaterThan(-1);
         Expectable.expect(this.createdNpc.getData().getLocation().x()).toEqual(12.0);
         Expectable.expect(this.createdNpc.getData().getLocation().y()).toEqual(154.0);
         Expectable.expect(this.createdNpc.getData().getLocation().z()).toEqual(842.0);
      });
   }

   @FPTest(
      name = "Create npc with world"
   )
   public void createNpcWithWorld(Player player) {
      String worldName;
      if (Bukkit.getWorld("world_the_nether") == null) {
         worldName = "world";
      } else {
         worldName = "world_the_nether";
      }

      Expectable.expect(player.performCommand("npc create " + this.npcName + " --world " + worldName)).toBe(true);
      Delay.delay(() -> {
         this.createdNpc = NPC_MANAGER.getNpc(this.npcName);
         Expectable.expect(this.createdNpc).toBeDefined();
         Expectable.expect(this.createdNpc.getEntityId()).toBeGreaterThan(-1);
         Expectable.expect(this.createdNpc.getData().getLocation().getWorld().getName()).toEqual(worldName);
      });
   }

   @FPTest(
      name = "Create npc with invalid name"
   )
   public void createNpcWithInvalidName(Player player) {
      Expectable.expect(player.performCommand("npc create invalid.name")).toBe(true);
      Delay.delay(() -> {
         this.createdNpc = NPC_MANAGER.getNpc("invalid.name");
         Expectable.expect(this.createdNpc).toBeNull();
      });
   }

   @FPTest(
      name = "Create npc with existing name"
   )
   public void createNpcWithExistingName(Player player) {
      Expectable.expect(player.performCommand("npc create " + this.npcName)).toBe(true);
      Delay.delay(() -> {
         this.createdNpc = NPC_MANAGER.getNpc(this.npcName);
         Expectable.expect(this.createdNpc).toBeDefined();
         Expectable.expect(player.performCommand("npc create " + this.npcName)).toBe(true);
         Npc existingNpc = NPC_MANAGER.getNpc(this.npcName);
         Expectable.expect(existingNpc).toBeDefined();
         Expectable.expect(existingNpc.getEntityId()).toBeGreaterThan(-1);
         Expectable.expect(existingNpc).toEqual(this.createdNpc);
      });
   }

   @FPTest(
      name = "Create npc with all flags"
   )
   public void createNpcWithAllFlags(Player player) {
      String worldName;
      if (Bukkit.getWorld("world_the_nether") == null) {
         worldName = "world";
      } else {
         worldName = "world_the_nether";
      }

      Expectable.expect(player.performCommand("npc create " + this.npcName + " --type COW --location 137 131 -571 --world " + worldName)).toBe(true);
      Delay.delay(() -> {
         this.createdNpc = NPC_MANAGER.getNpc(this.npcName);
         Expectable.expect(this.createdNpc).toBeDefined();
         Expectable.expect(this.createdNpc.getEntityId()).toBeGreaterThan(-1);
         Expectable.expect(this.createdNpc.getData().getType()).toEqual(EntityType.COW);
         Expectable.expect(this.createdNpc.getData().getLocation().x()).toEqual(137.0);
         Expectable.expect(this.createdNpc.getData().getLocation().y()).toEqual(131.0);
         Expectable.expect(this.createdNpc.getData().getLocation().z()).toEqual(-571.0);
         Expectable.expect(this.createdNpc.getData().getLocation().getWorld().getName()).toEqual(worldName);
      });
   }
}
