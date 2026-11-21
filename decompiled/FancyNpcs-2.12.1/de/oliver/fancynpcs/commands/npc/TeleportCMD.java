package de.oliver.fancynpcs.commands.npc;

import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Permission;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public enum TeleportCMD {
   INSTANCE;

   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   @Command(
      value = "npc teleport <npc>",
      requiredSender = Player.class
   )
   @Permission({"fancynpcs.command.npc.teleport"})
   public void onTeleport(@NotNull final Player sender, @NotNull final Npc npc) {
      Location location = npc.getData().getLocation();
      if (location.getWorld() == null) {
         this.translator.translate("npc_teleport_failure_world_not_loaded").withPrefix().send(sender);
      } else {
         sender.teleportAsync(location)
            .whenComplete(
               (isSuccess, thr) -> {
                  this.translator
                     .translate(isSuccess ? "npc_teleport_success" : "npc_teleport_failure_exception")
                     .replace("npc", npc.getData().getName())
                     .withPrefix()
                     .send(sender);
                  if (thr != null) {
                     thr.printStackTrace();
                  }
               }
            );
      }
   }
}
