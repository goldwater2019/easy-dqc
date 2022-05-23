package com.lambda.easydqc.entity;

import lombok.*;

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
public class TDsWorkflowV1 {
    /**
     * 流程定义
     */
    private ProcessDefinition processDefinition;
    /**
     * 任务关系
     */
    private String processTaskRelationList;
    /**
     * 任务定义
     */
    private String taskDefinitionList;
}

