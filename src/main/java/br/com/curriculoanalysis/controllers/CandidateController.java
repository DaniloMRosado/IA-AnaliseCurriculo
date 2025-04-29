package br.com.curriculoanalysis.controllers;

import br.com.curriculoanalysis.models.Candidate;
import br.com.curriculoanalysis.repositories.CandidateRepository;
import br.com.curriculoanalysis.services.ResumeAnalysisService;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class CandidateController {

    private final ResumeAnalysisService analysisService;
    private final CandidateRepository repository;
    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    public CandidateController(
            ResumeAnalysisService analysisService,
            CandidateRepository repository,
            Parser markdownParser,
            HtmlRenderer htmlRenderer
    ) {
        this.analysisService = analysisService;
        this.repository = repository;
        this.markdownParser = markdownParser;
        this.htmlRenderer = htmlRenderer;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/candidates/list";
    }

    @GetMapping("/candidates/new")
    public String form() {
        return "candidates/form";
    }

    @PostMapping("/candidates")
    public String upload(@RequestParam("file") MultipartFile file,
                         @RequestParam("name") String name) throws IOException {
        analysisService.process(file, name);
        return "redirect:/candidates/list";
    }

    @GetMapping("/candidates/list")
    public String list(Model model) {
        model.addAttribute("candidates", repository.findAll());
        return "candidates/list";
    }

    @GetMapping("/candidates/{id}/analysis")
    public String analysis(@PathVariable Long id, Model model) {
        Candidate candidate = repository.findById(id).orElseThrow();

        // 1) Parse o Markdown
        Node document = markdownParser.parse(candidate.getAnalysisText());
        // 2) Render para HTML
        String html = htmlRenderer.render(document);

        model.addAttribute("candidate", candidate);
        model.addAttribute("analysisHtml", html);
        return "candidates/analysis";
    }
}
