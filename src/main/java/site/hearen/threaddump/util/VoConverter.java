package site.hearen.threaddump.util;

import site.hearen.threaddump.entity.Person;
import site.hearen.threaddump.vo.PersonVo;

public final class VoConverter {
    private VoConverter() {

    }

    public static PersonVo getVo(Person person) {
        return PersonVo.builder()
                .fullName(String.format("%s. %s", person.getFirstName(), person.getLastName()))
                .build();
    }
}
