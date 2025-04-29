package br.com.curriculoanalysis.services;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class OpenAIService {

    private final OpenAiService service;
    private final String model;

    public OpenAIService(
            @Value("${openai.api.key}") String apiKey,
            @Value("${openai.model}") String model
    ) {
        // Timeout de 60 segundos para conexão, escrita e leitura
        this.service = new OpenAiService(apiKey, Duration.ofSeconds(60));
        this.model = model;
    }

    /**
     * Envia o currículo para análise ao modelo OpenAI, solicitando um resumo em português
     * com pontos fortes, pontos a melhorar e recomendações.
     */
    public String analyze(String resumeText) {
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(model)
                .messages(List.of(
                        new ChatMessage("system", "Você é um assistente de RH profissional e conciso."),
                        new ChatMessage("user",
                                "Por favor, analise este currículo de forma resumida em português. " +
                                        "Destaque:\n" +
                                        "1. Pontos fortes\n" +
                                        "2. Pontos a melhorar\n" +
                                        "3. Recomendações\n\n" +
                                        resumeText)
                ))
                .build();

        try {
            ChatCompletionResult result = service.createChatCompletion(request);
            return result.getChoices().get(0).getMessage().getContent().trim();
        } catch (Exception ex) {
            return "Erro ao comunicar com o OpenAI: " + ex.getMessage();
        }
    }
}
