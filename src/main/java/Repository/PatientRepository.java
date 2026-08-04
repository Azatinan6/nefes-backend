package Repository;

import Entity.Patient;
import Entity.Physiotherapist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Hasta Repository'si — patients tablosu üzerindeki işlemleri yönetir.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    // Bir fizyoterapiste bağlı tüm hastaları getirir
    // Fizyoterapist panelinde "Hastalarım" listesi için kullanılır
    List<Patient> findByPhysiotherapist(Physiotherapist physiotherapist);

    // Bir fizyoterapistin toplam hasta sayısını sayar — 40 limit kontrolü için
    long countByPhysiotherapist(Physiotherapist physiotherapist);
}
