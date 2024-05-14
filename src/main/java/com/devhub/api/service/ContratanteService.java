package com.devhub.api.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.Tag;
import com.devhub.api.domain.contratante.Contratante;
import com.devhub.api.domain.contratante.ContratanteRepository;
import com.devhub.api.domain.contratante.dto.ContratanteValidacaoDTO;
import com.devhub.api.domain.contratante.dto.CreateContratanteDTO;
import com.devhub.api.domain.contratante.dto.ListContratanteDTO;
import com.devhub.api.domain.contratante.dto.UpdateContratanteDTO;
import com.devhub.api.domain.freelancer.FreelancerRepository;
import com.devhub.api.domain.freelancer.dto.FreelancerValidacaoDTO;
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

@Service
public class ContratanteService {

    @Autowired
    private ContratanteRepository repository;
    @Autowired
    private FreelancerRepository freelancerRepository;

    @Value("${bucketName}")
    private String bucketName;
    private final AmazonS3 s3;
    public ContratanteService(AmazonS3 s3) {
        this.s3 = s3;
    }

    @Transactional
    public Contratante cadastrarContratante(CreateContratanteDTO data) {

        List<FreelancerValidacaoDTO> dadosFreelancer = freelancerRepository.validarDadosUnicos();
        List<ContratanteValidacaoDTO> dadosContratante = repository.validarDadosUnicos();

        List<Object> contasCadastradas = new ArrayList<>();
        contasCadastradas.addAll(dadosFreelancer);
        contasCadastradas.addAll(dadosContratante);

        var camposJaCadastrados = validarCamposCadastrados(contasCadastradas, data);

        if (!camposJaCadastrados.equals("Dados já cadastrados: ")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, camposJaCadastrados);
        }

        var contratante = new Contratante(data);

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.senha());

        contratante.setSenha(encryptedPassword);

        repository.save(contratante);

        return contratante;
    }

    protected String validarCamposCadastrados(List<Object> contasCadastradas, CreateContratanteDTO data) {
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
            } else if (conta instanceof ContratanteValidacaoDTO c) {
                if (c.getEmail().equalsIgnoreCase(data.email())) {
                    listaCampos.add("E-mail");
                }
                if (c.getTelefone().equalsIgnoreCase(data.telefone())) {
                    listaCampos.add("Telefone");
                }
                if (c.getCnpj().equalsIgnoreCase(data.cnpj())) {
                    listaCampos.add("CNPJ");
                }
            }
        }
        String campos = String.join(" | ", listaCampos);
        return camposJaCadastrados += campos;
    }

    public List<ListContratanteDTO> getContratantes() {
        List<Contratante > contratantes = repository.findAllByAtivoTrue();
        List<ListContratanteDTO> dtos = contratantes.stream().map(c -> new ListContratanteDTO(
                        c.getId(), c.getNome(), c.getCnpj(),
                        c.getTelefone(), c.getEmail(), c.getImagem(),
                        c.getContratacoes()
                )).toList();
        return dtos;
    }

    public ListContratanteDTO getContratanteById(Long id) {
        var contratante = repository.getReferenceById(id);
        if (contratante == null) {
            throw new EntityNotFoundException();
        }
        return new ListContratanteDTO(
                contratante.getId(), contratante.getNome(), contratante.getCnpj(),
                contratante.getTelefone(), contratante.getEmail(), contratante.getImagem(),
                contratante.getContratacoes());
    }

    @Transactional
    public Contratante atualizar(UpdateContratanteDTO data, Long id) {
        var contratante = repository.getReferenceById(id);
        if (contratante == null) {
            throw new EntityNotFoundException();
        }
        contratante.atuallizarInformacoes(data);
        return contratante;
    }

    public void excluir(Long id) {
        var contratante = repository.getReferenceById(id);
        if (contratante == null) {
            throw new EntityNotFoundException();
        }
        contratante.excluir();
    }

    public Integer atualizarFoto(MultipartFile novaFoto, Long idContratante) throws IOException {
        byte[] novaFotoByte = novaFoto.getBytes();
        int atualizados = repository.atualizarFoto(novaFotoByte, idContratante);

        ObjectTagging tagging = new ObjectTagging(Arrays.asList(new Tag("environment", "public")));
        Contratante contratante = repository.getReferenceById(idContratante);
        if (contratante == null) {
            throw new EntityNotFoundException();
        }

        Boolean savedInS3 = savePhotoInS3(contratante, novaFoto, tagging);
        int status = atualizados == 1 && savedInS3? 200 : 404;

        return status;
    }

    private Boolean savePhotoInS3(Contratante contratante, MultipartFile photo, ObjectTagging tagging) {
        String fileName = generateFileName(contratante.getNome());
        String fullPath = "contratantes/images/" + contratante.getId() + "_" + contratante.getNome() + "/" + fileName;
        try {
            uploadFile(fullPath, photo, tagging);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
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
        return nome + "_profile-photo.jpg";
    }
    public byte[] getFoto(int codigo) {
        var imagem = repository.getImagemById(codigo);
        return imagem;
    }
}
