package com.careermentor.repository;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.careermentor.model.Student;

public interface StudentRepository extends MongoRepository<Student, String> {

    boolean existsByEmail(String email);

    Optional<Student> findByEmail(String email);
}