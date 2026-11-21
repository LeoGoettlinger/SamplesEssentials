package ac.grim.grimac.checks.impl.chat;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.multiactions.MultiActionsC;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommand;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommandUnsigned;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@CheckData(
   name = "ChatC",
   stableKey = "grim.chat.moving_while_chatting",
   description = "Moving while chatting",
   experimental = true
)
public class ChatC extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("sprinting={bool}, sneaking={bool}, input={bool}");
   @Nullable
   private Predicate<String> exemptRegex;

   public ChatC(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.CHAT_MESSAGE) {
         this.check(new WrapperPlayClientChatMessage(event).getMessage(), event);
      }

      if (event.getPacketType() == PacketType.Play.Client.CHAT_COMMAND_UNSIGNED) {
         this.check("/" + new WrapperPlayClientChatCommandUnsigned(event).getCommand(), event);
      }

      if (event.getPacketType() == PacketType.Play.Client.CHAT_COMMAND) {
         this.check("/" + new WrapperPlayClientChatCommand(event).getCommand(), event);
      }
   }

   private void check(String message, PacketReceiveEvent event) {
      if (this.exemptRegex == null || !this.exemptRegex.test(message)) {
         boolean sprinting = MultiActionsC.isVerboseSprinting(this.player);
         boolean sneaking = MultiActionsC.isVerboseSneaking(this.player);
         boolean input = MultiActionsC.isVerboseInput(this.player);
         if ((sprinting || sneaking || input) && this.flag(V.write(this.verbose()).bool(sprinting).bool(sneaking).bool(input)) && this.shouldModifyPackets()) {
            event.setCancelled(true);
            this.player.onPacketCancel();
         }
      }
   }

   @Override
   public void onReload(ConfigManager config) {
      String regexString = config.getStringElse(this.getConfigName() + ".exempt-regex", null);
      this.exemptRegex = regexString == null ? null : Pattern.compile(regexString).asMatchPredicate();
   }
}
