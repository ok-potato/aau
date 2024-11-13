# About this Project
This project implements the generalization framework described in the paper 
<br>[A Framework for Approximate Generalization in Quantitative Theories](https://doi.org/10.1007/978-3-031-10769-6_34).

You can interact with the program [here](TODO) or include this library in your project.

> TODO license

# What do any of these words mean?

> This part is aimed at being a very shallow introduction to some basic concepts from formal + fuzzy logic.

> TODO define term??

In the context of formal logic, `variable terms` are objects that serve as placeholders.
<br> Replacing a variable with something else is a `substitution`.

Substituting variable terms with `function terms` is the basic operation this algorithm works with.

A term `generalizes` another term, if there is some sequence of substitutions
you can apply to its variables, which results in that other term.
<br> E.g. `f(x)` generalizes `f(g(a(), b()))`, because you can substitute `x` with `g(a(), b())`.

A typical term has a bunch of generalizations - and a single variable also trivially generalizes everything.
<br> So if you have a set of terms, and you want to find their common generalizations
(and want that information to actually be interesting) you're typically looking for the `least general generalizations` possible.

> TODO compare to gcd / what means least common here?

Our goal, then, is to find a set of all such `least general generalizations`, but with a twist.



> <br> in particular the idea of variable substitution
> <br> "as specific as possible"

# Running a Query
This section breaks down the parameters you can provide, and how you can interpret the generated solutions.

---
### 🔍 Problem
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
You can specify the following parameters:
- [equation](#-equation)
- [proximity relations](#-proximity-relations)
- [t-norm](#-t-norm)
- [lambda-cut](#-lambda-cut)
- [witnesses (setting)](#-setting-witnesses)
- [merge (setting)](#-setting-merge)


---
### 🧮 Equation
The minimum required input is an equation (a.k.a. a pair of terms),
which you can provide via a simple string input, e.g. `f(a(), b(), c()) ?= g(x, y)`,
or construct programmatically with `GroundTerm` and `MappedVariableTerm`.

> *Note:* The input terms must by definition be `ground` i.e. they can't contain variables.
> We "cheat" by mapping variables from the problem domain to unique constants,
> and in order to be explicit about this, we use `MappedVariableTerm`.

If you provide nothing besides a problem equation, you get a trivial generalization,
whereby only identical terms are substituted.
<br> E.g. `f(a()) ?= f(b())` results in the generalization `f(x1)`,
with substitution `x1: a()` on the left side and `x1: b()` on the right.

---
### 🧲 Proximity Relations
To introduce our fuzzy logic, we need some of the functions to have `proximates`, which we can use in our substitutions.

The `proximity` of such a relation is some value in the range `[0.0, 1.0]`.
`0.0` mean "not related at all", which is assumed unless otherwise specified.
`1.0` means "perfectly related", which is just regular equality.

Every (non-zero) relation also has an associated `argument relation`, defining how the functions' arguments map onto each other.

Provide your desired relations as a set of `ProximityRelations`, or via string:
<br> `g h [0.5] {(1 2) (3 1)} ; g h [0.7] {(1 1) (2 2) (2 3)}`

see *Problem.proximityRelations(String)* for details on the syntax

> Proximity relations are by definition symmetric, so `f g [0.5] {(1 2) (2 3)}` is equivalent to `g f [0.5] {(2 1) (3 2)}`.

> A function approximates itself with the identity `f f [1.0] {(1 1) ... (n n)}` - this doesn't need to be specified.

---
### 📐 T-Norm
When we compute a generalisation, we're doing as many (sensible) substitutions as we can - and for each substitution,
we're taking advantage of some proximity relation.

We get the proximity of the original terms to their generalization by applying the so-called `T-Norm` over all the proximities we encountered while substituting.

The `T-Norm` is just some function which satisfies certain mathematical properties `(commutativity, monotonicity, associativity and 1-identity
over the range [0.0, 1.0])`.

> Here, we use the `minimum` function as our `T-Norm` (which is also the one most commonly used in fuzzy logic).

---
### ⚖️ Lambda-Cut
You can specify a `lambda-cut` value within the range `[0.0, 1.0]`, which defines the minimum proximity a solution needs
to count as "close enough".
<br> The algorithm then creates generalizations which are as specific as possible without falling under the lambda-cut.

> A `1.0` lambda-cut gives you only the generalizations which are equal to the problem terms
> 
> A `0.0` lambda-cut is a bogus input, since it results in the infinite set of all terms


---
### ⚙️ Setting: Witnesses
For each solution, you can also generate a set of `witness substitutions` per side of the equation.

For each variable that appears in the solution, they contain a set of possible substitutions.
Applying one from each set gives you a `ground term` that approximates the problem term.

---
### ⚙️ Setting: Merge
Given a computed generalization, it's sometimes possible to combine the substitution sets of multiple variables together,
where the total set is still `consistent` (i.e. all the terms have some common proximate).

In that case, we can `merge` those variables, vis-à-vis their sets, for a more optimal solution.

> `Generating witnesses` and `merging variables` both require the preprocessing step `expand`,
> which can be significantly more expensive than the rest of the algorithm.
> If you only need the basic (`linear`) generalizations,
> you can skip that step by disabling both settings.

# 🤿 Diving deeper
In case you're interested in how the program works,
I've tried providing some concise documentation wherever possible.

- `Algorithm` contains the main loop and `conjunction` subroutine
- `ProblemMap` contains precalculated information on the occurring function symbols and restriction type
- `Config`, together with `AUT` and `Substitution`, represent branching states in the main loop
- `State` and `Expresssion` represent branching state during the `conjunction` subroutine
- `Parser` parses strings to terms / proximity relations
- the `util` package contains some relatively self-explanatory utility classes