package fr.edyp.epims.database.dao;

import fr.edyp.epims.database.entities.FileLink;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileLinkRepository extends JpaRepository<FileLink, Integer> {
}
