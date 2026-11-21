package ch.njol.skript.lang;

import org.bukkit.event.Event;

@FunctionalInterface
public interface EventRestrictedSyntax {
   Class<? extends Event>[] supportedEvents();
}
