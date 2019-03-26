---
layout: doc-page
title: "Union Types"
---

Used on types, the `|` operator creates a union type.

```scala
case class UserName(name: String) {
  def lookup(admin: Admin): UserData
}
case class Password(hash: Hash) {
  def lookup(admin: Admin): UserData
}

def help(id: UserName | Password) = {
  val user = id match {
    case UserName(name) => lookupName(name)
    case Password(hash) => lookupPassword(hash)
  }
  // ...
}
```

Union types are dual of intersection types. Values of type `A | B` are
all values of type `A` and all values of type `B`. `|` is _commutative_:
`A | B` is the same type as `B | A`.

The compiler will assign a union type to an expression only if such a
type is explicitly given.
This can be seen in the following REPL transcript:

```scala
scala> val password = Password(123)
val password: Password = Password(123)
scala> val name = UserName("Eve")
val name: UserName = UserName(Eve)
scala> if (true) name else password
val res2: Object & Product = UserName(Eve)
scala> val either: Password | UserName = if (true) name else password
val either: Password | UserName = UserName(Eve)
```

The type of `res2` is `Object & Product`, which is a supertype of
`UserName` and `Product`, but not the least supertype `Password |
UserName`.  If we want the least supertype, we have to give it
explicitly, as is done for the type of `either`.

[More details](./union-types-spec.html)

