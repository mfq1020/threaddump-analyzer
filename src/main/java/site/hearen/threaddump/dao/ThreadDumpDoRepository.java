package site.hearen.threaddump.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import site.hearen.threaddump.entity.ThreadDumpDo;


public interface ThreadDumpDoRepository extends JpaRepository<ThreadDumpDo, Long> {
}
