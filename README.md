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

## Assessing Souffle-Based Equivalence

Run `io.github.bineq.daleq.benchmark.RunBenchmark`, benchmark scenarios can be found in `src/main/resources/benchmark/scenario1`,
each scenario has a json file describing it. Example:

```java
{
  "name": "different constantpool order 1",
  "class1": "version1/Headers.class",
  "class2": "version2/Headers.class",
  "category": "CONSTANT_POOL_ORDER",
  "description": "Two versions of `com.squareup.okhttp.Headers` in `okhttp-2.7.5.jar` built by the developer (Maven-Central) and by Google (GAOSS) respectivly. \nBoth versions differ in the constant pool orders used by the binaries. See `https://github.com/binaryeq/bineq-study-supplementary/tree/main/mvnc-vs-gaoss/example3` for more info."
}
```
