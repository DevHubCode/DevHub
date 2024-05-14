package com.devhub.api.domain.freelancer;

import com.devhub.api.domain.freelancer.dto.FreelancerValidacaoDTO;
import com.devhub.api.domain.freelancer.dto.ListaFreelancerDTO;
import com.devhub.api.domain.freelancer.dto.PerfilFreelancerDTO;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface FreelancerRepository extends JpaRepository<Freelancer, Long> {
    List<Freelancer> findAllByAtivoTrue();
    List<Freelancer> findAll();

    UserDetails findByEmail(String email);

    Freelancer findByNomeAndTelefoneAndEmailAndValorHoraAndSenioridade(String nomeFreelancer, String telefone, String email, Double valorHora, String senioridade);

    @Query("""
    select new com.devhub.api.domain.freelancer.dto.FreelancerValidacaoDTO(f.email, f.cpf, f.telefone)
    from Freelancer f
    """)
    List<FreelancerValidacaoDTO> validarDadosUnicos();

    @Modifying
    @Transactional
    @Query("""
        update Freelancer f set f.imagem = ?1
        where f.id = ?2
            """)
    int atualizarFoto(byte[] foto, Long idFreelancer);

    @Query("""
    select
        f
    from
        Especialidade e
    join
        e.freelancer f
    where
        LOWER(f.funcao) like LOWER(CONCAT(:pesquisa, '%'))
    or
        LOWER(e.descricao) like LOWER(CONCAT(:pesquisa, '%'))
    """)
    List<Freelancer> getFreelancersBySearch(String pesquisa);

    @Query("""
    select
        f
    from
        Especialidade e
    join
        e.freelancer f
    where
        LOWER(e.descricao) in :especialidades
    and
        f.id <> :compareTo
    """)
    List<Freelancer> compareFreelancerBySpecialties(List<String> especialidades, Long compareTo);

    @Query("""
    select f.imagem from Freelancer f where f.id = ?1
    """)
    byte[] getImagemById(int codigo);
}
