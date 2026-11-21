package de.oliver.fancynpcs.commands.npc;

import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Flag;
import de.oliver.cloud.annotations.Permission;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcData;
import de.oliver.fancynpcs.api.events.NpcCreateEvent;
import java.util.UUID;
import java.util.regex.Pattern;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum CreateCMD {
   INSTANCE;

   private static final Pattern NPC_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9/_-]*$");
   private static final UUID EMPTY_UUID = new UUID(0L, 0L);
   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   @Command("npc create <name>")
   @Permission({"fancynpcs.command.npc.create"})
   public void onCreate(
      @NotNull final CommandSender sender,
      @NotNull final String name,
      @Flag("type") @Nullable final EntityType type,
      @Flag(value = "location",suggestions = "relative_location") @Nullable final Location location,
      @Flag("world") @Nullable final World world
   ) {
      if (!NPC_NAME_PATTERN.matcher(name).find()) {
         this.translator.translate("npc_create_failure_invalid_name").withPrefix().replaceStripped("name", name).send(sender);
      } else {
         UUID creator = sender instanceof Player player ? player.getUniqueId() : EMPTY_UUID;
         if ((!FancyNpcs.PLAYER_NPCS_FEATURE_FLAG.isEnabled() || FancyNpcs.getInstance().getNpcManager().getNpc(name, creator) == null)
            && (FancyNpcs.PLAYER_NPCS_FEATURE_FLAG.isEnabled() || FancyNpcs.getInstance().getNpcManager().getNpc(name) == null)) {
            if (sender instanceof ConsoleCommandSender && location == null) {
               this.translator.translate("npc_create_failure_must_specify_location").withPrefix().send(sender);
            } else if (sender instanceof ConsoleCommandSender && world == null) {
               this.translator.translate("npc_create_failure_must_specify_world").withPrefix().send(sender);
            } else {
               Location finalLocation = location == null && sender instanceof Player playerx ? playerx.getLocation() : location;
               if (world != null) {
                  finalLocation.setWorld(world);
               }

               Npc npc = FancyNpcs.getInstance().getNpcAdapter().apply(new NpcData(name, creator, finalLocation));
               npc.getData().setType(type != null ? type : EntityType.PLAYER);
               if (new NpcCreateEvent(npc, sender).callEvent()) {
                  npc.create();
                  FancyNpcs.getInstance().getNpcManagerImpl().registerNpc(npc);
                  npc.spawnForAll();
                  this.translator.translate("npc_create_success").withPrefix().replace("npc", name).send(sender);
               } else {
                  this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
               }
            }
         } else {
            this.translator
               .translate("npc_create_failure_already_exists")
               .withPrefix()
               .replace("npc", FancyNpcs.getInstance().getNpcManager().getNpc(name).getData().getName())
               .send(sender);
         }
      }
   }
}
