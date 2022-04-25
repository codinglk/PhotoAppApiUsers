package com.codinglk.photoapp.api.user.service;


import com.codinglk.photoapp.api.user.data.AlbumsServiceClient;
import com.codinglk.photoapp.api.user.data.UserEntity;
import com.codinglk.photoapp.api.user.data.UsersRepository;
import com.codinglk.photoapp.api.user.shared.UserDto;
import com.codinglk.photoapp.api.user.ui.model.AlbumResponseModel;
import feign.FeignException;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UsersServiceImpl implements UsersService {

    UsersRepository usersRepository;
    BCryptPasswordEncoder bCryptPasswordEncoder;
    Environment environment;
    //    RestTemplate restTemplate;
    AlbumsServiceClient albumsServiceClient;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public UsersServiceImpl(UsersRepository usersRepository, BCryptPasswordEncoder bCryptPasswordEncoder, Environment environment, AlbumsServiceClient albumsServiceClient) {

        this.usersRepository = usersRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.environment = environment;
//        this.restTemplate = restTemplate;
        this.albumsServiceClient = albumsServiceClient;
    }

    @Override
    public UserDto createUser(UserDto userDetails) {
        userDetails.setUserId(UUID.randomUUID().toString());
        userDetails.setEncryptedPassword(bCryptPasswordEncoder.encode(userDetails.getPassword()));
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserEntity userEntity = modelMapper.map(userDetails, UserEntity.class);

/*
       // long currentTime = System.currentTimeMillis();
        long currentTime = Instant.now().toEpochMilli(); // Java8
        userEntity.setEncryptedPassword("encrypt-later-"+currentTime);
*/

        usersRepository.save(userEntity);

        UserDto userDto = modelMapper.map(userEntity, UserDto.class);

        return userDto;
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {
        //Fetch user details by email provided by user
        UserEntity userEntity = usersRepository.findByEmail(email);

        // UsernameNotFoundException is provided by Spring, we can through our own custom exception also
        if (userEntity == null) throw new UsernameNotFoundException(email);

        return new ModelMapper().map(userEntity, UserDto.class);

    }

    // UsersService interface has extended the org.springframework.security.core.userdetails.UserDetailsService
    // We need to implement loadUserByUsername method here, it will be called by Spring framework
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //Fetch user details by email provided by user
        UserEntity userEntity = usersRepository.findByEmail(username);

        // UsernameNotFoundException is provided by Spring, we can through our own custom exception also
        if (userEntity == null) throw new UsernameNotFoundException(username);
        return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), true, true, true, true, new ArrayList<>());
    }

    @Override
    public UserDto getUserByUserId(String userId) {

        UserEntity userEntity = usersRepository.findByUserId(userId);
        if (userEntity == null) throw new UsernameNotFoundException("User not found");

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);

        /** calling album microservice using RestTemplate
         String albumsUrl = String.format(environment.getProperty("albums.url"), userId);

         ResponseEntity<List<AlbumResponseModel>> albumsListResponse = restTemplate.exchange(albumsUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<AlbumResponseModel>>() {
         });
         List<AlbumResponseModel> albumsList = albumsListResponse.getBody();
         */
        logger.info("Before calling albums Microservice");

        /** feign error handling adding try catch block
         // calling album microservice using FeignClient
         List<AlbumResponseModel> albumsList = null;
         try {
         albumsList = albumsServiceClient.getAlbums(userId);
         } catch (FeignException e) {
         logger.error(e.getLocalizedMessage());
         }
         */

        // feign error handling using FeignErrorDecoder class
        List<AlbumResponseModel> albumsList = albumsServiceClient.getAlbums(userId);
        logger.info("After calling albums Microservice");

        userDto.setAlbums(albumsList);

        return userDto;
    }
}
