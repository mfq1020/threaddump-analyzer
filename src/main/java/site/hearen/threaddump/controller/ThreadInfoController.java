package site.hearen.threaddump.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import site.hearen.threaddump.entity.ThreadDo;
import site.hearen.threaddump.service.DumpService;
import site.hearen.threaddump.service.ThreadInfoService;

@RestController
@RequestMapping("dump/thread-info")
public class ThreadInfoController {
    @Autowired
    DumpService dumpService;

    @Autowired
    ThreadInfoService infoService;

    @GetMapping("/{infoId}")
    public ThreadDo getThreadInfo(@PathVariable Long infoId) {
        return infoService.getInfo(infoId);
    }

    @GetMapping("/{dumpId}/state/{state}")
    public List<ThreadDo> listState(@PathVariable Long dumpId, @PathVariable String state) {
        return infoService.getStateInfoList(dumpId, state);
    }

    @GetMapping("/{dumpId}/pool/{groupName}")
    public List<ThreadDo> listByGroupName(@PathVariable Long dumpId, @PathVariable String groupName) {
        return infoService.getGroupInfoList(dumpId, groupName);
    }

    @GetMapping("/{dumpId}/daemon/{isDaemon}")
    public List<ThreadDo> listDaemonOrNondaemon(@PathVariable Long dumpId, @PathVariable boolean isDaemon) {
        return infoService.getDeamonOrNondaemon(dumpId, isDaemon);
    }

    @GetMapping("/{dumpId}/lockholder/{lock}")
    public List<ThreadDo> listLockHolders(@PathVariable Long dumpId, @PathVariable String lock) {
        return infoService.getLockHolders(dumpId, lock);
    }

    @GetMapping("/{dumpId}/lockwaiter/{lock}")
    public List<ThreadDo> listLockWaiters(@PathVariable Long dumpId, @PathVariable String lock) {
        return infoService.getLockWaiters(dumpId, lock);
    }

}
