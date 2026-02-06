package com.example.studyroomreservation.global.aop;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * 락의 이름 (Key). SpEL 표현식 사용 가능
     * 예: "'room:reservation:' + #key"
     */
    String key();

    /**
     * 락의 시간 단위 (기본: 초)
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 락 획득을 위해 기다리는 시간 (기본: 5초)
     * 락 획득에 실패하면 예외가 발생합니다.
     */
    long waitTime() default 5L;

    /**
     * 락을 임대하는 시간 (기본: 3초)
     * 락을 획득한 후 3초가 지나면 자동으로 해제됩니다.
     */
    long leaseTime() default 3L;
}