package site.hearen.threaddump.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import site.hearen.threaddump.dao.PersonRepository;
import site.hearen.threaddump.util.VoConverter;
import site.hearen.threaddump.vo.PersonVo;

@Service
public class PersonService {
    @Autowired
    private PersonRepository personRepository;

    public PersonVo getByLastName(String lastName) {
        return VoConverter.getVo(personRepository.findFirstByLastName(lastName));
    }
}
