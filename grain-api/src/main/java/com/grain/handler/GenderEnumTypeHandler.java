package com.grain.handler;

import com.grain.api.bean.enume.GenderEnum;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;

public class GenderEnumTypeHandler extends EnumOrdinalTypeHandler<GenderEnum> {
    public GenderEnumTypeHandler(Class type) {
        super(type);
    }
}
