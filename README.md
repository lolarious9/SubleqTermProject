
# Subtract if less than or equal to (SUBLEQ):
If the right-op is -1 print the left,
if the left is -1, take a character from standard input
else  memory[rop] -= memory[lop];

### Use:
* Use .slq files for code
* Both the runner and the parser will take either the file name passed via commandline or they will prompt if you forget.
* Hello.slq is Hello World, and test.slq displays some of the functionality for reference.
* The parser is in java, and the runner is in C++

### Grammar: 

##### Variables:
* Variables use regular expressions again
* `;([a-zA-Z_]+)\\s(?:(?<num>[-+]?\\d+)|(?:\\\"(?<str>[\\w]+|[\\S]{1})\\\"))$`
`;hello "helloworld"
;x 10`
* Variables start with a semi-colon, and then the name.
* String variables will be sent to the runner as the integers corresponding to each letter. 
* Chars can and should be passed as their integer value directly as well.


##### Includes (Marcos)
* Macros are included by using the an ampresand(@)
`^@(?<command>[a-zA-Z]+)`
* Macros use the command regex, so they follow the same rules.
* @add
* @print
* `#add a b {
    slq a z
    slq z b
    slq z z
}`


Please keep the Macros and Variables above the header
`#main {`

##### Commands:
* Uses Java's Regex to lex: 
`((?<command>#?[a-zA-Z]+)(?:\\s+)(?<lop>[-\\w]+)(?:$|\\s+(?<rop>[-\\w]+)|\\s+\\{$)(?:$|\\s+(?<third>[-\\w]+)|(?:\\s+\\{$|$)))`
* Command Left-Op Right-Op Third-op
* Command can be SLQ for subleq or the name of a macro
* Third-Op is optional, and if it's not there, it is assumed to be the next set of instructions.

`}` Don't forget to close up
