package org.enginehub.linbus.stream.token;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Optional;
import java.util.OptionalInt;
import org.enginehub.linbus.common.LinTagId;

public sealed interface LinToken
   permits LinToken.Name,
   LinToken.ByteArrayStart,
   LinToken.ByteArrayContent,
   LinToken.ByteArrayEnd,
   LinToken.Byte,
   LinToken.CompoundStart,
   LinToken.CompoundEnd,
   LinToken.Double,
   LinToken.Float,
   LinToken.IntArrayStart,
   LinToken.IntArrayContent,
   LinToken.IntArrayEnd,
   LinToken.Int,
   LinToken.ListStart,
   LinToken.ListEnd,
   LinToken.LongArrayStart,
   LinToken.LongArrayContent,
   LinToken.LongArrayEnd,
   LinToken.Long,
   LinToken.Short,
   LinToken.String {
   boolean isSimpleValue();

   default Optional<LinTagId> tagId() {
      return Optional.empty();
   }

   public record Byte(byte value) implements LinToken {
      @Override
      public boolean isSimpleValue() {
         return true;
      }

      @Override
      public Optional<LinTagId> tagId() {
         return Optional.of(LinTagId.BYTE);
      }
   }

   public record ByteArrayContent(ByteBuffer buffer) implements LinToken {
      public ByteArrayContent(ByteBuffer buffer) {
         if (!buffer.isReadOnly()) {
            throw new IllegalArgumentException("buffer must be read-only");
         } else {
            this.buffer = buffer;
         }
      }

      @Override
      public boolean isSimpleValue() {
         return false;
      }
   }

   public record ByteArrayEnd() implements LinToken {
      @Override
      public boolean isSimpleValue() {
         return false;
      }
   }

   public record ByteArrayStart(OptionalInt size) implements LinToken {
      public ByteArrayStart() {
         this(OptionalInt.empty());
      }

      public ByteArrayStart(int size) {
         this(OptionalInt.of(size));
      }

      @Override
      public boolean isSimpleValue() {
         return false;
      }

      @Override
      public Optional<LinTagId> tagId() {
         return Optional.of(LinTagId.BYTE_ARRAY);
      }
   }

   public record CompoundEnd() implements LinToken {
      @Override
      public boolean isSimpleValue() {
         return false;
      }
   }

   public record CompoundStart() implements LinToken {
      @Override
      public boolean isSimpleValue() {
         return false;
      }

      @Override
      public Optional<LinTagId> tagId() {
         return Optional.of(LinTagId.COMPOUND);
      }
   }

   public record Double(double value) implements LinToken {
      @Override
      public boolean isSimpleValue() {
         return true;
      }

      @Override
      public Optional<LinTagId> tagId() {
         return Optional.of(LinTagId.DOUBLE);
      }
   }

   public record Float(float value) implements LinToken {
      @Override
      public boolean isSimpleValue() {
         return true;
      }

      @Override
      public Optional<LinTagId> tagId() {
         return Optional.of(LinTagId.FLOAT);
      }
   }

   public record Int(int value) implements LinToken {
      @Override
      public boolean isSimpleValue() {
         return true;
      }

      @Override
      public Optional<LinTagId> tagId() {
         return Optional.of(LinTagId.INT);
      }
   }

   public record IntArrayContent(IntBuffer buffer) implements LinToken {
      public IntArrayContent(IntBuffer buffer) {
         if (!buffer.isReadOnly()) {
            throw new IllegalArgumentException("buffer must be read-only");
         } else {
            this.buffer = buffer;
         }
      }

      @Override
      public boolean isSimpleValue() {
         return false;
      }
   }

   public record IntArrayEnd() implements LinToken {
      @Override
      public boolean isSimpleValue() {
         return false;
      }
   }

   public record IntArrayStart(OptionalInt size) implements LinToken {
      public IntArrayStart() {
         this(OptionalInt.empty());
      }

      public IntArrayStart(int size) {
         this(OptionalInt.of(size));
      }

      @Override
      public boolean isSimpleValue() {
         return false;
      }

      @Override
      public Optional<LinTagId> tagId() {
         return Optional.of(LinTagId.INT_ARRAY);
      }
   }

   public record ListEnd() implements LinToken {
      @Override
      public boolean isSimpleValue() {
         return false;
      }
   }

   public record ListStart(OptionalInt size, Optional<LinTagId> elementId) implements LinToken {
      public ListStart() {
         this(OptionalInt.empty(), Optional.empty());
      }

      public ListStart(int size, LinTagId elementId) {
         this(OptionalInt.of(size), Optional.of(elementId));
      }

      @Override
      public boolean isSimpleValue() {
         return false;
      }

      @Override
      public Optional<LinTagId> tagId() {
         return Optional.of(LinTagId.LIST);
      }
   }

   public record Long(long value) implements LinToken {
      @Override
      public boolean isSimpleValue() {
         return true;
      }

      @Override
      public Optional<LinTagId> tagId() {
         return Optional.of(LinTagId.LONG);
      }
   }

   public record LongArrayContent(LongBuffer buffer) implements LinToken {
      public LongArrayContent(LongBuffer buffer) {
         if (!buffer.isReadOnly()) {
            throw new IllegalArgumentException("buffer must be read-only");
         } else {
            this.buffer = buffer;
         }
      }

      @Override
      public boolean isSimpleValue() {
         return false;
      }
   }

   public record LongArrayEnd() implements LinToken {
      @Override
      public boolean isSimpleValue() {
         return false;
      }
   }

   public record LongArrayStart(OptionalInt size) implements LinToken {
      public LongArrayStart() {
         this(OptionalInt.empty());
      }

      public LongArrayStart(int size) {
         this(OptionalInt.of(size));
      }

      @Override
      public boolean isSimpleValue() {
         return false;
      }

      @Override
      public Optional<LinTagId> tagId() {
         return Optional.of(LinTagId.LONG_ARRAY);
      }
   }

   public record Name(java.lang.String name, Optional<LinTagId> id) implements LinToken {
      public Name(java.lang.String name) {
         this(name, Optional.empty());
      }

      public Name(java.lang.String name, LinTagId id) {
         this(name, Optional.of(id));
      }

      @Override
      public boolean isSimpleValue() {
         return false;
      }
   }

   public record Short(short value) implements LinToken {
      @Override
      public boolean isSimpleValue() {
         return true;
      }

      @Override
      public Optional<LinTagId> tagId() {
         return Optional.of(LinTagId.SHORT);
      }
   }

   public record String(java.lang.String value) implements LinToken {
      @Override
      public boolean isSimpleValue() {
         return true;
      }

      @Override
      public Optional<LinTagId> tagId() {
         return Optional.of(LinTagId.STRING);
      }
   }
}
