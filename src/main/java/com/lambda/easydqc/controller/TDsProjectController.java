package com.lambda.easydqc.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lambda.easydqc.entity.TDsProject;
import com.lambda.easydqc.service.TDsProjectService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * ds项目表(TDsProject)表控制层
 *
 * @author xinsen
 * @since 2022-05-22 13:52:01
 */
@RestController
@RequestMapping("tDsProject")
public class TDsProjectController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TDsProjectService tDsProjectService;

    /**
     * 分页查询所有数据
     *
     * @param page       分页对象
     * @param tDsProject 查询实体
     * @return 所有数据
     */
    @GetMapping
    public R selectAll(Page<TDsProject> page, TDsProject tDsProject) {
        return success(this.tDsProjectService.page(page, new QueryWrapper<>(tDsProject)));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public R selectOne(@PathVariable Serializable id) {
        return success(this.tDsProjectService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param tDsProject 实体对象
     * @return 新增结果
     */
    @PostMapping
    public R insert(@RequestBody TDsProject tDsProject) {
        return success(this.tDsProjectService.save(tDsProject));
    }

    /**
     * 修改数据
     *
     * @param tDsProject 实体对象
     * @return 修改结果
     */
    @PutMapping
    public R update(@RequestBody TDsProject tDsProject) {
        return success(this.tDsProjectService.updateById(tDsProject));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @DeleteMapping
    public R delete(@RequestParam("idList") List<Long> idList) {
        return success(this.tDsProjectService.removeByIds(idList));
    }
}

