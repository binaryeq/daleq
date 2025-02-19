grammar Souffle;

souffle : declaration '\r'? '\n' ;   // windows uses \r\n for linebreaks, unix just \n
// comment : '// ~[\n\r]*' ;
declaration : DECL NAME attributes ;
attributes : '(' attribute? (',' attribute)* ')' ;
input : INP NAME ;
attribute : NAME ':' TYPE ;
TYPE : 'symbol'|'number'|'float'|'unsigned';
DECL : '.decl' ;
INP : '.input' ;
NAME : [a-zA-Z0-9_]+ ;  // predicate or slot
WS : [ \t\n\r] + -> skip ;
