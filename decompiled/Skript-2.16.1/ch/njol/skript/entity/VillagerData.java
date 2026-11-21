package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import java.util.Collections;
import java.util.Objects;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VillagerData extends EntityData<Villager> {
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
         {"villager", null},
         {"unemployed", Profession.NONE},
         {"armorer", Profession.ARMORER},
         {"butcher", Profession.BUTCHER},
         {"cartographer", Profession.CARTOGRAPHER},
         {"cleric", Profession.CLERIC},
         {"farmer", Profession.FARMER},
         {"fisherman", Profession.FISHERMAN},
         {"fletcher", Profession.FLETCHER},
         {"leatherworker", Profession.LEATHERWORKER},
         {"librarian", Profession.LIBRARIAN},
         {"mason", Profession.MASON},
         {"nitwit", Profession.NITWIT},
         {"shepherd", Profession.SHEPHERD},
         {"toolsmith", Profession.TOOLSMITH},
         {"weaponsmith", Profession.WEAPONSMITH}
      }
   );
   @Nullable
   private Profession profession = null;

   public VillagerData() {
   }

   public VillagerData(@Nullable Profession profession) {
      this.profession = profession;
      super.codeNameIndex = PATTERNS.getMatchedPattern(profession, 0).orElse(0);
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.profession = PATTERNS.getInfo(matchedCodeName);
      return true;
   }

   protected boolean init(@Nullable Class<? extends Villager> villagerClass, @Nullable Villager villager) {
      if (villager != null) {
         this.profession = villager.getProfession();
         super.codeNameIndex = PATTERNS.getMatchedPattern(this.profession, 0).orElse(0);
      }

      return true;
   }

   public void set(Villager villager) {
      Profession profession = this.profession;
      if (profession == null) {
         profession = CollectionUtils.getRandom(PROFESSIONS);
      }

      assert profession != null;

      villager.setProfession(profession);
      if (profession == Profession.NITWIT) {
         villager.setRecipes(Collections.emptyList());
      }
   }

   protected boolean match(Villager villager) {
      return this.dataMatch(this.profession, villager.getProfession());
   }

   @Override
   public Class<? extends Villager> getType() {
      return Villager.class;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new VillagerData();
   }

   @Override
   protected int hashCode_i() {
      return Objects.hashCode(this.profession);
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return entityData instanceof VillagerData other ? this.profession == other.profession : false;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      return entityData instanceof VillagerData other ? this.dataMatch(this.profession, other.profession) : false;
   }

   static {
      Variables.yggdrasil.registerSingleClass(Profession.class, "Villager.Profession");
      EntityData.register(VillagerData.class, "villager", Villager.class, 0, PATTERNS.getPatterns());
   }
}
