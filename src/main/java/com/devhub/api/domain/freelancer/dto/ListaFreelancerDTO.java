package com.devhub.api.domain.freelancer.dto;

import com.devhub.api.domain.avaliacao_freelancer.AvaliacaoFreelancer;
import com.devhub.api.domain.especialidade.Especialidade;
import com.devhub.api.domain.freelancer.Freelancer;
import com.devhub.api.domain.funcao.Funcao;
import lombok.Data;
import lombok.Getter;

import java.util.List;


public record ListaFreelancerDTO (
        Long id,
        String nome,
        byte[] imagem,
        Funcao funcao,
        List<Especialidade> especialidades,
        String senioridade,
        Double valorHora,
        Double nota,
        String descricao,
        String telefone
){
    public ListaFreelancerDTO(Freelancer freelancer, Double nota) {
        this(freelancer.getId(), freelancer.getNome(), freelancer.getImagem(),
                freelancer.getFuncao(),freelancer.getEspecialidades(), freelancer.getSenioridade(), freelancer.getValorHora(),
                nota, freelancer.getDescricao(),freelancer.getTelefone());
    }
}
