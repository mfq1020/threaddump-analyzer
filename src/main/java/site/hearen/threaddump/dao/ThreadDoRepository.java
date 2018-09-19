package site.hearen.threaddump.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import site.hearen.threaddump.entity.ThreadDo;
import site.hearen.threaddump.enums.StateEnum;


public interface ThreadDoRepository extends JpaRepository<ThreadDo, Long> {
    //CHECKSTYLE:OFF
    List<ThreadDo> findAllByDumpDo_IdAndStateEnum(Long dumpId, StateEnum stateEnum);

    List<ThreadDo> findAllByDumpDo_IdAndNameStartsWith(Long dumpId, String groupName);

    List<ThreadDo> findAllByDumpDo_IdAndDaemon(Long dumpId, boolean isDaemon);
    //CHECKSTYLE:ON

    @Query(value = "select * from thread_info as t where t.thread_dump_id = ?1 and t.locks_held like %?2%",
            nativeQuery = true)
    List<ThreadDo> findAllByDumpDoIdAndLocksHeldContains(Long dumpId, String lock);


    @Query(value = "select * from thread_info as t where t.thread_dump_id = :dumpId and t.locks_waiting like %:lock%",
            nativeQuery = true)
    List<ThreadDo> findAllByDumpDoIdAndLocksWaitingContains(@Param("dumpId") Long dumpId, @Param("lock") String lock);
}
