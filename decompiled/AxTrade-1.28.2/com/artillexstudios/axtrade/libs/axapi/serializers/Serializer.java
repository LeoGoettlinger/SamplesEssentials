package com.artillexstudios.axtrade.libs.axapi.serializers;

public interface Serializer<T, Z> {
   Z serialize(T object);

   T deserialize(Z value);
}
