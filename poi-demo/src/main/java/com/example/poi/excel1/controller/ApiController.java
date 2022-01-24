package com.example.poi.excel1.controller;


import com.example.poi.excel1.model.Record;
import com.example.poi.excel1.util.ExcelUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhouyw
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        List<Record> data = new ArrayList<>();
        ExcelUtils.writeExcel("登录日志", response, data, Record.class);
    }

}
