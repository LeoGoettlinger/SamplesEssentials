package de.oliver.fancynpcs.commands.npc;

import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Permission;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.events.NpcModifyEvent;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public enum RotateCMD {
   INSTANCE;

   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   @Command("npc rotate <npc> <yaw> <pitch>")
   @Permission({"fancynpcs.command.npc.rotate"})
   public void onRotate(@NotNull final CommandSender sender, @NotNull final Npc npc, final float yaw, final float pitch) {
      Location currentLocation = npc.getData().getLocation();
      Location newLocation = currentLocation.clone();
      newLocation.setYaw(yaw);
      newLocation.setPitch(pitch);
      if (new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.ROTATION, new float[]{yaw, pitch}, sender).callEvent()) {
         npc.getData().setLocation(newLocation);
         npc.updateForAll();
         this.translator
            .translate("npc_rotate_set_success")
            .withPrefix()
            .replace("npc", npc.getData().getName())
            .replace("yaw", String.valueOf(yaw))
            .replace("pitch", String.valueOf(pitch))
            .send(sender);
      } else {
         this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
      }
   }
}
