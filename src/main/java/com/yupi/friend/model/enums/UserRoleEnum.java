package com.yupi.friend.model.enums;

public enum UserRoleEnum {

    DEFAULT_ROLE(0, "普通用户"),
    ADMIN(1, "管理员");


    private Integer value;

    private String text;


    public static UserRoleEnum getEnumByValue(Integer value){
        if(value == null){
            return null;
        }

        for (UserRoleEnum teamStatusEnum : UserRoleEnum.values()) {
            if(teamStatusEnum.value.equals(value)){
                return teamStatusEnum;
            }
        }

        return null;
    }

    UserRoleEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
