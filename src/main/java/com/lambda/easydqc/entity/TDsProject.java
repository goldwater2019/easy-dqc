package com.lambda.easydqc.entity;

import java.util.Date;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.io.Serializable;

import lombok.*;

/**
 * ds项目表(TDsProject)表实体类
 *
 * @author xinsen
 * @since 2022-05-22 13:52:15
 */
@SuppressWarnings("serial")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class TDsProject extends Model<TDsProject> {
    //项目ID
    private Integer projectId;
    //用户ID
    private Integer userId;
    //项目code
    private Long projectCode;
    //项目名称
    private String projectName;
    //项目描述信息
    private String projectDescription;
    //工作流定义数量
    private Integer defCount;
    //正在运行的工作流数量
    private Integer runningCount;

    private Date createTime;

    private Date updateTime;


    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Long getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(Long projectCode) {
        this.projectCode = projectCode;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public Integer getDefCount() {
        return defCount;
    }

    public void setDefCount(Integer defCount) {
        this.defCount = defCount;
    }

    public Integer getRunningCount() {
        return runningCount;
    }

    public void setRunningCount(Integer runningCount) {
        this.runningCount = runningCount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 获取主键值
     *
     * @return 主键值
     */
    @Override
    public Serializable pkVal() {
        return this.projectId;
    }
}

