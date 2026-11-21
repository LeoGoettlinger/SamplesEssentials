package de.oliver.fancynpcs.commands.arguments;

import de.oliver.cloud.annotations.suggestion.Suggestions;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import org.bukkit.FluidCollisionMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

public enum LocationArgument {
   INSTANCE;

   private static final DecimalFormat COORDS_FORMAT = new DecimalFormat("#.##");

   @Suggestions("relative_location")
   public List<String> suggestLocation(final CommandContext<CommandSender> context, final CommandInput input) {
      if (context.sender() instanceof Player player) {
         RayTraceResult raytrace = player.rayTraceBlocks(32.0, FluidCollisionMode.ALWAYS);
         return raytrace != null
            ? List.of(
               COORDS_FORMAT.format(raytrace.getHitPosition().getX())
                  + " "
                  + COORDS_FORMAT.format(raytrace.getHitPosition().getY())
                  + " "
                  + COORDS_FORMAT.format(raytrace.getHitPosition().getZ()),
               "~ ~ ~"
            )
            : List.of("~ ~ ~");
      } else {
         return Collections.emptyList();
      }
   }
}
