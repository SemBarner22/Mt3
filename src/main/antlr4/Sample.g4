grammar Sample;

text : global+ EOF;

global : func_def | statement;

func_def : FUN VAR OB typedArgs CB statement+ (RET singleAE)? NUF;

typedArgs : (typedArg nextTypedArg*)?;

typedArg : VAR CN type;

nextTypedArg : CM typedArg;

args : arg (CM arg)*;

arg : VAR;

type : IT | DT | BT;

statement : writeOutput | literal;

readInput : RI | RD | RB;

writeOutput : PRINT OB literal CB;

literal : asgn | condition;

asgn : args ASGN arithmeticExpressions;

arithmeticExpressions : singleAE (CM singleAE)*;

singleAE : (arithmeticExpressionNumber | arithmeticExpressionLogic | arithmeticExpressionCompare | readInput);

arithmeticExpressionNumber : (number (operationNumber number)*);

number : INT | DOUBLE | VAR;

operationNumber : ADD | MS | MUL | DIV;

arithmeticExpressionLogic : (compareOrLogicVar (operationLogic compareOrLogicVar)*);

compareOrLogicVar : arithmeticExpressionCompare | logicVar;

operationLogic : AND | OR;

logicVar : TRUE | FALSE | VAR;

arithmeticExpressionCompare : arithmeticExpressionNumber compareOp arithmeticExpressionNumber;

compareOp : EQ | NEQ | LEQ | GEQ | GT | LT;

condition : ifStatement;

ifStatement : IF arithmeticExpressionLogic CN statement+ FI;

IF : 'if';
FI : 'fi';

TRUE : 'true';
FALSE : 'false';

PRINT : 'print';
RI : 'readint()';
RD : 'readdouble()';
RB : 'readbool()';

IT : 'int';
DT : 'double';
BT : 'bool';
RET : 'return';

ASGN : '=';
EQ : '==';
NEQ : '!=';
LEQ : '<=';
GEQ : '>=';
GT : '>';
LT : '<';
FUN : 'fun';
NUF : 'nuf';
OB : '(';
CB : ')';
CM : ',';
CN : ':';
MS : '-';
ADD : '+';
MUL : '*';
DIV : '/';
AND : '&';
OR : '|';
WHITESPACE : [ \t\r\n]+ -> skip;
INT : MS?[0-9]+;
DOUBLE : INT'.'[0-9]+;
fragment CHAR : [a-zA-Z];
VAR : CHAR (INT | CHAR)*;