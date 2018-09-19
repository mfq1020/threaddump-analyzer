package site.hearen.thread.dump.analyzer.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import site.hearen.thread.dump.analyzer.entity.S3Info;

public interface S3Repository extends JpaRepository<S3Info, Long> {
}
