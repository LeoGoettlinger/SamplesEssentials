package ac.grim.grimac.command.requirements;

import ac.grim.grimac.command.SenderRequirement;
import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.shaded.incendo.cloud.context.CommandContext;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;
import ac.grim.grimac.utils.anticheat.MessageUtil;

public enum PlayerSenderRequirement implements SenderRequirement {
   INSTANCE;

   @NotNull
   @Override
   public Component errorMessage(Sender sender) {
      return MessageUtil.getParsedComponent(sender, "run-as-player", "%prefix% &cThis command can only be used by players!");
   }

   @Override
   public boolean evaluateRequirement(@NotNull CommandContext<Sender> commandContext) {
      return commandContext.sender().isPlayer();
   }
}
