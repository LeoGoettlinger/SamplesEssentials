package de.oliver.fancynpcs.tests.commands;

import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.tests.api.NpcTestEnv;
import de.oliver.plugintests.Expectable;
import de.oliver.plugintests.annotations.FPAfterEach;
import de.oliver.plugintests.annotations.FPBeforeEach;
import de.oliver.plugintests.annotations.FPTest;
import de.oliver.plugintests.utils.Delay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class TypeCMDTest {
   private Npc npc;
   private String npcName;

   @FPBeforeEach
   public void setUp(Player player) {
      this.npc = NpcTestEnv.givenDefaultNpcIsCreated();
      this.npcName = this.npc.getData().getName();
      NpcTestEnv.givenNpcIsRegistered(this.npc);
   }

   @FPAfterEach
   public void tearDown(Player player) {
      NpcTestEnv.givenNpcIsUnregistered(this.npc);
      this.npc = null;
      this.npcName = null;
   }

   @FPTest(
      name = "Set type to COW"
   )
   public void setTypeToCow(Player player) {
      Expectable.expect(player.performCommand("npc type " + this.npcName + " COW")).toBe(true);
      Delay.delay(() -> Expectable.expect(this.npc.getData().getType()).toBe(EntityType.COW));
   }
}
