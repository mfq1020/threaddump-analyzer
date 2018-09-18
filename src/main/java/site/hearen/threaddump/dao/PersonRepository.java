package site.hearen.threaddump.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import site.hearen.threaddump.entity.Person;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    Person findFirstByLastName(String lastName);
}
