
grammar cal;

// parser rules
program:	              (dec_list function_list main) | EOF;

dec_list:                 (decl SEMI dec_list)?;

decl:                     var_decl | const_decl;

var_decl:                 VAR ID COLON type;

const_decl:               CONST ID COLON type ASSIGN expression;

function_list:            (function function_list)?;

function:                 type ID LBR parameter_list RBR IS
                          dec_list
                          BEGIN
                          statement_block
                          RETURN LBR expression? RBR SEMI
                          END;

type:                     INTEGER | BOOLEAN | VOID;

parameter_list:           nemp_parameter_list?;

nemp_parameter_list:      ID COLON type | ID COLON type COMMA nemp_parameter_list;

main:                     MAIN BEGIN dec_list statement_block END;

statement_block:          (statement statement_block)?;

statement:                ID ASSIGN expression SEMI
                        | ID LBR arg_list RBR SEMI
                        | BEGIN statement_block END
                        | IF condition BEGIN statement_block END
                        | ELSE BEGIN statement_block END
                        | WHILE condition BEGIN statement_block END
                        | SKIP_STATEMENT SEMI
                        ;
//                              allows multi var/const addition/subraction e.g. ans := x + y - z
expression:		          frag (binary_arith_op frag)*
                        | LBR expression RBR
                        | ID LBR arg_list RBR
                        | frag
                        ;

binary_arith_op:        ADD | MINUS;

frag:                   ID | MINUS ID | NUMBER | TRUE | FALSE;

condition:              NEG condition | LBR condition RBR | expression comp_op expression | condition (OR | AND) condition;

comp_op:                EQUAL | NOTEQUAL | LT | LTE | GT | GTE;

arg_list:               nemp_arg_list |;

nemp_arg_list:          ID | ID COMMA nemp_arg_list;

// alphabet of lowercase and uppercase letters
fragment A :            [aA];
fragment B :            [bB];
fragment C :            [cC];
fragment D :            [dD];
fragment E :            [eE];
fragment F :            [fF];
fragment G :            [gG];
fragment H :            [hH];
fragment I :            [iI];
fragment J :            [jJ];
fragment K :            [kK];
fragment L :            [lL];
fragment M :            [mM];
fragment N :            [nN];
fragment O :            [oO];
fragment P :            [pP];
fragment Q :            [qQ];
fragment R :            [rR];
fragment S :            [sS];
fragment T :            [tT];
fragment U :            [uU];
fragment V :            [vV];
fragment W :            [wW];
fragment X :            [xX];
fragment Y :            [yY];
fragment Z :            [zZ];

fragment Letter:		[a-zA-Z];
fragment Digit:			[0-9];
fragment UnderScore:	'_';

MAIN:                   M A I N;
BEGIN:					B E G I N;
END:					E N D;
VAR:                    V A R I A B L E;
CONST:                  C O N S T A N T;
TRUE:                   T R U E;
FALSE:                  F A L S E;
IF:                     I F;
ELSE:                   E L S E;
INTEGER:                I N T E G E R;
BOOLEAN:                B O O L E A N;
VOID:                   V O I D;
IS:                     I S;
RETURN:                 R E T U R N;
WHILE:                  W H I L E;
SKIP_STATEMENT:         S K I P;

LBR:					'(';
RBR:					')';
SEMI:					';';
COLON:                  ':';
COMMA:                  ',';

// operators
ASSIGN:					':=';
ADD:                    '+';
MINUS:                  '-';
NEG:					'~';
OR:						'|';
AND:					'&';
EQUAL:                  '=';
NOTEQUAL:               '!=';
LT:                     '<';
LTE:                    '<=';
GT:                     '>';
GTE:                    '>=';

NUMBER:                 '0' | (MINUS? [1-9] Digit*);
ID:					    Letter (Letter | Digit | UnderScore)*;

WS:						[ \t\n\r]+ -> skip;
LINE_COMMENT:           '//' .*? '\n' -> skip;
MULTI_LINE_COMMENT:     '/*' (MULTI_LINE_COMMENT|.)*? '*/' -> skip;