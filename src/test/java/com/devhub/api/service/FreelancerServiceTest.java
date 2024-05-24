package com.devhub.api.service;

import com.amazonaws.services.s3.AmazonS3;
import com.devhub.api.domain.avaliacao_freelancer.AvaliacaoFreelancerRepository;
import com.devhub.api.domain.contratante.ContratanteRepository;
import com.devhub.api.domain.contratante.dto.ContratanteValidacaoDTO;
import com.devhub.api.domain.especialidade.Especialidade;
import com.devhub.api.domain.freelancer.Freelancer;
import com.devhub.api.domain.freelancer.FreelancerRepository;
import com.devhub.api.domain.freelancer.dto.CreateFreelancerDTO;
import com.devhub.api.domain.freelancer.dto.FreelancerValidacaoDTO;
import com.devhub.api.domain.freelancer.dto.UpdateFreelancerDTO;
import com.devhub.api.domain.funcao.Funcao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = com.devhub.api.Application.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@ComponentScan(basePackages = "com.devhub.api")
public class FreelancerServiceTest {

    @Autowired
    private FreelancerService service;
    @MockBean
    private FreelancerRepository freelancerRepo;
    @MockBean
    private ContratanteRepository contratanteRepo;
    @MockBean
    private AmazonS3 s3;

    private CreateFreelancerDTO createFreelancerMock;
    private Freelancer freelancerMock;
    private AvaliacaoFreelancerRepository avaliacaoRepo;

    @BeforeEach
    public void setUp() {
        this.createFreelancerMock = new CreateFreelancerDTO(
                "John Doe",
                "11012753085",
                "11987654321",
                "john.doe@example.com",
                "123456",
                Funcao.DESENVOLVEDOR_BACKEND,
                100.0,
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                "Senior"
        );

        this.freelancerMock = new Freelancer(createFreelancerMock);
        this.freelancerMock.setId(1L);
    }

    @Test
    @DisplayName("Realizar cadastro com sucesso")
    void cadastrarFreelancer() {
        CreateFreelancerDTO data = this.createFreelancerMock;

        List<FreelancerValidacaoDTO> dadosFreelancer = new ArrayList<>();
        List<ContratanteValidacaoDTO> dadosContratante = new ArrayList<>();

        when(freelancerRepo.validarDadosUnicos()).thenReturn(dadosFreelancer);
        when(contratanteRepo.validarDadosUnicos()).thenReturn(dadosContratante);

        var response = service.cadastrarFreelancer(data);

        verify(freelancerRepo, times(1)).validarDadosUnicos();
        verify(contratanteRepo, times(1)).validarDadosUnicos();
        verify(freelancerRepo, times(1)).save(any(Freelancer.class));

        assertNotNull(response);
        assertTrue(new BCryptPasswordEncoder().matches("123456", response.getSenha()));
    }

    @Test
    @DisplayName("Realizar cadastro com dados já cadastrados")
    void cadastrarFreelancerJaCadastrado() {
        CreateFreelancerDTO data = this.createFreelancerMock;

        List<FreelancerValidacaoDTO> dadosFreelancer = new ArrayList<>();
        dadosFreelancer.add(new FreelancerValidacaoDTO("john.doe@example.com", "11012753085", "11987654321"));

        List<ContratanteValidacaoDTO> dadosContratante = new ArrayList<>();

        when(freelancerRepo.validarDadosUnicos()).thenReturn(dadosFreelancer);
        when(contratanteRepo.validarDadosUnicos()).thenReturn(dadosContratante);

        var response = assertThrows(ResponseStatusException.class, () -> service.cadastrarFreelancer(data));

        verify(freelancerRepo, times(1)).validarDadosUnicos();
        verify(contratanteRepo, times(1)).validarDadosUnicos();
        verify(freelancerRepo, times(0)).save(any(Freelancer.class));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Dados já cadastrados: E-mail | Telefone | CPF", response.getReason());
    }

    @Test
    @DisplayName("Mostrar freelancers com lista populada")
    void mostrarFreelancersListaPopulada() {
        Freelancer freelancer1 = freelancerMock;
        freelancer1.setId(1L);
        freelancer1.setEspecialidades(Arrays.asList(new Especialidade("Java", freelancer1), new Especialidade("Spring", freelancer1)));
        freelancer1.setSenioridade("Senior");
        freelancer1.setValorHora(100.0);

        Freelancer freelancer2 = freelancerMock;
        freelancer2.setId(2L);
        freelancer2.setNome("Freelancer 2");
        freelancer2.setFuncao(Funcao.WEB_DESIGNER);
        freelancer2.setEspecialidades(Arrays.asList(new Especialidade("Photoshop", freelancer2), new Especialidade("Illustrator", freelancer2)));
        freelancer2.setSenioridade("Junior");
        freelancer2.setValorHora(80.0);

        List<Freelancer> freelancers = Arrays.asList(freelancer1, freelancer2);

        when(freelancerRepo.findAllByAtivoTrue()).thenReturn(freelancers);

        var response = service.getFreelancers();

        verify(freelancerRepo, times(1)).findAllByAtivoTrue();
        assertNotNull(response);
        assertEquals(2, response.size());
    }

    @Test
    @DisplayName("Mostrar freelancers com lista vazia")
    void mostrarFreelancersListaVazia() {
        List<Freelancer> freelancers = new ArrayList<>();
        when(freelancerRepo.findAllByAtivoTrue()).thenReturn(freelancers);

        var response = service.getFreelancers();

        verify(freelancerRepo, times(1)).findAllByAtivoTrue();
        assertNotNull(response);
        assertEquals(0, response.size());
    }

    @Test
    @DisplayName("Mostrar freelancer por ID")
    void mostrarFreelancerPorId() {
        Long id = 1L;
        when(freelancerRepo.findById(id)).thenReturn(Optional.of(this.freelancerMock));

        var response = service.getFreelancerById(id);

        verify(freelancerRepo, times(1)).findById(id);
        assertNotNull(response);
    }

    @Test
    @DisplayName("Mostrar freelancer por ID inexistente")
    void mostrarFreelancerPorIdInexistente() {
        Long id = 1L;
        when(freelancerRepo.findById(id)).thenReturn(Optional.empty());

        var response = assertThrows(ResponseStatusException.class, () -> service.getFreelancerById(id));

        verify(freelancerRepo, times(1)).findById(id);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Atualizar freelancer com sucesso")
    void atualizarFreelancer() {
        Long id = 1L;
        UpdateFreelancerDTO data = new UpdateFreelancerDTO(
                "John Doe",
                "11012753085",
                "11987654321",
                Funcao.DESENVOLVEDOR_FULLSTACK,
                "123456",
                125.0
        );

        when(freelancerRepo.getReferenceById(id)).thenReturn(this.freelancerMock);

        var response = service.atualizar(data, id);

        verify(freelancerRepo, times(1)).getReferenceById(id);
        assertNotNull(response);
        assertEquals(Funcao.DESENVOLVEDOR_FULLSTACK, response.getFuncao());
        assertEquals(125.0, response.getValorHora());
    }

    @Test
    @DisplayName("Atualizar freelancer inexistente")
    void atualizarFreelancerInexistente() {
        Long id = 1L;
        UpdateFreelancerDTO data = new UpdateFreelancerDTO(
                "John Doe",
                "11012753085",
                "11987654321",
                Funcao.DESENVOLVEDOR_FULLSTACK,
                "123456",
                125.0
        );

        when(freelancerRepo.getReferenceById(id)).thenReturn(null);

        var response = assertThrows(ResponseStatusException.class, () -> service.atualizar(data, id));

        verify(freelancerRepo, times(1)).getReferenceById(id);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Excluir freelancer")
    void excluirFreelancer() {
        Long id = 1L;

        when(freelancerRepo.getReferenceById(id)).thenReturn(this.freelancerMock);
        service.excluir(id);

        verify(freelancerRepo, times(1)).getReferenceById(id);
    }

    @Test
    @DisplayName("Excluir freelancer inexistente")
    void excluirFreelancerInexistente() {
        Long id = 1L;

        when(freelancerRepo.getReferenceById(id)).thenReturn(null);

        var response = assertThrows(ResponseStatusException.class, () -> service.excluir(id));

        verify(freelancerRepo, times(1)).getReferenceById(id);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Cadastrar especialidades de um freelancer")
    void cadastrarEspecialidades() {
        Long id = 1L;
        List<String> especialidades = Arrays.asList("Java", "JavaScript", "Angular");

        when(freelancerRepo.findById(id)).thenReturn(Optional.of(this.freelancerMock));

        var response = service.cadastrarEspecialidades(especialidades, id);

        verify(freelancerRepo, times(1)).findById(id);
        assertNotNull(response);
        assertEquals(3, response.size());
    }
}
