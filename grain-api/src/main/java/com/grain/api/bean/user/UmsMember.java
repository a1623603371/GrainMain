package com.grain.api.bean.user;

import ch.qos.logback.core.db.dialect.MySQLDialect;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.grain.api.bean.enume.GenderEnum;
import com.grain.handler.GenderEnumTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.JdbcType;
import tk.mybatis.mapper.annotation.ColumnType;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Data
public class UmsMember implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String memberLevelId;
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private int status;
    private Date createTime;
    private String icon;
    @ColumnType(jdbcType = JdbcType.BIGINT, typeHandler = GenderEnumTypeHandler.class)
    private GenderEnum gender;
    private Date birthday;
    private String city;
    private String job;
    private String personalizedSignature;
    private String sourceType;
    private int integration;
    private int growth;
    private int luckeyCount;
    private int historyIntegration;
    private String sourceUid;
    private String accessToken;
    private String accessCode;


}
