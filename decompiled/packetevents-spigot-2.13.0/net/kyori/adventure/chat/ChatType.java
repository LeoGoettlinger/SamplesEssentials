package net.kyori.adventure.chat;

import java.util.Objects;
import java.util.stream.Stream;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.examination.Examinable;
import net.kyori.examination.ExaminableProperty;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ChatType extends Examinable, Keyed {
   ChatType CHAT = new ChatTypeImpl(Key.key("chat"));
   ChatType SAY_COMMAND = new ChatTypeImpl(Key.key("say_command"));
   ChatType MSG_COMMAND_INCOMING = new ChatTypeImpl(Key.key("msg_command_incoming"));
   ChatType MSG_COMMAND_OUTGOING = new ChatTypeImpl(Key.key("msg_command_outgoing"));
   ChatType TEAM_MSG_COMMAND_INCOMING = new ChatTypeImpl(Key.key("team_msg_command_incoming"));
   ChatType TEAM_MSG_COMMAND_OUTGOING = new ChatTypeImpl(Key.key("team_msg_command_outgoing"));
   ChatType EMOTE_COMMAND = new ChatTypeImpl(Key.key("emote_command"));

   @NotNull
   static ChatType chatType(@NotNull final Keyed key) {
      return (ChatType)(key instanceof ChatType ? (ChatType)key : new ChatTypeImpl(Objects.requireNonNull(key, "key").key()));
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   @NotNull
   default ChatType.Bound bind(@NotNull final ComponentLike name) {
      return this.bind(name, null);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   @NotNull
   default ChatType.Bound bind(@NotNull final ComponentLike name, @Nullable final ComponentLike target) {
      return new ChatTypeImpl.BoundImpl(this, Objects.requireNonNull(name.asComponent(), "name"), ComponentLike.unbox(target));
   }

   @NotNull
   @Override
   default Stream<? extends ExaminableProperty> examinableProperties() {
      return Stream.of(ExaminableProperty.of("key", this.key()));
   }

   @NotNull
   @Override
   Key key();

   public interface Bound extends Examinable {
      @Contract(
         pure = true
      )
      @NotNull
      ChatType type();

      @Contract(
         pure = true
      )
      @NotNull
      Component name();

      @Contract(
         pure = true
      )
      @Nullable
      Component target();

      @NotNull
      @Override
      default Stream<? extends ExaminableProperty> examinableProperties() {
         return Stream.of(
            ExaminableProperty.of("type", this.type()), ExaminableProperty.of("name", this.name()), ExaminableProperty.of("target", this.target())
         );
      }
   }
}
