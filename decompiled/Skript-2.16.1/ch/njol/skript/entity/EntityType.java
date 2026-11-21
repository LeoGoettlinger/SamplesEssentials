package ch.njol.skript.entity;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.YggdrasilSerializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.yggdrasil.YggdrasilSerializable;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class EntityType implements Cloneable, YggdrasilSerializable {
   public int amount = -1;
   public final EntityData<?> data;

   private EntityType() {
      this.data = null;
   }

   public EntityType(EntityData<?> data, int amount) {
      assert data != null;

      this.data = data;
      this.amount = amount;
   }

   public EntityType(Class<? extends Entity> c, int amount) {
      assert c != null;

      this.data = EntityData.fromClass(c);
      this.amount = amount;
   }

   public EntityType(Entity e) {
      this.data = EntityData.fromEntity(e);
   }

   public EntityType(EntityType other) {
      this.amount = other.amount;
      this.data = other.data;
   }

   public boolean isInstance(Entity entity) {
      return this.data.isInstance(entity);
   }

   @Override
   public String toString() {
      return this.getAmount() == 1 ? this.data.toString(0) : this.amount + " " + this.data.toString(1);
   }

   public String toString(int flags) {
      return this.getAmount() == 1 ? this.data.toString(flags) : this.amount + " " + this.data.toString(flags | 1);
   }

   public int getAmount() {
      return this.amount == -1 ? 1 : this.amount;
   }

   public boolean sameType(EntityType other) {
      return this.data.equals(other.data);
   }

   @Nullable
   public static EntityType parse(String s) {
      assert s != null && s.length() != 0;

      int amount = -1;
      if (s.matches("\\d+ .+")) {
         amount = Utils.parseInt(s.split(" ", 2)[0]);
         s = s.split(" ", 2)[1];
      } else if (s.matches("(?i)an? .+")) {
         s = s.split(" ", 2)[1];
      }

      EntityData<?> data = EntityData.parseWithoutIndefiniteArticle(s);
      return data == null ? null : new EntityType(data, amount);
   }

   public EntityType clone() {
      return new EntityType(this);
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
      result = 31 * result + this.amount;
      return 31 * result + this.data.hashCode();
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (!(obj instanceof EntityType other)) {
         return false;
      } else {
         return this.amount != other.amount ? false : this.data.equals(other.data);
      }
   }

   static {
      Classes.registerClass(
         new ClassInfo<>(EntityType.class, "entitytype")
            .name("Entity Type with Amount")
            .description(
               "An <a href='#entitydata'>entity type</a> with an amount, e.g. '2 zombies'. I might remove this type in the future and make a more general 'type' type, i.e. a type that has a number and a type."
            )
            .usage("&lt;<a href='#number'>number</a>&gt; &lt;entity type&gt;")
            .examples("spawn 5 creepers behind the player")
            .since("1.3")
            .defaultExpression(new SimpleLiteral<>(new EntityType(Entity.class, 1), true))
            .parser(new Parser<EntityType>() {
               @Nullable
               public EntityType parse(String s, ParseContext context) {
                  return EntityType.parse(s);
               }

               public String toString(EntityType t, int flags) {
                  return t.toString(flags);
               }

               public String toVariableNameString(EntityType t) {
                  return "entitytype:" + t.toString();
               }
            })
            .serializer(new YggdrasilSerializer<>())
      );
   }
}
