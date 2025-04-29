package br.com.curriculoanalysis.services;

import br.com.curriculoanalysis.models.Candidate;
import br.com.curriculoanalysis.repositories.CandidateRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ResumeAnalysisService {

    private final Path uploadDir;
    private final OpenAIService openAIService;
    private final CandidateRepository repository;

    public ResumeAnalysisService(
            @Value("${file.upload-dir}") String uploadDir,
            OpenAIService openAIService,
            CandidateRepository repository
    ) {
        this.uploadDir = Paths.get(uploadDir);
        this.openAIService = openAIService;
        this.repository = repository;
    }

    public Candidate process(MultipartFile file, String name) throws IOException {
        // salva o arquivo
        String original = file.getOriginalFilename();
        String extension = original != null && original.contains(".")
                ? original.substring(original.lastIndexOf(".")).toLowerCase()
                : "";
        String filename = System.currentTimeMillis() + "_" + original;
        Path target = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), target);

        // extrai texto
        String content;
        if (".pdf".equals(extension)) {
            try (PDDocument document = PDDocument.load(target.toFile())) {
                PDFTextStripper stripper = new PDFTextStripper();
                content = stripper.getText(document);
            }
        } else {
            content = Files.readString(target);
        }

        // gera análise via OpenAI
        String analysis = openAIService.analyze(content);

        // persiste candidato
        Candidate candidate = new Candidate();
        candidate.setName(name);
        candidate.setResumeFilename(filename);
        candidate.setAnalysisText(analysis);
        return repository.save(candidate);
    }
}
