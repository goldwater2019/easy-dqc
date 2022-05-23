package com.lambda.easydqc.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lambda.easydqc.entity.*;
import lombok.*;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @Author: zhangxinsen
 * @Date: 2022/5/22 4:21 PM
 * @Desc: 小海豚工作流 工具类
 * @Version: v1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class DSWorkFlowUtils {
    private String token;
    String baseUrl;

    private OkHttpClient okHttpClient;

    private static final String QUERY_ALL_PROJECT_LIST = "/dolphinscheduler/projects/list";
    private static final String CREATE_PROJECT = "/dolphinscheduler/projects";
    private static final String UPDATE_PROJECT = "/dolphinscheduler/projects/{code}";
    private static final String DELETE_PROJECT = "/dolphinscheduler/projects/{code}";

    private static final String CREATE_EMPTY_WORKFLOW = "/dolphinscheduler/projects/{projectCode}/process-definition/empty";
    private static final String QUERY_ALL_WORKFLOW_LIST = "/dolphinscheduler/projects/{projectCode}/process-definition/list";


    /**
     * 整理请求头信息
     *
     * @param requestHeaderList 请求头
     * @return
     */
    public static Headers createHeaders(List<RequestHeader> requestHeaderList) {
        Headers headers = null;
        Headers.Builder headersBuilder = new Headers.Builder();
        if (requestHeaderList != null) {
            requestHeaderList.forEach(requestHeader -> {
                headersBuilder.add(requestHeader.getKey(), requestHeader.getValue());
            });
        }
        headers = headersBuilder.build();
        return headers;
    }


    /**
     * @param tDsWorkflowV1
     * @param forceCreate:  当project不存在的时候, true->创建; false->报错
     * @throws Exception
     */
    public boolean createEmptyWorkflow(TDsWorkflowV1 tDsWorkflowV1,
                                       boolean forceCreate) throws Exception {
        ProcessDefinition processDefinition = tDsWorkflowV1.getProcessDefinition();
        Assert.isTrue(processDefinition != null, "process definition should not be null");
        String processName = processDefinition.getProcessName();
        String tenantCode = processDefinition.getTenantCode();
        // TODO 确保tenantCode一定已经存在
        DSProjectUtils dsProjectUtils = DSProjectUtils.builder()
                .token(getToken())
                .baseUrl(getBaseUrl())
                .build();
        TDsProject existProject = dsProjectUtils.findProject(TDsProject
                .builder()
                .projectCode(processDefinition.getProjectCode())
                .projectName(processDefinition.getProjectName())
                .build());

        if (existProject == null) {
            if (forceCreate) {
                dsProjectUtils.createProject(
                        TDsProject.builder()
                                .projectName(processDefinition.getProjectName())
                                .projectCode(processDefinition.getProjectCode())
                                .build()
                );
                existProject = dsProjectUtils.findProject(
                        TDsProject.builder()
                                .projectName(processDefinition.getProjectName())
                                .projectCode(processDefinition.getProjectCode())
                                .build()
                );
            } else {
                throw new Exception("project not exists," +
                        "name: " + processDefinition.getProjectName() + ", " +
                        "code: " + processDefinition.getProjectCode());
            }
        }
        processDefinition.setProjectCode(existProject.getProjectCode());
        processDefinition.setProjectName(existProject.getProjectName());

        String url = baseUrl + CREATE_EMPTY_WORKFLOW.replace("{projectCode}",
                String.valueOf(processDefinition.getProjectCode()));

        List<RequestHeader> headerList = new LinkedList<>();
        headerList.add(RequestHeader.builder().key("token").value(token).build());
        FormBody body = new FormBody.Builder()
                .add("name", processDefinition.getProcessName())
                .add("tenantCode", processDefinition.getTenantCode())
                .add("description", processDefinition.getProcessDescription())
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder().url(url).post(body).headers(createHeaders(headerList)).build();
        OkHttpClient okHttpClient = new OkHttpClient();
        Response response = okHttpClient.newCall(request).execute();

        if (response.isSuccessful()) {
            String responseBody = Objects.requireNonNull(response.body().string(), "返回的数据为空");
            DSResponse dsResponse = JSONObject.parseObject(responseBody, DSResponse.class);
            if (dsResponse.getCode() != 0) {
                throw new Exception(dsResponse.getMsg());
            }
            response.close();
            return dsResponse.getSuccess();
        } else {
            response.close();
            throw new IOException("Unexpected code " + response);
        }
    }

    /**
     * 查询所有project下的所有工作流定义
     * @return
     * @throws Exception
     */
    public List<TDsWorkflowV1> queryAllWorkflowList() throws Exception {
        List<TDsWorkflowV1> tDsWorkflowV1List = new LinkedList<>();
        DSProjectUtils dsProjectUtils = DSProjectUtils.builder()
                .token(getToken())
                .baseUrl(getBaseUrl())
                .build();
        List<TDsProject> tDsProjectList = dsProjectUtils.queryAllProjectList();
        for (TDsProject project : tDsProjectList) {
            List<TDsWorkflowV1> tDsWorkflowV1s = queryAllWorkflowList(project);
            tDsWorkflowV1List.addAll(tDsWorkflowV1s);
        }
        return tDsWorkflowV1List;
    }


    /**
     * 查询给定project下的工作流定义
     * @param project
     * @return
     * @throws Exception
     */
    public List<TDsWorkflowV1> queryAllWorkflowList(TDsProject project) throws Exception {
        List<TDsWorkflowV1> tDsWorkflowV1List = new LinkedList<>();
        Assert.isTrue(!StringUtils.isEmpty(project.getProjectName()) || project.getProjectCode() != null,
                "neither name or code should at least one item not null");
        DSProjectUtils dsProjectUtils = DSProjectUtils.builder()
                .token(getToken())
                .baseUrl(getBaseUrl())
                .build();
        TDsProject existedProject = dsProjectUtils.findProject(project);
        if (existedProject == null) {
            throw new Exception("project not exists, name: " + project.getProjectName() +
                    ", code: " + project.getProjectCode());
        }
        project.setProjectName(existedProject.getProjectName());
        project.setProjectCode(existedProject.getProjectCode());

        String url = baseUrl + QUERY_ALL_WORKFLOW_LIST.replace("{projectCode}",
                String.valueOf(project.getProjectCode()));

        List<RequestHeader> headerList = new LinkedList<>();
        headerList.add(RequestHeader.builder().key("token").value(token).build());

        okhttp3.Request request = new okhttp3.Request.Builder().url(url).get().headers(createHeaders(headerList)).build();
        OkHttpClient okHttpClient = new OkHttpClient();
        Response response = okHttpClient.newCall(request).execute();

        if (response.isSuccessful()) {
            String responseBody = Objects.requireNonNull(response.body().string(), "返回的数据为空");
            DSResponse dsResponse = JSONObject.parseObject(responseBody, DSResponse.class);
            if (dsResponse.getCode() != 0) {
                throw new Exception(dsResponse.getMsg());
            }
            response.close();
            JSONArray data = (JSONArray) dsResponse.getData();
            for (Object projectObject : data) {
                JSONObject jsonObject = JSONObject.parseObject(projectObject.toString());
                JSONObject processDefinition = jsonObject.getJSONObject("processDefinition");
                JSONArray processTaskRelationList = jsonObject.getJSONArray("processTaskRelationList");
                JSONArray taskDefinitionList = jsonObject.getJSONArray("taskDefinitionList");
                tDsWorkflowV1List.add(
                        TDsWorkflowV1.builder()
                                // TODO taskDefinition和taskRelationList更新
                                .processTaskRelationList(null)
                                .taskDefinitionList(null)
                                .processDefinition(
                                        ProcessDefinition.builder()
                                                .processId(processDefinition.getInteger("id"))
                                                .processCode(processDefinition.getLong("code"))
                                                .processName(processDefinition.getString("name"))
                                                .processVersion(processDefinition.getInteger("version"))
                                                .isRelease(processDefinition.getString("releaseState").equals("ONLINE"))
                                                .projectCode(processDefinition.getLong("projectCode"))
                                                .processDescription(processDefinition.getString("description"))
                                                .globalParams(processDefinition.getString("globalParams"))
                                                .globalParamList(processDefinition.getString("globalParamList"))
                                                .globalParamMap(processDefinition.getString("globalParamMap"))
                                                .createTime(processDefinition.getTimestamp("createTime"))
                                                .updateTime(processDefinition.getTimestamp("updateTime"))
                                                .flag(processDefinition.getString("flag"))
                                                .userId(processDefinition.getInteger("userId"))
                                                .userName(processDefinition.getString("userName")) // TODO 根据userId补全此处的name
                                                .projectName(project.getProjectName())
                                                .locations(processDefinition.getString("locations"))
                                                .scheduleReleaseState(processDefinition.getString("scheduleReleaseState"))
                                                .timeout(processDefinition.getInteger("timeout"))
                                                .tenantId(processDefinition.getInteger("tenantId"))
                                                .tenantCode(processDefinition.getString("tenantCode")) // TODO 根据tenantId补全此处的code
                                                .modifiedBy(processDefinition.getString("modifyBy"))
                                                .warningGroupId(processDefinition.getInteger("warningGroupId"))
                                                .build()
                                )
                                .build()
                );
            }
        } else {
            response.close();
            throw new IOException("Unexpected code " + response);
        }
        return tDsWorkflowV1List;
    }

    /**
     * QUERY_ALL_PROJECT_LIST
     * 获得所有的project
     *
     * @throws Exception
     */
    public List<TDsProject> queryAllProjectList() throws Exception {
        List<TDsProject> tDsProjectList = new LinkedList<>();
        String url = baseUrl + QUERY_ALL_PROJECT_LIST;
        List<RequestHeader> headerList = new LinkedList<>();
        headerList.add(RequestHeader.builder().key("token").value(token).build());
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).get().headers(createHeaders(headerList)).build();
        OkHttpClient okHttpClient = new OkHttpClient();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            String responseBody = Objects.requireNonNull(response.body().string(), "返回的数据为空");
            DSResponse dsResponse = JSONObject.parseObject(responseBody, DSResponse.class);
            if (dsResponse.getCode() != 0) {
                throw new Exception(dsResponse.getMsg());
            }
            JSONArray data = (JSONArray) dsResponse.getData();
            for (Object projectObject : data) {
                JSONObject jsonObject = JSONObject.parseObject(projectObject.toString());
                tDsProjectList.add(
                        TDsProject.builder()
                                .projectId(jsonObject.getInteger("id"))
                                .userId(jsonObject.getInteger("userId"))
                                .projectCode(jsonObject.getLong("code"))
                                .projectName(jsonObject.getString("name"))
                                .projectDescription(jsonObject.getString("description"))
                                .defCount(jsonObject.getInteger("defCount"))
                                .runningCount(jsonObject.getInteger("instRunningCount"))
                                .createTime(jsonObject.getTimestamp("createTime"))
                                .updateTime(jsonObject.getTimestamp("updateTime"))
                                .build()
                );
            }
        } else {
            throw new IOException("Unexpected code " + response);
        }
        response.close();
        return tDsProjectList;
    }


    public boolean createProject(TDsProject tDsProject) throws Exception {
        String projectName = tDsProject.getProjectName();
        String description = tDsProject.getProjectDescription();
        Assert.isTrue(!StringUtils.isEmpty(projectName), "project name can not be null");

        String url = baseUrl + CREATE_PROJECT;
        List<RequestHeader> headerList = new LinkedList<>();
        headerList.add(RequestHeader.builder().key("token").value(token).build());
        FormBody body = new FormBody.Builder().add("projectName", projectName)
                .add("description", description)
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder().url(url).post(body).headers(createHeaders(headerList)).build();
        OkHttpClient okHttpClient = new OkHttpClient();
        Response response = okHttpClient.newCall(request).execute();

        if (response.isSuccessful()) {
            String responseBody = Objects.requireNonNull(response.body().string(), "返回的数据为空");
            DSResponse dsResponse = JSONObject.parseObject(responseBody, DSResponse.class);
            if (dsResponse.getCode() != 0) {
                throw new Exception(dsResponse.getMsg());
            }
            response.close();
            return dsResponse.getSuccess();
        } else {
            response.close();
            throw new IOException("Unexpected code " + response);
        }
    }

    /**
     * 更新/创建 project项目内容
     * 1. 当code存在, 以code为准
     * 2. code和name都存在时, 如果code和name没有对应上, 则报错
     * 3. 当name存在但是code未指定时, 以name为准
     *
     * @param tDsProject
     */
    public boolean upsertProject(TDsProject tDsProject) throws Exception {
        List<TDsProject> tDsProjectList = queryAllProjectList();
        boolean isExists = false;
        TDsProject oldTDsProject = null;
        for (TDsProject dsProject : tDsProjectList) {
            if (tDsProject.getProjectCode() != null) {
                if (dsProject.getProjectCode() == tDsProject.getProjectCode()) {
                    isExists = true;
                    oldTDsProject = dsProject;
                    break;
                }
            } else {
                if (dsProject.getProjectName().equals(tDsProject.getProjectName())) {
                    isExists = true;
                    oldTDsProject = dsProject;
                    break;
                }
            }
        }
        if (isExists) {
            // update it
            tDsProject.setProjectCode(oldTDsProject.getProjectCode());
            return updateProject(tDsProject);
        } else {
            return createProject(tDsProject);
        }
    }

    /**
     * 更新project信息
     *
     * @param tDsProject
     * @return
     */
    public boolean updateProject(TDsProject tDsProject) throws Exception {
        Long projectCode = tDsProject.getProjectCode();
        String projectName = tDsProject.getProjectName();
        String projectDescription = tDsProject.getProjectDescription();
        Assert.isTrue(!StringUtils.isEmpty(projectName), "project name can not be null");


        String url = baseUrl + UPDATE_PROJECT.replace("{code}", String.valueOf(projectCode));
        List<RequestHeader> headerList = new LinkedList<>();
        headerList.add(RequestHeader.builder().key("token").value(token).build());
        FormBody body = new FormBody.Builder().add("projectName", projectName)
                .add("description", projectDescription)
                .add("userName", "admin")
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder().url(url).put(body).headers(createHeaders(headerList)).build();
        OkHttpClient okHttpClient = new OkHttpClient();
        Response response = okHttpClient.newCall(request).execute();

        if (response.isSuccessful()) {
            String responseBody = Objects.requireNonNull(response.body().string(), "返回的数据为空");
            DSResponse dsResponse = JSONObject.parseObject(responseBody, DSResponse.class);
            if (dsResponse.getCode() != 0) {
                throw new Exception(dsResponse.getMsg());
            }
            response.close();
            return dsResponse.getSuccess();
        } else {
            response.close();
            throw new IOException("Unexpected code " + response);
        }
    }

    /**
     * 删除相应的project
     * 如果同时指定了code和name, name需要和code对应上
     *
     * @param tDsProject
     */
    public boolean deleteProject(TDsProject tDsProject) throws Exception {
        Assert.isTrue(tDsProject.getProjectCode() != null
                        || !StringUtils.isEmpty(tDsProject.getProjectName())
                , "either code or name should be specified");
        List<TDsProject> tDsProjectList = queryAllProjectList();
        boolean isExists = false;
        TDsProject oldProject = null;
        for (TDsProject project : tDsProjectList) {
            if (tDsProject.getProjectCode() != null) {
                if (project.getProjectCode() == tDsProject.getProjectCode()) {
                    isExists = true;
                    oldProject = project;
                    break;
                }
            }
            if (!StringUtils.isEmpty(tDsProject.getProjectName())) {
                if (project.getProjectName().equals(tDsProject.getProjectName())) {
                    isExists = true;
                    oldProject = project;
                    break;
                }
            }
        }
        if (!isExists) {
            throw new Exception("project not exists, project code " + tDsProject.getProjectCode()
                    + ", project name: " + tDsProject.getProjectName());
        }
        if (!StringUtils.isEmpty(tDsProject.getProjectName())) {
            if (!tDsProject.getProjectName().equals(oldProject.getProjectName())) {
                throw new Exception("invalid project, name should be " + oldProject.getProjectName() + ", not "
                        + tDsProject.getProjectName());
            }
        }
        tDsProject.setProjectCode(oldProject.getProjectCode());

        String url = baseUrl + UPDATE_PROJECT.replace("{code}", String.valueOf(tDsProject.getProjectCode()));
        List<RequestHeader> headerList = new LinkedList<>();
        headerList.add(RequestHeader.builder().key("token").value(token).build());

        okhttp3.Request request = new okhttp3.Request.Builder().url(url).delete().headers(createHeaders(headerList)).build();
        OkHttpClient okHttpClient = new OkHttpClient();
        Response response = okHttpClient.newCall(request).execute();

        if (response.isSuccessful()) {
            String responseBody = Objects.requireNonNull(response.body().string(), "返回的数据为空");
            DSResponse dsResponse = JSONObject.parseObject(responseBody, DSResponse.class);
            if (dsResponse.getCode() != 0) {
                throw new Exception(dsResponse.getMsg());
            }
            response.close();
            return dsResponse.getSuccess();
        } else {
            response.close();
            throw new IOException("Unexpected code " + response);
        }
    }
}
