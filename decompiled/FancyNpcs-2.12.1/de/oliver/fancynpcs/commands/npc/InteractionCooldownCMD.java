package de.oliver.fancynpcs.commands.npc;

import de.oliver.cloud.annotations.Argument;
import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Permission;
import de.oliver.cloud.annotations.parser.Parser;
import de.oliver.cloud.annotations.suggestion.Suggestions;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.events.NpcModifyEvent;
import de.oliver.fancynpcs.api.utils.Interval;
import de.oliver.fancynpcs.commands.exceptions.ReplyingParseException;
import it.unimi.dsi.fastutil.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum InteractionCooldownCMD {
   INSTANCE;

   private static final Pattern SPLIT_PATTERN = Pattern.compile("(?<=\\d)(?=\\D)");
   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   @Command("npc interaction_cooldown <npc> <cooldown>")
   @Permission({"fancynpcs.command.npc.interaction_cooldown"})
   public void onInteractionCooldown(
      @NotNull final CommandSender sender, @NotNull final Npc npc, @Argument(parserName = "InteractionCooldownCMD/cooldown") @NotNull final Interval cooldown
   ) {
      if (new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.INTERACTION_COOLDOWN, cooldown, sender).callEvent()) {
         npc.getData().setInteractionCooldown((float)cooldown.as(Interval.Unit.MILLISECONDS) / 1000.0F);
         this.translator
            .translate(cooldown.as(Interval.Unit.MILLISECONDS) != 0.0 ? "npc_interaction_cooldown_set" : "npc_interaction_cooldown_disabled")
            .withPrefix()
            .replace("npc", npc.getData().getName())
            .replace("time", cooldown.toString())
            .send(sender);
      } else {
         this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
      }
   }

   @Parser(
      name = "InteractionCooldownCMD/cooldown",
      suggestions = "InteractionCooldownCMD/cooldown"
   )
   @NotNull
   public Interval parse(final CommandContext<CommandSender> context, final CommandInput input) {
      String value = input.readString();
      if (value.equalsIgnoreCase("disabled")) {
         return Interval.of(0L, Interval.Unit.MILLISECONDS);
      } else {
         String[] split = SPLIT_PATTERN.split(value);
         Long num = split.length == 2 ? this.parseLong(split[0]) : null;
         Interval.Unit unit = split.length == 2 ? Interval.Unit.fromShortCode(split[1].toLowerCase()) : null;
         if (num != null && unit != null) {
            return Interval.of(Math.max(0L, num), unit);
         } else {
            throw ReplyingParseException.replying(
               () -> this.translator.translate("command_invalid_interval").withPrefix().replaceStripped("input", value).send(context.sender())
            );
         }
      }
   }

   @Suggestions("InteractionCooldownCMD/cooldown")
   @NotNull
   public Collection<String> suggest(final CommandContext<CommandSender> context, final CommandInput input) {
      String value = input.readString();
      String[] split = SPLIT_PATTERN.split(value);
      final Long num = this.parseLong(split[0]);
      return (Collection<String>)(num != null && num > 0L
         ? new ArrayList<String>() {
            {
               Objects.requireNonNull(InteractionCooldownCMD.this);
               this.add("disabled");
               this.addAll(
                  Stream.of(
                        Pair.of(Interval.of(num, Interval.Unit.MILLISECONDS), Interval.Unit.MILLISECONDS),
                        Pair.of(Interval.of(num, Interval.Unit.SECONDS), Interval.Unit.SECONDS),
                        Pair.of(Interval.of(num, Interval.Unit.MINUTES), Interval.Unit.MINUTES),
                        Pair.of(Interval.of(num, Interval.Unit.HOURS), Interval.Unit.HOURS)
                     )
                     .map(pair -> num + ((Interval.Unit)pair.second()).getShortCode())
                     .toList()
               );
            }
         }
         : List.of("30s", "5min", "8h", "disabled"));
   }

   @Nullable
   private Long parseLong(@NotNull final String value) {
      try {
         return Long.parseLong(value);
      } catch (NumberFormatException var3) {
         return null;
      }
   }
}
