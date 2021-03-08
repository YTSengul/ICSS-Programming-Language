grammar ICSS;

//--- LEXER: ---
// IF support:
IF: 'if';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;

//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';

//--- PARSER: --- Dus de geimplementeerde grammar.
// De bovenstre stuk van de grammatica. Waar gaat het hierin verder? Natuurlijk met de stylerule.
// Selecteer deze, doe rechtermuisknop, en klik op 'test rule stylesheet' om het te testen.
// Hiermee genereer je dus de parse tree die je met behulp van ANTLR kunt runnen.
stylesheet: (variableAssigning | stylerule) * EOF;

stylerule:  selector OPEN_BRACE (variableAssigning|declaration|if_statement)+ CLOSE_BRACE;
styleruleBody: styleruleBodyItem+;
styleruleBodyItem: declaration | variableAssigning | if_statement;

// Variabelen
variableAssigning:variableReference ASSIGNMENT_OPERATOR expression SEMICOLON;
variableReference: CAPITAL_IDENT;
variableValue: bool | color | pixelsize | variableReference | percentage;

declaration : property COLON value SEMICOLON | property COLON expression SEMICOLON;

property: 'color' | 'background-color' | 'width' | 'height';

value: color | pixelsize | variableReference | percentage | bool;

// Hoe zit een selector in elkaar? hier zeg je eigenlijk dat stylesheet>stylerule> met welke dingen beginnen. zoals #menu en p.
// De waardes die hier worden gebruikt worden eronder geconstateerd.
selector: selectorId | selectorClass | selectorTag;

selectorId: ID_IDENT;
selectorClass: CLASS_IDENT;
selectorTag: LOWER_IDENT;


// Expressies
// Welke expressies kunnen er allemaal gemaakt worden?
expression: literal #literalexpression|
            variableReference #varref|
            expression MUL expression #multiplyOperation|
            expression MIN expression #substractOperation|
            expression PLUS expression #addOperation;
expressionValue: scalar | variableReference | pixelsize | percentage;

literal: scalar|pixelsize|percentage|color| bool;

// IF Statements
if_statement: IF BOX_BRACKET_OPEN (variableReference|bool) BOX_BRACKET_CLOSE OPEN_BRACE (declaration|if_statement)+ CLOSE_BRACE;

// Variabelen die gebruikt kunnen worden
color : COLOR;
pixelsize : PIXELSIZE;
percentage : PERCENTAGE;
bool: (TRUE | FALSE);
scalar: SCALAR;

// Na deze stap volgt ASTListener waarbij je alles van de superklasse moet overriden volgens de dia.

// Kijk deze bronnen voor het gebruik van antlr opnieuw als je het niet begrijpt.
// https://www.youtube.com/watch?v=svEZtRjVBTY
// https://github.com/antlr/antlr4/blob/master/doc/getting-started.md

//https://github.com/michelportier/icss2020
//You can also run the application from an IDE, e.g. IntellIJ.
// To do so, import startcode as Maven project. Whenever you make
// changes to grammar, make sure you run mvn generate-sources prior
// to compiling. Most IDEs do not update the ANLTR parser automatically.
