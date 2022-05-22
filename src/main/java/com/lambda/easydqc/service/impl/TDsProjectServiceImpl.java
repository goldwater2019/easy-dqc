package com.lambda.easydqc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lambda.easydqc.dao.TDsProjectDao;
import com.lambda.easydqc.entity.TDsProject;
import com.lambda.easydqc.service.TDsProjectService;
import org.springframework.stereotype.Service;

/**
 * ds项目表(TDsProject)表服务实现类
 *
 * @author xinsen
 * @since 2022-05-22 13:52:22
 */
@Service("tDsProjectService")
public class TDsProjectServiceImpl extends ServiceImpl<TDsProjectDao, TDsProject> implements TDsProjectService {

}

