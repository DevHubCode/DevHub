package com.devhub.api.domain.contratante;

import com.devhub.api.domain.contratante.dto.ContratanteValidacaoDTO;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface ContratanteRepository extends JpaRepository<Contratante, Long> {
    List<Contratante> findAllByAtivoTrue();
    UserDetails findByEmail(String email);

    Contratante findByCnpj(String cnpj);

    @Query("""
    select new com.devhub.api.domain.contratante.dto.ContratanteValidacaoDTO(c.email, c.cnpj, c.telefone)
    from Contratante c
    """)
    List<ContratanteValidacaoDTO> validarDadosUnicos();

    @Modifying
    @Transactional
    @Query("""
        update Contratante c set c.imagem = ?1
        where c.id = ?2     
            """)
    int atualizarFoto(byte[] foto, Long idFreelancer);

    @Query("""
    select c.imagem from Contratante c where c.id = ?1
    """)
    byte[] getImagemById(int codigo);
}
