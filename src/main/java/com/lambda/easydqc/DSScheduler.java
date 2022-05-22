package com.lambda.easydqc;

import com.lambda.easydqc.entity.TDsProject;
import com.lambda.easydqc.utils.DSProjectUtils;
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
    }
}
