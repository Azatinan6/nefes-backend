package DTO;

import lombok.Data;
import java.util.UUID;

@Data
public class ProgressRequest {
    private UUID userId;
    private Long gameId;
    private int score;
    private int breathCrystals; // Kazanılan nefes kristalleri verisini taşıyacağız[cite: 2].
}
