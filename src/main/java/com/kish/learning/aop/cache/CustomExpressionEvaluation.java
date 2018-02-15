package com.kish.learning.aop.cache;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CustomExpressionEvaluation<T> extends CachedExpressionEvaluator{

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private final Map<AnnotatedElementKey,Method>  targetMethodCache = new ConcurrentHashMap<>();

    public EvaluationContext createEvaluationContext(Object object,Class<?> targetClass,Method method,Object[] args){
        Method targetMethod = getTargetMethod(targetClass,method);
        ExpressionRootObject root = new ExpressionRootObject(object,args);
        return new MethodBasedEvaluationContext(root,targetMethod,args,this.parameterNameDiscoverer);
    }

    private Method getTargetMethod(Class<?> targetClass,Method method){
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method,targetClass);
        Method targetMethod = this.targetMethodCache.get(methodKey);
        if(targetMethod == null){
            targetMethod = AopUtils.getMostSpecificMethod(method,targetClass);
            if(targetMethod == null){
                targetMethod = method;
            }
            this.targetMethodCache.put(methodKey,targetMethod);
        }
        return  targetMethod;
    }

}
