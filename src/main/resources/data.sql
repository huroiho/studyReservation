-- 기존 데이터 충돌 방지 (필요 시 주석 해제 후 실행)
-- SET FOREIGN_KEY_CHECKS = 0;
-- TRUNCATE TABLE members;
-- TRUNCATE TABLE operation_policies;
-- TRUNCATE TABLE operation_schedules;
-- TRUNCATE TABLE room_rules;
-- TRUNCATE TABLE refund_policies;
-- TRUNCATE TABLE refund_rules;
-- TRUNCATE TABLE rooms;
-- TRUNCATE TABLE reservations;
-- TRUNCATE TABLE payments;
-- TRUNCATE TABLE payment_attempts;
-- TRUNCATE TABLE refunds;
-- SET FOREIGN_KEY_CHECKS = 1;

-- 1. 회원 (Member)
INSERT INTO members (created_at, updated_at, deleted_at, email, name, password, phone_number, role)
VALUES
    (NOW(), NOW(), NULL, 'admin@example.com', '관리자', '1234', '010-0000-0000', 'ADMIN'),
    (NOW(), NOW(), NULL, 'user@example.com', '테스트유저', '1234', '010-1234-5678', 'USER');

-- 2. 운영 정책 (OperationPolicy)
-- [수정] slot_unit: 'ONE_HOUR' -> 'MINUTES_60' (Java Enum 이름과 일치)
INSERT INTO operation_policies (created_at, is_active, active_updated_at, name, slot_unit)
VALUES
    (NOW(), 1, NOW(), '기본 운영 정책 (1시간 단위)', 'MINUTES_60');

-- 3. 운영 스케줄 (OperationSchedule)
INSERT INTO operation_schedules (created_at, operation_policy_id, day_of_week, open_time, close_time, is_closed)
VALUES
    (NOW(), 1, 'MONDAY', '09:00:00', '22:00:00', 0),
    (NOW(), 1, 'TUESDAY', '09:00:00', '22:00:00', 0),
    (NOW(), 1, 'WEDNESDAY', '09:00:00', '22:00:00', 0),
    (NOW(), 1, 'THURSDAY', '09:00:00', '22:00:00', 0),
    (NOW(), 1, 'FRIDAY', '09:00:00', '22:00:00', 0),
    (NOW(), 1, 'SATURDAY', '10:00:00', '20:00:00', 0),
    (NOW(), 1, 'SUNDAY', '10:00:00', '20:00:00', 0);

-- 4. 룸 규칙 (RoomRule)
INSERT INTO room_rules (created_at, is_active, active_updated_at, name, min_duration_minutes, booking_open_days)
VALUES
    (NOW(), 1, NOW(), '일반 룸 규칙', 60, 30);

-- 5. 환불 정책 (RefundPolicy)
INSERT INTO refund_policies (created_at, is_active, active_updated_at, name)
VALUES
    (NOW(), 1, NOW(), '표준 환불 정책');

-- 6. 환불 규칙 (RefundRule)
INSERT INTO refund_rules (created_at, refund_policy_id, name, refund_base_minutes, refund_rate)
VALUES
    (NOW(), 1, '이용 24시간 전', 1440, 100),
    (NOW(), 1, '이용 1시간 전', 60, 50);

-- 7. 스터디룸 (Room)
INSERT INTO rooms (created_at, updated_at, deleted_at, operation_policy_id, room_rule_id, refund_policy_id, name, max_capacity, price, amenities, status)
VALUES
    (NOW(), NOW(), NULL, 1, 1, 1, '스터디룸 A (4인실)', 4, 10000, '["WIFI", "WHITEBOARD", "AIR_CONDITIONER"]', 'ACTIVE'),
    (NOW(), NOW(), NULL, 1, 1, 1, '스터디룸 B (6인실)', 6, 15000, '["WIFI", "PROJECTOR", "COFFEE_MACHINE"]', 'ACTIVE');

-- 8. 예약 (Reservation)
-- Case A: 결제 대기 (TEMP) -> id=1
INSERT INTO reservations (created_at, updated_at, member_id, room_id, applied_operation_policy_id, applied_refund_policy_id, status, start_time, end_time, total_amount, expires_at)
VALUES
    (NOW(), NOW(), 2, 1, 1, 1, 'TEMP',
     DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 26 HOUR),
     20000, DATE_ADD(NOW(), INTERVAL 10 MINUTE));

-- Case B: 결제 완료 (CONFIRMED) -> id=2
INSERT INTO reservations (created_at, updated_at, member_id, room_id, applied_operation_policy_id, applied_refund_policy_id, status, start_time, end_time, total_amount, confirmed_at)
VALUES
    (NOW(), NOW(), 2, 2, 1, 1, 'CONFIRMED',
     DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 50 HOUR),
     30000, NOW());

-- 9. 결제 (Payment)
-- reservation_id=2 에 대한 결제
INSERT INTO payments (created_at, updated_at, reservation_id, amount, payment_method, payment_status, pg_tid, approved_at)
VALUES
    (NOW(), NOW(), 2, 30000, 'PG', 'SUCCESS', 'toss_payment_key_sample_12345', NOW());

-- 10. 결제 시도 (PaymentAttempt)
INSERT INTO payment_attempts (created_at, reservation_id, amount, payment_method, payment_attempt_status, order_id, pg_tid)
VALUES
    (NOW(), 2, 30000, 'PG', 'SUCCESS', CONCAT('RES_2_', UUID()), 'toss_payment_key_sample_12345');

-- 11. 환불 (Refund)
INSERT INTO refunds (created_at, updated_at, payment_id, applied_refund_policy_id, refund_amount, status, refunded_at)
VALUES
    (NOW(), NOW(), 1, 1, 15000, 'SUCCESS', NOW());