package de.oliver.fancynpcs.commands.npc;

import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Flag;
import de.oliver.cloud.annotations.Permission;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum NearbyCMD {
   INSTANCE;

   private final Translator translator = FancyNpcs.getInstance().getTranslator();
   private static final DecimalFormat COORDS_FORMAT = new DecimalFormat("#.##");
   private static final DecimalFormat DISTANCE_FORMAT = new DecimalFormat("#.#");

   @Command(
      value = "npc nearby",
      requiredSender = Player.class
   )
   @Permission({"fancynpcs.command.npc.nearby"})
   public void onCommand(
      @NotNull final Player sender,
      @Flag("radius") @Nullable final Long radius,
      @Flag("type") @Nullable final EntityType type,
      @Flag("sort") @Nullable final NearbyCMD.SortType sort
   ) {
      Stream<Npc> stream = FancyNpcs.getInstance().getNpcManagerImpl().getAllNpcs().stream();
      Location senderLocation = sender.getLocation();
      AtomicInteger totalCount = new AtomicInteger(0);
      stream = stream.filter(npc -> {
         if (npc.getData().getLocation().getWorld().equals(senderLocation.getWorld())) {
            totalCount.incrementAndGet();
            return true;
         } else {
            return false;
         }
      });
      if (FancyNpcs.PLAYER_NPCS_FEATURE_FLAG.isEnabled()) {
         stream = stream.filter(npc -> npc.getData().getCreator().equals(sender.getUniqueId()));
      }

      if (radius != null) {
         stream = stream.filter(npc -> npc.getData().getLocation().distance(senderLocation) <= radius.longValue());
      }

      if (type != null) {
         stream = stream.filter(npc -> npc.getData().getType() == type);
      }

      switch (sort != null ? sort : NearbyCMD.SortType.NEAREST) {
         case NAME:
            stream = stream.sorted(Comparator.comparing(npc -> npc.getData().getName()));
            break;
         case NAME_REVERSED:
            stream = stream.sorted(Comparator.<Npc, String>comparing(npc -> npc.getData().getName()).reversed());
            break;
         case NEAREST:
            stream = stream.sorted(Comparator.comparingDouble(npc -> npc.getData().getLocation().distance(senderLocation)));
            break;
         case FARTHEST:
            stream = stream.sorted(Comparator.<Npc>comparingDouble(npc -> npc.getData().getLocation().distance(senderLocation)).reversed());
      }

      this.translator.translate("npc_nearby_header").send(sender);
      AtomicInteger count = new AtomicInteger(0);
      stream.forEachOrdered(
         npc -> this.translator
            .translate("npc_nearby_entry")
            .replace("number", String.valueOf(count.incrementAndGet()))
            .replace("npc", npc.getData().getName())
            .replace("distance", DISTANCE_FORMAT.format(npc.getData().getLocation().distance(senderLocation)))
            .replace("location_x", COORDS_FORMAT.format(npc.getData().getLocation().x()))
            .replace("location_y", COORDS_FORMAT.format(npc.getData().getLocation().y()))
            .replace("location_z", COORDS_FORMAT.format(npc.getData().getLocation().z()))
            .replace("world", npc.getData().getLocation().getWorld().getName())
            .send(sender)
      );
      this.translator
         .translate("npc_nearby_footer")
         .replace("count", String.valueOf(count))
         .replace("count_formatted", "· ".repeat(3 - String.valueOf(count).length()) + count)
         .replace("total", String.valueOf(totalCount))
         .replace("total_formatted", "· ".repeat(3 - String.valueOf(totalCount).length()) + totalCount)
         .send(sender);
   }

   static {
      COORDS_FORMAT.setMinimumFractionDigits(2);
   }

   public static enum SortType {
      NAME,
      NAME_REVERSED,
      NEAREST,
      FARTHEST;
   }
}
