package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.util.CachedServerIcon;
import org.jetbrains.annotations.Nullable;

@Name("Server Icon")
@Description({"Icon of the server in the server list. Can be set to an icon that loaded using the", "<a href='#EffLoadServerIcon'>load server icon</a> effect,", "or can be reset to the default icon in a <a href='#server_list_ping'>server list ping</a>.", "'default server icon' returns the default server icon (server-icon.png) always and cannot be changed."})
@Example("on script load:\n\tset {server-icons::default} to the default server icon\n")
@Since({"2.3"})
public class ExprServerIcon extends SimpleExpression<CachedServerIcon> {
   private boolean isServerPingEvent;
   private boolean isDefault;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.isServerPingEvent = this.getParser().isCurrentEvent(PaperServerListPingEvent.class);
      this.isDefault = parseResult.mark == 0 && !this.isServerPingEvent || parseResult.mark == 1;
      if (!this.isServerPingEvent && !this.isDefault) {
         Skript.error("The 'shown' server icon expression can't be used outside of a server list ping event");
         return false;
      } else {
         return true;
      }
   }

   @Nullable
   public CachedServerIcon[] get(Event e) {
      CachedServerIcon icon;
      if (this.isServerPingEvent && !this.isDefault) {
         if (!(e instanceof PaperServerListPingEvent)) {
            return null;
         }

         icon = ((PaperServerListPingEvent)e).getServerIcon();
      } else {
         icon = Bukkit.getServerIcon();
      }

      return icon != null && icon.getData() != null ? CollectionUtils.array(icon) : null;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (this.isServerPingEvent && !this.isDefault) {
         if (this.getParser().getHasDelayBefore().isTrue()) {
            Skript.error("Can't change the server icon anymore after the server list ping event has already passed");
            return null;
         }

         if (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.RESET) {
            return CollectionUtils.array(CachedServerIcon.class);
         }
      }

      return null;
   }

   @Override
   public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (e instanceof PaperServerListPingEvent event) {
         switch (mode) {
            case SET:
               event.setServerIcon((CachedServerIcon)delta[0]);
               break;
            case RESET:
               event.setServerIcon(Bukkit.getServerIcon());
         }
      }
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends CachedServerIcon> getReturnType() {
      return CachedServerIcon.class;
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "the " + (this.isServerPingEvent && !this.isDefault ? "shown" : "default") + " server icon";
   }

   static {
      Skript.registerExpression(ExprServerIcon.class, CachedServerIcon.class, ExpressionType.PROPERTY, "[the] [(1¦(default)|2¦(shown|sent))] [server] icon");
   }
}
