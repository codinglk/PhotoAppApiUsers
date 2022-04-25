package com.codinglk.photoapp.api.user.shared;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    Environment environment;

    @Autowired
    public FeignErrorDecoder(Environment environment)
    {
        this.environment = environment;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()) {
            case 400:
                // Do something
                // return new BadRequestException();
                break;
            case 404: {
                if (methodKey.contains("getAlbums")) {
                    return new ResponseStatusException(HttpStatus.valueOf(response.status()), environment.getProperty("albums.exceptions.albums-not-found"));
                }
                break;
            }
            default:
                return new Exception(response.reason());
        }
        return null;
    }

}
