package com.example.studyroomreservation.global.aop;

import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // @Transactional 보다 먼저 실행됨
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAop {

    private final RedissonClient redissonClient;

    @Around("@annotation(com.example.studyroomreservation.global.aop.DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        // 키 생성 (SpEL 파싱)
        String key = CustomSpringELParser.getDynamicValue(
                signature.getParameterNames(),
                joinPoint.getArgs(),
                distributedLock.key()
        ).toString();

        RLock rLock = redissonClient.getLock(key);

        try {
            // 락 획득 시도
            boolean available = rLock.tryLock(
                    distributedLock.waitTime(),
                    distributedLock.leaseTime(),
                    distributedLock.timeUnit()
            );

            if (!available) {
                log.warn("[락 획득 실패] key: {}", key);
                // 락 획득 실패 시 에러 던짐 (재시도 요청 등)
                throw new BusinessException(ErrorCode.RES_CONCURRENT_ACCESS);
            }

            return joinPoint.proceed();
        } catch (InterruptedException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        } finally {
            // 락 해제
            try {
                rLock.unlock();
            } catch (IllegalMonitorStateException e) {
                // 이미 락이 해제된 경우(leaseTime 초과 등) 로그만 남기고 무시
                log.info("Redisson Lock Already Unlocked - Key: {}", key);
            }
        }
    }
}