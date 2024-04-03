create table tag
(
    id         bigint not null
        primary key,
    tagName    varchar(256) null comment '用户昵称',
    userId     bigint null comment '用户id',
    parentId   bigint null comment '父标签id',
    isParent   tinyint null comment '是否是父标签 0 - 不是   1 - 是',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0 null comment '是否删除',
    constraint uniIndex_tagName
        unique (tagName)
) comment '标签表';

create index idx_userId
    on tag (userId);

create table user
(
    id           bigint not null
        primary key,
    username     varchar(256) null comment '用户昵称',
    userAccount  varchar(256) null comment '账号',
    avatarUrl    varchar(1024) null comment '用户头像',
    gender       tinyint null comment '用户性别',
    userPassword varchar(512) null comment '用户密码',
    phone        varchar(512) null comment '电话',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    userStatus   int      default 0 null comment ' 用户状态 0 - 正常 ',
    isDelete     tinyint  default 0 null comment '是否删除',
    email        varchar(512) null comment '邮箱',
    userRole     int      default 0 null,
    code         varchar(512) null,
    tags         varchar(1024) null comment '标签列表'
) comment '用户表';

-- user表添加字段
ALTER TABLE user
    ADD COLUMN teamIds VARCHAR(512) NULL COMMENT '队伍id列表',
    ADD COLUMN userIds VARCHAR(512) NULL COMMENT '添加的好友';

-- 添加个人简介字段
alter table user
    add profile varchar(512) null;

-- 修改 user 表主键id
alter table user
    modify id bigint auto_increment;

alter table user
    auto_increment = 1;

-- 创建队伍表
create table team
(
    id          bigint auto_increment comment 'id'
        primary key,
    name        varchar(256)       not null comment '队伍名称',
    description varchar(1024) null comment '描述',
    maxNum      int      default 1 not null comment '最大人数',
    expireTime  datetime null comment '过期时间',
    userId      bigint comment '用户id',
    status      int      default 0 not null comment '0 - 公开，1 - 私有，2 - 加密',
    password    varchar(512) null comment '密码',

    createTime  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete    tinyint  default 0 not null comment '是否删除'
) comment '队伍';

-- 用户队伍关系表
create table user_team
(
    id         bigint auto_increment comment 'id'
        primary key,
    userId     bigint comment '用户id',
    teamId     bigint comment '队伍id',
    joinTime   datetime null comment '加入时间',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete   tinyint  default 0 not null comment '是否删除'
) comment '用户队伍关系表';

-- 2024.4.3 好友申请表
create table friends
(
    id         bigint auto_increment comment '好友申请id'
        primary key,
    fromId     bigint             not null comment '发送申请的用户id',
    receiveId  bigint null comment '接收申请的用户id ',
    isRead     tinyint  default 0 not null comment '是否已读(0-未读 1-已读)',
    status     tinyint  default 0 not null comment '申请状态 默认0 （0-未通过 1-已同意 2-已过期 3-已撤销）',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null,
    isDelete   tinyint  default 0 not null comment '是否删除',
    remark     varchar(214) null comment '好友申请备注信息'
) comment '好友申请管理表' charset = utf8mb4;