package net.kyori.adventure.key;

public interface Namespaced {
   @KeyPattern.Namespace
   String namespace();
}
