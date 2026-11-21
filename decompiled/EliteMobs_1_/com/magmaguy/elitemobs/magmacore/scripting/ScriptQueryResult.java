package com.magmaguy.elitemobs.magmacore.scripting;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

public record ScriptQueryResult(ScriptQueryResult.Kind kind, String text, Double number, Boolean flag) {
   public ScriptQueryResult(ScriptQueryResult.Kind kind, String text, Double number, Boolean flag) {
      Objects.requireNonNull(kind, "kind");
      int values = (text == null ? 0 : 1) + (number == null ? 0 : 1) + (flag == null ? 0 : 1);

      int expectedValues = switch (kind) {
         case UNHANDLED, NIL, FAILED -> 0;
         case STRING, NUMBER, BOOLEAN -> 1;
      };
      if (values == expectedValues
         && (kind != ScriptQueryResult.Kind.STRING || text != null)
         && (kind != ScriptQueryResult.Kind.NUMBER || number != null)
         && (kind != ScriptQueryResult.Kind.BOOLEAN || flag != null)) {
         this.kind = kind;
         this.text = text;
         this.number = number;
         this.flag = flag;
      } else {
         throw new IllegalArgumentException("Query result payload does not match " + kind);
      }
   }

   public static ScriptQueryResult unhandled() {
      return new ScriptQueryResult(ScriptQueryResult.Kind.UNHANDLED, null, null, null);
   }

   public static ScriptQueryResult nil() {
      return new ScriptQueryResult(ScriptQueryResult.Kind.NIL, null, null, null);
   }

   public static ScriptQueryResult string(String value) {
      return new ScriptQueryResult(ScriptQueryResult.Kind.STRING, Objects.requireNonNull(value, "value"), null, null);
   }

   public static ScriptQueryResult number(double value) {
      return new ScriptQueryResult(ScriptQueryResult.Kind.NUMBER, null, value, null);
   }

   public static ScriptQueryResult bool(boolean value) {
      return new ScriptQueryResult(ScriptQueryResult.Kind.BOOLEAN, null, null, value);
   }

   public static ScriptQueryResult failed() {
      return new ScriptQueryResult(ScriptQueryResult.Kind.FAILED, null, null, null);
   }

   public boolean handled() {
      return this.kind != ScriptQueryResult.Kind.UNHANDLED;
   }

   public Optional<String> stringValue() {
      return Optional.ofNullable(this.text);
   }

   public OptionalDouble numberValue() {
      return this.number == null ? OptionalDouble.empty() : OptionalDouble.of(this.number);
   }

   public Optional<Boolean> booleanValue() {
      return Optional.ofNullable(this.flag);
   }

   public static enum Kind {
      UNHANDLED,
      NIL,
      STRING,
      NUMBER,
      BOOLEAN,
      FAILED;
   }
}
