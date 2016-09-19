package com.spotify.heroic.grammar;

import java.util.function.BiFunction;

public class AnyExpressionTest extends AbstractExpressionTest<AnyExpression> {
    @Override
    protected AnyExpression build(final Context ctx) {
        return new AnyExpression(ctx);
    }

    @Override
    protected BiFunction<Expression.Visitor<Void>, AnyExpression, Void> visitorMethod() {
        return Expression.Visitor::visitAny;
    }
}
