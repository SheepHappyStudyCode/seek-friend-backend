package com.yupi.friend.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Objects;

@Slf4j
public class LuaUtils {
    //读取Lua脚本文件
    private String readLua(File file) {
        StringBuilder sbf = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String temp;
            while (Objects.nonNull(temp = reader.readLine())) {
                sbf.append(temp);
                sbf.append('\n');
            }
            return sbf.toString();
        } catch (FileNotFoundException e) {
            log.error("[{}]文件不存在", file.getPath());
        } catch (IOException e) {
            log.error("[{}]文件读取异常", file.getPath());
        }
        return null;
    }


}
