package de.theredend2000.advancedhunt.lib.kyori.adventure.chat;

import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;
import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Keyed;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.ComponentLike;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public interface ChatType {
   ChatType CHAT = new ChatTypeImpl(Key.key("chat"));
   ChatType SAY_COMMAND = new ChatTypeImpl(Key.key("say_command"));
   ChatType MSG_COMMAND_INCOMING = new ChatTypeImpl(Key.key("msg_command_incoming"));
   ChatType MSG_COMMAND_OUTGOING = new ChatTypeImpl(Key.key("msg_command_outgoing"));
   ChatType TEAM_MSG_COMMAND_INCOMING = new ChatTypeImpl(Key.key("team_msg_command_incoming"));
   ChatType TEAM_MSG_COMMAND_OUTGOING = new ChatTypeImpl(Key.key("team_msg_command_outgoing"));
   ChatType EMOTE_COMMAND = new ChatTypeImpl(Key.key("emote_command"));

   static ChatType chatType(final Keyed key) {
      return (ChatType)(key instanceof ChatType ct ? ct : new ChatTypeImpl(Objects.requireNonNull(key, "key").key()));
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   default ChatType.Bound bind(final ComponentLike name) {
      return this.bind(name, null);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   default ChatType.Bound bind(final ComponentLike name, @Nullable final ComponentLike target) {
      return new ChatTypeImpl.BoundImpl(this, Objects.requireNonNull(name.asComponent(), "name"), ComponentLike.unbox(target));
   }

   @Nullable
   Key key();

   public interface Bound {
      @Contract(
         pure = true
      )
      ChatType type();

      @Contract(
         pure = true
      )
      Component name();

      @Contract(
         pure = true
      )
      @Nullable
      Component target();
   }
}
