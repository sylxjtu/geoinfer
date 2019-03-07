grammar Datalog;

@header {
package cn.edu.nju.ws.geoinfer.antlr;
}

ID: [a-zA-Z_][a-zA-Z_0-9:]*;
Str: '"' .*? '"';
Comment: '%' .*? '\n' -> skip; // skip comment
WS: [, \t\r\n]+ -> skip; // skip spaces, tabs, newlines

builtInPredicate: '@' ID;
forceSipPredicate: '!' ID;
rawPredicate: ID;
predicate: builtInPredicate | rawPredicate | forceSipPredicate;
variable: ID;
constant: Str;
element: variable | constant;
literal: predicate '(' element+ ')';

dstLiteral: literal;
srcLiteralList: literal+;

logicFact: literal '.';
logicRule: dstLiteral ':-' srcLiteralList '.';
statement: logicFact | logicRule;
goal: '?-' literal '.';
logicRules: statement+ goal?;