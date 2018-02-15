package com.kish.learning.aop.cache;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component

public class KishCacheAnnotationInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(KishCacheAnnotationInterceptor.class);

    private ConcurrentHashMap<Object,Object> tempCache = new ConcurrentHashMap<>();


    private CustomExpressionEvaluation evaluation =new CustomExpressionEvaluation();


    @Around("@annotation(KishCache)")
    public Object cacheable(ProceedingJoinPoint jp) throws Throwable {
        Object value = null;

        MethodSignature methodSignature = (MethodSignature) jp.getSignature();
        Method method = methodSignature.getMethod();

        KishCache kishCache = method.getAnnotation(KishCache.class);

        if(kishCache !=null){
            ExpressionParser expressionParser = new SpelExpressionParser();
            EvaluationContext evaluationContext =  evaluation.createEvaluationContext(jp.getTarget(),jp.getTarget().getClass(),((MethodSignature) jp.getSignature()).getMethod(), jp.getArgs());
            Expression expression = expressionParser.parseExpression(kishCache.key());
            Object key = expression.getValue(evaluationContext);
            value = tempCache.get(key);
            if(value == null){
               value =jp.proceed();
               tempCache.put(key,value);
            }
        }
        return  value;
    }
}
