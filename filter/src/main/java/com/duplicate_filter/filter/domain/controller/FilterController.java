package com.duplicate_filter.filter.domain.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("api")
public class FilterController {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @PostMapping("/store")
    public ResponseEntity<String> storeData(@RequestBody Map<String,String> body){
        String string=body.get("string"); //파라미터 검사
        if(string==null){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("no argument provided");
        }

        String query="SELECT COUNT(*) FROM storage WHERE string = ?"; //DB에서 중복 검사
        Integer count=jdbcTemplate.queryForObject(query, Integer.class, string);
        if(count!=0L){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("already exist");
        }

        String insertQuery="INSERT INTO storage (string) VALUES (?)"; //데이터 저장
        jdbcTemplate.update(insertQuery,string);
        return ResponseEntity.status(HttpStatus.OK).body("success");
    }
}
