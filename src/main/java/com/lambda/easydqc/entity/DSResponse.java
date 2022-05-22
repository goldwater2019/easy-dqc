package com.lambda.easydqc.entity;

import lombok.*;

/**
 * @Author: zhangxinsen
 * @Date: 2022/5/22 1:34 PM
 * @Desc:
 * @Version: v1.0
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class DSResponse<T> {
    private Integer code;
    private String msg;
    private T data;
    private Boolean failed;
    private Boolean success;
}
