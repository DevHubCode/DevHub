package com.devhub.api.domain.freelancer;

import com.devhub.api.domain.funcao.Funcao;

public record DetailFreelancerData(String nome, String telefone, String email, Integer contratacoes, Funcao funcao, Double valorHora, String descricao, Boolean ativo) {
    public DetailFreelancerData(Freelancer freelancer){
        this(
        freelancer.getNome(),
        freelancer.getTelefone(),
        freelancer.getEmail(),
        freelancer.getContratacoes(),
        freelancer.getFuncao(),
        freelancer.getValorHora(),
        freelancer.getDescricao(),
        freelancer.getAtivo());
    }
}