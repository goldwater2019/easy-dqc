create table if not exists easydqc.t_ds_project
(
    project_id          int auto_increment comment '项目ID',
    user_id             int                                not null comment '用户ID',
    project_code        bigint                             not null comment '项目code',
    project_name        varchar(128)                       null comment '项目名称',
    project_description text                               null comment '项目描述信息',
    def_count           int      default 0                 null comment '工作流定义数量',
    running_count       int      default 0                 null comment '正在运行的工作流数量',
    create_time         datetime default current_timestamp null,
    update_time         datetime default current_timestamp null,
    constraint t_ds_project_pk
    primary key (project_id)
    )
    comment 'ds项目表';