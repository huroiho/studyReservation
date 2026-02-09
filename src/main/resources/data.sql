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
TRUNCATE TABLE room_images;
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
    (1, 1, 1, 1, '미팅룸 A (소형)', 3, 10000,
     '["WIFI", "AIR_CONDITIONER"]',
     'ACTIVE', NOW(), NOW(), NULL),

    (2, 1, 2, 1, '미팅룸 B (4인)', 4, 15000,
     '["WIFI", "WHITEBOARD", "AIR_CONDITIONER"]',
     'ACTIVE', NOW(), NOW(), NULL),

    (3, 2, 4, 1, '회의실 C (5인)', 5, 25000,
     '["WIFI", "WHITEBOARD", "PROJECTOR", "AIR_CONDITIONER"]',
     'ACTIVE', NOW(), NOW(), NULL),

    (4, 1, 3, 2, '대회의실 D (8인)', 8, 40000,
     '["WIFI", "WHITEBOARD", "PROJECTOR", "AIR_CONDITIONER", "COFFEE_MACHINE"]',
     'ACTIVE', NOW(), NOW(), NULL),

    (5, 2, 3, 2, '세미나실 E (12인)', 12, 60000,
     '["WIFI", "WHITEBOARD", "PROJECTOR", "AIR_CONDITIONER", "COFFEE_MACHINE", "SOUND_SYSTEM"]',
     'ACTIVE', NOW(), NOW(), NULL),

    (6, 1, 1, 1, '포커스룸 F (2인)', 2, 8000,
     '["WIFI", "AIR_CONDITIONER"]',
     'ACTIVE', NOW(), NOW(), NULL),

    (7, 1, 2, 1, '스터디룸 G (4인)', 4, 12000,
     '["WIFI", "WHITEBOARD"]',
     'ACTIVE', NOW(), NOW(), NULL),

    (8, 2, 4, 1, '프로젝트룸 H (6인)', 6, 22000,
     '["WIFI", "WHITEBOARD", "PROJECTOR"]',
     'ACTIVE', NOW(), NOW(), NULL),

    (9, 1, 1, 1, '미니룸 I (2인)', 2, 9000,
     '["WIFI"]',
     'ACTIVE', NOW(), NOW(), NULL),

    (10, 3, 2, 1, '주말룸 J (4인)', 4, 18000,
     '["WIFI", "AIR_CONDITIONER", "COFFEE_MACHINE"]',
     'ACTIVE', NOW(), NOW(), NULL),

    (11, 1, 3, 2, '컨퍼런스룸 K (10인)', 10, 50000,
     '["WIFI", "WHITEBOARD", "PROJECTOR", "AIR_CONDITIONER", "SOUND_SYSTEM"]',
     'ACTIVE', NOW(), NOW(), NULL),

    (12, 2, 4, 1, '야간룸 L (6인)', 6, 28000,
     '["WIFI", "WHITEBOARD", "AIR_CONDITIONER"]',
     'ACTIVE', NOW(), NOW(), NULL),

    (13, 1, 2, 1, '조용한 방 M (3인)', 3, 14000,
     '["WIFI", "AIR_CONDITIONER"]',
     'INACTIVE', NOW(), NOW(), NULL),

    (14, 1, 1, 1, '1인실 N', 1, 7000,
     '["WIFI"]',
     'ACTIVE', NOW(), NOW(), NULL),

    (15, 2, 3, 2, '프리미엄룸 O (8인)', 8, 45000,
     '["WIFI", "WHITEBOARD", "PROJECTOR", "AIR_CONDITIONER", "COFFEE_MACHINE"]',
     'ACTIVE', NOW(), NOW(), NULL),

    (16, 1, 4, 1, '플렉스룸 P (6인)', 6, 20000,
     '["WIFI", "WHITEBOARD", "AIR_CONDITIONER"]',
     'ACTIVE', NOW(), NOW(), NULL),

    (17, 3, 2, 1, '주말전용 Q (5인)', 5, 16000,
     '["WIFI", "PROJECTOR"]',
     'INACTIVE', NOW(), NOW(), NULL),

    (18, 1, 1, 1, '코워킹룸 R (2인)', 2, 11000,
     '["WIFI", "AIR_CONDITIONER", "COFFEE_MACHINE"]',
     'ACTIVE', NOW(), NOW(), NULL),

    (19, 2, 3, 2, '이벤트홀 S (10인)', 10, 55000,
     '["WIFI", "WHITEBOARD", "PROJECTOR", "AIR_CONDITIONER", "COFFEE_MACHINE", "SOUND_SYSTEM"]',
     'ACTIVE', NOW(), NOW(), NULL),

    (20, 1, 4, 1, '브레인스톰룸 T (8인)', 8, 35000,
     '["WIFI", "WHITEBOARD", "PROJECTOR", "AIR_CONDITIONER"]',
     'INACTIVE', NOW(), NOW(), NULL);

-- =============================================================================
-- D-1) ROOM IMAGES (다중 이미지 및 타입 테스트 데이터)
-- =============================================================================

INSERT INTO room_images (id, room_id, image_url, type, sort_order, created_at)
VALUES
    (1,  1,  '/img/16.png',  'MAIN', 1, NOW()),
    (2,  1,  '/img/16.png',  'THUMBNAIL', 1, NOW()),
    (3,  1, '/img/2.png',  'GENERAL',   1, NOW()),
    (4,  1, '/img/3.png',  'GENERAL',   2, NOW()),


    (5,  2,  '/img/3.png',  'MAIN', 1, NOW()),
    (6,  2,  '/img/3.png',  'THUMBNAIL', 1, NOW()),
    (7,  2, '/img/4.png',  'GENERAL',   1, NOW()),
    (8,  2, '/img/5.png',  'GENERAL',   2, NOW()),


    (9,  3,  '/img/7.png',  'MAIN', 1, NOW()),
    (10,  3,  '/img/7.png',  'THUMBNAIL', 1, NOW()),
    (11, 3, '/img/6.png',  'GENERAL',   1, NOW()),
    (12, 3, '/img/7.png',  'GENERAL',   2, NOW()),
    (13, 3, '/img/8.png',  'GENERAL',   3, NOW()),


    (14,  4,  '/img/6.png',  'MAIN', 1, NOW()),
    (15,  4,  '/img/6.png',  'THUMBNAIL', 1, NOW()),
    (16, 4, '/img/9.png',  'GENERAL',   1, NOW()),
    (17, 4, '/img/10.png', 'GENERAL',   2, NOW()),
    (18, 4, '/img/11.png', 'GENERAL',   3, NOW()),


    (19,  5,  '/img/8.png',  'MAIN', 1, NOW()),
    (20,  5,  '/img/8.png',  'THUMBNAIL', 1, NOW()),
    (21, 5, '/img/12.png', 'GENERAL',   1, NOW()),
    (22, 5, '/img/13.png', 'GENERAL',   2, NOW()),
    (23, 5, '/img/14.png', 'GENERAL',   3, NOW()),
    (24, 5, '/img/15.png', 'GENERAL',   4, NOW()),


    (25,  6,  '/img/10.png',  'MAIN', 1, NOW()),
    (26,  6,  '/img/10.png',  'THUMBNAIL', 1, NOW()),
    (27, 6, '/img/16.png', 'GENERAL',   1, NOW()),
    (28, 6, '/img/17.png', 'GENERAL',   2, NOW()),


    (29,  7,  '/img/7.png',  'MAIN', 1, NOW()),
    (30,  7,  '/img/7.png',  'THUMBNAIL', 1, NOW()),
    (31, 7, '/img/18.png', 'GENERAL',   1, NOW()),
    (32, 7, '/img/19.png', 'GENERAL',   2, NOW()),


    (33,  8,  '/img/11.png',  'MAIN', 1, NOW()),
    (34,  8,  '/img/11.png',  'THUMBNAIL', 1, NOW()),
    (35, 8, '/img/20.png', 'GENERAL',   1, NOW()),
    (36, 8, '/img/21.png', 'GENERAL',   2, NOW()),
    (37, 8, '/img/22.png', 'GENERAL',   3, NOW()),


    (38,  9,  '/img/2.png',  'MAIN', 1, NOW()),
    (39,  9,  '/img/2.png',  'THUMBNAIL', 1, NOW()),
    (40, 9, '/img/1.png',  'GENERAL',   1, NOW()),
    (41, 9, '/img/2.png',  'GENERAL',   2, NOW()),


    (42, 10, '/img/4.png', 'MAIN', 1, NOW()),
    (43, 10, '/img/4.png', 'THUMBNAIL', 1, NOW()),
    (44, 10, '/img/3.png',  'GENERAL',   1, NOW()),
    (45, 10, '/img/4.png',  'GENERAL',   2, NOW()),


    (46, 11, '/img/6.png', 'MAIN', 1, NOW()),
    (47, 11, '/img/6.png', 'THUMBNAIL', 1, NOW()),
    (48, 11, '/img/5.png',  'GENERAL',   1, NOW()),
    (49, 11, '/img/6.png',  'GENERAL',   2, NOW()),
    (50, 11, '/img/7.png',  'GENERAL',   3, NOW()),


    (51, 12, '/img/9.png', 'MAIN', 1, NOW()),
    (52, 12, '/img/9.png', 'THUMBNAIL', 1, NOW()),
    (53, 12, '/img/8.png',  'GENERAL',   1, NOW()),
    (54, 12, '/img/9.png',  'GENERAL',   2, NOW()),



    (55, 13, '/img/15.png', 'MAIN', 1, NOW()),
    (56, 13, '/img/15.png', 'THUMBNAIL', 1, NOW()),
    (57, 13, '/img/10.png', 'GENERAL',   1, NOW()),
    (58, 13, '/img/11.png', 'GENERAL',   2, NOW()),


    (59, 14, '/img/1.png', 'MAIN', 1, NOW()),
    (60, 14, '/img/1.png', 'THUMBNAIL', 1, NOW()),
    (61, 14, '/img/12.png', 'GENERAL',   1, NOW()),
    (62, 14, '/img/13.png', 'GENERAL',   2, NOW()),
    (63, 14, '/img/15.png', 'GENERAL',   3, NOW()),


    (64, 15, '/img/5.png', 'MAIN', 1, NOW()),
    (65, 15, '/img/5.png', 'THUMBNAIL', 1, NOW()),
    (66, 15, '/img/16.png', 'GENERAL',   1, NOW()),
    (67, 15, '/img/17.png', 'GENERAL',   2, NOW()),
    (68, 15, '/img/18.png', 'GENERAL',   3, NOW()),


    (69, 16, '/img/17.png', 'MAIN', 1, NOW()),
    (70, 16, '/img/17.png', 'THUMBNAIL', 1, NOW()),
    (71, 16, '/img/19.png', 'GENERAL',   1, NOW()),
    (72, 16, '/img/20.png', 'GENERAL',   2, NOW()),


    (73, 17, '/img/13.png', 'MAIN', 1, NOW()),
    (74, 17, '/img/13.png', 'THUMBNAIL', 1, NOW()),
    (75, 17, '/img/21.png', 'GENERAL',   1, NOW()),
    (76, 17, '/img/22.png', 'GENERAL',   2, NOW()),


    (77, 18, '/img/20.png', 'MAIN', 1, NOW()),
    (78, 18, '/img/20.png', 'THUMBNAIL', 1, NOW()),
    (79, 18, '/img/1.png',  'GENERAL',   1, NOW()),
    (80, 18, '/img/2.png',  'GENERAL',   2, NOW()),


    (81, 19, '/img/19.png', 'MAIN', 1, NOW()),
    (82, 19, '/img/19.png', 'THUMBNAIL', 1, NOW()),
    (83, 19, '/img/3.png',  'GENERAL',   1, NOW()),
    (84, 19, '/img/4.png',  'GENERAL',   2, NOW()),
    (85, 19, '/img/5.png',  'GENERAL',   3, NOW()),
    (86, 19, '/img/6.png',  'GENERAL',   4, NOW()),


    (87, 20, '/img/18.png', 'MAIN', 1, NOW()),
    (88, 20, '/img/18.png', 'THUMBNAIL', 1, NOW()),
    (89, 20, '/img/7.png',  'GENERAL',   1, NOW()),
    (90, 20, '/img/8.png',  'GENERAL',   2, NOW());

-- AUTO_INCREMENT 값 조정 (총 90개 들어갔으므로 91로 설정)
ALTER TABLE room_images AUTO_INCREMENT = 91;

-- =============================================================================
-- E) RESERVATIONS (6 reservations)
-- =============================================================================
-- ReservationStatus enum: TEMP, CONFIRMED, EXPIRED, CANCELED, USED
-- TEMP requires expires_at
-- Using room_id 1~5, operation_policy_id/refund_policy_id should match rooms' policy settings

INSERT INTO reservations (
    id,
    member_id,
    room_id,
    applied_operation_policy_id,
    applied_refund_policy_id,
    status,
    start_time,
    end_time,
    total_amount,
    expires_at,
    confirmed_at,
    canceled_at,
    used_at,
    created_at,
    updated_at
)

VALUES
    -- =========================================================
    -- 고정 날짜 데이터 (rooms 기준: price/op/ref 모두 일치하도록 유지)
    -- =========================================================

    -- Room 1 (op=1, ref=1, price=10000): 2026-02-10
    (1, 1, 1, 1, 1, 'CONFIRMED', '2026-02-10 09:00:00', '2026-02-10 10:00:00', 10000, NULL, '2026-02-09 10:00:00', NULL, NULL, '2026-02-09 10:00:00', '2026-02-09 10:00:00'),
    (2, 2, 1, 1, 1, 'USED',      '2026-02-10 10:00:00', '2026-02-10 11:00:00', 10000, NULL, '2026-02-09 11:00:00', NULL, '2026-02-10 10:30:00', '2026-02-09 11:00:00', '2026-02-10 10:30:00'),

    -- Room 2 (op=1, ref=1, price=15000): 2026-02-10
    (3, 1, 2, 1, 1, 'CANCELED',  '2026-02-10 09:00:00', '2026-02-10 10:00:00', 15000, NULL, '2026-02-09 09:00:00', '2026-02-09 20:00:00', NULL, '2026-02-09 09:00:00', '2026-02-09 20:00:00'),
    (4, 3, 2, 1, 1, 'CONFIRMED', '2026-02-10 11:00:00', '2026-02-10 12:00:00', 15000, NULL, '2026-02-09 12:00:00', NULL, NULL, '2026-02-09 12:00:00', '2026-02-09 12:00:00'),

    -- Room 3 (op=2, ref=1, price=25000): 2026-02-10
    (5, 2, 3, 2, 1, 'USED',     '2026-02-10 08:00:00', '2026-02-10 09:00:00', 25000, NULL, '2026-02-09 08:00:00', NULL, '2026-02-10 08:30:00', '2026-02-09 08:00:00', '2026-02-10 08:30:00'),
    (6, 1, 3, 2, 1, 'EXPIRED',  '2026-02-10 09:00:00', '2026-02-10 10:00:00', 25000, NULL, NULL, NULL, NULL, '2026-02-09 09:00:00', '2026-02-09 09:30:00'),

    -- Room 4 (op=1, ref=2, price=40000): 2026-02-10 (2시간)
    (7, 3, 4, 1, 2, 'CONFIRMED', '2026-02-10 09:00:00', '2026-02-10 11:00:00', 80000, NULL, '2026-02-08 10:00:00', NULL, NULL, '2026-02-08 10:00:00', '2026-02-08 10:00:00'),
    (8, 1, 4, 1, 2, 'CANCELED',  '2026-02-10 11:00:00', '2026-02-10 13:00:00', 80000, NULL, '2026-02-08 11:00:00', '2026-02-09 10:00:00', NULL, '2026-02-08 11:00:00', '2026-02-09 10:00:00'),

    -- Room 5 (op=2, ref=2, price=60000): 2026-02-10 (2시간)
    (9,  2, 5, 2, 2, 'USED', '2026-02-10 08:00:00', '2026-02-10 10:00:00', 120000, NULL, '2026-02-08 08:00:00', NULL, '2026-02-10 09:00:00', '2026-02-08 08:00:00', '2026-02-10 09:00:00'),
    (10, 3, 5, 2, 2, 'TEMP', '2026-02-15 10:00:00', '2026-02-15 12:00:00', 120000, '2026-02-14 12:00:00', NULL, NULL, NULL, '2026-02-14 11:45:00', '2026-02-14 11:45:00'),

    -- Room 6 (op=1, ref=1, price=8000): 2026-02-11
    (11, 1, 6, 1, 1, 'CONFIRMED', '2026-02-11 09:00:00', '2026-02-11 10:00:00', 8000, NULL, '2026-02-10 09:00:00', NULL, NULL, '2026-02-10 09:00:00', '2026-02-10 09:00:00'),
    (12, 2, 6, 1, 1, 'USED',      '2026-02-11 10:00:00', '2026-02-11 11:00:00', 8000, NULL, '2026-02-10 10:00:00', NULL, '2026-02-11 10:30:00', '2026-02-10 10:00:00', '2026-02-11 10:30:00'),

    -- Room 7 (op=1, ref=1, price=12000): 2026-02-11
    (13, 3, 7, 1, 1, 'CANCELED',  '2026-02-11 09:00:00', '2026-02-11 10:00:00', 12000, NULL, '2026-02-10 09:00:00', '2026-02-10 20:00:00', NULL, '2026-02-10 09:00:00', '2026-02-10 20:00:00'),
    (14, 1, 7, 1, 1, 'CONFIRMED', '2026-02-11 11:00:00', '2026-02-11 12:00:00', 12000, NULL, '2026-02-10 11:00:00', NULL, NULL, '2026-02-10 11:00:00', '2026-02-10 11:00:00'),

    -- Room 8 (op=2, ref=1, price=22000): 2026-02-11
    (15, 2, 8, 2, 1, 'USED',    '2026-02-11 08:00:00', '2026-02-11 09:00:00', 22000, NULL, '2026-02-10 08:00:00', NULL, '2026-02-11 08:30:00', '2026-02-10 08:00:00', '2026-02-11 08:30:00'),
    (16, 3, 8, 2, 1, 'EXPIRED', '2026-02-11 09:00:00', '2026-02-11 10:00:00', 22000, NULL, NULL, NULL, NULL, '2026-02-10 09:00:00', '2026-02-10 09:30:00'),

    -- Room 9 (op=1, ref=1, price=9000): 2026-02-11
    (17, 1, 9, 1, 1, 'CONFIRMED', '2026-02-11 09:00:00', '2026-02-11 10:00:00', 9000, NULL, '2026-02-10 09:00:00', NULL, NULL, '2026-02-10 09:00:00', '2026-02-10 09:00:00'),
    (18, 2, 9, 1, 1, 'TEMP',      '2026-02-16 10:00:00', '2026-02-16 11:00:00', 9000, '2026-02-15 12:00:00', NULL, NULL, NULL, '2026-02-15 11:45:00', '2026-02-15 11:45:00'),

    -- Room 10 (op=3, ref=1, price=18000): 2026-02-14 (주말)
    (19, 3, 10, 3, 1, 'USED',     '2026-02-14 09:00:00', '2026-02-14 10:00:00', 18000, NULL, '2026-02-13 09:00:00', NULL, '2026-02-14 09:30:00', '2026-02-13 09:00:00', '2026-02-14 09:30:00'),
    (20, 1, 10, 3, 1, 'CANCELED', '2026-02-14 11:00:00', '2026-02-14 12:00:00', 18000, NULL, '2026-02-13 11:00:00', '2026-02-13 20:00:00', NULL, '2026-02-13 11:00:00', '2026-02-13 20:00:00'),

    -- Room 11 (op=1, ref=2, price=50000): 2026-02-12 (2시간)
    (21, 2, 11, 1, 2, 'CONFIRMED', '2026-02-12 09:00:00', '2026-02-12 11:00:00', 100000, NULL, '2026-02-10 09:00:00', NULL, NULL, '2026-02-10 09:00:00', '2026-02-10 09:00:00'),
    (22, 3, 11, 1, 2, 'USED',      '2026-02-12 11:00:00', '2026-02-12 13:00:00', 100000, NULL, '2026-02-10 11:00:00', NULL, '2026-02-12 12:00:00', '2026-02-10 11:00:00', '2026-02-12 12:00:00'),

    -- Room 12 (op=2, ref=1, price=28000): 2026-02-12
    (23, 1, 12, 2, 1, 'EXPIRED',   '2026-02-12 08:00:00', '2026-02-12 09:00:00', 28000, NULL, NULL, NULL, NULL, '2026-02-11 08:00:00', '2026-02-11 08:30:00'),
    (24, 2, 12, 2, 1, 'CONFIRMED', '2026-02-12 09:00:00', '2026-02-12 10:00:00', 28000, NULL, '2026-02-11 09:00:00', NULL, NULL, '2026-02-11 09:00:00', '2026-02-11 09:00:00'),

    -- Room 13 (op=1, ref=1, price=14000): 2026-02-12
    (25, 3, 13, 1, 1, 'CANCELED', '2026-02-12 09:00:00', '2026-02-12 10:00:00', 14000, NULL, '2026-02-11 09:00:00', '2026-02-11 21:00:00', NULL, '2026-02-11 09:00:00', '2026-02-11 21:00:00'),
    (26, 1, 13, 1, 1, 'USED',     '2026-02-12 10:00:00', '2026-02-12 11:00:00', 14000, NULL, '2026-02-11 10:00:00', NULL, '2026-02-12 10:30:00', '2026-02-11 10:00:00', '2026-02-12 10:30:00'),

    -- Room 14 (op=1, ref=1, price=7000): 2026-02-12
    (27, 2, 14, 1, 1, 'CONFIRMED', '2026-02-12 09:00:00', '2026-02-12 10:00:00', 7000, NULL, '2026-02-11 09:00:00', NULL, NULL, '2026-02-11 09:00:00', '2026-02-11 09:00:00'),
    (28, 3, 14, 1, 1, 'TEMP',      '2026-02-17 10:00:00', '2026-02-17 11:00:00', 7000, '2026-02-16 12:00:00', NULL, NULL, NULL, '2026-02-16 11:45:00', '2026-02-16 11:45:00'),

    -- Room 15 (op=2, ref=2, price=45000): 2026-02-13 (2시간)
    (29, 1, 15, 2, 2, 'USED',     '2026-02-13 08:00:00', '2026-02-13 10:00:00', 90000, NULL, '2026-02-11 08:00:00', NULL, '2026-02-13 09:00:00', '2026-02-11 08:00:00', '2026-02-13 09:00:00'),
    (30, 2, 15, 2, 2, 'CANCELED', '2026-02-13 10:00:00', '2026-02-13 12:00:00', 90000, NULL, '2026-02-11 10:00:00', '2026-02-12 10:00:00', NULL, '2026-02-11 10:00:00', '2026-02-12 10:00:00'),

    -- Room 16 (op=1, ref=1, price=20000): 2026-02-13
    (31, 3, 16, 1, 1, 'CONFIRMED', '2026-02-13 09:00:00', '2026-02-13 10:00:00', 20000, NULL, '2026-02-12 09:00:00', NULL, NULL, '2026-02-12 09:00:00', '2026-02-12 09:00:00'),
    (32, 1, 16, 1, 1, 'EXPIRED',   '2026-02-13 10:00:00', '2026-02-13 11:00:00', 20000, NULL, NULL, NULL, NULL, '2026-02-12 10:00:00', '2026-02-12 10:30:00'),

    -- Room 17 (op=3, ref=1, price=16000): 2026-02-15
    (33, 2, 17, 3, 1, 'USED',      '2026-02-15 09:00:00', '2026-02-15 10:00:00', 16000, NULL, '2026-02-14 09:00:00', NULL, '2026-02-15 09:30:00', '2026-02-14 09:00:00', '2026-02-15 09:30:00'),
    (34, 3, 17, 3, 1, 'CONFIRMED', '2026-02-15 10:00:00', '2026-02-15 11:00:00', 16000, NULL, '2026-02-14 10:00:00', NULL, NULL, '2026-02-14 10:00:00', '2026-02-14 10:00:00'),

    -- Room 18 (op=1, ref=1, price=11000): 2026-02-13
    (35, 1, 18, 1, 1, 'CANCELED', '2026-02-13 09:00:00', '2026-02-13 10:00:00', 11000, NULL, '2026-02-12 09:00:00', '2026-02-12 22:00:00', NULL, '2026-02-12 09:00:00', '2026-02-12 22:00:00'),
    (36, 2, 18, 1, 1, 'USED',     '2026-02-13 10:00:00', '2026-02-13 11:00:00', 11000, NULL, '2026-02-12 10:00:00', NULL, '2026-02-13 10:30:00', '2026-02-12 10:00:00', '2026-02-13 10:30:00'),

    -- Room 19 (op=2, ref=2, price=55000): 2026-02-14 (2시간)
    (37, 3, 19, 2, 2, 'CONFIRMED', '2026-02-14 08:00:00', '2026-02-14 10:00:00', 110000, NULL, '2026-02-12 08:00:00', NULL, NULL, '2026-02-12 08:00:00', '2026-02-12 08:00:00'),
    (38, 1, 19, 2, 2, 'TEMP',      '2026-02-18 10:00:00', '2026-02-18 12:00:00', 110000, '2026-02-17 12:00:00', NULL, NULL, NULL, '2026-02-17 11:45:00', '2026-02-17 11:45:00'),

    -- Room 20 (op=1, ref=1, price=35000): 2026-02-14
    (39, 2, 20, 1, 1, 'EXPIRED',   '2026-02-14 09:00:00', '2026-02-14 10:00:00', 35000, NULL, NULL, NULL, NULL, '2026-02-13 09:00:00', '2026-02-13 09:30:00'),
    (40, 3, 20, 1, 1, 'CONFIRMED', '2026-02-14 10:00:00', '2026-02-14 11:00:00', 35000, NULL, '2026-02-13 10:00:00', NULL, NULL, '2026-02-13 10:00:00', '2026-02-13 10:00:00'),

    (41, 1, 1, 1, 1, 'TEMP',
     NOW() + INTERVAL 2 HOUR,
     NOW() + INTERVAL 3 HOUR + INTERVAL 30 MINUTE,
     10000,
     NOW() + INTERVAL 15 MINUTE,
     NULL, NULL, NULL,
     NOW(), NOW()),

    (42, 1, 2, 1, 1, 'CONFIRMED',
     NOW() + INTERVAL 1 DAY,
     NOW() + INTERVAL 1 DAY + INTERVAL 2 HOUR,
     15000,
     NULL,
     NOW(), NULL, NULL,
     NOW(), NOW()),

    (43, 2, 3, 2, 1, 'EXPIRED',
     NOW() + INTERVAL 4 HOUR,
     NOW() + INTERVAL 5 HOUR,
     25000,
     NULL,
     NULL, NULL, NULL,
     NOW(), NOW()),

    (44, 2, 4, 1, 2, 'CANCELED',
     NOW() + INTERVAL 2 DAY,
     NOW() + INTERVAL 2 DAY + INTERVAL 2 HOUR,
     40000,
     NULL,
     NULL, NOW(), NULL,
     NOW(), NOW()),

    (45, 3, 5, 2, 2, 'USED',
     NOW() - INTERVAL 3 HOUR,
     NOW() - INTERVAL 1 HOUR,
     60000,
     NULL,
     NOW(), NULL, NOW() - INTERVAL 30 MINUTE,
     NOW(), NOW()),

    -- 6. 관리자: 임시 예약 (TEMP)
    (46, 3, 2, 1, 1, 'TEMP',
     NOW() + INTERVAL 6 HOUR,
     NOW() + INTERVAL 7 HOUR,
     15000,
     NOW() + INTERVAL 10 MINUTE,
     NULL, NULL, NULL,
     NOW(), NOW()),

    -- 7. [추가] 홍길동: 과거 이용 완료 데이터 (히스토리용)
    (47, 1, 3, 2, 1, 'USED',
     NOW() - INTERVAL 2 DAY,
     NOW() - INTERVAL 2 DAY + INTERVAL 2 HOUR,
     25000,
     NULL,
     NOW() - INTERVAL 2 DAY, NULL, NOW() - INTERVAL 2 DAY + INTERVAL 1 HOUR,
     NOW(), NOW()),

    -- 8. [추가] 홍길동: 예약 취소 데이터 (히스토리용)
    (48, 1, 4, 1, 2, 'CANCELED',
     NOW() + INTERVAL 3 DAY,
     NOW() + INTERVAL 3 DAY + INTERVAL 2 HOUR,
     40000,
     NULL,
     NULL, NOW() - INTERVAL 1 HOUR, NULL,
     NOW(), NOW()),

    -- 9. [추가] 홍길동: 50% 환불 테스트용 (5시간 뒤 예약, 일반 정책)
    (49, 1, 1, 1, 1, 'CONFIRMED',
     NOW() + INTERVAL 5 HOUR,
     NOW() + INTERVAL 7 HOUR,
     20000,
     NULL,
     NOW(), NULL, NULL,
     NOW(), NOW()),

    -- 10. [추가] 홍길동: 20% 환불 테스트용 (13시간 뒤 예약, 엄격 정책)
    (50, 1, 4, 1, 2, 'CONFIRMED',
     NOW() + INTERVAL 13 HOUR,
     NOW() + INTERVAL 15 HOUR,
     80000,
     NULL,
     NOW(), NULL, NULL,
     NOW(), NOW()),

    -- 11. [추가] 홍길동: 0% 환불 테스트용 (30분 뒤 예약, 일반 정책)
    (51, 1, 1, 1, 1, 'CONFIRMED',
     NOW() + INTERVAL 30 MINUTE,
     NOW() + INTERVAL 2 HOUR + INTERVAL 30 MINUTE,
     20000,
     NULL,
     NOW(), NULL, NULL,
     NOW(), NOW());

-- =============================================================================
-- Reset AUTO_INCREMENT (데이터 51개이므로 52로 설정)
-- =============================================================================
ALTER TABLE reservations AUTO_INCREMENT = 52;

-- =============================================================================
-- E-1) PAYMENTS (For Refund Test)
-- =============================================================================
-- 환불 테스트를 위해서는 결제 정보(Payment)가 반드시 있어야 합니다.
-- Reservation ID 49, 50, 51에 대한 가상 결제 정보 추가

INSERT INTO payments (
    id,
    reservation_id,
    pg_tid,
    success_order_id,
    amount,
    payment_method,
    payment_status,
    approved_at,
    created_at,
    updated_at
)
VALUES
    (1, 49, 'test_payment_key_9', 'order_id_9', 20000, 'PG', 'SUCCESS', NOW(), NOW(), NOW()),
    (2, 50, 'test_payment_key_10', 'order_id_10', 80000, 'PG', 'SUCCESS', NOW(), NOW(), NOW()),
    (3, 51, 'test_payment_key_11', 'order_id_11', 20000, 'PG', 'SUCCESS', NOW(), NOW(), NOW());

ALTER TABLE payments AUTO_INCREMENT = 4;


-- =============================================================================
-- F) MEMBERS (3 members)
-- =============================================================================
-- Role enum: USER, ADMIN
-- password는 암호화 안 된 더미 값 (실제 서비스에서는 BCrypt 등 사용)

INSERT INTO members (
    id,
    name,
    email,
    password,
    phone_number,
    role,
    created_at,
    updated_at,
    deleted_at
)
VALUES
    (1, '홍길동', 'hong@test.com', '$2a$10$r2IayIlyry63ToWDymcDkOrZ.1RgcdT28d0drqkR9XSwd2b9USgZm', '010-1234-1234', 'USER', NOW(), NOW(), NULL),
    (2, '김개발', 'dev@test.com', '$2a$10$r2IayIlyry63ToWDymcDkOrZ.1RgcdT28d0drqkR9XSwd2b9USgZm', '010-2222-2222', 'USER', NOW(), NOW(), NULL),
    (3, '관리자', 'admin2@test.com', '$2a$10$r2IayIlyry63ToWDymcDkOrZ.1RgcdT28d0drqkR9XSwd2b9USgZm', '010-9999-9999', 'ADMIN', NOW(), NOW(), NULL);

-- AUTO_INCREMENT 맞추기
-- =============================================================================
-- Reset AUTO_INCREMENT for future inserts
-- =============================================================================
ALTER TABLE members AUTO_INCREMENT = 4;
ALTER TABLE reservations AUTO_INCREMENT = 52;
ALTER TABLE operation_policies AUTO_INCREMENT = 4;
ALTER TABLE operation_schedules AUTO_INCREMENT = 22;
ALTER TABLE room_rules AUTO_INCREMENT = 6;
ALTER TABLE refund_policies AUTO_INCREMENT = 3;
ALTER TABLE refund_rules AUTO_INCREMENT = 10;
ALTER TABLE rooms AUTO_INCREMENT = 21;
