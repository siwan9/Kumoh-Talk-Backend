package com.example.demo.application.board.dto.request;

import static com.example.demo.global.regex.S3UrlRegex.*;

import com.example.demo.domain.board.service.entity.BoardFileInfo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Schema(name = "FileRequest", description = "파일 생성 요청")
public record FileRequest (
	@Schema(description = "게시물 ID", example = "1")
	@NotNull(message = "게시물 id는 필수 입니다.")
	Long boardId,
	@Schema(description = "파일 url", example = "https://kumoh-talk-bucket.s3.ap-northeast-2.amazonaws.com/")
	@NotBlank(message = "파일 url은 필수 입니다.")
	@Pattern(regexp = S3_BOARD_FILE_URL)
	String url
){
	public BoardFileInfo toBoardFileInfo() {
		return BoardFileInfo.of(url);
	}
}
