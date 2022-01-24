package com.example.poi.excel2.controller;

import com.example.poi.excel1.model.Record;
import com.example.poi.excel2.util.ExcelUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhouyw
 * @date 2022-01-24
 * @describe com.example.poi.excel2.controller
 */
@RestController
@RequestMapping("/excel2")
public class Excel2Controller {

    @GetMapping("/export")
    public void export() {
        List<Record> list = new ArrayList<>();
        ExcelUtil<Record> util = new ExcelUtil<Record>(Record.class);
        util.exportExcel(list, "用户数据");
    }

}
