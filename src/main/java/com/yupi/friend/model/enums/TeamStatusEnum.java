package com.yupi.friend.model.enums;

public enum TeamStatusEnum {

    PUBLIC(0, "公开"),
    PRIVATE(1, "私有"),
    SECRET(2, "加密");


    private Integer value;

    private String text;


    public static TeamStatusEnum getEnumByValue(Integer value){
        if(value == null){
            return null;
        }

        for (TeamStatusEnum teamStatusEnum : TeamStatusEnum.values()) {
            if(teamStatusEnum.value.equals(value)){
                return teamStatusEnum;
            }
        }

        return null;
    }

    TeamStatusEnum(Integer value, String text) {
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
