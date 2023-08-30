package com.devhub.api.domain.especialidades;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Especialidades {

    private String especialidade1;
    private String especialidade2;
    private String especialidade3;

//    public Especialidades() {
//        this.especialidade1 = especialidade1;
//        this.especialidade2 = especialidade2;
//        this.especialidade3 = especialidade3;
//    }
}
