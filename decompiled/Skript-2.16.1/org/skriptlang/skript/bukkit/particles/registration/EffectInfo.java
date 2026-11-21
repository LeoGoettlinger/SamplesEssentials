package org.skriptlang.skript.bukkit.particles.registration;

public record EffectInfo<E, D>(E effect, String pattern, DataSupplier<D> dataSupplier, ToString toStringFunction) {
}
