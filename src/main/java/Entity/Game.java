package Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String exerciseType; // Örn: Diyafram Solunumu
    private String physiologicalTarget; // Örn: FVC artışı
}
