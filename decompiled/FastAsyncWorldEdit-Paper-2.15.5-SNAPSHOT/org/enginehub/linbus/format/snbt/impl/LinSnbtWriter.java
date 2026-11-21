package org.enginehub.linbus.format.snbt.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayDeque;
import java.util.OptionalInt;
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.exception.NbtWriteException;
import org.enginehub.linbus.stream.token.LinToken;

public class LinSnbtWriter {
   private final ArrayDeque<LinSnbtWriter.WriteState> stateStack = new ArrayDeque<>();

   public void write(Appendable output, LinStream tokens) throws IOException {
      while (true) {
         LinSnbtWriter.WriteState state = this.stateStack.peekLast();
         LinToken token = tokens.nextOrNull();
         if (token == null) {
            return;
         }

         switch (token) {
            case LinToken.Name var7:
               LinToken.Name var93 = var7;

               try {
                  var94 = var93.name();
               } catch (Throwable var58) {
                  throw new MatchException(var58.toString(), var58);
               }

               String var59 = var94;
               var93 = var7;

               try {
                  var93.id();
               } catch (Throwable var57) {
                  throw new MatchException(var57.toString(), var57);
               }

               if (state instanceof LinSnbtWriter.WriteState.Compound compound) {
                  if (compound.hasPrevious) {
                     output.append(',');
                     this.replaceLast(new LinSnbtWriter.WriteState.Compound(false));
                  }

                  output.append(Elusion.escapeIfNeeded(var59)).append(':');
                  break;
               }

               throw new NbtWriteException("Names can only appear inside compounds");
            case LinToken.ByteArrayStart byteArrayStart:
               output.append("[B;");
               break;
            case LinToken.ByteArrayContent(ByteBuffer var92):
               ByteBuffer var61 = var92;
               ByteBuffer buffer = var61;
               if (state instanceof LinSnbtWriter.WriteState.WritingArray) {
                  output.append(',');
               } else {
                  this.stateStack.addLast(new LinSnbtWriter.WriteState.WritingArray());
               }

               while (buffer.hasRemaining()) {
                  output.append(String.valueOf(buffer.get())).append('B');
                  if (buffer.hasRemaining()) {
                     output.append(',');
                  }
               }
               break;
            case LinToken.ByteArrayEnd byteArrayEnd:
               if (state instanceof LinSnbtWriter.WriteState.WritingArray) {
                  this.stateStack.removeLast();
               }

               output.append(']');
               this.handleValueEnd(output);
               break;
            case LinToken.Byte(byte var62):
               output.append(String.valueOf((int)var62)).append('B');
               this.handleValueEnd(output);
               break;
            case LinToken.CompoundStart compoundStart:
               output.append('{');
               this.stateStack.addLast(new LinSnbtWriter.WriteState.Compound(false));
               break;
            case LinToken.CompoundEnd compoundEnd:
               output.append('}');
               this.stateStack.removeLast();
               this.handleValueEnd(output);
               break;
            case LinToken.Double(double var63):
               output.append(String.valueOf(var63)).append('D');
               this.handleValueEnd(output);
               break;
            case LinToken.Float(float var64):
               output.append(String.valueOf(var64)).append('F');
               this.handleValueEnd(output);
               break;
            case LinToken.IntArrayStart intArrayStart:
               output.append("[I;");
               break;
            case LinToken.IntArrayContent(IntBuffer var84):
               IntBuffer var65 = var84;
               IntBuffer buffer = var65;
               if (state instanceof LinSnbtWriter.WriteState.WritingArray) {
                  output.append(',');
               } else {
                  this.stateStack.addLast(new LinSnbtWriter.WriteState.WritingArray());
               }

               while (buffer.hasRemaining()) {
                  output.append(String.valueOf(buffer.get()));
                  if (buffer.hasRemaining()) {
                     output.append(',');
                  }
               }
               break;
            case LinToken.IntArrayEnd intArrayEnd:
               if (state instanceof LinSnbtWriter.WriteState.WritingArray) {
                  this.stateStack.removeLast();
               }

               output.append(']');
               this.handleValueEnd(output);
               break;
            case LinToken.Int(int var66):
               output.append(String.valueOf(var66));
               this.handleValueEnd(output);
               break;
            case LinToken.ListStart var29:
               LinToken.ListStart var78 = var29;

               try {
                  var79 = var78.size();
               } catch (Throwable var50) {
                  throw new MatchException(var50.toString(), var50);
               }

               OptionalInt var67 = var79;
               var78 = var29;

               try {
                  var78.elementId();
               } catch (Throwable var49) {
                  throw new MatchException(var49.toString(), var49);
               }

               output.append('[');
               this.stateStack.addLast(new LinSnbtWriter.WriteState.List(var67.orElseThrow()));
               break;
            case LinToken.ListEnd listEnd:
               output.append(']');
               this.stateStack.removeLast();
               this.handleValueEnd(output);
               break;
            case LinToken.LongArrayStart longArrayStart:
               output.append("[L;");
               break;
            case LinToken.LongArrayContent(LongBuffer var77):
               LongBuffer var68 = var77;
               LongBuffer buffer = var68;
               if (state instanceof LinSnbtWriter.WriteState.WritingArray) {
                  output.append(',');
               } else {
                  this.stateStack.addLast(new LinSnbtWriter.WriteState.WritingArray());
               }

               while (buffer.hasRemaining()) {
                  output.append(String.valueOf(buffer.get())).append('L');
                  if (buffer.hasRemaining()) {
                     output.append(',');
                  }
               }
               break;
            case LinToken.LongArrayEnd longArrayEnd:
               if (state instanceof LinSnbtWriter.WriteState.WritingArray) {
                  this.stateStack.removeLast();
               }

               output.append(']');
               this.handleValueEnd(output);
               break;
            case LinToken.Long(long var69):
               output.append(String.valueOf(var69)).append('L');
               this.handleValueEnd(output);
               break;
            case LinToken.Short(short var70):
               output.append(String.valueOf((int)var70)).append('S');
               this.handleValueEnd(output);
               break;
            case LinToken.String(String var44):
               output.append(Elusion.escapeIfNeeded(var44));
               this.handleValueEnd(output);
               break;
            default:
               throw new NbtWriteException("Unknown token: " + token);
         }
      }
   }

   private void handleValueEnd(Appendable output) throws IOException {
      LinSnbtWriter.WriteState state = this.stateStack.pollLast();
      switch (state) {
         case null:
            break;
         case LinSnbtWriter.WriteState.List(int var10):
            int var9 = var10;
            this.stateStack.addLast(new LinSnbtWriter.WriteState.List(var9 - 1));
            if (var9 - 1 > 0) {
               output.append(',');
            }
            break;
         case LinSnbtWriter.WriteState.Compound compound:
            this.stateStack.addLast(new LinSnbtWriter.WriteState.Compound(true));
            break;
         default:
            throw new NbtWriteException("Unexpected state: " + state);
      }
   }

   private void replaceLast(LinSnbtWriter.WriteState state) {
      this.stateStack.removeLast();
      this.stateStack.addLast(state);
   }

   private sealed interface WriteState permits LinSnbtWriter.WriteState.List, LinSnbtWriter.WriteState.Compound, LinSnbtWriter.WriteState.WritingArray {
      public record Compound(boolean hasPrevious) implements LinSnbtWriter.WriteState {
      }

      public record List(int remainingValues) implements LinSnbtWriter.WriteState {
      }

      public record WritingArray() implements LinSnbtWriter.WriteState {
      }
   }
}
