# DALEQ - Datalog-based Binary Equivalence


## Build and Use CLI 

build: `mvn package` , this creates jar in `target`

then run as follows (souffle path must be set):

```
java -DSOUFFLE=<souffle-executable> -jar daleq-1.0.0.jar
 -j1,--jar1 <arg>   the first jar file to compare
 -j2,--jar2 <arg>   the second jar file to compare
 -o,--out <arg>     the output folder where the report will be generated
 -s1,--src1 <arg>   the first jar file with source code to compare
 -s2,--src2 <arg>   the second jar file with source code to compare
```

## Overview

*Daleq* takes Java bytecode as input an produces a relational database.
This is done in two steps.


### Step 1 - EDB Extraction

```mermaid
flowchart LR
    bytecode["bytecode (.class)"] --> asm(("asm")) --> edb["EDB"] --> souffle(("souffle")) --> idb["IDB"]
```



A database representing the bytecode is extracted. An [asm-based](https://asm.ow2.io/) static analysis is used for this purpose. 
The result is the EDB (extensional database), a folder with tsv files, each tsv file corresponding to a predicate.
Those facts represent the bytecode, including class name, bytecode version, super types and interfaces, 
access flags for method, fields and classes, bytecode instructions in methods, etc. 

The representation is low-level, however, there is one significant abstraction (provided by _asm_): constant pool references are
resolved. 

Each fact has a unique generated id that is used to provide provenance.

### Step 2 - IDB Computation

IDB computation applies rules to compute a second set of facts (IDB files) that normalises
the EBD to establish whether two bytecode compared are equivalent or not. 

Rules that are applied for this purpose are defined in
`src/main/resources/rules/`, `advanced.souffle` is the default set.

The API to generate the IDB is provided by `io.github.bineq.daleq.Souffle::createIDB`.
This requires *souffle* to be installed.
Any application that uses this API must provide the location of [souffle](https://souffle-lang.github.io/) via an argument
that is passed to the JVM:

```
-DSOUFFLE=<path to souffle binary>
```

## Provenance

The first slot (term) for each fact is an *id*. 
For EDB facts, those ids are generated during the bytecode analysis / extracyion phase.

Example (from `AALOAD.facts`):

```csv
F1428629	org/apache/hadoop/hdfs/HAUtil::getConfForOtherNodes(Lorg/apache/hadoop/conf/Configuration;)Ljava/util/List;	3200
```

When the IDB is created, each IDB fact also has this slot.
The  values encode a derivation tree. Example:

```csv
R_AALOAD[F1428629]  ...
```

This fact has been computed by applying rule `R_AALOAD` to fact `F1428629`. 
I.e. those ids encode a derivation tree:

```mermaid
graph TD;
R_AALOAD --> F1428629;
```

A less trivial example is `R_42[F1,R_43[F2,F3]] ` , this encodes the following derivation tree:  

```mermaid
graph TD;
    R_42 --> F1;
    R_42 --> R_43;
    R_43 --> F2;
    R_43 --> F3;
```

The project contains a parser to transform those strings into 
trees (`io.github.bineq.daleq.souffle.provenance.ProvenanceParser`).

The grammar of the encoded proofs is defined here:
`src/main/antlr4/io/github/bineq/daleq/souffle/provenance/Proof.g4`.


## Comparing Bytecode

When trying to establish equivalence between two (compiled) classes, IDBs cannot be compared directly for the following reasons: 

1. The provenance (id) terms -- this reflects the process of normalisation that might be different for each of the classes compared
2. the IDB contains additional facts for predicates to keep track of facts that have been removed (i.e. EDB facts that have no counterpart in the IDB), or moved. 
3. facts corresponding to bytecode instruction have an `instructioncounter` slot to define the order of instructions within a method. Those might change in the IDB as normalisation may move/remove/insert facts corresponding to instructions.

### Projection

The purpose of *projection* is to remove the aforementioned parts (facts and slots) from the database.
At the moment normalisation is implemented in Java.

### Workflow to Compare two Binaries

The classes `io.github.bineq.daleq.idb.IDBReader` and `io.github.bineq.daleq.idb.IDBPrinter` are used to read an IDB,
and convert it into a textual representation suitable for comparison and diffing (using a standard text fiff tool such as _diff_ or _kdiff3_). This also supports projection.

```mermaid
flowchart LR
    bytecode1["bytecode1\n(.class)"] --> asm1(("asm")) --> edb1["EDB1"] --> souffle1(("souffle")) --> idb1["IDB1"] --> IDBReader1(("IDBReader/\nIDPPrinter"))  --> file1 --> diff(("diff")) --> result 
    bytecode2["bytecode2\n(.class)"] --> asm2(("asm")) --> edb2["EDB2"] --> souffle2(("souffle")) --> idb2["IDB2"] --> IDBReader2(("IDBReader/\nIDPPrinter"))  --> file2 --> diff(("diff"))

```

## Extracting Facts from Bytecode

```
usage: java -cp <path-to-built-jar> io.github.bineq.daleq.edb.FactExtractor
 -cl <classes>   the location of compiled classes, a jar file or folder
 -f <facts>      a folder where to create the extension database (input
                 .facts files), the folder will be created if it does not
                 exist
 -s <souffle>    a file where to create the souffle program (.souffle
                 file) containing imports and input predicate declarations
 -v              whether to verify the datalog facts against the schema,
                 must be true or false, default is true
```

### Mapping Bytecode Instructions to Predicates

The mapping is defined by a *mapping spec* (a JSON file) in `src/main/resources/instruction-predicates` for each byte code instruction, e.g. `AALOAD.json`.

```json
{
  "name": "AALOAD",
  "opCode": 50,
  "slots": [
    {
      "name": "factid",
      "type": "SYMBOL",
      "jtype": "java.lang.String"
    },
    {
      "name": "methodid",
      "type": "SYMBOL",
      "jtype": "java.lang.String"
    },
    {
      "name": "instructioncounter",
      "type": "NUMBER",
      "jtype": "int"
    }
  ],
  "id": "0e041734-aa00-467a-ba0f-e089b5d888cf",
  "asmNodeType": "org.objectweb.asm.tree.InsnNode"
}
```

From this file, a mapping class is statically generated, the sources can be found in `src/main/java/io/github/bineq/daleq/edb/instruction_fact_factories/`, e.g. `InstructionFactFactory__AALOAD.java`.
Those mappings are read using a service factory, i.e. the following file must contain a line with the name of each mapping used:
`src/main/resources/META-INF/services/io.github.bineq.daleq.edb.InstructionPredicateFactFactory`. 

### Adding new Mappings

When an instruction with no associated mapping is encountered, a warning appears in the log, and the mapping spec is generated in `inferred-instruction-predicates`. 

The proceed as follows: 

1. copy the new spec into `src/main/resources/instruction-predicates`
2. run `io.github.bineq.daleq.edb.InstructionFactFactoryCodeGenerator` to generate the respective mapping class
3. copy the mapping class into `src/main/java/io/github/bineq/daleq/edb/instruction_fact_factories/`
4. add a row with the new mapping class to `src/main/resources/META-INF/services/io.github.bineq.daleq.edb.InstructionPredicateFactFactory`
5. update the rules in `src/main/resources/rules` to add support for the new mapping

For the last step, each instruction has the following block defining an IDB predicate (the respective EDB predicate is generated), and 
a rule that maps this to a generic `IDB_INSTRUCTION` predicate. Example:

```
.decl IDB_AALOAD(factid: symbol,methodid: symbol,instructioncounter: number)
IDB_AALOAD(cat("R_AALOAD","[",factid,"]"),methodid,instructioncounter) :- AALOAD(factid,methodid,instructioncounter),!REMOVED_INSTRUCTION(_,methodid,instructioncounter).
.output IDB_AALOAD
IDB_INSTRUCTION(cat("R_AALOAD","[",factid,"]"),methodid,instructioncounter,"AALOAD") :- AALOAD(factid,methodid,instructioncounter).
```

For a new predicate, such a block must be added. 
The rule generator `io.github.bineq.daleq.idb.rulegeneration.BaselineRuleGeneration` can be used for this purpose.

## Building the Project

Building with tests requires souffle to be set up (see also **IDB Computation** section).
This has to be done locally at the moment, see also [issue 11](https://github.com/binaryeq/daleq/issues/11).
Tests that require souffle will not fail but will be skipped if souffle is not available.
