
## Fact Extraction Layer

The fact extraction layer creates facts that represent the program. This includes facts reflecting the project structure
(e.g. the class hierarchy), and facts representing the bytecode instructions within methods. 

Much of this is done by generated predicate factories, see `src/main/java/io/github/bineq/daleq/factextraction/instruction_fact_factories`.
Those factory classes are generated using `io.github.bineq.daleq.factextraction.InstructionFactFactoryCodeGenerator` from
the predicate specs in `src/main/resources/instruction-predicates`. Those specs are generated 
from ASM tree API notes.   Predicate factories are loaded using the service loader mechanism.

Note that ASM already abstracts some [JVM bytecode instructions](https://en.wikipedia.org/wiki/List_of_Java_bytecode_instructions).
For instance, while there are several dedicated instructions to load local variables (`aload_0`,`aload_1`, etc),
ASM represents them as [a single `aload` instruction](https://asm.ow2.io/javadoc/org/objectweb/asm/Opcodes.html). 
We follow this approach.

The facts are extracted from bytecode using `io.github.bineq.daleq.factextraction.FactExtractor`, and 
the database is created in a local folder. This **extensional database (EDB for short)** consists
of `<predicate-name>.fact` files (tab separated) containing fact definitions, and a `.souffle` 
file containing predicate definitions and import declarations for the respective fact files.

## Rules 

Rules are used to infer additional facts from the EDB. The result is called the **intentional database (IDB for short)**. 
An IDB usually contains the EDB as a closure operation is applied to compute this (this is the datalog fixpoint).

However, we can only consider a part of the IDB by only looking at facts for certain predicates. 
Those dedicated predicates are declared as *output predicates* in *souffle*, and do not have any facts in the EDB.
We refer to this part of the IDB as **ODB (output database)**.

This ODB is the base of establishing equivalence: two classes (bytecodes) are the same if their respective ODBs are the same. 

## Provenance

TODO: implement

To achieve soundness, all predicates have two dedictated slots: `id` and `provenance`.  
For facts, the provenance slot is empty (empty string, or fixed string `asserted` or similar).

Rules have a rule id predicate in the body. The provenance value in the 
head is synthesised from the rule id and the ids of the premisses
to encode a proof tree. 

Example:

[Souffle syntax](https://souffle-lang.github.io/arguments)

```
SUBCLASS(cat("R1","[",id,"]"),x,y) :- SUPERCLASS(id,y,x).
```

Note the rule id (`R1`) in the head.

## Soundness

Each EDB fact **should** be used to compute the ODB, this can be checked
by analysing the provenance values. 

TODO: expand discussion. 

## Example (from Listing 3 in TODO)

The first slot is the unique fact id, the second slot the context (method containing the instruction).

``` 
// PROGRAM 1 EDB
invokeinterface('id1','Foo::foo()V',42,'ch/qos/logback/access/spi/IAccessEvent','getClass','()java/lang/Class;');
```

``` 
// PROGRAM 2 EDB
invokevirtual('id1','Foo::foo()V',42,'java/lang/Object','getClass','()java/lang/Class;');
```

### Rules

```
// uses several helper predicates / facts that are defined by further rules
// output predicates have the `_` prefix
// the rule id is `rid1`

_invokevirtual('rid1'+'['+id+']',context,instructioncounter,class,methodName,descriptor) :- 
   invokevirtual(id,context,instructioncounter,class,methodName,descriptor').
_invokeinterface('rid2'+'['+id1+','+id2+']',context,instructioncounter,class,methodName,descriptor) :- 
   invokeinterface(id1,context,instructioncounter,class,methodName,descriptor'),
   !rootmethod(id2,class,methodname,descriptor).   
```




