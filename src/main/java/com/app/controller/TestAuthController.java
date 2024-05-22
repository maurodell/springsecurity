package com.app.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/method")
//@PreAuthorize("denyAll()")
public class TestAuthController {

    @GetMapping("/get")
    //@PreAuthorize("hasAuthority('READ')")
    public String methodGet(){
        return " - GET -";
    }

    @PostMapping("/post")
    //@PreAuthorize("hasAuthority('READ') or hasAuthority('CREATE')")
    public String methodPost(){
        return " - POST -";
    }

    @PutMapping("/put")
    public String methodPut(){
        return " - PUT -";
    }

    @DeleteMapping("/delete")
    //@PreAuthorize("hasAuthority('DELETE')")
    public String methodDelete(){
        return " - DETELE -";
    }

    @PatchMapping("/patch")
    //@PreAuthorize("hasAuthority('REFACTOR')")
    public String methodPatch(){
        return " - PATCH -";
    }
}
