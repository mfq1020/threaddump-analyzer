package site.hearen.threaddump.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import site.hearen.threaddump.entity.S3Info;

public interface S3Repository extends JpaRepository<S3Info, Long> {
}
