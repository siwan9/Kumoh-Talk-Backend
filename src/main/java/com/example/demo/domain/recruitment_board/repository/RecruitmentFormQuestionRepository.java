package com.example.demo.domain.recruitment_board.repository;

import com.example.demo.domain.recruitment_board.domain.entity.RecruitmentFormQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecruitmentFormQuestionRepository extends JpaRepository<RecruitmentFormQuestion, Long>, QueryDslRecruitmentFormQuestionRepository {
    @Modifying
    @Query(value = "DELETE FROM recruitment_form_questions q WHERE q.id IN :ids", nativeQuery = true)
    void hardDeleteQuestionsByIds(List<Long> ids);
}
