# About this Project
This project implements the generalization framework described in the paper 
<br>[A Framework for Approximate Generalization in Quantitative Theories](https://doi.org/10.1007/978-3-031-10769-6_34).

> TODO links, license?

You can interact with the program [here (TODO)]() or include this library in your project.

## Intuition

_Here I provide a basic, non-rigorous intuition for the concepts surrounding the framework._
_For using the program, see the [next section](#running-a-query)._

### 🧬 Terms and Symbols

There's a somewhat subtle distinction between `function terms` and their `symbols`.

The `symbol` of a function has a unique `name` and an `arity` (the number of arguments the function takes).
Function symbols with arity = 0 are `constants`.
A `term` is a tree-like structure, with function symbols as nodes. The children of a function term are its `arguments`.

### ♻️ Variables, Substitutions, Generalizations

`Variables` are childless terms, which serve as placeholders.
If you take a term, and replace all instances of a given `variable` with a subterm, that's called a `substitution`.

Given a term `s`, another term `t generalizes s`,
iff there's some sequence of substitutions you can do to arrive from `t` at `s`.
<br>
For example, `f(x)` generalizes `f(g(a(), b()))`, because you can substitute `x` with `g(a(), b())`.

> We generally don't care about the names of variables, as long as they're unique within a term.
> <br>
> E.g. we consider the terms `f(x, x, y)` and `f(x1, x1, x2)` to be equivalent "up to variable renaming".

### 🌫️ Proximity

To make a mathematical system useful, we must make some `statements`, which tie attributes of different parts of the system together.
For example, we can state that a function is (or is not) `equal` to another function under some condition.
Classically, those statements are either `true` or `false`.

In `fuzzy logic`, a truth value can take on any value in the range `[0.0, 1.0]`, instead of only `{true, false}`.
We can then say that a statement of truth value `0.0 < α < 1.0` is only "somewhat" true.

Equality with a fuzzy truth value is called a `proximity`.
Proximitiy has slightly different mathematical properties than equality - in particular, it's not transitive.

### 🌳 Approximate Generalizations

Putting the concepts of `generalization` and `proximity` together:
<br>
If you can apply substitutions to the term `t`, which result in some new term `t'`, and that new term `t'` approximates the term `s`,
that means `t` is an `approximate generalization` of `s`.

Our algorithm takes two terms, as well as the proximity relations of the system,
and calculates a `set of approximate generalizations`, which both terms have in common.

Specifically, we calculate the `mimimal complete set of approximate generalizations`,
which means that we leave out more general solutions, if less general solutions also exist.

E.g. the solution set for `f(a(), b()) ?= g(c(), d())` (with no proximity relations) would simply be `{ x1 }`,
since there isn't a more specific generalization.
<br>
But if we define a proximity relation between `f` and `g`, we get the solution set `{ f(x1,x2), g(x1,x2) }` -
`x1` is also a valid solution, but we consider that trivial and leave it out.

## Running a Query

> This section breaks down the parameters you can provide, and how you can interpret the generated solutions.

### 🔍 Problem
An anti-unification problem can be defined through the provided `Problem` builder class.
Calling `myProblem.run()` executes the program.
E.g.:

```Java
Set<Solution> solutions =
    new Problem("f(a(), b() c()) ?= g(x, y)")
        .proximityRelations("g h [0.5] {(1 2) (3 1)} ; g h [0.7] {(1 1) (2 2) (2 3)}")
        .lambda(0.5f)
        // ...
        .run();
```
You can specify the following parameters:
- [equation (Constructor)](#-equation)
- [proximity relations](#-proximity-relations)
- [t-norm](#-t-norm)
- [lambda-cut](#-lambda-cut)
- [witnesses (setting)](#-setting-witnesses)
- [merge (setting)](#-setting-merge)


### 🧮 Equation
The minimum required input is a pair of terms (a.k.a. an equation),
which you can provide via a simple string input, e.g. `"f(a(), b(), c()) ?= g(x, y)"`,
or construct programmatically with `GroundTerm` and `MappedVariableTerm`.

> *Note:* By definition, the input terms must be `ground`, so they can't contain variables.
> Variables from the problem domain are marked as `MappedVariableTerm`, but otherwise treated as constants.

If you provide nothing besides a problem equation, you get a trivial, non-fuzzy generalization, whereby only identical terms are substituted.
<br>
E.g. `f(a()) ?= f(b())` results in the single solution `f(x1)`, with the possible substitutions `x1: a()` on the left and `x1: b()` on the right.

### 🧲 Proximity Relations
> Default: {}

To introduce our fuzzy logic, we need some of the functions to have `proximity relations`, which we can use in our substitutions.

The `proximity` is a value in the range `[0.0, 1.0]`. If not specified, it's assumed to be `0`.
<br>
The `argument relation` defines how the functions' arguments map onto each other.

Proximity relations are symmetric, e.g. `f g [0.5] {(1 2) (2 3)}` is equivalent to `g f [0.5] {(2 1) (3 2)}`.
<br>
A function approximates itself with the identity `f f [1.0] {(1 1) ... (n n)}`.

You can provide relations as a set of `ProximityRelations`, or represented via string (see javadoc for syntax).

### 📐 T-Norm
> Default: Math.min(a, b)

When we compute a generalisation, we're doing as many substitutions as we can - and using some proximity relation for each substitution.

We get the proximity of a generalization to the original terms by applying the `T-Norm` over all proximities we used while substituting.

The `T-Norm` can be any bi-function over the range [0.0, 0.1], which satisfies the mathematical properties `commutativity, monotonicity, associativity and 1-identity`.

### 🔪 Lambda-Cut
> Default: 1.0

You can specify a `lambda-cut` value within the range `[0.0, 1.0]`, which is the minimum proximity for a solution to count as "close".
<br> The algorithm then creates the least general generalizations above the lambda-cut.

A `1.0` lambda-cut just gives you the non-fuzzy generalizations of terms equal to the problem terms.

A `0.0` lambda-cut is essentially a bogus input, since it results in an infinite set of terms.

### 🔧 Setting: Witnesses
> Default: true

For each solution, you can also generate a set of `witness substitutions` per side of the equation.

For each variable that appears in the solution, they contain a set of possible substitutions.
Applying one from each set gives you a `ground term` that approximates the problem term.

### 🔧 Setting: Merge
> Default: true

Given a computed generalization, it's sometimes possible to combine the substitution sets of multiple variables together.
If `merge` is enabled, the program checks for this, and merges the variables where possible.

### 🦄 Custom Arities

The program does its best to infer function arities,
but specifying arities is required for one relatively specific scenario.

_That is, When the function doesn't appear in the problem terms, and its last argument position doesn't appear in the proximity relations.
An example of this occurring is `Example 7` in the paper, where the final positions of `h1` and `h2` are each irrelevant positions._

When using this library in code, it's probably best to always provide the arities, to avoid potential pitfalls.

### 🎭 Custom Fuzzy System

The default implementation of the algorithm assumes that you can enumerate all proximity relations that exist in your fuzzy system.
But this isn't technically required.

If required, you can provide a custom implementation of `FuzzySystem`, which must implement the methods:
- `ProximityRelation proximityRelation(String f, String g)` 
- `ArraySet<String> commonProximates(ArraySet<GroundTerm> f)`
- `int arity(String f)`
- `RestrictionType restrictionType()`

## 📦 Miscellaneous

 - Both generating witnesses and merging variables require the preprocessing step `expand`,
which can be significantly more expensive than the rest of the algorithm.
If you only need the basic (`linear`) generalizations without witness substitutions,
you can skip `expand` by disabling both settings.


 - Colorful logging can be switched on/off globally with the `util.ANSI.enabled` flag (`true` by default)

## 🤿 Diving deeper
In case you're interested in how the program works,
I've tried providing some concise documentation in the key parts of the algorithm:

- `Algorithm` contains the main loop and `conjunction` subroutine
- `PredefinedFuzzySystem` contains precalculated information on the occurring function symbols
- `Config`, together with `AUT` and `Substitution`, represent branching states in the main loop
- `State` and `Expresssion` represent branching state during the `conjunction` subroutine
- `Parser` parses strings to terms / proximity relations
- the `util` package contains some relatively self-explanatory utility classes
