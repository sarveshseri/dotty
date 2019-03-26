---
layout: doc-page
title: "Context Query Types - More Details"
---

## Syntax

    Type              ::=  ...
                        |  `given' FunArgTypes `=>' Type
    Expr              ::=  ...
                        |  `given' FunParams `=>' Expr

Context query types associate to the right, e.g.
`given S => given T => U` is the same as `given S => (given T => U)`.

## Implementation

Context query types are shorthands for class types that define `apply`
methods with inferable parameters. Specifically, the `N`-ary function type
`T1, ..., TN => R` is a shorthand for the class type
`ImplicitFunctionN[T1 , ... , TN, R]`. Such class types are assumed to have the following definitions, for any value of `N >= 1`:
```scala
package scala
trait ImplicitFunctionN[-T1 , ... , -TN, +R] {
  def apply given (x1: T1 , ... , xN: TN): R
}
```
Context query types erase to normal function types, so these classes are
generated on the fly for typechecking, but not realized in actual code.

Context query literals `given (x1: T1, ..., xn: Tn) => e` map
inferable parameters `xi` of types `Ti` to a result given by expression `e`.
The scope of each implicit parameter `xi` is `e`. The parameters must have pairwise distinct names.

If the expected type of the query literal is of the form
`scala.ImplicitFunctionN[S1, ..., Sn, R]`, the expected type of `e` is `R` and
the type `Ti` of any of the parameters `xi` can be omitted, in which case `Ti
= Si` is assumed. If the expected type of the query literal is
some other type, all inferable parameter types must be explicitly given, and the expected type of `e` is undefined. The type of the query literal is `scala.ImplicitFunctionN[S1, ...,Sn, T]`, where `T` is the widened
type of `e`. `T` must be equivalent to a type which does not refer to any of
the inferable parameters `xi`.

The query literal is evaluated as the instance creation
expression:
```scala
new scala.ImplicitFunctionN[T1, ..., Tn, T] {
  def apply given (x1: T1, ..., xn: Tn): T = e
}
```
In the case of a single untyped parameter, `given (x) => e` can be
abbreviated to `given x => e`.

An inferable parameter may also be a wildcard represented by an underscore `_`. In
that case, a fresh name for the parameter is chosen arbitrarily.

Note: The closing paragraph of the
[Anonymous Functions section](https://www.scala-lang.org/files/archive/spec/2.12/06-expressions.html#anonymous-functions)
of Scala 2.12 is subsumed by query types and should be removed.

Query literals `given (x1: T1, ..., xn: Tn) => e` are
automatically created for any expression `e` whose expected type is
`scala.ImplicitFunctionN[T1, ..., Tn, R]`, unless `e` is
itself a query literal. This is analogous to the automatic
insertion of `scala.Function0` around expressions in by-name argument position.

Context query types generalize to `N > 22` in the same way that function types do, see [the corresponding
documentation](https://dotty.epfl.ch/docs/reference/dropped-features/limit22.html).

## Examples

See the section on Expressiveness from [Simplicitly: foundations and
applications of implicit function
types](https://dl.acm.org/citation.cfm?id=3158130). I've extracted it in [this
Gist](https://gist.github.com/OlivierBlanvillain/234d3927fe9e9c6fba074b53a7bd9
592), it might easier to access than the pdf.

### Type Checking

After desugaring no additional typing rules are required for context query types.
