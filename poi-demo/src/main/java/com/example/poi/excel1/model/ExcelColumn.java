package com.example.poi.excel1.model;

import java.lang.annotation.*;

/**
 * @author zhouyw
 * @date 2022-01-24
 * @describe com.nbios.os.application.loginlog2.entity
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelColumn {

    /**
     * Excel标题
     *
     * @return
     * @author Lynch
     */
    String value() default "";

    /**
     * Excel从左往右排列位置
     *
     * @return
     * @author Lynch
     */ int col() default 0;

}
