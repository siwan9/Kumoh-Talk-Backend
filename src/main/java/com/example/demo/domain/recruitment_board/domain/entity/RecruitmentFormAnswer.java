package com.example.demo.domain.recruitment_board.domain.entity;

import com.example.demo.domain.recruitment_board.domain.dto.request.RecruitmentFormAnswerRequest;
import com.example.demo.global.base.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "recruitment_form_answers")
@NoArgsConstructor
@Getter
@SQLDelete(sql = "UPDATE recruitment_form_answers SET deleted_at = NOW() where id=?")
@SQLRestriction(value = "deleted_at is NULL")
public class RecruitmentFormAnswer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer number;

    @Column(nullable = false, length = 50)
    private String answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitment_form_question_id", nullable = false)
    private RecruitmentFormQuestion recruitmentFormQuestion;

    @Builder
    public RecruitmentFormAnswer(int number, String answer, RecruitmentFormQuestion recruitmentFormQuestion) {
        this.number = number;
        this.answer = answer;
        this.recruitmentFormQuestion = recruitmentFormQuestion;
    }

    public static RecruitmentFormAnswer from(
            RecruitmentFormAnswerRequest answerRequest,
            RecruitmentFormQuestion recruitmentFormQuestion) {
        return RecruitmentFormAnswer.builder()
                .number(answerRequest.getNumber())
                .answer(answerRequest.getAnswer())
                .recruitmentFormQuestion(recruitmentFormQuestion)
                .build();
    }

    public void updateFromRequest(RecruitmentFormAnswerRequest recruitmentFormAnswerRequest) {
        this.number = recruitmentFormAnswerRequest.getNumber();
        this.answer = recruitmentFormAnswerRequest.getAnswer();
    }
}