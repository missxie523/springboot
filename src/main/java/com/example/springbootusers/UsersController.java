package com.example.springbootusers;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.function.Consumer;

@RestController
@RequestMapping("/")
public class UsersController {

    @RequestMapping(value = "/users", method = RequestMethod.POST, consumes = "application/json")
    public User createUser(@RequestBody User user){
        System.out.println(user.getUsername().concat(" " + user.getPassword()));
        return user;
    }

}
