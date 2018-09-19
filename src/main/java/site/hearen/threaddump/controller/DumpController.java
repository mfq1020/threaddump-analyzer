package site.hearen.threaddump.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import site.hearen.threaddump.entity.ThreadDo;
import site.hearen.threaddump.service.DumpService;
import site.hearen.threaddump.vo.ThreadDumpVo;

@RestController
@RequestMapping("dump/thread")
public class DumpController {
    @Autowired
    DumpService dumpService;

    @GetMapping("/hello")
    public String sayHi() {
        return "Hello";
    }

    @GetMapping("/parse/seq/{filename}")
    public ThreadDumpVo parseSeq(@PathVariable String filename) throws Exception {
        return dumpService.parse(filename, false);
    }

    @GetMapping("/parse/para/{filename}")
    public ThreadDumpVo parsePara(@PathVariable String filename) throws Exception {
        return dumpService.parse(filename, true);
    }

    @GetMapping("/testparser")
    public void testParser() throws Exception {
        dumpService.testParser();
    }

    @GetMapping("/old/{dumpId}")
    public ThreadDumpVo getOldVo(@PathVariable Long dumpId) {
        return dumpService.getOldVo(dumpId);
    }

    @GetMapping("/{dumpId}/simple-deadlock")
    public List<List<String>> getSimpleDeadLockLoops(@PathVariable Long dumpId) {
        return dumpService.getSimpleDeadLockLoops(dumpId);
    }

    @PutMapping("/{dumpId}/stackTrace")
    public List<ThreadDo> listStackTrace(@PathVariable Long dumpId, @RequestBody String stack) {
        return dumpService.getStackTrace(dumpId, stack);
    }

    @PutMapping("/{dumpId}/stackTrace/{state}")
    public List<ThreadDo> listStackWithState(@PathVariable Long dumpId, @PathVariable String state,
                                             @RequestBody String stack) {
        return dumpService.getStackWithState(dumpId, stack, state);
    }

    @PutMapping("/{dumpId}/mostUsedMethod")
    public List<ThreadDo> getMostUsedMethodList(@PathVariable Long dumpId,
                                                @RequestBody String mostUsedMethod) {
        return dumpService.getMostUsedMethod(dumpId, mostUsedMethod);
    }

    @GetMapping("/{dumpId}/cpuConsuming")
    public Map<String, List<ThreadDo>> getCpuConsuming(@PathVariable Long dumpId) {
        return dumpService.getCpuConsuming(dumpId);
    }

    @GetMapping("/{dumpId}/blocking")
    public Map<String, List<ThreadDo>> getBlocking(@PathVariable Long dumpId) {
        return dumpService.getBlocking(dumpId);
    }

    @GetMapping("/{dumpId}/gc")
    public List<ThreadDo> getGc(@PathVariable Long dumpId) {
        return dumpService.getGcThreads(dumpId);
    }

    @GetMapping("/{dumpId}/finalizer")
    public List<ThreadDo> getFinalizer(@PathVariable Long dumpId) {
        return dumpService.getFinalizerThreads(dumpId);

    }
}
