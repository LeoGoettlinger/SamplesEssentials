package de.theredend2000.advancedhunt.lib.kyori.adventure.text.event;

import de.theredend2000.advancedhunt.lib.kyori.adventure.dialog.DialogLike;
import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;
import de.theredend2000.advancedhunt.lib.kyori.adventure.nbt.api.BinaryTagHolder;
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
