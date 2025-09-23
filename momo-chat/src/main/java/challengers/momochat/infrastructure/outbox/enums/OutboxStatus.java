package challengers.momochat.infrastructure.outbox.enums;

public enum OutboxStatus {
    PENDING,     // 아직 처리되지 않은 상태 (기본 상태)
    PROCESSING, // 중복 처리 방지용
    SUCCESS,    // 전송 성공
    DEAD,       // DLT로 전송됨.

    SENT,        // Kafka 등 외부 시스템으로 전송 성공
    FAILED,      // 전송 실패
    RETRYING,    // 재시도 중
    DLQ,         // Dead Letter Queue로 보낸 상태 (재시도 불가, 관리자 확인 필요)
    COMPLETED    // 완전히 처리 완료 (SENT와 구분없이 사용하기도 함)
}