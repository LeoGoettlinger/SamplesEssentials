package de.oliver.fancynpcs.commands.npc;

import de.oliver.cloud.annotations.Argument;
import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Flag;
import de.oliver.cloud.annotations.Permission;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.events.NpcModifyEvent;
import java.text.DecimalFormat;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum MoveToCMD {
   INSTANCE;

   private static final DecimalFormat COORDS_FORMAT = new DecimalFormat("#.##");
   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   @Command("npc move_to <npc> <location> [world]")
   @Permission({"fancynpcs.command.npc.move_to"})
   public void onCommand(
      @NotNull final CommandSender sender,
      @NotNull final Npc npc,
      @Argument(suggestions = "relative_location") @NotNull final Location location,
      @Nullable final World world,
      @Flag("look-in-my-direction") final boolean shouldLookInSenderDirection
   ) {
      World finalWorld = world == null && sender instanceof Player player ? player.getWorld() : world;
      if (finalWorld == null) {
         this.translator.translate("npc_move_to_failure_must_specify_world").withPrefix().send(sender);
      } else {
         location.setWorld(finalWorld);
         if (shouldLookInSenderDirection && sender instanceof Player playerx) {
            location.setDirection(playerx.getLocation().subtract(location).toVector());
         }

         if (new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.LOCATION, location, sender).callEvent()) {
            npc.getData().setLocation(location);
            npc.updateForAll();
            this.translator
               .translate("npc_move_to_success")
               .withPrefix()
               .replace("npc", npc.getData().getName())
               .replace("x", COORDS_FORMAT.format(location.x()))
               .replace("y", COORDS_FORMAT.format(location.y()))
               .replace("z", COORDS_FORMAT.format(location.z()))
               .replace("world", finalWorld.getName())
               .send(sender);
         } else {
            this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
         }
      }
   }
}
