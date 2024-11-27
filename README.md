# About this Project
This project implements the generalization framework described in the paper 
<br>[A Framework for Approximate Generalization in Quantitative Theories](https://doi.org/10.1007/978-3-031-10769-6_34).

> TODO link

You can interact with the program [here](TODO) or include this library in your project.

> TODO license?

## Theory Overview

> Here I try to give a basic, non-rigorous intuition for the concepts surrounding this framework.

### 🧬 Terms and Symbols

There's a distinction between `function terms` and their `symbols`.
A term is basically a specific "execution" of function symbols.

The `symbol` of a function has a unique `name` and an `arity` (how many arguments it takes).
Function symbols with arity = 0 are `constants`.
A `term` is a tree-like structure, where the children of a function term are its `arguments`.

---
### ♻️ Variables, Substitutions, Generalizations

`Variables` are childless terms, which serve as placeholders.
If you take a term, and replace all instances of a given `variable` with another term, that's called a `substitution`.

Given a term `s`, another term `t generalizes s`,
if there's some sequence of substitutions you can do to get from `t` back to `s`.
<br>
For example, `f(x)` generalizes `f(g(a(), b()))`, because you can substitute `x` with `g(a(), b())`.

> Outside individual terms, we generally don't care about the names of variables.
> <br>
> E.g. we consider the terms `f(x, x, y)` and `f(x1, x1, x2)` to be the same "up to variable renaming".

---
### 🌫️ Proximity

To model a system (e.g. a real-life system), we make some `statements` about it.
Classically, those statements are either `true` or `false`.
For example, we can state that a function is (or isn't) `equal` to another function under certain circumstances.

In `fuzzy logic`, we define this `truth value` within the range `[0.0, 1.0]`, instead of only `{true, false}`.
A statement of truth value `0.0 < α < 1.0` is only "somewhat" true.

An `equality` with an added fuzzy truth value is called a `proximity`.
Proximities behave a bit different from equalities, in particular they aren't transitive.

---
### 🌳 Approximate Generalizations

Putting the concepts of `generalization` and `proximity` together:
<br>
If you can apply substitutions to a term `t`, which result in a new term, and that new term approximates a term `s`,
that means `t` is an `approximate generalization` of `s`.

Our algorithm takes two terms, as well as the proximity relations to consider,
and calculates a set of approximate generalizations, which both terms have in common.
<br>
Specifically, we calculate the `mimimal complete set of approximate generalizations`.
This basically means we don't include generalizations that trivially follow from others in the set.

E.g. the solution set for `f(a(), b()) ?= g(c(), d())` (with no proximity relations) would simply be `{ x1 }`,
since there isn't a more specific generalization.
<br>
But if we define a proximity relation between `f` and `g`, we get the solution set `{ f(x1,x2), g(x1,x2) }`,
since including `x1` would be trivial here.

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


---
### 🧮 Equation
The minimum required input is a pair of terms (i.e. an equation),
which you can provide via a simple string input, e.g. `f(a(), b(), c()) ?= g(x, y)`,
or construct programmatically with `GroundTerm` and `MappedVariableTerm`.

> *Note:* The input terms must by definition be `ground` i.e. they can't contain variables.
> We "cheat" by mapping variables from the problem domain to unique constants.
> To be explicit about this, we use `MappedVariableTerm`.

If you provide nothing besides a problem equation, you get a trivial generalization,
whereby only identical terms are substituted.
<br> E.g. `f(a()) ?= f(b())` results in the generalization `f(x1)`,
with the only possible substitution `x1: a()` on the left and `x1: b()` on the right.

---
### 🧲 Proximity Relations
To introduce our fuzzy logic, we need some of the functions to have `proximaty relations`, which we can use in our substitutions.

The `proximity` is some value in the range `[0.0, 1.0]`.
<br>
`0.0` means "not related at all", which is assumed for all proximity relations that aren't defined.
<br>
`1.0` is just regular equality.

Every relation also has an associated `argument relation`, defining how the functions' arguments map onto each other.

You can provide relations as a set of `ProximityRelations`, or represented as a string.

> see *Problem.proximityRelations(String)* for syntax

Proximity relations are by definition symmetric, so `f g [0.5] {(1 2) (2 3)}` is equivalent to `g f [0.5] {(2 1) (3 2)}`.

Also by definition, a function approximates itself with the identity `f f [1.0] {(1 1) ... (n n)}`.

> If nothing is provided, we assume no proximity relations besides the `id` relations.

---
### 📐 T-Norm
When we compute a generalisation, we're doing as many substitutions as we can - and for each substitution,
we're taking advantage of some proximity relation.

We get the proximity a generalization to the original terms by applying the `T-Norm` over all proximities we used while substituting.

The `T-Norm` is just some function which satisfies certain mathematical properties `(commutativity, monotonicity, associativity and 1-identity
over the range [0.0, 1.0])`.

> If nothing is provided, we use the `minimum` function as our `T-Norm` (which is most commonly used in fuzzy logic).

---
### 🔪 Lambda-Cut
You can specify a `lambda-cut` value within the range `[0.0, 1.0]`, which defines a solution's minimum proximity
to count as close enough.
<br> The algorithm creates generalizations which are as specific as possible without falling under the lambda-cut.

A `1.0` lambda-cut gives you only the generalizations which are equal to the problem terms

A `0.0` lambda-cut is a bogus input, since it results in an infinite set of terms

> If nothing is provided, we assume `λ = 1.0`.

---
### ⚙️ Setting: Witnesses

> Default: true

For each solution, you can also generate a set of `witness substitutions` per side of the equation.

For each variable that appears in the solution, they contain a set of possible substitutions.
Applying one from each set gives you a `ground term` that approximates the problem term.

---
### ⚙️ Setting: Merge

> Default: true

Given a computed generalization, it's sometimes possible to combine the substitution sets of multiple variables together.
If `merge` is enabled, the program checks for this, and merges the variables where possible.

---
### 🦄 Custom Arities

The program does its best to infer function arities,
but custom arities are needed in one (relatively specific) scenario.

If a function appears in the problem terms, the arity is taken from there.
<br>
If a function only appears in the proximity relations, we assume that the maximum position that appears in the argument mappings
is the last position of the function.

Ergo, if the function doesn't appear in the problem terms, and the last position is not a `relevant position` -
i.e. there are no argument mappings to/from it - then the arity of that function has to be manually defined.

An example of this occurring is `Example 7` in the paper,
where the final positions of `h1` and `h2` are each irrelevant positions.

---
### 🎭 Custom Fuzzy System

The default implementation of the algorithm assumes that you can enumerate all proximity relations that exist in your fuzzy system.
However, this isn't technically required.

The proximity classes must be finite, but there could, for instance, be an infinite number of functions in the system.
It might then be impractical to figure out which proximity relations can be 'touched' by the algorithm.

In this case, the user can provide a custom implementation of `FuzzySystem`.
It needs to implement the methods:
- `ProximityRelation proximityRelation(String f, String g)` 
- `ArraySet<String> commonProximates(ArraySet<GroundTerm> f)`
- `int arity(String f)`
- `RestrictionType restrictionType()`

---

### 📦 Miscellaneous

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