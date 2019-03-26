
/*
 * Copyright (C) 2012-2014 Typesafe Inc. <http://www.typesafe.com>
 */

package dotty.runtime.function;

@FunctionalInterface
public interface JFunction2$mcJJJ$sp extends JFunction2 {
    abstract long apply$mcJJJ$sp(long v1, long v2);

    default Object apply(Object v1, Object v2) { return (Long) apply$mcJJJ$sp(scala.runtime.BoxesRunTime.unboxToLong(v1), scala.runtime.BoxesRunTime.unboxToLong(v2)); }
}
