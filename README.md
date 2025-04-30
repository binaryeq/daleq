# daleq
datalog-based binary equivalence


## Extract Facts from Bytecode

```
usage: java -cp <path-to-built-jar>
            io.github.bineq.daleq.edb.FactExtractor
 -cl <classes>   the location of compiled classes, a jar file or folder
 -f <facts>      a folder where to create the extension database (input
                 .facts files), the folder will be created if it does not
                 exist
 -s <souffle>    a file where to create the souffle program (.souffle
                 file) containing imports and input predicate declarations
 -v              whether to verify the datalog facts against the schema,
                 must be true or false, default is true
```

## Mapping Bytecode Instructions to Predicates

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
