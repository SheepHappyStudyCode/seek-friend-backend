use friend;

create table if not exists  user
(
    id              bigint auto_increment comment 'id'
        primary key,
    username        varchar(256)                       null comment '用户昵称',
    userDescription varchar(512)                       null comment '用户描述',
    userAccount     varchar(256)                       null comment '账号',
    avatarUrl       varchar(1024)                      null comment '用户头像',
    gender          tinyint                            null comment ' 性别 0 - 男  1 - 女',
    secretKey       varchar(256)                       not null,
    userPassword    varchar(512)                       not null comment '密码',
    qq              varchar(64)                        null comment 'qq号',
    phone           varchar(128)                       null comment '电话',
    email           varchar(512)                       null comment '邮箱',
    userStatus      int      default 0                 not null comment '状态 0 - 正常',
    createTime      datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime      datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete        tinyint  default 0                 not null comment '是否删除',
    userRole        int      default 0                 not null comment '用户角色 0 - 普通用户 1 - 管理员',
    tags            varchar(1024)                      null comment '标签 json 数组'
)
    comment '用户';


create table if not exists  user_team
(
    id         bigint auto_increment comment 'id'
        primary key,
    userId     bigint                             null comment '用户id',
    teamId     bigint                             null comment '队伍id',
    joinTime   datetime default CURRENT_TIMESTAMP null comment '用户加入队伍时间',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
)
    comment '用户-队伍关系表';


create table if not exists  team
(
    id          bigint auto_increment comment 'id'
        primary key,
    name        varchar(256)                       not null comment '队伍名称',
    description varchar(1024)                      null comment '队伍描述',
    maxNum      int      default 1                 null comment '队伍的最大人数',
    userId      bigint                             not null comment '创建人id',
    expireTime  datetime                           null comment '队伍过期时间',
    status      int      default 0                 null comment '队伍类型 0 - 公开， 1 - 私密， 2 - 加密',
    password    varchar(512)                       null comment '加密队伍的密码',
    createTime  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除'
)
    comment '队伍';

create table if not exists post
(
    id          bigint auto_increment comment 'id'
        primary key,
    title        varchar(128)                        null comment '帖子标题',
    content varchar(2048)                      null comment '帖子内容',
    category        int      default 0                 null comment '帖子分类',
    createUserId      bigint                             not null comment '创建人id',
    createTime  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除'
)
    comment '帖子';

create table if not exists comment
(
    id          bigint auto_increment comment 'id'
        primary key,
    postId      bigint                             not null comment '帖子id',
    createUserId      bigint                             not null comment '创建人id',
    content varchar(512)                      null comment '评论内容',
    hot tinyint                                not null default 0 comment '热度 0-普通 1-热评',
    createTime  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除'
)
    comment '评论';

create table if not exists comment_answer
(
    id          bigint auto_increment comment 'id'
        primary key,
    commentId      bigint                             not null comment '评论id',
    createUserId      bigint                             not null comment '创建人id',
    content varchar(512)                      null comment '回复内容',
    hot tinyint                                not null default 0 comment '热度 0-普通 1-热评',
    type tinyint                                    not null default 0 comment '回复类型 0-普通 1-二级回复',
    toAnswerId bigint                               null comment '如果是二级回复， 回复的answerId',
    createTime  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除'
)
    comment '评论回复表';

INSERT INTO friend.user (id, username, userDescription, userAccount, avatarUrl, gender, secretKey, userPassword, qq, phone, email, userStatus, createTime, updateTime, isDelete, userRole, tags) VALUES (1, 'yupi1234', '我爱吃鱼，所以我叫鱼皮', 'yupi', 'https://web-friend-sheephappy.oss-cn-hangzhou.aliyuncs.com/a36e4121-6574-4014-a04c-ed6c1b556fd7_20221114215948_1cf96.thumb.400_0.jpeg', 0, '', 'b0dd3697a192885d7c055db46155b26a', '12345678', '13180148958', '2870@yupi.com', 0, '2024-07-26 10:51:35', '2024-08-17 23:10:02', 0, 0, '["movies","男","java"]');
INSERT INTO friend.user (id, username, userDescription, userAccount, avatarUrl, gender, secretKey, userPassword, qq, phone, email, userStatus, createTime, updateTime, isDelete, userRole, tags) VALUES (2, 'sheephappy01', '我要看一辈子的少女乐队动画', 'sheephappy', 'https://web-friend-sheephappy.oss-cn-hangzhou.aliyuncs.com/f3970621-ca72-4085-8949-1954a282cfbc_20200409235851_xwrjt.jpg', 0, '79518a1cc45ec8aa420d2b694cbcda3ada15be76d5078c67812af0b9c48efb8212a44b19541d3c824eb9a0d4f141c4d9e16bc0d7531daf2e577654db40404997', 'da34826c9b682c5cf480c2d8086f5d73', '2870839006', '', null, 0, '2024-08-13 17:35:12', '2024-08-27 20:24:40', 0, 0, '["python","java"]');
INSERT INTO friend.user (id, username, userDescription, userAccount, avatarUrl, gender, secretKey, userPassword, qq, phone, email, userStatus, createTime, updateTime, isDelete, userRole, tags) VALUES (3, 'Alice', 'Loves music and traveling.', 'alice123', 'https://picture.gptkong.com/20240729/20585eb5ece1594408bd6526c5004906bb.jpeg', 1, '', 'b0dd3697a192885d7c055db46155b26a', null, '+1234567890', 'alice@example.com', 0, '2024-07-29 20:43:35', '2024-08-05 17:11:23', 0, 0, '["sports", "movies", "男"]');
INSERT INTO friend.user (id, username, userDescription, userAccount, avatarUrl, gender, secretKey, userPassword, qq, phone, email, userStatus, createTime, updateTime, isDelete, userRole, tags) VALUES (4, 'Bob', 'Enjoys playing sports and watching movies.', 'bob123', 'https://ts1.cn.mm.bing.net/th/id/R-C.cf6fdc8ce5cf10ef8a1e3d25ba1cf5af?rik=77GnZ5J6xVI9iQ&riu=http%3a%2f%2fi2.hdslb.com%2fbfs%2farchive%2f24418dc1f90f56a818978d849f53695ae545ee69.jpg&ehk=%2bFoPa4qMssXgghxBCqryhm8lhmLvWQ8f%2bJpw0eZFO8Y%3d&risl=&pid=ImgRaw&r=0', 0, '', 'b0dd3697a192885d7c055db46155b26a', null, '+0987654321', 'bob@example.com', 0, '2024-07-29 20:43:35', '2024-08-05 17:04:20', 0, 0, '["sports", "java", "男"]');
INSERT INTO friend.user (id, username, userDescription, userAccount, avatarUrl, gender, secretKey, userPassword, qq, phone, email, userStatus, createTime, updateTime, isDelete, userRole, tags) VALUES (5, 'Charlie', 'Likes reading books and taking photos.', 'charlie123', 'https://picture.gptkong.com/20240729/20585eb5ece1594408bd6526c5004906bb.jpeg', 1, '', 'b0dd3697a192885d7c055db46155b26a', null, '+1112223333', 'charlie@example.com', 0, '2024-07-29 20:43:35', '2024-08-05 17:04:20', 0, 0, '["reading", "sports", "java", "女"]');
INSERT INTO friend.user (id, username, userDescription, userAccount, avatarUrl, gender, secretKey, userPassword, qq, phone, email, userStatus, createTime, updateTime, isDelete, userRole, tags) VALUES (6, 'Diana', 'Passionate about cooking and exploring nature.', 'diana123', 'https://ts1.cn.mm.bing.net/th/id/R-C.cf6fdc8ce5cf10ef8a1e3d25ba1cf5af?rik=77GnZ5J6xVI9iQ&riu=http%3a%2f%2fi2.hdslb.com%2fbfs%2farchive%2f24418dc1f90f56a818978d849f53695ae545ee69.jpg&ehk=%2bFoPa4qMssXgghxBCqryhm8lhmLvWQ8f%2bJpw0eZFO8Y%3d&risl=&pid=ImgRaw&r=0', 0, '', 'b0dd3697a192885d7c055db46155b26a', null, '+4445556666', 'diana@example.com', 0, '2024-07-29 20:43:35', '2024-08-05 17:04:20', 0, 0, '["movies", "hiking", "男"]');
INSERT INTO friend.user (id, username, userDescription, userAccount, avatarUrl, gender, secretKey, userPassword, qq, phone, email, userStatus, createTime, updateTime, isDelete, userRole, tags) VALUES (7, 'Eve', 'Art enthusiast and loves gardening.', 'eve123', 'https://picture.gptkong.com/20240729/20585eb5ece1594408bd6526c5004906bb.jpeg', 1, '', 'b0dd3697a192885d7c055db46155b26a', null, '+2223334444', 'eve@example.com', 0, '2024-07-29 20:43:35', '2024-07-29 21:13:47', 0, 0, '["art", "gardening", "女"]');
INSERT INTO friend.user (id, username, userDescription, userAccount, avatarUrl, gender, secretKey, userPassword, qq, phone, email, userStatus, createTime, updateTime, isDelete, userRole, tags) VALUES (8, 'fakeUsre', '我不是小黑子 我不是小黑子 我不是小黑子', 'fakeUsre1', 'https://ts1.cn.mm.bing.net/th/id/R-C.cf6fdc8ce5cf10ef8a1e3d25ba1cf5af?rik=77GnZ5J6xVI9iQ&riu=http%3a%2f%2fi2.hdslb.com%2fbfs%2farchive%2f24418dc1f90f56a818978d849f53695ae545ee69.jpg&ehk=%2bFoPa4qMssXgghxBCqryhm8lhmLvWQ8f%2bJpw0eZFO8Y%3d&risl=&pid=ImgRaw&r=0', 0, '', 'b0dd3697a192885d7c055db46155b26a', null, '12345678', 'eve@example.com', 0, '2024-07-31 17:17:48', '2024-08-13 20:45:15', 0, 0, '["reading", "photography", "java", "女"]');
INSERT INTO friend.user (id, username, userDescription, userAccount, avatarUrl, gender, secretKey, userPassword, qq, phone, email, userStatus, createTime, updateTime, isDelete, userRole, tags) VALUES (9, 'fakeUsre', '我不是小黑子 我不是小黑子 我不是小黑子', 'fakeUsre2', 'https://ts1.cn.mm.bing.net/th/id/R-C.cf6fdc8ce5cf10ef8a1e3d25ba1cf5af?rik=77GnZ5J6xVI9iQ&riu=http%3a%2f%2fi2.hdslb.com%2fbfs%2farchive%2f24418dc1f90f56a818978d849f53695ae545ee69.jpg&ehk=%2bFoPa4qMssXgghxBCqryhm8lhmLvWQ8f%2bJpw0eZFO8Y%3d&risl=&pid=ImgRaw&r=0', 0, '', 'b0dd3697a192885d7c055db46155b26a', null, '12345678', 'eve@example.com', 0, '2024-07-31 17:17:48', '2024-08-13 20:45:15', 0, 0, '["reading", "photography", "java", "女"]');
INSERT INTO friend.user (id, username, userDescription, userAccount, avatarUrl, gender, secretKey, userPassword, qq, phone, email, userStatus, createTime, updateTime, isDelete, userRole, tags) VALUES (10, 'fakeUsre', '我不是小黑子 我不是小黑子 我不是小黑子', 'fakeUsre', 'https://ts1.cn.mm.bing.net/th/id/R-C.cf6fdc8ce5cf10ef8a1e3d25ba1cf5af?rik=77GnZ5J6xVI9iQ&riu=http%3a%2f%2fi2.hdslb.com%2fbfs%2farchive%2f24418dc1f90f56a818978d849f53695ae545ee69.jpg&ehk=%2bFoPa4qMssXgghxBCqryhm8lhmLvWQ8f%2bJpw0eZFO8Y%3d&risl=&pid=ImgRaw&r=0', 0, '', 'b0dd3697a192885d7c055db46155b26a', null, '12345678', 'eve@example.com', 0, '2024-07-31 17:17:48', '2024-07-31 17:17:48', 0, 0, '["reading", "photography", "java", "女"]');
INSERT INTO friend.user (id, username, userDescription, userAccount, avatarUrl, gender, secretKey, userPassword, qq, phone, email, userStatus, createTime, updateTime, isDelete, userRole, tags) VALUES (602013, null, null, 'testMan', null, null, '79518a1cc45ec8aa420d2b694cbcda3ada15be76d5078c67812af0b9c48efb8212a44b19541d3c824eb9a0d4f141c4d9e16bc0d7531daf2e577654db40404997', 'da34826c9b682c5cf480c2d8086f5d73', null, null, null, 0, '2024-08-27 20:20:42', '2024-08-27 20:20:42', 0, 0, null);
