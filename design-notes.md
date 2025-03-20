
## Fact Extraction Layer

The fact extraction layer creates facts that represent the program. This includes facts reflecting the project structure
(e.g. the class hierarchy), and facts representing the bytecode instructions within methods. 

Much of this is done by generated predicate factories, see `src/main/java/io/github/bineq/daleq/factextraction/instruction_fact_factories`.
Those factory classes are generated using `io.github.bineq.daleq.edb.InstructionFactFactoryCodeGenerator` from
the predicate specs in `src/main/resources/instruction-predicates`. Those specs are generated 
from ASM tree API notes.   Predicate factories are loaded using the service loader mechanism.

Note that ASM already abstracts some [JVM bytecode instructions](https://en.wikipedia.org/wiki/List_of_Java_bytecode_instructions).
For instance, while there are several dedicated instructions to load local variables (`aload_0`,`aload_1`, etc),
ASM represents them as [a single `aload` instruction](https://asm.ow2.io/javadoc/org/objectweb/asm/Opcodes.html). 
We follow this approach.

The facts are extracted from bytecode using `io.github.bineq.daleq.edb.FactExtractor`, and 
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

### Bytecode Versions

Some data from FSE sect 3 experiment:

Different classes:
```
version 1: 48, version 2: 51 -> 204
version 1: 49, version 2: 46 -> 236
version 1: 49, version 2: 50 -> 15459
version 1: 49, version 2: 52 -> 356
version 1: 50, version 2: 196653 -> 336
version 1: 50, version 2: 46 -> 2584
version 1: 50, version 2: 47 -> 4428
version 1: 50, version 2: 48 -> 1455
version 1: 50, version 2: 49 -> 35048
version 1: 50, version 2: 51 -> 115
version 1: 50, version 2: 52 -> 184
version 1: 51, version 2: 46 -> 287
version 1: 51, version 2: 48 -> 404
version 1: 51, version 2: 49 -> 4369
version 1: 51, version 2: 50 -> 2828
version 1: 52, version 2: 49 -> 7245
version 1: 52, version 2: 50 -> 18941
version 1: 52, version 2: 51 -> 8114
version 1: 52, version 2: 55 -> 20
version 1: 53, version 2: 52 -> 102
version 1: 55, version 2: 52 -> 888
version 1: 60, version 2: 52 -> 1920
```

Different Jars:

```
version 1: 47, version 2: 49 -> 1
version 1: 49, version 2: 50 -> 15
version 1: 49, version 2: 52 -> 8
version 1: 50, version 2: 196653 -> 2
version 1: 50, version 2: 46 -> 7
version 1: 50, version 2: 47 -> 12
version 1: 50, version 2: 48 -> 9
version 1: 50, version 2: 49 -> 115
version 1: 50, version 2: 52 -> 2
version 1: 51, version 2: 46 -> 1
version 1: 51, version 2: 48 -> 2
version 1: 51, version 2: 49 -> 10
version 1: 51, version 2: 50 -> 9
version 1: 52, version 2: 49 -> 10
version 1: 52, version 2: 50 -> 24
version 1: 52, version 2: 51 -> 37
version 1: 52, version 2: 55 -> 4
version 1: 53, version 2: 52 -> 1
version 1: 55, version 2: 52 -> 2
version 1: 60, version 2: 52 -> 1
```



#### Source code (decompiled from  XMLPropertyListConfiguration::write)

```java
writer.println("<?xml version=\"1.0\" encoding=\"" + this.locator.getEncoding() + "\"?>");
```


#### Pre-JEP280

```
IDB_NEW		java/lang/StringBuilder 
IDB_DUP
IDB_INVOKESPECIAL		java/lang/StringBuilder	<init>	()V	false
IDB_LDC		<?xml version="1.0" encoding="
IDB_INVOKEVIRTUAL		java/lang/StringBuilder	append	(Ljava/lang/String;)Ljava/lang/StringBuilder;	false
IDB_ALOAD		0
IDB_GETFIELD		org/apache/commons/configuration2/plist/XMLPropertyListConfiguration	locator	Lorg/apache/commons/configuration2/io/FileLocator;
IDB_INVOKEVIRTUAL		org/apache/commons/configuration2/io/FileLocator	getEncoding	()Ljava/lang/String;	false
IDB_INVOKEVIRTUAL		java/lang/StringBuilder	append	(Ljava/lang/String;)Ljava/lang/StringBuilder;	false
IDB_LDC		"?>
IDB_INVOKEVIRTUAL		java/lang/StringBuilder	append	(Ljava/lang/String;)Ljava/lang/StringBuilder;	false
IDB_INVOKEVIRTUAL		java/lang/StringBuilder	toString	()Ljava/lang/String;	false

```
Is this all happening within the same basic block ? 

#### Post-JEP280

```java
IDB_INVOKEVIRTUAL		org/apache/commons/configuration2/io/FileLocator	getEncoding	()Ljava/lang/String;	false
IDB_INVOKEDYNAMIC		makeConcatWithConstants	(Ljava/lang/String;)Ljava/lang/String;	<?xml version="1.0" encoding=""?>	java/lang/invoke/StringConcatFactory	makeConcatWithConstants	(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;	6	false
```

- SOH seems to separate the two constants in `INVOKEDYNAMIC`
- how  does makeConcatWithConstants at which position to use the value on the stack ?
- 


## Additional Predicates and Default Rules

### Facilitate removals

General idea: introduce predicates with a certain  prefix (annotation), keep in IDB as they carry provenance.
Generalise Handing of checkcast.

```
.decl NOPE(factid: symbol,methodid: symbol,instructioncounter: number)
NOPE(cat("R_REMOVE_REDUNDANT_CHECKCAST","[",factid1,",",factid2,"]"),methodid,instructioncounter+1) :- CHECKCAST(factid1,methodid,instructioncounter+1,desc),CHECKCAST(factid2,methodid,instructioncounter,desc).
.output NOPE

.decl IDB_CHECKCAST(factid: symbol,methodid: symbol,instructioncounter: number,desc: symbol)
IDB_CHECKCAST(cat("R_CHECKCAST","[",factid1,"]"),methodid,instructioncounter,desc) :- CHECKCAST(factid1,methodid,instructioncounter,desc), !CHECKCAST(_,methodid,instructioncounter-1,desc).
.output IDB_CHECKCAST
```

```
.decl REMOVED_INSTRUCTION(factid: symbol,methodid: symbol,instructioncounter: number)
```

Then checkcast rule becomes:

```
.decl REMOVED_INSTRUCTION(factid: symbol,methodid: symbol,instructioncounter: number)
.output REMOVED_INSTRUCTION

.decl IDB_CHECKCAST(factid: symbol,methodid: symbol,instructioncounter: number,desc: symbol)
IDB_CHECKCAST(cat("R_CHECKCAST","[",factid1,"]"),methodid,instructioncounter,desc) :- CHECKCAST(factid1,methodid,instructioncounter,desc), !REMOVED_INSTRUCTION(factid,methodid,instructioncounter).
.output IDB_CHECKCAST

// in custom rule set
REMOVED_INSTRUCTION(cat("R_REMOVE_REDUNDANT_CHECKCAST","[",factid1,",",factid2,"]"),methodid,instructioncounter+1) :- CHECKCAST(factid1,methodid,instructioncounter+1,desc),CHECKCAST(factid2,methodid,instructioncounter,desc).
```

The difference to the existing solution is that `REMOVED_INSTRUCTION` can be declared in standard rule set and used in the `IDB_CHECKCAST` rule.
By default, it would stay empty. 

Problem: will souffle handle the negation?

Similar predicates: 

```
REMOVED_METHOD(factid: symbol,id: symbol)
.output REMOVED_METHOD
```

Additional slots can be ommitted.

Rule for methods:
```
.decl IDB_METHOD(factid: symbol,id: symbol,classname: symbol,name: symbol,descriptor: symbol)
IDB_METHOD(cat("R_METHOD","[",factid,"]"),id,classname,name,descriptor) :- METHOD(factid,id,classname,name,descriptor),!REMOVED_METHOD(_,id).
.output IDB_METHOD
```

Example rule of `REMOVED_METHOD`:

```
REMOVED_METHOD(cat("R_REMOVE_SYNTH_METHODS","[",factid,"]"),id) :- METHOD(factid,id,classname,name,descriptor),IDB_IS_SYNTHETIC(_,id).
```

Similar for `REMOVED_FIELD`.

If a method is removed, facts related this method would stay in the database (e.g. for predicates representing access flags and signatures). 

Should we have similar flags for relationships like superclasses and interfaces ? Not initially, but need could arise later. 


## Modelling Inlining of Methods

```
.decl INLINE(factid: symbol,hostmethodid: symbol, inlinedmethodid: symbol,instructioncounter: numeric)
```

The `instructioncounter` is the callsite (`INVOKE*` instruction) in the host method where code is copied into. 

#### Part 1: removal
`REMOVED_INSTRUCTION(..,inlinedmethodid,instructioncounter) :- INLINE(..,_,inlinedmethodid,instructioncounter)`

#### Part 2: addition


- requires a rule for each instruction predicate !! 
- can this be generated ?
- could mess up labels ! a global label counter could fix this !


```
IDB_IALOAD(..,methodid,instructioncounter) :- IALOAD(factid,methodid,instructioncounter),
!REMOVED_INSTRUCTION(factid2,instructioncounter2).
IDB_IALOAD(..,methodid2,instructioncounter2) :- IALOAD(factid1,methodid1,instructioncounter1),
MOVED_INSTRUCTION(factid2,instructioncounter2,"IALOAD").
.
.output IDB_IALOAD
```