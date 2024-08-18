package com.yupi.friend.common;

import lombok.Data;

@Data
public class PageQuery {
    protected long current = 1;

    protected long pageSize = 10;
}
