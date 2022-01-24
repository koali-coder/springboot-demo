package com.example.poi.excel1.controller;


import com.example.poi.excel1.model.Record;
import com.example.poi.excel1.util.ExcelUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author zhouyw
 */
@RestController
@RequestMapping("/excel")
public class Excel1Controller {

    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        String json = "[{\"id\":1, \"name\":\"one\"},{\"id\":2, \"name\":\"two\"},{\"id\":3, \"name\":\"three\"}]";
        List<Record> data = new Gson().fromJson(json, new TypeToken<List<Record>>(){}.getType());
        ExcelUtils.writeExcel("excel", response, data, Record.class);
    }

}
