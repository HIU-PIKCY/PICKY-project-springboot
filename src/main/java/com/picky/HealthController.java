package com.picky;

import com.picky.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Health Check", description = "서버 상태 확인 API")
public class HealthController {

    private final DataSource dataSource;

    @GetMapping("/health")
    @Operation(summary = "헬스 체크", description = "서버와 DB 연결 상태를 확인합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> healthStatus = new HashMap<>();
        boolean isHealthy = true;

        // 1. 애플리케이션 기본 상태
        healthStatus.put("application", "UP");

        // 2. DB 연결 상태 확인
        try {
            Connection connection = dataSource.getConnection();
            connection.close();
            healthStatus.put("database", "UP");
        } catch (Exception e) {
            healthStatus.put("database", "DOWN");
            healthStatus.put("database_error", e.getMessage());
            isHealthy = false;
        }

        // 3. 전체 상태 결정
        String overallStatus = isHealthy ? "UP" : "DOWN";
        healthStatus.put("status", overallStatus);

        // 4. 응답 생성
        if (isHealthy) {
            return ResponseEntity.ok(
                ApiResponse.onSuccess("서버가 정상 작동 중입니다.", healthStatus)
            );
        } else {
            return ResponseEntity.status(503)  // Service Unavailable
                                 .body(ApiResponse.onFailure("HEALTH_500", "서버에 문제가 있습니다.",
                                     healthStatus));
        }
    }

    // 간단한 핑 엔드포인트 (DB 체크 없이)
    @GetMapping("/ping")
    @Operation(summary = "핑 체크", description = "서버 기본 응답만 확인합니다.")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(ApiResponse.onSuccess("pong"));
    }
}