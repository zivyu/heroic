package com.spotify.heroic.grammar;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import static com.spotify.heroic.grammar.ExpressionTests.biFuncTest;
import static com.spotify.heroic.grammar.ExpressionTests.uniFuncTest;
import static org.junit.Assert.assertEquals;

public class FloatExpressionTest extends AbstractExpressionTest<FloatExpression> {
    @Override
    protected FloatExpression build(final Context ctx) {
        return new FloatExpression(ctx, 42D);
    }

    @Override
    protected BiFunction<Expression.Visitor<Void>, FloatExpression, Void> visitorMethod() {
        return Expression.Visitor::visitDouble;
    }

    @Test
    public void castTest() {
        final FloatExpression d = build();

        assertEquals(new IntegerExpression(ctx, (long) d.getValue()),
            d.cast(IntegerExpression.class));
        assertEquals(new DurationExpression(ctx, TimeUnit.MILLISECONDS, (int) d.getValue()),
            d.cast(DurationExpression.class));
    }

    @Test
    public void operationsTest() {
        biFuncTest(a -> new FloatExpression(a, 21D), b -> new FloatExpression(b, 21D),
            r -> new FloatExpression(r, 42D), FloatExpression::add);

        biFuncTest(a -> new FloatExpression(a, 21D), b -> new FloatExpression(b, 21D),
            r -> new FloatExpression(r, 0D), FloatExpression::sub);

        biFuncTest(a -> new FloatExpression(a, 20D), b -> new FloatExpression(b, 2D),
            r -> new FloatExpression(r, 10D), FloatExpression::divide);

        biFuncTest(a -> new FloatExpression(a, 10D), b -> new FloatExpression(b, 20D),
            r -> new FloatExpression(r, 200D), FloatExpression::multiply);

        uniFuncTest(a -> new FloatExpression(a, 10D), r -> new FloatExpression(r, -10D),
            FloatExpression::negate);
    }
}
