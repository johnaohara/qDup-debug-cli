# QDup cli debugger

1. Start qDup in debug mode

```
$ java -jar /path/to/qdup-uber.jar -b /tmp/testQdup qdup.yaml --debug

11:00:56.348 [main] INFO  io.hyperfoil.tools.qdup.QDup - Starting with output path = /tmp/qdupTest/20220126_110055
11:00:56.456 [main] INFO  io.hyperfoil.tools.qdup.JsonServer - listening at localhost.localdomain:31337
11:00:56.526 [main] INFO  io.hyperfoil.tools.qdup.DebugServer - listening at localhost.localdomain:21337
11:00:56.527 Running qDup version 0.6.10-SNAPSHOT @ 9b49154
11:00:56.527 [main] INFO  io.hyperfoil.tools.qdup.Run - Waiting for debugger to connect
```


2. start debugger cli

```
$ jbang ./src/main/java/io/hyperfoil/tools/QDupCli.java
[qDup]$
```

![qDup Debugging Example](./docs/images/qDupDebug.gif)

## Using Debugger

### Set Breakpoint

```shell
[qDup]$ breakpoint 4
ok
```

### Get list of breakpoints
```
[qDup]$ breakpoints
{"breakpoints": [4]}
```

### Start execution

```shell
[qDup]$ start
Debug session started
```

### Resume Execution

```shell
[qDup]$ resume
Debug session resumed
```

### quit debugger

```shell
[qDup]$ quit
```

### Inspect State

```shell
[qDup]$ state
{"COUNTER":3,"GREETING":"Hello qDup!","USER":"johara","HOST":"localhost","localhost":{"another-script":{"id=17":{"COUNTER":21}},"hello-qdup":{"id=15":{"GREETING":"Local Greeting!"}}}}
```

### Evaluate State Expression

```shell
[qDup]$ eval ${{RUN.COUNTER}}
{"result": "3"}
[qDup]$ eval ${{=${{RUN.COUNTER}}+100}}
{"result": "103"}
```

### Error in evaluation

```shell
[qDup]$ eval ${{=${{COUNTER}}+100id}}
{"result": "${{=${{COUNTER}}+100id}}"}
```

corresponding error message in qDup output

```shell
11:02:00.927 [vert.x-eventloop-thread-1] ERROR io.hyperfoil.tools.qdup.cmd.Cmd - Failed to evaluate JS: SyntaxError: <function>:1:13 Missing space after numeric literal
return 21+100id
             ^
```