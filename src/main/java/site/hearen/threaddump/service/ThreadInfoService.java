package site.hearen.threaddump.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import site.hearen.threaddump.dao.ThreadDoRepository;
import site.hearen.threaddump.entity.ThreadDo;
import site.hearen.threaddump.enums.StateEnum;

@Service
@Slf4j
public class ThreadInfoService {
    @Autowired
    private ThreadDoRepository threadDoRepository;

    public ThreadDo getInfo(Long infoId) {
        return threadDoRepository.getOne(infoId);
    }

    public List<ThreadDo> getStateInfoList(Long dumpId, String state) {
        return threadDoRepository.findAllByDumpDo_IdAndStateEnum(dumpId, StateEnum.valueOf(state));
    }

    public List<ThreadDo> getGroupInfoList(Long dumpId, String groupName) {
        return threadDoRepository.findAllByDumpDo_IdAndNameStartsWith(dumpId, groupName);
    }

    public List<ThreadDo> getDeamonOrNondaemon(Long dumpId, boolean isDaemon) {
        return threadDoRepository.findAllByDumpDo_IdAndDaemon(dumpId, isDaemon);
    }

    public List<ThreadDo> getLockHolders(Long dumpId, String lock) {
        List<ThreadDo> dos = threadDoRepository.findAllByDumpDoIdAndLocksHeldContains(dumpId, lock);
        return dos;
    }

    public List<ThreadDo> getLockWaiters(Long dumpId, String lock) {
        return threadDoRepository.findAllByDumpDoIdAndLocksWaitingContains(dumpId, lock);
    }
}
