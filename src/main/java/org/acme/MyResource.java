package org.acme;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import io.smallrye.mutiny.Uni;

@Path("/")
public class MyResource {

    @Inject
    MyService myService;

    @GET
    @Path("create")
    public Uni<Response> create() {
        return myService.getEntityWithRetry();
    }

    @GET
    @Path("list")
    public List<MyEntity> list() {
        return myService.listEntity();
    }
}