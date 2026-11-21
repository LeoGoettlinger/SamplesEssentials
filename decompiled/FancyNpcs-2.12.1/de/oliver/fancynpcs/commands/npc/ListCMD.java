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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ListCMD {
   INSTANCE;

   private static final DecimalFormat COORDS_FORMAT = new DecimalFormat("#.##");
   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   @Command("npc list")
   @Permission({"fancynpcs.command.npc.list"})
   public void onCommand(
      @NotNull final CommandSender sender, @Flag("type") @Nullable final EntityType type, @Flag("sort") @Nullable final ListCMD.SortType sort
   ) {
      Stream<Npc> stream = FancyNpcs.getInstance().getNpcManagerImpl().getAllNpcs().stream();
      if (FancyNpcs.PLAYER_NPCS_FEATURE_FLAG.isEnabled() && sender instanceof Player player) {
         stream = stream.filter(npc -> npc.getData().getCreator().equals(player.getUniqueId()));
      }

      if (type != null) {
         stream = stream.filter(npc -> npc.getData().getType() == type);
      }

      switch (sort != null ? sort : ListCMD.SortType.NAME) {
         case NAME:
            stream = stream.sorted(Comparator.comparing(npc -> npc.getData().getName()));
            break;
         case NAME_REVERSED:
            stream = stream.sorted(Comparator.<Npc, String>comparing(npc -> npc.getData().getName()).reversed());
      }

      this.translator.translate("npc_list_header").send(sender);
      AtomicInteger count = new AtomicInteger(0);
      stream.forEachOrdered(
         npc -> this.translator
            .translate("npc_list_entry")
            .replace("number", String.valueOf(count.incrementAndGet()))
            .replace("npc", npc.getData().getName())
            .replace("location_x", COORDS_FORMAT.format(npc.getData().getLocation().x()))
            .replace("location_y", COORDS_FORMAT.format(npc.getData().getLocation().y()))
            .replace("location_z", COORDS_FORMAT.format(npc.getData().getLocation().z()))
            .replace("world", npc.getData().getLocation().getWorld().getName())
            .send(sender)
      );
      int totalCount = count.get();
      this.translator
         .translate("npc_list_footer")
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
      NAME_REVERSED;
   }
}
