package com.codinglk.photoapp.api.user.data;

import com.codinglk.photoapp.api.user.ui.model.AlbumResponseModel;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

@FeignClient(name="photo-app-api-albums")
public interface AlbumsServiceClient {

    @GetMapping("/users/{id}/albums")
    @Retry(name="photo-app-api-albums")
    @CircuitBreaker(name="photo-app-api-albums", fallbackMethod = "getAlbumsFallback")
    public List<AlbumResponseModel> getAlbums(@PathVariable String id);

    default List<AlbumResponseModel> getAlbumsFallback(String id, Throwable exception){
        System.out.println("Param= "+ id);
        System.out.println("Exception took place: "+exception.getMessage());
        return new ArrayList<>();
    }

}




/**
//@FeignClient(name="photo-app-api-albums", fallback = AlbumsFallback.class) // for netflix-hystrix, AlbumsFallback
//@FeignClient(name = "photo-app-api-albums", fallbackFactory = AlbumsFallbackFactory.class) // for netflix-hystrix, AlbumsFallbackFactory
public interface AlbumsServiceClient {

    @GetMapping("/users/{id}/albums")
    public List<AlbumResponseModel> getAlbums(@PathVariable String id);
}*/

/** for netflix-hystrix , AlbumsFallback
@Component
class AlbumsFallback implements AlbumsServiceClient {

    @Override
    public List<AlbumResponseModel> getAlbums(String id) {
        return new ArrayList<>();
    }
}
*/

/** for netflix-hystrix, AlbumsFallbackFactory
@Component
class AlbumsFallbackFactory implements FallbackFactory<AlbumsServiceClient> {

    @Override
    public AlbumsServiceClient create(Throwable cause) {
        // TODO Auto-generated method stub
        return new AlbumsServiceClientFallback(cause);
    }

}

class AlbumsServiceClientFallback implements AlbumsServiceClient {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Throwable cause;

    public AlbumsServiceClientFallback(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public List<AlbumResponseModel> getAlbums(String id) {
        // TODO Auto-generated method stub

        if (cause instanceof FeignException && ((FeignException) cause).status() == 404) {
            logger.error("404 error took place when getAlbums was called with userId: " + id + ". Error message: "
                    + cause.getLocalizedMessage());
        } else {
            logger.error("Other error took place: " + cause.getLocalizedMessage());
        }

        return new ArrayList<>();
    }

}
 */