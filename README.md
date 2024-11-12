# About
This project implements the algorithm described in the paper 
[A Framework for Approximate Generalization in Quantitative Theories](https://doi.org/10.1007/978-3-031-10769-6_34).

# Using the program
This section contains a breakdown of the parameters you can provide the algorithm with, and how you can interpret the results.

---
### Problem
An anti-unification problem can be defined using the `Problem` builder class.
Calling `Problem.run()` executes the program. For example:

```Java
Set<Solution> solutions =
    new Problem("f(a(), b() c()) ?= g(x, y)")
        .proximityRelations("g h [0.5] {(1 2) (3 1)} ; g h [0.7] {(1 1) (2 2) (2 3)}")
        .lambda(0.5f)
        // ...
        .run();
```
You can specify the following parameters... 

---
### Equation
The minimum required input is an equation (a.k.a. a pair of terms),
which you can provide via a simple string input, e.g. `f(a(), b(), c()) ?= g(x, y)`,
or construct programmatically with `GroundTerm` and `MappedVariableTerm`.

> *Note on the naming:* The input terms must by definition be "ground", i.e. they must contain no variables `TODO explain this more`.
> `MappedVariableTerm` is basically a "cheat" so you can have variables from the original problem domain in the input.

If you provide nothing besides a problem equation, you get a standard generalization,
which just tells you the "top level" terms that are the same.
<br>
E.g. `f(a()) ?= f(b())` results only in the generalization `f(x1)`,
with substitution `x1: a()` on the left side and `x1: b()` on the right.

---
### Proximity Relations
To introduce our fuzzy logic, we need some of the functions appearing in the equation to approximate each other (`f ~ g`)
or share some common proximate (`f ~ h` & `g ~ h`).

The `proximity` of such a relation is some value in the range `[0.0, 1.0]`.
<br> Each (non-zero) relation also has an associated `argument relation`, defining which of their arguments map onto each others'.

Provide your desired relations as a set of `ProximityRelations`, or via string:
<br> `g h [0.5] {(1 2) (3 1)} ; g h [0.7] {(1 1) (2 2) (2 3)}` _*)_

> Proximity relations are by definition symmetric, so `f g [0.5] {(1 2) (2 3)}` is equivalent to `g f [0.5] {(2 1) (3 2)}`.
> A function relates to itself with the identity `f f [1.0] {(1 1) ... (n n)}`.

_*) see ***Problem.proximityRelations(String)*** for details on the syntax_

---
### T-Norm
Proximity relations are not transitive. Instead, if `f ~ h` with proximity `β1` and `g ~ h` with proximity `β2`,
we calculate the proximity of `f ~ g` as `TNorm(β1, β2)`

---
### Lambda-Cut
TODO

---
### Merge
TODO

---
### Witnesses
TODO

---
# Diving deeper
In case you're interested in more specific details on how the program works,
I've tried providing each class with some concise, intuitive documentation.

- `Algorithm` contains the main loop and `conjunction` subroutine
- `ProblemMap` contains precalculated information on the occurring function symbols and restriction type
- `Config`, together with `AUT` and `Substitution`, represent branching states in the main loop
- `State` and `Expresssion` represent branching state during the `conjunction` subroutine
- `Parser` parses strings to terms / proximity relations
- the `util` package contains some relatively self-explanatory utility classes

  [//]: # "TODO"