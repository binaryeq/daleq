grammar Proof;

proof : node <EOF>  ;
node : ID children? ;
children : '[' ID (',' ID)* ']' ;
ID : [a-zA-Z0-9_]+ ;  // id of a fact or rule
WS : [ \t\n\r] + -> skip ;
