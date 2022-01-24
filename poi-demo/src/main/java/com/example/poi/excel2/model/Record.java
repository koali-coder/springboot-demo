package com.example.poi.excel2.model;

import com.example.poi.excel1.model.ExcelColumn;
import lombok.Data;

/**
 * @author zhouyw
 * @date 2022-01-24
 * @describe com.example.poi.excel1.model
 */
@Data
public class Record {

    @ExcelColumn(value = "id", col = 1)
    private Integer id;

    @ExcelColumn(value = "name", col = 2)
    private String name;

}
