package de.oliver.fancynpcs.tests.commands;

import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.tests.api.NpcTestEnv;
import de.oliver.plugintests.Expectable;
import de.oliver.plugintests.annotations.FPAfterEach;
import de.oliver.plugintests.annotations.FPBeforeEach;
import de.oliver.plugintests.annotations.FPTest;
import de.oliver.plugintests.utils.Delay;
import java.util.List;
import org.bukkit.entity.Player;

public class DisplayNameCMDTest {
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
      name = "Set display name"
   )
   public void setDisplayName(Player player) {
      String displayName = "<red>Test Display Name";
      Expectable.expect(player.performCommand("npc displayname " + this.npcName + " " + displayName)).toBe(true);
      Delay.delay(() -> Expectable.expect(this.npc.getData().getDisplayName()).toEqual(displayName));
   }

   @FPTest(
      name = "Set display name to none"
   )
   public void setDisplayNameToNone(Player player) {
      Expectable.expect(player.performCommand("npc displayname " + this.npcName + " @none")).toBe(true);
      Delay.delay(() -> Expectable.expect(this.npc.getData().getDisplayName()).toEqual("<empty>"));
   }

   @FPTest(
      name = "Set display name to empty"
   )
   public void setDisplayNameToEmpty(Player player) {
      Expectable.expect(player.performCommand("npc displayname " + this.npcName + " <empty>")).toBe(true);
      Delay.delay(() -> Expectable.expect(this.npc.getData().getDisplayName()).toEqual("<empty>"));
   }

   @FPTest(
      name = "Set display name with blocked command"
   )
   public void setDisplayNameWithBlockedCommand(Player player) {
      List<String> blockedCommands = FancyNpcs.getInstance().getFancyNpcConfig().getBlockedCommands();
      if (!blockedCommands.isEmpty()) {
         String blockedCommand = blockedCommands.get(0);
         Expectable.expect(player.performCommand("npc displayname " + this.npcName + " <click:run_command:'/" + blockedCommand + "'>hello</click>")).toBe(true);
         Delay.delay(() -> Expectable.expect(this.npc.getData().getDisplayName()).toEqual(this.npcName));
      }
   }
}
