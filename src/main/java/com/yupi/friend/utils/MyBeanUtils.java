package com.yupi.friend.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public class MyBeanUtils {

    public static <T, S> List<T> copyList(List<S> sourceList, Class<T> targetClass) {
        ArrayList<T> tArrayList = new ArrayList<>();
        for (S source : sourceList) {
            T t = null;
            try {
                t = targetClass.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            BeanUtils.copyProperties(source, t);
            tArrayList.add(t);
        }

        return tArrayList;
    }
}
