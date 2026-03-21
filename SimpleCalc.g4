grammar SimpleCalc;

// Parser Rules
expr   : expr op=('*'|'/') expr
       | expr op=('+'|'-') expr
       | INT
       | '(' expr ')'
       ;

// Lexer Rules
INT    : [0-9]+ ;
WS     : [ \t\r\n]+ -> skip ;
