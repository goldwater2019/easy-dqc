package com.lambda.easydqc.entity;

import lombok.*;

/**
 * @Author: zhangxinsen
 * @Date: 2022/5/19 3:50 PM
 * @Desc:
 * @Version: v1.0
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class DataSource {
    public String type;
    public String name;
    public String jdbcUrl;
    public String username;
    public String password;

}
