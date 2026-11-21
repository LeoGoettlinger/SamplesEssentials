package com.viaversion.viaversion.api.type.types;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Types;
import java.util.List;

public final class FakeEnumType extends VarIntType {
   private final FakeEnumType.Entry[] entries;

   public FakeEnumType(List<String> initialNames, FakeEnumType.Entry... remainingEntries) {
      this.entries = new FakeEnumType.Entry[initialNames.size() + remainingEntries.length];

      for (int i = 0; i < initialNames.size(); i++) {
         this.entries[i] = FakeEnumType.Entry.of(i, initialNames.get(i));
      }

      System.arraycopy(remainingEntries, 0, this.entries, initialNames.size(), remainingEntries.length);
   }

   public FakeEnumType(FakeEnumType.Entry... entries) {
      this.entries = entries;
   }

   @Override
   public void write(Ops ops, Integer value) {
      FakeEnumType.Entry entry = null;

      for (FakeEnumType.Entry e : this.entries) {
         if (e.id == value) {
            entry = e;
            break;
         }
      }

      Types.STRING.write(ops, entry != null ? entry.name : this.entries[0].name);
   }

   public FakeEnumType.Entry[] entries() {
      return this.entries;
   }

   public record Entry(int id, String name) {
      public static FakeEnumType.Entry of(int id, String name) {
         return new FakeEnumType.Entry(id, name);
      }
   }
}
