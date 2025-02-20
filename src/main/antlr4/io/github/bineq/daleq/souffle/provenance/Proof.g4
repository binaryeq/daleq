grammar Proof;

proof : node <EOF> ;
node : ID children? ;
children : '[' node (',' node)* ']' ;
ID : [a-zA-Z0-9_]+ ;  // id of a fact or rule
