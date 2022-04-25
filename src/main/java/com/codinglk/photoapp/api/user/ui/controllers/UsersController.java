/**
 *
 * @author codinglk
 */
package com.codinglk.photoapp.api.user.ui.controllers;

import com.codinglk.photoapp.api.user.service.UsersService;
import com.codinglk.photoapp.api.user.shared.UserDto;
import com.codinglk.photoapp.api.user.ui.model.CreateUserRequestModel;
import com.codinglk.photoapp.api.user.ui.model.CreateUserResponseModel;
import com.codinglk.photoapp.api.user.ui.model.UserResponseModel;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/users")
public class UsersController {

    @Autowired
    private Environment env;

    @Autowired
    UsersService usersService;

    @GetMapping("/status/check")
    public String status(){

        return "Working on port "+ env.getProperty("local.server.port") + ", with token.secret " + env.getProperty("token.secret");
    }

    // This method can consume and produce both XML and JSON media types, It supports JSON by default
    @PostMapping(
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity createUser(@RequestBody CreateUserRequestModel userDetails){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto userDto = modelMapper.map(userDetails, UserDto.class);

        UserDto createdUser = usersService.createUser(userDto);

        CreateUserResponseModel returnValue = modelMapper.map(createdUser, CreateUserResponseModel.class);

//        return new ResponseEntity(HttpStatus.CREATED);
        return ResponseEntity.status(HttpStatus.CREATED).body(returnValue);
    }

    @GetMapping(value="/{userId}", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<UserResponseModel> getUser(@PathVariable("userId") String userId) {

        UserDto userDto = usersService.getUserByUserId(userId);
        UserResponseModel returnValue = new ModelMapper().map(userDto, UserResponseModel.class);

        return ResponseEntity.status(HttpStatus.OK).body(returnValue);
    }

}
