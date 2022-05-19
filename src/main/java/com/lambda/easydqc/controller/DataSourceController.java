package com.lambda.easydqc.controller;

import com.lambda.easydqc.entity.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author: zhangxinsen
 * @Date: 2022/5/19 3:44 PM
 * @Desc:
 * @Version: v1.0
 */


/**
 * 用户管理
 */
@Controller
@RequestMapping("/datasources")
public class DataSourceController {

    /**
     * 分页查询信息
     */
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = "list/timax")
    public String list(Model model) {

        DataSource data = DataSource.builder()
                .jdbcUrl("jdbc:mysql//vnfibmflb,fl;b")
                .username("timax")
                .password("timax")
                .name("天象")
                .build();
        model.addAttribute("title", "数据源查看/编辑");
        model.addAttribute("datasource", data);

        return "datasources/edit";
    }
}