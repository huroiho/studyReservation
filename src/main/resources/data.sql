-- =============================================================================
-- data.sql - Study Room Reservation Service Seed Data
-- =============================================================================
-- VERIFIED SCHEMA (scanned from entity classes 2026-01-29):
--
-- Inheritance Chains:
--   BasePolicyEntity → BaseCreatedEntity → BaseIdEntity
--     (id, created_at, name, is_active, active_updated_at)
--     Used by: operation_policies, room_rules, refund_policies
--
--   BaseCreatedEntity → BaseIdEntity
--     (id, created_at)
--     Used by: operation_schedules, refund_rules
--
--   BaseSoftDeletableEntity → BaseAuditableEntity → BaseCreatedEntity → BaseIdEntity
--     (id, created_at, updated_at, deleted_at)
--     Used by: rooms
--
-- Column Details:
--   id:                BIGINT AUTO_INCREMENT PK
--   created_at:        DATETIME (nullable)
--   updated_at:        DATETIME NOT NULL (only in rooms table)
--   deleted_at:        DATETIME (nullable, only in rooms table)
--   is_active:         BIT(1) NOT NULL (1=true, 0=false)
--   is_closed:         BIT(1) NOT NULL (1=closed, 0=open)
--   active_updated_at: DATETIME NOT NULL
--
-- WHY EXPLICIT IDs ARE SAFE:
--   - Script starts with TRUNCATE which resets AUTO_INCREMENT to 1
--   - IDs are sequential (1, 2, 3...) matching MySQL's default behavior
--   - Makes the seed file deterministic and reproducible
--   - FK references are guaranteed correct after TRUNCATE
--
-- Functional Constraints Respected:
--   - Each OperationPolicy has exactly 7 schedules (MON-SUN)
--   - open_time < close_time (same-day only, no overnight)
--   - At least one policy includes closed days (is_closed = 1)
--   - name is unique for all policy entities
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- Clear existing data (resets AUTO_INCREMENT)
TRUNCATE TABLE rooms;
TRUNCATE TABLE refund_rules;
TRUNCATE TABLE refund_policies;
TRUNCATE TABLE room_rules;
TRUNCATE TABLE operation_schedules;
TRUNCATE TABLE operation_policies;

SET FOREIGN_KEY_CHECKS = 1;


-- =============================================================================
-- A) OPERATION POLICIES (3 policies)
-- =============================================================================
-- SlotUnit enum values: MINUTES_30, MINUTES_60

INSERT INTO operation_policies (id, name, slot_unit, is_active, active_updated_at, created_at)
VALUES
    (1, '표준 운영 정책', 'MINUTES_60', 1, NOW(), NOW()),
    (2, '야간 확장 정책', 'MINUTES_30', 1, NOW(), NOW()),
    (3, '주말 전용 정책', 'MINUTES_60', 1, NOW(), NOW());


-- =============================================================================
-- A-1) OPERATION SCHEDULES (7 schedules per policy = 21 total)
-- =============================================================================
-- DayOfWeek enum: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
-- is_closed: 0 = open (requires open_time/close_time), 1 = closed (times must be NULL)
-- Constraint: open_time < close_time (same-day only)

-- -----------------------------------------------------------------------------
-- Policy 1: 표준 운영 정책 (operation_policy_id = 1)
-- Weekdays: 09:00-22:00, Weekends: 10:00-20:00
-- -----------------------------------------------------------------------------
INSERT INTO operation_schedules (id, operation_policy_id, day_of_week, open_time, close_time, is_closed, created_at)
VALUES
    (1,  1, 'MONDAY',    '09:00:00', '22:00:00', 0, NOW()),
    (2,  1, 'TUESDAY',   '09:00:00', '22:00:00', 0, NOW()),
    (3,  1, 'WEDNESDAY', '09:00:00', '22:00:00', 0, NOW()),
    (4,  1, 'THURSDAY',  '09:00:00', '22:00:00', 0, NOW()),
    (5,  1, 'FRIDAY',    '09:00:00', '22:00:00', 0, NOW()),
    (6,  1, 'SATURDAY',  '10:00:00', '20:00:00', 0, NOW()),
    (7,  1, 'SUNDAY',    '10:00:00', '20:00:00', 0, NOW());

-- -----------------------------------------------------------------------------
-- Policy 2: 야간 확장 정책 (operation_policy_id = 2)
-- Mon-Sat: 08:00-23:00, Sunday: CLOSED
-- -----------------------------------------------------------------------------
INSERT INTO operation_schedules (id, operation_policy_id, day_of_week, open_time, close_time, is_closed, created_at)
VALUES
    (8,  2, 'MONDAY',    '08:00:00', '23:00:00', 0, NOW()),
    (9,  2, 'TUESDAY',   '08:00:00', '23:00:00', 0, NOW()),
    (10, 2, 'WEDNESDAY', '08:00:00', '23:00:00', 0, NOW()),
    (11, 2, 'THURSDAY',  '08:00:00', '23:00:00', 0, NOW()),
    (12, 2, 'FRIDAY',    '08:00:00', '23:00:00', 0, NOW()),
    (13, 2, 'SATURDAY',  '08:00:00', '23:00:00', 0, NOW()),
    (14, 2, 'SUNDAY',    NULL,       NULL,       1, NOW());

-- -----------------------------------------------------------------------------
-- Policy 3: 주말 전용 정책 (operation_policy_id = 3)
-- Mon-Fri: CLOSED, Sat-Sun: 09:00-21:00
-- -----------------------------------------------------------------------------
INSERT INTO operation_schedules (id, operation_policy_id, day_of_week, open_time, close_time, is_closed, created_at)
VALUES
    (15, 3, 'MONDAY',    NULL,       NULL,       1, NOW()),
    (16, 3, 'TUESDAY',   NULL,       NULL,       1, NOW()),
    (17, 3, 'WEDNESDAY', NULL,       NULL,       1, NOW()),
    (18, 3, 'THURSDAY',  NULL,       NULL,       1, NOW()),
    (19, 3, 'FRIDAY',    NULL,       NULL,       1, NOW()),
    (20, 3, 'SATURDAY',  '09:00:00', '21:00:00', 0, NOW()),
    (21, 3, 'SUNDAY',    '09:00:00', '21:00:00', 0, NOW());


-- =============================================================================
-- B) ROOM RULES (5 rules)
-- =============================================================================

INSERT INTO room_rules (id, name, min_duration_minutes, booking_open_days, is_active, active_updated_at, created_at)
VALUES
    (1, '기본 예약 규칙',     60,  14, 1, NOW(), NOW()),
    (2, '단기 예약 규칙',     30,  7,  1, NOW(), NOW()),
    (3, '장기 예약 규칙',     120, 30, 1, NOW(), NOW()),
    (4, '유연 예약 규칙',     30,  14, 1, NOW(), NOW()),
    (5, '프리미엄 예약 규칙', 60,  60, 0, NOW(), NOW());


-- =============================================================================
-- C) REFUND POLICIES (2 policies)
-- =============================================================================

INSERT INTO refund_policies (id, name, is_active, active_updated_at, created_at)
VALUES
    (1, '일반 환불 정책', 1, NOW(), NOW()),
    (2, '엄격 환불 정책', 1, NOW(), NOW());


-- =============================================================================
-- C-1) REFUND RULES
-- =============================================================================
-- refund_base_minutes: Minutes before reservation start
-- refund_rate: Percentage (0-100)

-- Policy 1: 일반 환불 정책 (refund_policy_id = 1)
INSERT INTO refund_rules (id, refund_policy_id, name, refund_base_minutes, refund_rate, created_at)
VALUES
    (1, 1, '24시간 이전 취소', 1440, 100, NOW()),
    (2, 1, '12시간 이전 취소', 720,  80,  NOW()),
    (3, 1, '4시간 이전 취소',  240,  50,  NOW()),
    (4, 1, '1시간 이전 취소',  60,   30,  NOW()),
    (5, 1, '1시간 미만 취소',  0,    0,   NOW());

-- Policy 2: 엄격 환불 정책 (refund_policy_id = 2)
INSERT INTO refund_rules (id, refund_policy_id, name, refund_base_minutes, refund_rate, created_at)
VALUES
    (6, 2, '48시간 이전 취소', 2880, 100, NOW()),
    (7, 2, '24시간 이전 취소', 1440, 50,  NOW()),
    (8, 2, '12시간 이전 취소', 720,  20,  NOW()),
    (9, 2, '12시간 미만 취소', 0,    0,   NOW());


-- =============================================================================
-- D) ROOMS (5 rooms)
-- =============================================================================
-- status enum: ACTIVE, INACTIVE
-- amenities: JSON array of AmenityType enum values
--   (WIFI, WHITEBOARD, PROJECTOR, AIR_CONDITIONER, COFFEE_MACHINE, SOUND_SYSTEM)

INSERT INTO rooms (id, operation_policy_id, room_rule_id, refund_policy_id, name, max_capacity, price, amenities, status, created_at, updated_at, deleted_at)
VALUES
    (1, 1, 1, 1, '미팅룸 A (소형)', 2, 10000,
     '["WIFI", "AIR_CONDITIONER"]',
     'ACTIVE', NOW(), NOW(), NULL),

    (2, 1, 2, 1, '미팅룸 B (4인)', 4, 15000,
     '["WIFI", "WHITEBOARD", "AIR_CONDITIONER"]',
     'ACTIVE', NOW(), NOW(), NULL),

    (3, 2, 4, 1, '회의실 C (6인)', 6, 25000,
     '["WIFI", "WHITEBOARD", "PROJECTOR", "AIR_CONDITIONER"]',
     'ACTIVE', NOW(), NOW(), NULL),

    (4, 1, 3, 2, '대회의실 D (8인)', 8, 40000,
     '["WIFI", "WHITEBOARD", "PROJECTOR", "AIR_CONDITIONER", "COFFEE_MACHINE"]',
     'ACTIVE', NOW(), NOW(), NULL),

    (5, 2, 3, 2, '세미나실 E (12인)', 12, 60000,
     '["WIFI", "WHITEBOARD", "PROJECTOR", "AIR_CONDITIONER", "COFFEE_MACHINE", "SOUND_SYSTEM"]',
     'ACTIVE', NOW(), NOW(), NULL);


-- =============================================================================
-- Reset AUTO_INCREMENT for future inserts
-- =============================================================================
ALTER TABLE operation_policies AUTO_INCREMENT = 4;
ALTER TABLE operation_schedules AUTO_INCREMENT = 22;
ALTER TABLE room_rules AUTO_INCREMENT = 6;
ALTER TABLE refund_policies AUTO_INCREMENT = 3;
ALTER TABLE refund_rules AUTO_INCREMENT = 10;
ALTER TABLE rooms AUTO_INCREMENT = 6;
