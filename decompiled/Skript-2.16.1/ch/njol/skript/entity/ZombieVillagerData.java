package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Patterns;
import ch.njol.util.coll.CollectionUtils;
import java.util.Objects;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.entity.Villager.Profession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ZombieVillagerData extends EntityData<ZombieVillager> {
   private static final Profession[] PROFESSIONS = new Profession[]{
      Profession.NONE,
      Profession.ARMORER,
      Profession.BUTCHER,
      Profession.CARTOGRAPHER,
      Profession.CLERIC,
      Profession.FARMER,
      Profession.FISHERMAN,
      Profession.FLETCHER,
      Profession.LEATHERWORKER,
      Profession.LIBRARIAN,
      Profession.MASON,
      Profession.NITWIT,
      Profession.SHEPHERD,
      Profession.TOOLSMITH,
      Profession.WEAPONSMITH
   };
   private static final Patterns<Profession> PATTERNS = new Patterns<>(
      new Object[][]{
         {"zombie villager", null},
         {"zombie normal", Profession.NONE},
         {"zombie armorer", Profession.ARMORER},
         {"zombie butcher", Profession.BUTCHER},
         {"zombie cartographer", Profession.CARTOGRAPHER},
         {"zombie cleric", Profession.CLERIC},
         {"zombie farmer", Profession.FARMER},
         {"zombie fisherman", Profession.FISHERMAN},
         {"zombie fletcher", Profession.FLETCHER},
         {"zombie leatherworker", Profession.LEATHERWORKER},
         {"zombie librarian", Profession.LIBRARIAN},
         {"zombie mason", Profession.MASON},
         {"zombie nitwit", Profession.NITWIT},
         {"zombie shepherd", Profession.SHEPHERD},
         {"zombie toolsmith", Profession.TOOLSMITH},
         {"zombie weaponsmith", Profession.WEAPONSMITH}
      }
   );
   @Nullable
   private Profession profession = null;

   public ZombieVillagerData() {
   }

   public ZombieVillagerData(@Nullable Profession profession) {
      this.profession = profession;
      super.codeNameIndex = PATTERNS.getMatchedPattern(profession, 0).orElse(0);
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.profession = PATTERNS.getInfo(matchedCodeName);
      return true;
   }

   protected boolean init(@Nullable Class<? extends ZombieVillager> entityClass, @Nullable ZombieVillager zombieVillager) {
      if (zombieVillager != null) {
         this.profession = zombieVillager.getVillagerProfession();
         super.codeNameIndex = PATTERNS.getMatchedPattern(this.profession, 0).orElse(0);
      }

      return true;
   }

   public void set(ZombieVillager zombieVillager) {
      Profession profession = this.profession;
      if (profession == null) {
         profession = CollectionUtils.getRandom(PROFESSIONS);
      }

      assert profession != null;

      zombieVillager.setVillagerProfession(profession);
   }

   protected boolean match(ZombieVillager zombieVillager) {
      return this.dataMatch(this.profession, zombieVillager.getVillagerProfession());
   }

   @Override
   public Class<? extends ZombieVillager> getType() {
      return ZombieVillager.class;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new ZombieVillagerData();
   }

   @Override
   protected int hashCode_i() {
      return Objects.hashCode(this.profession);
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return entityData instanceof ZombieVillagerData other ? this.profession == other.profession : false;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      return entityData instanceof ZombieVillagerData other ? this.dataMatch(this.profession, other.profession) : false;
   }

   static {
      EntityData.register(ZombieVillagerData.class, "zombie villager", ZombieVillager.class, 0, PATTERNS.getPatterns());
   }
}
