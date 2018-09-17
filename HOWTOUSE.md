# threaddump-analyzer
Analyze the thread dumps and provide a meaningful result to help online troubleshooting

Thread dump is a very helpful tool to assist trouble shooting and application performance optimisation. Here we are to discuss how to generate, understand and analyze thread dumps properly.

## How to take a thread dump

Find the PID for the application using `jps -l` and then generate the thread dump using the following commands: 

```
jstack 37320 > /opt/tmp/threadDump.txt
```

Or 

```
jcmd 37320 Thread.print > /opt/tmp/threadDump.txt
```

For more details, you may check [other options](https://dzone.com/articles/how-to-take-thread-dumps-7-options).

## Basics

A complete thread dump block describing the details for a thread status can be as follows:

```
"Reference Handler" #2 daemon prio=10 os_prio=0 tid=0x00007fd758083000 nid=0x3107 in Object.wait() [0x00007fd73d052000]
   java.lang.Thread.State: WAITING (on object monitor)
	at java.lang.Object.wait(Native Method)
	- waiting on <0x00000006c72088b8> (a java.lang.ref.Reference$Lock)
	at java.lang.Object.wait(Object.java:502)
	at java.lang.ref.Reference.tryHandlePending(Reference.java:191)
	- locked <0x00000006c72088b8> (a java.lang.ref.Reference$Lock)
	at java.lang.ref.Reference$ReferenceHandler.run(Reference.java:153)
```

- thread name: "Reference Handler"
- daemon or not: if there is the keyword `daemon`, then the thread is a `daemon` thread;
- thread priority (`prio=10`): thread priority in JVM is in range of 1 to 10;
- native thread priority (`os_prio=0`): each thread in JVM will be mapped to a native thread whose priority could be different from the JVM thread priority, which depends on the OS thread scheduling mechanism;
- thread ID (`tid=0x00007fd758083000`): the unique ID to identify the thread;
- native thread ID (`nid=0x3107`): the unique thread ID in OS level;
- thread status (`java.lang.Thread.State: WAITING`): there are five thread states in JVM including NEW, RUNNABLE, BLOCKED, WAITING, and TIMED_WATING;
- call stack trace: each line starts with `at` here;
- locks: each thread can hold and wait for locks, as for this thread it's now waiting for lock `<0x00000006c72088b8>` and holding the lock `<0x00000006c72088b8>`.

Understanding the basics mentioned is far from enough to understand the overview of the applications. To understand it further and better, we have to understand the `RUNNABLE` state for a thread is actually running at most of the time, blocking threads are always a bad sign and more. 

## Advanced

### RUNNABLE
There is no **RUNNING** state in JVM as [Thread.State](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.State.html) mentioned. 

Threads in ‘runnable’ state consume CPU. So when you are analyzing thread dumps for high CPU consumption, threads in ‘runnable’ state should be thoroughly reviewed.Typically in thread dumps several threads are classified in ‘RUNNABLE’ state. But in reality several of them wouldn’t be actually running, rather they would be just waiting. But still, JVM classifies them in ‘RUNNABLE’ state. You need to learn to differentiate from really running threads with pretending/misleading RUNNABLE threads.

In the thread dump, you can locate the `nid` (native thread id) to check the real state for the thread to locate the most cpu-consuming thread and then via the stack trace, you can further locate the method that consuming the CPU to make sure whether it's working as expected.

If it's not, perhaps you should refactor your code to make the `runnable` thread hand over the `runnable` privilege to other busier threads. 

```
top -Hp <PID>
```

### Deadlock

After thread dump, it's easy to locate the *deadlock* but it's still not easy to check **how many** threads are holding the lock,  **how many** are waiting for the lock and meantime the call stack traces for the *holders* and *waiters*. 

1. A ripple effect caused the entire application to become unresponsive, after counting the *holders* and *waiters*, we have a chance to solve this **unresponsiveness** even online by kill it directly (as long as we have recovery mechanism or strong fault tolerability)

2. It's important for us to understand this so we can refactor the code to **remove** the error-prone inter-leaving logic. 

### Blocked

Threads blocked are not always a good sign. When bad things (low responsiveness, high memory usage) come up, it's necessary to check how many threads are **blocked** and **by what** they are blocked to locate the root cause to do troubleshooting, perhaps the `synchronised` scope is too wide. We can also utilize this feature to do performance optimization before going online.

### Infinite Loop Detection

When there are some badly coded recursive method (directly or indirectly), the CPU will spike up doing some senseless work reducing the responsiveness dramatically. 

By the infinite loop call stack trace, actually we can effectively locate this issue. There are two undeniable features for this kind of issue:

1. `RUNNABLE` state;
2. same call stack trace and same thread id among different thread dumps;

> The dumps will be taken by an interval of 15 seconds to ensure accuracy. 


An example for not-thread-safe operation indirectly causing this issue:

> When multiple threads try to access HashMap’s get() and put() APIs concurrently it would cause threads go into infinite looping at `java.util.HashMap.put(HashMap.java:374)`. This problem can be addressed by replacing the HashMap with ConcurrentHashMap.



### Call Stack Trace

Why aggregating the call stack trace becomes so essential? 

1. Understanding **how many** threads are working on the same call stack trace will enable you to locate the **bottleneck** of the performance, perhaps it's third-party service, database or other shared resources. 

2. It's working as expected, the threads are managed well, not out of control, not randomly creating thread pools without enough caution.

> Without proper thread pool selected, for example using parallelStream (using default ForkJoinPool whose size is restricted to available CPU core counts) directly to process I/O blocking related tasks, lots of tasks might queued to dramatically consume the memory; via the call stack trace, we can easily understand where the weak points lie and handle them properly.

### Most Used Methods

Sometimes using *Call Strack Trace* alone may not be accurate and that's why we also need **Most Used Methods** to help us to filter out the interfering calls in the stack trace.

In another way around, the bottleneck also can be directly detected by the **most used methods** when lots of threads are **gathering up** in the same method.


### Group

Normally different services in the application will be using different thread pools. By grouping the threads, it becomes easier to locate in which service the issue lies. 

> Without proper thread pool size limitation (for example using newCachedThreadPool), lots of senseless threads will be created to swallow the CPU and memory resulting in low responsiveness. 

### Rapid Additive 

Newly **rapidly added threads** should get more attention since this abnormal rapidness among several consecutive thread dumps with short intervals might be directly tracked by the newly added threads.  

> We can present it in comparison table by *red plus*.


### Thread Count

1. Gauge severity level when bad things happen or used as an health indicator when progressing well;
2. Compare the thread count level between different releases to make sure it's working as usual;


### Daemon

[Daemon threads](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.html#setDaemon-boolean-) provide services to user thread for background supporting task. Daemon threads are useful for background supporting tasks such as garbage collection, releasing memory of unused objects and removing unwanted entries from the cache and logging or monitoring. Most of the JVM created threads are daemon threads.

JVM **doesn't wait** for any daemon thread to finish before existing, so **daemon** threads are not recommended for I/O tasks which might result in corrupted file or inconsistent database or the like.

Any thread inherits the daemon status of the thread that created it.


### GC Threads 

Different types of GC configurations might result in different amounts of GC threads. Monitoring the GC threads is necessary to make sure no senseless GC threads are created unknowingly. 

To ensure the GC threads created reasonably. 

The following algorithms should be checked.

#### Parallel GC
If you are using Parallel GC algorithm, then number of GC threads is controlled by `-XX:ParallelGCThreads` property. 
Default value for `-XX:ParallelGCThreads` on Linux/x86 machine is derived based on the formula:
```
if (num of processors <=8) {
   return num of processors; } else {
   return 8+(num of processors-8)*(5/8);
}
```
So if your JVM is running on server with 32 processors, then `ParallelGCThread` value is going to be:
```
23 = (8 + (32 – 8)*(5/8)).
```
#### CMS GC
If you are using CMS GC algorithm, then number of GC threads is controlled by `-XX:ParallelGCThreads` and `-XX:ConcGCThreads` properties. 
Default value of `-XX:ConcGCThreads` is derived based on the formula:
```
max((ParallelGCThreads+2)/4, 1)
```
So if your JVM is running on server with 32 processors, then ParallelGCThread value is going to be: 
```
23 = (8 + (32 – 8)*(5/8))
```
ConcGCThreads value is going to be: 6.
So total GC thread count is: 
```
29 = (23 + 6)
```

#### G1 GC

If you are using G1 GC algorithm, then number of GC threads is controlled by `-XX:ParallelGCThreads`, `-XX:ConcGCThreads`, `-XX:G1ConcRefinementThreads` properties. 
Default value of `-XX:G1ConcRefinementThreads` is derived based on the formula:
```
ParallelGCThreads+1
```
So if your JVM is running on server with 32 processors, then
```
ParallelGCThread value is going to be: 23 = (8 + (32 – 8)*(5/8))
ConcGCThreads value is going to be: 6
G1ConcRefinementThreads value is going to be 24 = (23 + 1)
So total GC thread count is: 53 = (23 + 6 + 24)
```

### Finalizer Threads

Badly designed `finally` might cause the *Finalizer* thread to be blocked which will cause the internal queue of java.lang.ref.Finalize *rapidly* grow resulting in memory consumption spike. When it comes to memory consumption dramatic increase, it's also necessary to check the stack trace of the *Finalizer* thread.

