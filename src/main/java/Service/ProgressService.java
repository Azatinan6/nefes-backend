package Service;

import DTO.ProgressRequest;
import Entity.Game;
import Entity.User;
import Entity.UserProgress;
import Repository.GameRepository;
import Repository.UserProgressRepository;
import Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final UserProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public void saveGameProgress(ProgressRequest request) {
        // 1. Kullanıcıyı ve Oyunu veritabanından bul
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));
        
        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new RuntimeException("Oyun bulunamadı!"));

        // 2. DTO'dan gelen verileri Entity'e aktar
        UserProgress progress = new UserProgress();
        progress.setUser(user);
        progress.setGame(game);
        progress.setScore(request.getScore());
        progress.setBreathCrystals(request.getBreathCrystals()); // Nefes kristallerini kaydediyoruz[cite: 2].

        // 3. Veritabanına kaydet
        progressRepository.save(progress);
    }
}
