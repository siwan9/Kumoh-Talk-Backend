package com.example.demo.infra.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.infra.board.entity.ImageFile;

@Repository
public interface ImageFileJpaRepository extends JpaRepository<ImageFile, Long> {
}
