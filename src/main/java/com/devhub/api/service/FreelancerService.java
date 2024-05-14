package com.devhub.api.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.Tag;
import com.devhub.api.domain.avaliacao_freelancer.AvaliacaoFreelancerRepository;
import com.devhub.api.domain.contratante.ContratanteRepository;
import com.devhub.api.domain.contratante.dto.ContratanteValidacaoDTO;
import com.devhub.api.domain.especialidade.Especialidade;
import com.devhub.api.domain.especialidade.EspecialidadeDTO;
import com.devhub.api.domain.especialidade.EspecialidadeRepository;
import com.devhub.api.domain.freelancer.*;
import com.devhub.api.domain.freelancer.dto.*;
import com.devhub.api.domain.usuario.UserRole;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FreelancerService {

    @Autowired
    private FreelancerRepository repository;
    @Autowired
    private EspecialidadeRepository especialidadeRepository;
    @Autowired
    private ContratanteRepository contratanteRepository;
    @Autowired
    private AvaliacaoFreelancerRepository avaliacaoRepo;

    @Value("${bucketName}")
    private String bucketName;
    private final AmazonS3 s3;
    public FreelancerService(AmazonS3 s3) {
        this.s3 = s3;
    }

    @Transactional
    public Freelancer cadastrarFreelancer(CreateFreelancerDTO data) {
        
        List<FreelancerValidacaoDTO> dadosFreelancer = repository.validarDadosUnicos();
        List<ContratanteValidacaoDTO> dadosContratante = contratanteRepository.validarDadosUnicos();

        List<Object> contasCadastradas = new ArrayList<>();
        contasCadastradas.addAll(dadosFreelancer);
        contasCadastradas.addAll(dadosContratante);

        var camposJaCadastrados = validarCamposCadastrados(contasCadastradas, data);

        if (!camposJaCadastrados.equals("Dados já cadastrados: ")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, camposJaCadastrados);
        }
        var freelancer = new Freelancer(data);
        String encryptedPassword = new BCryptPasswordEncoder().encode(data.senha());

        freelancer.setSenha(encryptedPassword);

        repository.save(freelancer);
        return freelancer;
    }

    protected String validarCamposCadastrados(List<Object> contasCadastradas, CreateFreelancerDTO data) {
        String camposJaCadastrados = "Dados já cadastrados: ";
        List<String> listaCampos = new ArrayList<>();
        for (Object conta: contasCadastradas) {
            if (conta instanceof FreelancerValidacaoDTO f) {
                if (f.getEmail().equalsIgnoreCase(data.email())) {
                    listaCampos.add("E-mail");
                }
                if (f.getTelefone().equalsIgnoreCase(data.telefone())) {
                    listaCampos.add("Telefone");
                }
                if (f.getCpf().equalsIgnoreCase(data.cpf())) {
                    listaCampos.add("CPF");
                }
            } else if (conta instanceof ContratanteValidacaoDTO c) {
                if (c.getEmail().equalsIgnoreCase(data.email())) {
                    listaCampos.add("E-mail");
                }
                if (c.getTelefone().equalsIgnoreCase(data.telefone())) {
                    listaCampos.add("Telefone");
                }
            }
        }
        String campos = String.join(" | ", listaCampos);
        return camposJaCadastrados += campos;
    }

    public List<ListaFreelancerDTO> getFreelancers() {
        List<Freelancer> freelancers = repository.findAllByAtivoTrue();
        List<ListaFreelancerDTO> dtos = freelancers.stream().map(f ->
                     new ListaFreelancerDTO(
                                f.getId(), f.getNome(), f.getImagem(), f.getFuncao(), f.getEspecialidades(),
                                f.getSenioridade(), f.getValorHora(),
                                avaliacaoRepo.calcularMediaNotas(f)
                )).toList();
        return dtos;
    }

    public PerfilFreelancerDTO getFreelancerById(Long id) {
        var freelancerOpt = repository.findById(id);
        if (freelancerOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        var freelancer = freelancerOpt.get();
        PerfilFreelancerDTO dto = new PerfilFreelancerDTO(
                freelancer.getId(), freelancer.getNome(), freelancer.getEmail(), freelancer.getFuncao(),
                freelancer.getEspecialidades(), freelancer.getValorHora(),
                freelancer.getSenioridade(), freelancer.getDescricao(), freelancer.getTelefone(),
                freelancer.getImagem(), avaliacaoRepo.calcularMediaNotas(freelancer)
        );
        return dto;
    }

    @Transactional
    public Freelancer atualizar(UpdateFreelancerDTO data, Long id) {
        var freelancer = repository.getReferenceById(id);
        if (freelancer == null) {
            throw new EntityNotFoundException();
        }
        freelancer.atuallizarInformacoes(data);
        return freelancer;
    }

    @Transactional
    public void excluir(Long id) {
        var freelancer = repository.getReferenceById(id);
        if (freelancer == null) {
            throw new EntityNotFoundException();
        }
        freelancer.excluir();
    }

    public List<Especialidade> cadastrarEspecialidades(List<String> lista, Long id) {
        var freelancer = repository.findById(id);
        if (freelancer.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        List<Especialidade> especialidades = new ArrayList<>();
        for (String especialidade : lista) {
            especialidades.add(new Especialidade(especialidade, freelancer.get()));
        }
        especialidadeRepository.saveAll(especialidades);
        return especialidades;
    }

    public Integer atualizarFoto(MultipartFile novaFoto, Long idFreelancer) throws IOException {
        byte[] novaFotoByte = novaFoto.getBytes();
        int atualizados = repository.atualizarFoto(novaFotoByte, idFreelancer);

        ObjectTagging tagging = new ObjectTagging(Arrays.asList(new Tag("environment", "public")));
        Freelancer freelancer = repository.getReferenceById(idFreelancer);
        if (freelancer == null) {
            throw new EntityNotFoundException();
        }

        savePhotoInS3(freelancer, novaFoto, tagging);
        int status = atualizados == 1 ? 200 : 404;

        return status;
    }

    private void savePhotoInS3(Freelancer freelancer, MultipartFile photo, ObjectTagging tagging) {
        String fileName = generateFileName(freelancer.getNome());
        String fullPath = "freelancers/images/" + freelancer.getId() + "_" + freelancer.getNome() + "/" + fileName;
        try {
            uploadFile(fullPath, photo, tagging);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String uploadFile(String fullPath, MultipartFile photo, ObjectTagging tagging) {
        int count = 0;
        int maxTries = 3;
        while (true) {
            try {
                File multiPartFile = convertMultPartToFile(photo);
                PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fullPath, multiPartFile)
                        .withTagging(tagging);
                PutObjectResult putObjectResult = s3.putObject(putObjectRequest);
                return putObjectResult.getContentMd5();
            } catch (IOException e) {
                if (++count == maxTries) throw new RuntimeException(e);
            }
        }
    }

    private File convertMultPartToFile(MultipartFile file) throws IOException {
        File convertFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convertFile);
        fos.write(file.getBytes());
        fos.close();
        return convertFile;
    }

    private String generateFileName(String nome) {
        if (nome != null) nome = nome.replaceAll("\\s+", "");
        if (nome.isBlank()) nome = "desconhecido";
        return nome + "_profile-photo.webp";
    }

    public List<PerfilFreelancerDTO> getFreelancersBySearch(String pesquisa) {
         List<Freelancer> freelancers = repository.getFreelancersBySearch(pesquisa);
         List<PerfilFreelancerDTO> dtos = freelancers.stream()
                 .map(f -> new PerfilFreelancerDTO(
                         f.getId(), f.getNome(), f.getEmail(), f.getFuncao(),
                         f.getEspecialidades(), f.getValorHora(),
                         f.getSenioridade(), f.getDescricao(), f.getTelefone(),
                         f.getImagem(), avaliacaoRepo.calcularMediaNotas(f)
                 )).toList();
         return dtos;
    }

    public List<PerfilFreelancerDTO> compareFreelancers(List<EspecialidadeDTO> filters, Long compareTo) {
        List<String> especialidades = filters.stream()
                .map(EspecialidadeDTO::descricao)
                .toList();
        var freelancers = repository.compareFreelancerBySpecialties(especialidades, compareTo);
        List<PerfilFreelancerDTO> dtos = freelancers.stream()
                .map(f -> new PerfilFreelancerDTO(
                        f.getId(), f.getNome(), f.getEmail(), f.getFuncao(),
                        f.getEspecialidades(), f.getValorHora(),
                        f.getSenioridade(), f.getDescricao(), f.getTelefone(),
                        f.getImagem(), avaliacaoRepo.calcularMediaNotas(f)
                )).toList();
        return dtos;
    }

    public byte[] getFoto(int codigo) {
        return repository.getImagemById(codigo);
    }
}
