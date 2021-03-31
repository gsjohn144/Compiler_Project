# CSC 408 - Programming Languages
## Compiler Project
***
## Contents
- [Languages](#languages)
- [Error Codes](#error-codes)
- [PL/0 Op Codes](#op-codes)
- Other stuff

## Languages

We are to add additional features to a partially built compiler for the following languages:
* Python
* Groovy
* Lua

## Error Codes

The following is a table of all error codes used within the compiler at present. Use these as appropriate.
| Error Code | Error Message |
| ---------: | :------------ |
| 1          | Use = instead of := |
| 2          | = must be followed by a number |
| 3          | Identifier must be followed by = |
| 4          | Const, Var, Procedure must be followed by an identifier |
| 5          | Semicolon or comma missing |
| 6          | Incorrect symbol after procedure declaration |
| 7          | Statement expected |
| 8          | Incorrect symbol after statement part in block |
| 9          | Period expected |
| 10         | Semicolon between statements is missing |
| 11         | Undeclared identifier |
| 12         | Assignment to a constant or procedure is not allowed |
| 13         | Assignment operator := expected |
| 14         | Identifier expected |
| 15         | Call of a constant or a variable is meaningless |
| 16         | 'Then' expected |
| 17         | Semicolon or 'end' expected |
| 18         | 'Do' expected |
| 19         | Incorrect symbol following statement |
| 20         | Relational operator expected |
| 21         | Expression must not contain a procedure identifier |
| 22         | Right parenthesis missing |
| 23         | The preceding factor cannot be followed by this symbol |
| 24         | An expression cannot begin with this symbol |
| 25         | Constant or Number is expected |
| 26         | This number is too large |

## Op Codes
| Op  | Name | l  | a  | Description |
| :-: | :--: | -: | -: | :---------- |
| LIT | Literal | 0 | value | Pushes a literal value to the stack |
| LOD | Load | level | offset | Loads a variable at depth level and index offset |
| STO | Store | level | offset | Stores a variable at depth level and index offset |
| CAL | Call | level | offset | Calls a procedure at depth level and index offset |
| INT | Increment t | 0 | value | Increments the top pointer for the stack by value |
| JMP | Jump | 0 | address | Sets the program counter to address |
| JPC | Conditional Jump | 0 | address | Sets the program counter to address only if stack[top] is 0 |
| OPR | Operation - End | 0 | 0 | Signifies the end of a block |
| OPR | Operation - Unary Minus | 0 | 1 | Performs negation of one value |
| OPR | Operation - Addition | 0 | 2 | Performs addition of two values |
| OPR | Operation - Subtraction | 0 | 3 | Performs subtraction of two values |
| OPR | Operation - Multiplication | 0 | 4 | Performs multiplication of two values |
| OPR | Operation - Integer Division | 0 | 5 | Performs integer division of two values |
| OPR | Operation - Odd | 0 | 6 | Tests if a value is odd |
| OPR | Operation - NOP | 0 | 7 | No op |
| OPR | Operation - Equality | 0 | 8 | Tests if two values are equal |
| OPR | Operation - Inequality | 0 | 9 | Tests if two values are not equal |
| OPR | Operation - Less than | 0 | 10 | Tests if one value is strictly less than the other |
| OPR | Operation - Greater than or Equal | 0 | 11 | Tests if one value is not less than the other |
| OPR | Operation - Greater than | 0 | 12 | Tests if one value is strictly greater than the other |
| OPR | Operation - Less than or Equal | 0 | 13 | Tests if one value is no greater than the other |
| OPR | Operation - Write | 0 | 14 | Writes stack[top] to the out |
| OPR | Operation - Write Line | 0 | 15 | Writes a new line character to the out |
