package br.com.curriculoanalysis.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String resumeFilename;

    @Lob
    private String analysisText;

    private LocalDateTime createdAt = LocalDateTime.now();
}