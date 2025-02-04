package com.authorization.system.log;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import com.authorization.common.util.RequestUtils;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 当前工程的接口日志aop配置类:
 * <p>
 * 切点表达式参考：
 * https://cloud.tencent.com/developer/article/1497814
 * <p>
 * https://www.jianshu.com/p/88a6ac46f7ed
 * <p>
 * 相关通知的定义：
 * <p>
 * https://www.cnblogs.com/qlqwjy/p/8729280.html
 * </p>
 *
 * @author wangjunming
 * @since 2022/3/27 13:02
 */
@Slf4j
@Aspect
@Component
public class LogAopAdvice {

    /**
     * 控制层的切点-拦截包或者子包中定义的方法，即 拦截 controller 包或者子包中定义的所有方法
     */
    @Pointcut("execution(* com.authorization.system.controller..*.*(..))")
    private void controllerPointCut() {
    }

    /**
     * dubbo接口的切点--拦截包或者子包中定义的全部方法，即 拦截 remote 包及子包中任意类的任意方法
     */
    @Pointcut("execution(* com.authorization.system.controller..*.*(..))")
    private void remotePointCut() {
    }

    /**
     * 注解的切点
     */
    @Pointcut("@annotation(com.authorization.system.log.LogAdvice)")
    private void annotationPointCut() {
    }

    /**
     * 需要进行通知的切点
     */
    @Pointcut("controllerPointCut() || remotePointCut() || annotationPointCut()")
    private void pointCut() {
    }

    /**
     * 环绕通知
     */
    @Around("pointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        log.info("around advice start ...");
        Log.LogBuilder builder = Log.builder()
                .threadId(Long.toString(Thread.currentThread().getId()))
                .threadName(Thread.currentThread().getName());
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (Objects.nonNull(requestAttributes)) {
            HttpServletRequest request = RequestUtils.getRequest();
            builder.url(RequestUtils.getServerAndContextUrl() + request.getRequestURI())
                    .httpMethod(request.getMethod());
        }
        getAnnotationValue(point);
        String classMethod = String.format("%s.%s", point.getSignature().getDeclaringTypeName(), point.getSignature().getName());
        builder.classMethod(classMethod)
                .requestParams(getNameAndValue(point))
                .timeCost(System.currentTimeMillis() - startTime);
        final Log logParamsInfo = builder.build();
        log.info("around advice params = {}", JSONUtil.toJsonStr(JSONUtil.parse(logParamsInfo), 2));
        Object result = point.proceed();
        builder.result(result);
        final Log logResultInfo = builder.build();
        log.info("around advice params and result = {}", JSONUtil.toJsonStr(logResultInfo));
        return result;
    }

    /**
     * 获取方法上注解的参数信息
     */
    private void getAnnotationValue(ProceedingJoinPoint joinPoint) {
        final Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        //日志注解
        LogAdvice logAdvice = methodSignature.getMethod().getAnnotation(LogAdvice.class);
        if (Objects.nonNull(logAdvice)) {
            String name = logAdvice.name();
            log.info("before advice LogAopAno = {}", name);
        }
    }

    /**
     * 获取接口传参的信息
     */
    private Map<String, Object> getNameAndValue(ProceedingJoinPoint joinPoint) {
        final Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        final String[] names = methodSignature.getParameterNames();
        final Object[] args = joinPoint.getArgs();
        if (ArrayUtil.isEmpty(names) || ArrayUtil.isEmpty(args)) {
            return Collections.emptyMap();
        }
        if (names.length != args.length) {
            log.warn("{}方法参数名和参数值数量不一致", methodSignature.getName());
            return Collections.emptyMap();
        }
        Map<String, Object> map = Maps.newHashMap();
        for (int i = 0; i < names.length; i++) {
            map.put(names[i], args[i]);
        }
        return map;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Log {
        /**
         * 线程id
         */
        private String threadId;
        /**
         * 线程名称
         */
        private String threadName;
        /**
         * url
         */
        private String url;
        /**
         * http方法 GET POST PUT DELETE PATCH
         */
        private String httpMethod;
        /**
         * 类方法
         */
        private String classMethod;
        /**
         * 请求参数
         */
        private Object requestParams;
        /**
         * 返回参数
         */
        private Object result;
        /**
         * 接口耗时
         */
        private Long timeCost;
    }


}
