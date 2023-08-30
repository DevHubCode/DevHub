package com.devhub.api.domain.freelancer;

import com.devhub.api.domain.especialidades.EspecialidadesData;
import com.devhub.api.domain.funcao.Funcao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.br.CPF;

public record CreateFreelancerData(
        @NotBlank
        String nome,
        @NotBlank
        @CPF
        String cpf,
        @NotBlank
        @Pattern(regexp = "\\d{11}")
        String telefone,
        @Email
        String email,
        @NotNull
        Funcao funcao,
        @NotBlank
        Double valorHora,
        @NotNull
        @Valid
        EspecialidadesData especialidades,
        @NotBlank
        String descricao



) {
}
