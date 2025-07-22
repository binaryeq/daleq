

## Examples in commons-configuration2-2.9.0 (MVN vs BFS)

`org/apache/commons/configuration2/XMLConfiguration$XMLBuilderVisitor.class`


### Normalisation through EDB Extraction, no rules applied

`org/apache/commons/configuration2/builder/combined/CombinedBuilderParametersImpl.class`


### InvokeStatic (Maven) vs InvokeVirtual (BFS)

`org/apache/commons/configuration2/XMLConfiguration$XMLBuilderVisitor.class`

### Souffle Fails  (Might not be Reproducible !)


`org/apache/commons/configuration2/builder/BuilderConfigurationWrapperFactory$BuilderConfigurationWrapperInvocationHandler.class`

```
Exception running analysis: "daleq"
java.io.IOException: Souffle exited with 134
	at io.github.bineq.daleq.Souffle.createIDB(Souffle.java:92)
	at io.github.bineq.daleq.cli.DaleqAnalyser.computeAndParseIDB(DaleqAnalyser.java:211)
	at io.github.bineq.daleq.cli.DaleqAnalyser.analyse(DaleqAnalyser.java:106)
	at io.github.bineq.daleq.cli.Main.analyse(Main.java:126)
	at io.github.bineq.daleq.cli.Main.main(Main.java:78)
```

see also [https://github.com/binaryeq/daleq/issues/22](https://github.com/binaryeq/daleq/issues/22)

### Classes are Javap-equivalent, but not DALEQ-equivalent

`org/apache/commons/configuration2/interpol/ConfigurationInterpolator.class`

There is a case of unsoundness in javap that we describe in the paper.

In this example, the issue seems to be the labels.
There is potentially a way to normalise this during the extraction phase.
It still needs to be confirmed that this mapping is isomorphic.

See also [https://github.com/binaryeq/daleq/issues/24](https://github.com/binaryeq/daleq/issues/24)
