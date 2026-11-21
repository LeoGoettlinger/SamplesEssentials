package net.kyori.adventure.text.event;

import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import org.jspecify.annotations.Nullable;

final class PayloadImpl {
   private PayloadImpl() {
   }

   record CustomImpl(Key key, @Nullable BinaryTagHolder nbt) implements ClickEvent.Payload.Custom {
   }

   record DialogImpl(DialogLike dialog) implements ClickEvent.Payload.Dialog {
   }

   record IntImpl(int integer) implements ClickEvent.Payload.Int {
   }

   record TextImpl(String value) implements ClickEvent.Payload.Text {
   }
}
