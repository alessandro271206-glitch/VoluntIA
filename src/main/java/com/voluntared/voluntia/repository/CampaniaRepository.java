package com.voluntared.voluntia.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.voluntared.voluntia.entity.Campania;
public interface CampaniaRepository extends JpaRepository<Campania,Integer> { java.util.List<Campania> findByOrganizacionId(Integer id); }
