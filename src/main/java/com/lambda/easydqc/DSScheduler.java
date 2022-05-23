package com.lambda.easydqc;

import com.alibaba.fastjson.JSONObject;
import com.lambda.easydqc.entity.ProcessDefinition;
import com.lambda.easydqc.entity.TDsProject;
import com.lambda.easydqc.entity.TDsWorkflowV1;
import com.lambda.easydqc.utils.DSProjectUtils;
import com.lambda.easydqc.utils.DSWorkFlowUtils;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @Author: zhangxinsen
 * @Date: 2022/5/22 1:03 PM
 * @Desc:
 * @Version: v1.0
 */

@Data
@Builder
public class DSScheduler {
    private String token;

    public static void main(String[] args) throws Exception {
        String baseUrl = "http://localhost:12345";
        String token = "3e92c07bca15b8ce52e313d2154c9799";
        DSProjectUtils dsProjectUtils = DSProjectUtils.builder()
                .baseUrl(baseUrl)
                .token(token)
                .build();

        TDsProject tDsProject = TDsProject.builder().projectName("测试项目3")
                .projectDescription("测试项目")
                .build();
        dsProjectUtils.upsertProject(tDsProject);
        dsProjectUtils.deleteProject(tDsProject);

        List<TDsProject> tDsProjectList = dsProjectUtils.queryAllProjectList();
        for (TDsProject project : tDsProjectList) {
            if (project.getProjectName().contains("测试") || project.getProjectName().contains("test")) {
                dsProjectUtils.deleteProject(project);
            }
        }
        tDsProjectList = dsProjectUtils.queryAllProjectList();
        for (TDsProject project : tDsProjectList) {
            System.out.println(project);
        }

        // 创建一个空的workflow
        DSWorkFlowUtils dsWorkFlowUtils = DSWorkFlowUtils.builder()
                .token(token)
                .baseUrl(baseUrl)
                .build();
        TDsWorkflowV1 tDsWorkflowV1 = TDsWorkflowV1.builder()
                .processDefinition(
                        ProcessDefinition.builder()
                                .projectName("xinsen-api")
                                .tenantCode("hive01")
                                .processDescription("API接口测试")
                                .processName("api-quickstart")
                                .build()
                )
                .processTaskRelationList(null)
                .taskDefinitionList(null)
                .build();
        List<TDsWorkflowV1> tDsWorkflowV1List = dsWorkFlowUtils.queryAllWorkflowList(TDsProject.builder()
                .projectName("xinsen-api").build());
        for (TDsWorkflowV1 dsWorkflowV1 : tDsWorkflowV1List) {
            System.out.println(JSONObject.toJSON(dsWorkflowV1));
        }
        dsWorkFlowUtils.createEmptyWorkflow(tDsWorkflowV1, true);
    }
}
