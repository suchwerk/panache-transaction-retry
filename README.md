# panach-transaction-retry

I would expect that this leads finaly to only one entity in the database:

```java
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
```

But the entity is stored 3 times. Why?

In a real world project i see the complete different behavior: there it seams that the entity is not comitted after a retry. Where could this come from? The difference is, that there i get a Deadlock Execption form the database.

## See test

```shell script
 mvn quarkus:dev
```

and 'r' for resume whith tests.

## Debug

I set a breakpoint on storeRetryEntity and the stacktrace showed me that, FaultToleranceInterceptor is called after the TransactionInterceptor.

Thx to @Christian Beikov for opening my eyes for that :)

```log
MyService.storeRetryEntity(int) (.../src/main/java/org/acme/MyService.java:45)
MyService_Subclass.storeRetryEntity$$superforward1(int) (Unknown Source:431)
MyService_Subclass$$function$$5.apply(Object) (Unknown Source:35)
AroundInvokeInvocationContext.proceed() (/arc-2.4.2.Final.jar/io.quarkus.arc.impl/AroundInvokeInvocationContext.class:54)
FaultToleranceInterceptor.lambda$invocationContext$3(InvocationContext) (/smallrye-fault-tolerance-5.2.1.jar/io.smallrye.faulttolerance/FaultToleranceInterceptor.class:201)
1973362338.call() (Unknown Source:-1)
InvocationContext.call() (/smallrye-fault-tolerance-core-5.2.1.jar/io.smallrye.faulttolerance.core/InvocationContext.class:20)
Invocation.apply(InvocationContext) (/smallrye-fault-tolerance-core-5.2.1.jar/io.smallrye.faulttolerance.core/Invocation.class:29)
Retry.doApply(InvocationContext) (/smallrye-fault-tolerance-core-5.2.1.jar/io.smallrye.faulttolerance.core.retry/Retry.class:90)
Retry.apply(InvocationContext) (/smallrye-fault-tolerance-core-5.2.1.jar/io.smallrye.faulttolerance.core.retry/Retry.class:44)
FaultToleranceInterceptor.syncFlow(FaultToleranceOperation,InvocationContext,InterceptionPoint) (/smallrye-fault-tolerance-5.2.1.jar/io.smallrye.faulttolerance/FaultToleranceInterceptor.class:186)
FaultToleranceInterceptor.interceptCommand(InvocationContext) (/smallrye-fault-tolerance-5.2.1.jar/io.smallrye.faulttolerance/FaultToleranceInterceptor.class:163)
FaultToleranceInterceptor_Bean.intercept(InterceptionType,Object,InvocationContext) (Unknown Source:541)
InterceptorInvocation.invoke(InvocationContext) (/arc-2.4.2.Final.jar/io.quarkus.arc.impl/InterceptorInvocation.class:41)
AroundInvokeInvocationContext.proceed() (/arc-2.4.2.Final.jar/io.quarkus.arc.impl/AroundInvokeInvocationContext.class:50)
InvocationInterceptor.proceed(Invocation$Builder,InvocationContext,ManagedContext,InvocationTree) (/quarkus-arc-2.4.2.Final.jar/io.quarkus.arc.runtime.devconsole/InvocationInterceptor.class:62)
InvocationInterceptor.monitor(InvocationContext) (/quarkus-arc-2.4.2.Final.jar/io.quarkus.arc.runtime.devconsole/InvocationInterceptor.class:51)
InvocationInterceptor_Bean.intercept(InterceptionType,Object,InvocationContext) (Unknown Source:516)
InterceptorInvocation.invoke(InvocationContext) (/arc-2.4.2.Final.jar/io.quarkus.arc.impl/InterceptorInvocation.class:41)
AroundInvokeInvocationContext.proceed() (/arc-2.4.2.Final.jar/io.quarkus.arc.impl/AroundInvokeInvocationContext.class:50)
TransactionalInterceptorBase.invokeInCallerTx(InvocationContext,Transaction) (/quarkus-narayana-jta-2.4.2.Final.jar/io.quarkus.narayana.jta.runtime.interceptor/TransactionalInterceptorBase.class:302)
TransactionalInterceptorRequired.doIntercept(TransactionManager,Transaction,InvocationContext) (/quarkus-narayana-jta-2.4.2.Final.jar/io.quarkus.narayana.jta.runtime.interceptor/TransactionalInterceptorRequired.class:40)
TransactionalInterceptorBase.intercept(InvocationContext) (/quarkus-narayana-jta-2.4.2.Final.jar/io.quarkus.narayana.jta.runtime.interceptor/TransactionalInterceptorBase.class:57)
TransactionalInterceptorRequired.intercept(InvocationContext) (/quarkus-narayana-jta-2.4.2.Final.jar/io.quarkus.narayana.jta.runtime.interceptor/TransactionalInterceptorRequired.class:32)
TransactionalInterceptorRequired_Bean.intercept(InterceptionType,Object,InvocationContext) (Unknown Source:335)
InterceptorInvocation.invoke(InvocationContext) (/arc-2.4.2.Final.jar/io.quarkus.arc.impl/InterceptorInvocation.class:41)
AroundInvokeInvocationContext.perform(Object,Method,Function,Object[],List,Set) (/arc-2.4.2.Final.jar/io.quarkus.arc.impl/AroundInvokeInvocationContext.class:41)
InvocationContexts.performAroundInvoke(Object,Method,Function,Object[],List,Set) (/arc-2.4.2.Final.jar/io.quarkus.arc.impl/InvocationContexts.class:32)
MyService_Subclass.storeRetryEntity(int) (Unknown Source:838)
MyService.storeEntity(int) (/home/jk/projects/junk/code-with-quarkus/src/main/java/org/acme/MyService.java:39)
MyService_Subclass.storeEntity$$superforward1(int) (Unknown Source:416)
MyService_Subclass$$function$$4.apply(Object) (Unknown Source:35)
AroundInvokeInvocationContext.proceed() (/arc-2.4.2.Final.jar/io.quarkus.arc.impl/AroundInvokeInvocationContext.class:54)
InvocationInterceptor.proceed(Invocation$Builder,InvocationContext,ManagedContext,InvocationTree) (/quarkus-arc-2.4.2.Final.jar/io.quarkus.arc.runtime.devconsole/InvocationInterceptor.class:62)
InvocationInterceptor.monitor(InvocationContext) (/quarkus-arc-2.4.2.Final.jar/io.quarkus.arc.runtime.devconsole/InvocationInterceptor.class:51)
InvocationInterceptor_Bean.intercept(InterceptionType,Object,InvocationContext) (Unknown Source:516)
InterceptorInvocation.invoke(InvocationContext) (/arc-2.4.2.Final.jar/io.quarkus.arc.impl/InterceptorInvocation.class:41)
AroundInvokeInvocationContext.proceed() (/arc-2.4.2.Final.jar/io.quarkus.arc.impl/AroundInvokeInvocationContext.class:50)
TransactionalInterceptorBase.invokeInOurTx(InvocationContext,TransactionManager,RunnableWithException) (/quarkus-narayana-jta-2.4.2.Final.jar/io.quarkus.narayana.jta.runtime.interceptor/TransactionalInterceptorBase.class:132)
TransactionalInterceptorBase.invokeInOurTx(InvocationContext,TransactionManager) (/quarkus-narayana-jta-2.4.2.Final.jar/io.quarkus.narayana.jta.runtime.interceptor/TransactionalInterceptorBase.class:103)
TransactionalInterceptorRequired.doIntercept(TransactionManager,Transaction,InvocationContext) (/quarkus-narayana-jta-2.4.2.Final.jar/io.quarkus.narayana.jta.runtime.interceptor/TransactionalInterceptorRequired.class:38)
TransactionalInterceptorBase.intercept(InvocationContext) (/quarkus-narayana-jta-2.4.2.Final.jar/io.quarkus.narayana.jta.runtime.interceptor/TransactionalInterceptorBase.class:57)
TransactionalInterceptorRequired.intercept(InvocationContext) (/quarkus-narayana-jta-2.4.2.Final.jar/io.quarkus.narayana.jta.runtime.interceptor/TransactionalInterceptorRequired.class:32)
TransactionalInterceptorRequired_Bean.intercept(InterceptionType,Object,InvocationContext) (Unknown Source:335)
InterceptorInvocation.invoke(InvocationContext) (/arc-2.4.2.Final.jar/io.quarkus.arc.impl/InterceptorInvocation.class:41)
AroundInvokeInvocationContext.perform(Object,Method,Function,Object[],List,Set) (/arc-2.4.2.Final.jar/io.quarkus.arc.impl/AroundInvokeInvocationContext.class:41)
InvocationContexts.performAroundInvoke(Object,Method,Function,Object[],List,Set) (/arc-2.4.2.Final.jar/io.quarkus.arc.impl/InvocationContexts.class:32)
MyService_Subclass.storeEntity(int) (Unknown Source:750)
MyService.lambda$getEntityWithRetry$0() (/home/jk/projects/junk/code-with-quarkus/src/main/java/org/acme/MyService.java:27)
2145936178.get() (Unknown Source:-1)
CompletableFuture$AsyncSupply.run() (/java.base/java.util.concurrent/CompletableFuture.class:1700)
CompletableFuture$AsyncSupply.exec() (/java.base/java.util.concurrent/CompletableFuture.class:1692)
ForkJoinTask.doExec() (/java.base/java.util.concurrent/ForkJoinTask.class:290)
ForkJoinPool$WorkQueue.topLevelExec(ForkJoinTask,ForkJoinPool$WorkQueue,int) (/java.base/java.util.concurrent/ForkJoinPool.class:1020)
ForkJoinPool.scan(ForkJoinPool$WorkQueue,int) (/java.base/java.util.concurrent/ForkJoinPool.class:1656)
ForkJoinPool.runWorker(ForkJoinPool$WorkQueue) (/java.base/java.util.concurrent/ForkJoinPool.class:1594)
ForkJoinWorkerThread.run() (/java.base/java.util.concurrent/ForkJoinWorkerThread.class:183)
```
