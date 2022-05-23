package com.lambda.easydqc.entity;

import lombok.*;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author xinsen
 * @since 2022-05-22 13:52:15
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Data
public class ProcessDefinition {
    private Integer processId;
    private Long processCode;
    private String processName;
    private Integer processVersion;
    private Boolean isRelease;
    private Long projectCode;
    private String processDescription;
    private String globalParams;
    private String globalParamList;
    private String globalParamMap;
    private Date createTime;
    private Date updateTime;
    private String flag; // TODO
    private Integer userId;
    private String userName;
    private String projectName;
    private String locations; // TODO
    private String scheduleReleaseState; // TODO null
    private Integer timeout;
    private Integer tenantId;
    private String tenantCode;
    private String modifiedBy;
    private Integer warningGroupId;
}

