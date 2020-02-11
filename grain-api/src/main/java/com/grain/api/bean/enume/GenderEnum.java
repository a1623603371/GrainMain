package com.grain.api.bean.enume;

import com.fasterxml.jackson.annotation.JsonValue;
import com.grain.api.service.constant.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Administrator
 */
@Getter
@AllArgsConstructor
public enum GenderEnum implements BaseEnum {

    UNKNOWN("未知"), MAN("男"), WOMAN("女");
    @Setter
    private String gender;


    @Override
    @JsonValue
    public int getOrdinal() {
        return this.ordinal();
    }
}
