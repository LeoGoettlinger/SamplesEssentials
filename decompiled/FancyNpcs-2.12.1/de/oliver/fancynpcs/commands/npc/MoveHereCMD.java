package de.oliver.fancynpcs.commands.npc;

import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Permission;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.events.NpcModifyEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public enum MoveHereCMD {
   INSTANCE;

   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   @Command(
      value = "npc move_here <npc>",
      requiredSender = Player.class
   )
   @Permission({"fancynpcs.command.npc.move_here"})
   public void onCommand(@NotNull final Player sender, @NotNull final Npc npc) {
      Location location = sender.getLocation();
      String oldWorld = npc.getData().getLocation().getWorld().getName();
      if (new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.LOCATION, location, sender).callEvent()) {
         npc.getData().setLocation(location);
         if (oldWorld.equals(location.getWorld().getName())) {
            npc.updateForAll();
         } else {
            npc.removeForAll();
            npc.spawnForAll();
         }

         this.translator.translate("npc_move_here_success").withPrefix().replace("npc", npc.getData().getName()).send(sender);
      } else {
         this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
      }
   }
}
