package org.skriptlang.skript.bukkit.bossbar.elements.conditions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BossBar;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Has Boss Bar Flag")
@Description({"Checks whether a boss bar has a specific flag.\nThere are three flags:\n- 'darken the sky'\n- 'create fog'\n- 'play boss music'\n"})
@Example("if {_mybar} does darken the sky:\n\tbroadcast \"It's getting dark around here..\"\n")
@Since({"2.16"})
public class CondHasBossBarFlag extends Condition {
   private static final Patterns<BarFlag> PATTERNS = new Patterns<>(
      new Object[][]{
         {"%bossbars% [do[es]] darken[s] the sky", BarFlag.DARKEN_SKY},
         {"%bossbars% (doesn't|does not|do not|don't) darken the sky", BarFlag.DARKEN_SKY},
         {"%bossbars% [do[es]] create[s] fog", BarFlag.CREATE_FOG},
         {"%bossbars% (doesn't|does not|do not|don't) create fog", BarFlag.CREATE_FOG},
         {"%bossbars% [do[es]] play[s] boss music", BarFlag.PLAY_BOSS_MUSIC},
         {"%bossbars% (doesn't|does not|do not|don't) play boss music", BarFlag.PLAY_BOSS_MUSIC}
      }
   );
   private Expression<BossBar> bars;
   private BarFlag flag;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.CONDITION, SyntaxInfo.builder(CondHasBossBarFlag.class).addPatterns(PATTERNS.getPatterns()).supplier(CondHasBossBarFlag::new).build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.flag = PATTERNS.getInfo(matchedPattern);
      this.bars = (Expression<BossBar>)exprs[0];
      this.setNegated(matchedPattern % 2 != 0);
      return true;
   }

   @Override
   public boolean check(Event event) {
      return this.bars.check(event, bar -> bar.hasFlag(this.flag), this.isNegated());
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return new SyntaxStringBuilder(event, debug).append(this.bars).append(this.isNegated() ? "does not" : "does").append(switch (this.flag) {
         case DARKEN_SKY -> "darken the sky";
         case CREATE_FOG -> "create fog";
         case PLAY_BOSS_MUSIC -> "play boss music";
         default -> throw new MatchException(null, null);
      }).toString();
   }
}
