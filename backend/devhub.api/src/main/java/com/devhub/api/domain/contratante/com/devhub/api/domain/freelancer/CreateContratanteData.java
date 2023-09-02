package com.devhub.api.domain.freelancer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.br.CNPJ;


public record CreateContratanteData(
        @NotBlank
        String nome,
        @NotBlank
        @CNPJ
        String cnpj,
        @NotBlank
        @Pattern(regexp = "\\d{14}")
        String telefone,
        @Email
        String email,
        @NotNull
        Integer contratacoes
) {
}