package com.example.studyroomreservation.global.aop;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class CustomSpringELParser {
    private CustomSpringELParser() {
    }
    private final static SpelExpressionParser SPEL_EXPRESSION_PARSER = new SpelExpressionParser();

    public static Object getDynamicValue(String[] parameterNames, Object[] args, String key) {
        StandardEvaluationContext context = new StandardEvaluationContext();

        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        return SPEL_EXPRESSION_PARSER.parseExpression(key).getValue(context, Object.class);
    }
}