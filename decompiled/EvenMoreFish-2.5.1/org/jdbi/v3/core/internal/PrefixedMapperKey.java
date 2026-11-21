package org.jdbi.v3.core.internal;

import java.lang.reflect.Type;

public record PrefixedMapperKey(Type type, String prefix) {
}
