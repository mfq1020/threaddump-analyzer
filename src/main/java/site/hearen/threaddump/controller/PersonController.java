package site.hearen.threaddump.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import site.hearen.threaddump.service.PersonService;
import site.hearen.threaddump.vo.PersonVo;

@RestController
@RequestMapping("/")
public class PersonController {
    @Autowired
    private PersonService personService;

    @GetMapping("/{lastName}")
    public PersonVo sayHello(@PathVariable String lastName) {
        return personService.getByLastName(lastName);
    }
}
