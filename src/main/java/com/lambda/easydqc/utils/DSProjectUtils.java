package com.lambda.easydqc.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lambda.easydqc.entity.DSResponse;
import com.lambda.easydqc.entity.RequestHeader;
import com.lambda.easydqc.entity.TDsProject;
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
 * @Date: 2022/5/22 1:21 PM
 * @Desc:
 * @Version: v1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class DSProjectUtils {
    private String token;
    String baseUrl;

    private static final String QUERY_ALL_PROJECT_LIST = "/dolphinscheduler/projects/list";
    private static final String CREATE_PROJECT = "/dolphinscheduler/projects";
    private static final String UPDATE_PROJECT = "/dolphinscheduler/projects/{code}";
    private static final String DELETE_PROJECT = "/dolphinscheduler/projects/{code}";

    /**
     * 整理请求头信息
     *
     * @param requestHeaderList 请求头
     * @return
     */
    public static Headers createHeaders(List<RequestHeader> requestHeaderList) {
        Headers headers = null;
        okhttp3.Headers.Builder headersBuilder = new okhttp3.Headers.Builder();
        if (requestHeaderList != null) {
            requestHeaderList.forEach(requestHeader -> {
                headersBuilder.add(requestHeader.getKey(), requestHeader.getValue());
            });
        }
        headers = headersBuilder.build();
        return headers;
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
                .add("description", description == null ? "" : description)
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
                .add("description", projectDescription == null ? "" : projectDescription)
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
        TDsProject oldProject = findProject(tDsProject);
        Assert.isTrue(oldProject != null, "project not exists, name: " + tDsProject.getProjectName() + "," +
                "code: " + tDsProject.getProjectCode());
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

    /**
     * 根据projectCode或者projectName查找相应的project
     * 如果code和name不相匹配,则报错
     *
     * @param dsProject
     * @return
     * @throws Exception
     */
    public TDsProject findProject(TDsProject dsProject) throws Exception {
        TDsProject oldProject = null;
        List<TDsProject> tDsProjectList = queryAllProjectList();
        for (TDsProject project : tDsProjectList) {
            if (dsProject.getProjectCode() != null) {
                if (dsProject.getProjectCode() == project.getProjectCode()) {
                    oldProject = project;
                    break;
                }
            } else if (!StringUtils.isEmpty(dsProject.getProjectName())) {
                if (dsProject.getProjectName().equals(project.getProjectName())) {
                    oldProject = project;
                    break;
                }
            }
        }
        // 同时指定的情况下, 确保能够正常拿到数据(保持一致性)
        if (dsProject.getProjectCode() != null && !StringUtils.isEmpty(dsProject.getProjectName())) {
            if (oldProject != null && !dsProject.getProjectName().equals(oldProject.getProjectName())) {
                throw new Exception("project code and name not matched, code: " + dsProject.getProjectCode() + "," +
                        "name: " + dsProject.getProjectName());
            }
        }
        return oldProject;
    }
}
