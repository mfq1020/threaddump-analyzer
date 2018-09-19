package site.hearen.thread.dump.analyzer.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import site.hearen.thread.dump.analyzer.entity.ThreadDumpDo;


public interface ThreadDumpDoRepository extends JpaRepository<ThreadDumpDo, Long> {
}
