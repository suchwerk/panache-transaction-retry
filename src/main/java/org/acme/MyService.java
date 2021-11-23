package org.acme;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.faulttolerance.Retry;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class MyService {

    private static final String SOMETHING_WENT_WRONG = "something went wrong";
    AtomicInteger counter = new AtomicInteger(0);

    public Uni<Response> getEntityWithRetry() {
        return Uni.createFrom().completionStage(CompletableFuture.supplyAsync(() -> {
            try {
                var entity = storeEntity(2);
                return Response.ok(entity).build();
            } catch (Exception e) {
                Log.error(SOMETHING_WENT_WRONG, e);
            }
            return Response.ok(null).build();
        }));
    }

    @Transactional
    public MyEntity storeEntity(int retries) throws Exception {
        counter.set(0);
        return storeRetryEntity(retries);
    }

    @Retry(maxRetries = 3, delay = 1000)
    @Transactional
    public MyEntity storeRetryEntity(int retries) {
        Log.infof("storeRetryEntity %d of %d", counter.get(), retries);
        MyEntity entity = new MyEntity();
        entity.persist();
        if (counter.getAndIncrement() < retries) {
            Log.info(SOMETHING_WENT_WRONG);
            throw new RuntimeException(SOMETHING_WENT_WRONG);
        }
        return entity;
    }

    @Transactional
    public List<MyEntity> listEntity() {
        return MyEntity.listAll();
    }
}
