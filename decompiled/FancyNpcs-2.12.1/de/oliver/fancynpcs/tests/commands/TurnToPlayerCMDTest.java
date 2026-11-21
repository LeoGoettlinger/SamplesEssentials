package de.oliver.fancynpcs.tests.commands;

import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.tests.api.NpcTestEnv;
import de.oliver.plugintests.Expectable;
import de.oliver.plugintests.annotations.FPAfterEach;
import de.oliver.plugintests.annotations.FPBeforeEach;
import de.oliver.plugintests.annotations.FPTest;
import de.oliver.plugintests.utils.Delay;
import org.bukkit.entity.Player;

public class TurnToPlayerCMDTest {
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
      name = "Set turnToPlayer to true"
   )
   public void setTurnToPlayerToTrue(Player player) {
      Expectable.expect(player.performCommand("npc turn_to_player " + this.npcName + " true")).toBe(true);
      Delay.delay(() -> Expectable.expect(this.npc.getData().isTurnToPlayer()).toBe(true));
   }

   @FPTest(
      name = "Set turnToPlayer to false"
   )
   public void setTurnToPlayerToFalse(Player player) {
      this.npc.getData().setTurnToPlayer(true);
      Expectable.expect(player.performCommand("npc turn_to_player " + this.npcName + " false")).toBe(true);
      Delay.delay(() -> Expectable.expect(this.npc.getData().isTurnToPlayer()).toBe(false));
   }
}
