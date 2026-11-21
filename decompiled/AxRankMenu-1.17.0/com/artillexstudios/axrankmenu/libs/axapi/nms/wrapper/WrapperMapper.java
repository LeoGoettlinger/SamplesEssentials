package com.artillexstudios.axrankmenu.libs.axapi.nms.wrapper;

public interface WrapperMapper<T extends Wrapper<?>> {
   T map(Object object);
}
