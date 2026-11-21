package xyz.lychee.lagfixer.libs.kyori.adventure.text.event;

import java.util.Objects;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.adventure.dialog.DialogLike;
import xyz.lychee.lagfixer.libs.kyori.adventure.internal.Internals;
import xyz.lychee.lagfixer.libs.kyori.adventure.key.Key;
import xyz.lychee.lagfixer.libs.kyori.adventure.nbt.api.BinaryTagHolder;
import xyz.lychee.lagfixer.libs.kyori.examination.ExaminableProperty;

abstract class PayloadImpl implements ClickEvent.Payload {
   @Override
   public String toString() {
      return Internals.toString(this);
   }

   static final class CustomImpl extends PayloadImpl implements ClickEvent.Payload.Custom {
      private final Key key;
      private final BinaryTagHolder nbt;

      CustomImpl(@NotNull final Key key, @NotNull final BinaryTagHolder nbt) {
         this.key = key;
         this.nbt = nbt;
      }

      @NotNull
      @Override
      public Key key() {
         return this.key;
      }

      @NotNull
      @Override
      public String data() {
         return this.nbt.string();
      }

      @NotNull
      @Override
      public BinaryTagHolder nbt() {
         return this.nbt;
      }

      @NotNull
      @Override
      public Stream<? extends ExaminableProperty> examinableProperties() {
         return Stream.of(ExaminableProperty.of("key", this.key), ExaminableProperty.of("nbt", this.nbt));
      }

      @Override
      public boolean equals(final Object other) {
         if (this == other) {
            return true;
         } else if (other != null && this.getClass() == other.getClass()) {
            PayloadImpl.CustomImpl that = (PayloadImpl.CustomImpl)other;
            return Objects.equals(this.key, that.key) && Objects.equals(this.nbt, that.nbt);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         int result = this.key.hashCode();
         return 31 * result + this.nbt.hashCode();
      }
   }

   static final class DialogImpl extends PayloadImpl implements ClickEvent.Payload.Dialog {
      private final DialogLike dialogLike;

      DialogImpl(@NotNull final DialogLike dialogLike) {
         this.dialogLike = dialogLike;
      }

      @NotNull
      @Override
      public DialogLike dialog() {
         return this.dialogLike;
      }

      @NotNull
      @Override
      public Stream<? extends ExaminableProperty> examinableProperties() {
         return Stream.of(ExaminableProperty.of("dialog", this.dialogLike));
      }

      @Override
      public boolean equals(final Object other) {
         if (this == other) {
            return true;
         } else if (other != null && this.getClass() == other.getClass()) {
            PayloadImpl.DialogImpl that = (PayloadImpl.DialogImpl)other;
            return Objects.equals(this.dialogLike, that.dialogLike);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return this.dialogLike.hashCode();
      }
   }

   static final class IntImpl extends PayloadImpl implements ClickEvent.Payload.Int {
      private final int integer;

      IntImpl(final int integer) {
         this.integer = integer;
      }

      @Override
      public int integer() {
         return this.integer;
      }

      @NotNull
      @Override
      public Stream<? extends ExaminableProperty> examinableProperties() {
         return Stream.of(ExaminableProperty.of("integer", this.integer));
      }

      @Override
      public boolean equals(final Object other) {
         if (this == other) {
            return true;
         } else if (other != null && this.getClass() == other.getClass()) {
            PayloadImpl.IntImpl that = (PayloadImpl.IntImpl)other;
            return Objects.equals(this.integer, that.integer);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return this.integer;
      }
   }

   static final class TextImpl extends PayloadImpl implements ClickEvent.Payload.Text {
      private final String value;

      TextImpl(@NotNull final String value) {
         this.value = value;
      }

      @NotNull
      @Override
      public String value() {
         return this.value;
      }

      @NotNull
      @Override
      public Stream<? extends ExaminableProperty> examinableProperties() {
         return Stream.of(ExaminableProperty.of("value", this.value));
      }

      @Override
      public boolean equals(final Object other) {
         if (this == other) {
            return true;
         } else if (other != null && this.getClass() == other.getClass()) {
            PayloadImpl.TextImpl that = (PayloadImpl.TextImpl)other;
            return Objects.equals(this.value, that.value);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return this.value.hashCode();
      }
   }
}
