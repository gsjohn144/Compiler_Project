#! C:\Program Files\Python26\pythonw
import sys, string, getopt

norw = 22  # number of reserved words
txmax = 100  # length of identifier table
nmax = 14  # max number of digits in number
al = 10  # length of identifiers
CXMAX = 500  # maximum allowed lines of assembly code
STACKSIZE = 500
a = []
chars = []
rword = []
table = []  # symbol table
code = []  # code array
stack = [0] * STACKSIZE  # interpreter stack
global infile, outfile, ch, sym, id, num, linlen, kk, line, errorFlag, linelen, codeIndx, prevIndx, codeIndx0

####-- Argument Handling --####

# Arguments: script.py -i [inputFile] -o [outputFile]
infilePath = None
outfilePath = None
try:
    opts, args = getopt.getopt(sys.argv[1:], "hi:o:",["help", "input=", "output="])
except getopt.GetoptError:
    print('Usage: {file} -i [inputFile] -o [outputFile]'.format(file=__file__))
    sys.exit(2)
for opt, arg in opts:
    if opt in ('-h', '--help'):
        print('Usage: {file} -i [inputfile] -o [outputFile]'.format(file=__file__))
        sys.exit()
    elif opt in ('-i', '--input'):
        infilePath = arg
    elif opt in ('-o', '--output'):
        outfilePath = arg
if infilePath is None:
    infile = sys.stdin
else:
    infile = open(infilePath, "r")
if outfilePath is None:
    outfile = open("./a.out", "w")
else:
    outfile = open(outfilePath, "w")


# -------------values to put in the symbol table------------------------------------------------------------
class tableValue():
    def __init__(self, name, kind, level, adr, value):
        self.name = name
        self.kind = kind
        self.adr = adr
        self.value = value
        self.level = level


# ----------commands to put in the array of assembly code-----------------------------------------------
class Cmd():
    def __init__(self, line, cmd, statLinks, value):
        self.line = line
        self.cmd = cmd
        self.statLinks = statLinks
        self.value = value


# -------------function to generate assembly commands--------------------------------------------------
def gen(cmd, statLinks, value):
    global codeIndx, CXMAX
    if codeIndx > CXMAX:
        outfile.write("Error, Program is too long\n")
        exit(0)
    x = Cmd(codeIndx, cmd, statLinks, value)
    code.append(x)
    codeIndx += 1


# --------------function to change jump commands---------------------------------------
def fixJmp(cx, jmpTo):
    code[cx].value = jmpTo


# --------------Function to print p-Code for a given block-----------------------------
def printCode():
    global codeIndx, codeIndx0
    for i in range(codeIndx0, codeIndx):
        outfile.write(
            "{line}\t{cmd}\t{stat}\t{val}\n".format(
                 line = code[i].line
                ,cmd = code[i].cmd
                ,stat = code[i].statLinks
                ,val = code[i].value
            )
        )
    prevIndx = codeIndx


# -------------Function to find a new base----------------------------------------------
def Base(statLinks, base):
    b1 = base
    while (statLinks > 0):
        b1 = stack[b1]
        statLinks -= 1
    return b1


# -------------P-Code Interpreter-------------------------------------------------------
def Interpret():
    outfile.write("Start PL/0\n")
    top = 0
    base = 1
    pos = 0
    stack[1] = 0
    stack[2] = 0
    stack[3] = 0
    while True:
        instr = code[pos]
        pos += 1
        #       LIT COMMAND
        if instr.cmd == "LIT":
            top += 1
            stack[top] = int(instr.value)
        #       OPR COMMAND
        elif instr.cmd == "OPR":
            if instr.value == 0:  # end
                top = base - 1
                base = stack[top + 2]
                pos = stack[top + 3]
            elif instr.value == 1:  # unary minus
                stack[top] = -stack[top]
            elif instr.value == 2:  # addition
                top -= 1
                stack[top] = stack[top] + stack[top + 1]
            elif instr.value == 3:  # subtraction
                top -= 1
                stack[top] = stack[top] - stack[top + 1]
            elif instr.value == 4:  # multiplication
                top -= 1
                stack[top] = stack[top] * stack[top + 1]
            elif instr.value == 5:  # integer division
                top -= 1
                stack[top] = stack[top] / stack[top + 1]
            elif instr.value == 6:  # logical odd function
                if stack[top] % 2 == 0:
                    stack[top] = 1
                else:
                    stack[top] = 0
            # case 7 n/a, used to debug programs
            elif instr.value == 8:  # test for equality if stack[top-1] = stack[top], replace pair with true, otherwise false
                top -= 1
                if stack[top] == stack[top + 1]:
                    stack[top] = 1
                else:
                    stack[top] = 0
            elif instr.value == 9:  # test for inequality
                top -= 1
                if stack[top] != stack[top + 1]:
                    stack[top] = 1
                else:
                    stack[top] = 0
            elif instr.value == 10:  # test for < (if stack[top-1] < stack[t])
                top -= 1
                if stack[top] < stack[top + 1]:
                    stack[top] = 1
                else:
                    stack[top] = 0
            elif instr.value == 11:  # test for >=
                top -= 1
                if stack[top] >= stack[top + 1]:
                    stack[top] = 1
                else:
                    stack[top] = 0
            elif instr.value == 12:  # test for >
                top -= 1
                if stack[top] > stack[top + 1]:
                    stack[top] = 1
                else:
                    stack[top] = 0
            elif instr.value == 13:  # test for <=
                top -= 1
                if stack[top] <= stack[top + 1]:
                    stack[top] = 1
                else:
                    stack[top] = 0
            elif instr.value == 14:  # write/print stack[top]
                outfile.write(str(stack[top]))
                top -= 1
            elif instr.value == 15:  # write/print a newline
                # Worth noting in the original this was just "print",
                # no chevron, so it may have been meant to print a newline in stdout
                outfile.write("\n")
        #      LOD COMMAND
        elif instr.cmd == "LOD":
            top += 1
            stack[top] = stack[Base(instr.statLinks, base) + instr.value]
        #    STO COMMAND
        elif instr.cmd == "STO":
            stack[Base(instr.statLinks, base) + instr.value] = stack[top]
            top -= 1
        #    CAL COMMAND
        elif instr.cmd == "CAL":
            stack[top + 1] = Base(instr.statLinks, base)
            stack[top + 2] = base
            stack[top + 3] = pos
            base = top + 1
            pos = instr.value
        #    INT COMMAND
        elif instr.cmd == "INT":
            top = top + instr.value
        #     JMP COMMAND
        elif instr.cmd == "JMP":
            pos = instr.value
        #     JPC COMMAND
        elif instr.cmd == "JPC":
            if stack[top] == instr.statLinks:
                pos = instr.value
            top -= 1
        if pos == 0:
            break
    outfile.write("End PL/0\n")


# --------------Error Messages----------------------------------------------------------
def error(num):
    global errorFlag
    errorFlag = 1
    print("\n")
    if num == 1:
        outfile.write("Use = instead of :=\n")
    elif num == 2:
        outfile.write("= must be followed by a number\n")
    elif num == 3:
        outfile.write("Identifier must be followed by =\n")
    elif num == 4:
        outfile.write("Const, Var, Procedure must be followed by an identifier\n")
    elif num == 5:
        outfile.write("Semicolon or comma missing\n")
    elif num == 6:
        outfile.write("Incorrect symbol after procedure declaration\n")
    elif num == 7:
        outfile.write("Statement expected\n")
    elif num == 8:
        outfile.write("Incorrect symbol after statement part in block\n")
    elif num == 9:
        outfile.write("Period expected\n")
    elif num == 10:
        outfile.write("Semicolon between statements is missing\n")
    elif num == 11:
        outfile.write("Undeclared identifier\n")
    elif num == 12:
        outfile.write("Assignment to a constant or procedure is not allowed\n")
    elif num == 13:
        outfile.write("Assignment operator := expected\n")
    elif num == 14:
        outfile.write("Identifier expected\n")
    elif num == 15:
        outfile.write("Call of a constant or a variable is meaningless\n")
    elif num == 16:
        outfile.write("'Then' expected\n")
    elif num == 17:
        outfile.write("Semicolon or 'end' expected\n")
    elif num == 18:
        outfile.write("'Do' expected\n")
    elif num == 19:
        outfile.write("Incorrect symbol following statement\n")
    elif num == 20:
        outfile.write("Relational operator expected\n")
    elif num == 21:
        outfile.write("Expression must not contain a procedure identifier\n")
    elif num == 22:
        outfile.write( "Right parenthesis missing\n")
    elif num == 23:
        outfile.write("The preceding factor cannot be followed by this symbol\n")
    elif num == 24:
        outfile.write("An expression cannot begin with this symbol\n")
    elif num == 25:
        outfile.write("Constant or Number is expected\n")
    elif num == 26:
        outfile.write("This number is too large\n")
    exit(0)


# ---------GET CHARACTER FUNCTION-------------------------------------------------------------------
def getch():
    global whichChar, ch, linelen, line
    if whichChar == linelen:  # if at end of line
        whichChar = 0
        line = infile.readline()  # get next line
        linelen = len(line)
        sys.stdout.write(line)
    if linelen != 0:
        ch = line[whichChar]
        whichChar += 1
    return ch


# ----------GET SYMBOL FUNCTION---------------------------------------------------------------------
def getsym():
    global charcnt, ch, al, a, norw, rword, sym, nmax, id, num
    while ch == " " or ch == "\n" or ch == "\r":
        getch()
    a = []
    if ch.isalpha():
        k = 0
        while True:
            a.append(ch.upper())
            getch()
            if not ch.isalnum():
                break
        id = "".join(a)
        flag = 0
        for i in range(0, norw):
            if rword[i] == id:
                sym = rword[i]
                flag = 1
        if flag == 0:  # sym is not a reserved word
            sym = "ident"
    elif ch.isdigit():
        k = 0
        num = 0
        sym = "number"
        while True:
            a.append(ch)
            k += 1
            getch()
            if not ch.isdigit():
                break
        if k > nmax:
            error(30)
        else:
            num = "".join(a)
    elif ch == ':':
        getch()
        if ch == '=':
            sym = "becomes"
            getch()
        else:
            sym = "colon"
    elif ch == '>':
        getch()
        if ch == '=':
            sym = "geq"
            getch()
        else:
            sym = "gtr"
    elif ch == '<':
        getch()
        if ch == '=':
            sym = "leq"
            getch()
        elif ch == '>':
            sym = "neq"
            getch()
        else:
            sym = "lss"
    else:
        sym = ssym[ch]
        getch()


# --------------POSITION FUNCTION----------------------------
def position(tx, id):
    global table
    table[0] = tableValue(id, "TEST", "TEST", "TEST", "TEST")
    i = tx
    while table[i].name != id:
        i -= 1
    return i


# ---------------ENTER PROCEDURE-------------------------------
def enter(tx, k, level, dx):
    global id, num, codeIndx
    tx[0] += 1
    while (len(table) > tx[0]):
        table.pop()
    if k == "const":
        x = tableValue(id, k, level, "NULL", num)
    elif k == "variable":
        x = tableValue(id, k, level, dx, "NULL")
        dx += 1
    elif k == "procedure":
        x = tableValue(id, k, level, dx, "NULL")
    # The original didn't have an else clause because it should never
    # be called w/o the necessary arguments, and if it is, we want it to
    # error, not propogate bad data

    #else:
    #    x = tableValue(None, None, None, None, None)
    table.append(x)
    return dx


# --------------CONST DECLARATION---------------------------
def constdeclaration(tx, level):
    global sym, id, num
    if sym == "ident":
        getsym()
        if sym == "eql":
            getsym()
            if sym == "number":
                enter(tx, "const", level, "null")
                getsym()
            else:
                error(2)
        else:
            error(3)
    else:
        error(4)


# -------------VARIABLE DECLARATION--------------------------------------
def vardeclaration(tx, level, dx):
    global sym
    if sym == "ident":
        dx = enter(tx, "variable", level, dx)
        getsym()
    else:
        error(4)
    return dx


# -------------BLOCK-------------------------------------------------------------
def block(tableIndex, level):
    global sym, id, codeIndx, codeIndx0
    tx = [None]
    tx[0] = tableIndex
    tx0 = tableIndex
    dx = 3
    cx1 = codeIndx
    gen("JMP", 0, 0)
    while sym == "PROCEDURE" or sym == "VAR" or sym == "CONST":
        if sym == "CONST":
            while True:  # makeshift do while in python
                getsym()
                constdeclaration(tx, level)
                if sym != "comma":
                    break
            if sym != "semicolon":
                error(10)
            getsym()
        if sym == "VAR":
            while True:
                getsym()
                dx = vardeclaration(tx, level, dx)
                if sym != "comma":
                    break
            if sym != "semicolon":
                error(10)
            getsym()
        while sym == "PROCEDURE":
            getsym()
            if sym == "ident":
                enter(tx, "procedure", level, codeIndx)
                getsym()
            else:
                error(4)
            if sym != "semicolon":
                error(10)
            getsym()
            block(tx[0], level + 1)

            if sym != "semicolon":
                error(10)
            getsym()
    fixJmp(cx1, codeIndx)
    if tx0 != 0:
        table[tx0].adr = codeIndx
    codeIndx0 = codeIndx
    gen("INT", 0, dx)
    statement(tx[0], level)
    gen("OPR", 0, 0)
    # print code for this block
    printCode()


# --------------STATEMENT----------------------------------------
def statement(tx, level):
    global sym, id, num
    if sym == "ident":
        i = position(tx, id)
        if i == 0:
            error(11)
        elif table[i].kind != "variable":
            error(12)
        getsym()
        if sym != "becomes":
            error(13)
        getsym()
        expression(tx, level)
        gen("STO", level - table[i].level, table[i].adr)
    elif sym == "CALL":
        getsym()
        if sym != "ident":
            error(14)
        i = position(tx, id)
        if i == 0:
            error(11)
        if table[i].kind != "procedure":
            error(15)
        gen("CAL", level - table[i].level, table[i].adr)
        getsym()
    elif sym == "IF":
        getsym()
        condition(tx, level)
        cx1 = codeIndx
        gen("JPC", 0, 0)
        if sym != "THEN":
            error(16)
        getsym()
        statement(tx, level)
        fixJmp(cx1, codeIndx)
    # TODO: place your code for ELSE here
    elif sym == "ELSE":
        getsym()
    elif sym == "BEGIN":
        while True:
            getsym()
            statement(tx, level)
            if sym != "semicolon":
                break
        if sym != "END":
            error(17)
        getsym()
    elif sym == "WHILE":
        getsym()
        cx1 = codeIndx
        condition(tx, level)
        cx2 = codeIndx
        gen("JPC", 0, 0)
        if sym != "DO":
            error(18)
        getsym()
        statement(tx, level)
        gen("JMP", 0, cx1)
        fixJmp(cx2, codeIndx)
    # TODO: place your code for REPEAT here
    elif sym == "REPEAT":
        pass
    # REVIEW: place your code for FOR here
    elif sym == "FOR":
        getsym()
        cx1 = codeIndx
        condition(tx, level)
        cx2 = codeIndx
        gen("JPC", 0, 0)
        if sym != "DO":
            error(18)
        getsym()
        statement(tx, level)
        gen("JMP", 0, cx1)
        fixJmp(cx2, codeIndx)
    # TODO: place your code for CASE here
    elif sym == "CASE":
        pass
    elif sym == "WRITE":
        getsym()
        if sym != "ident":
            error(14)
        i = position(tx, id)
        if i == 0:
            error(11)
        if table[i].kind == "const":
            gen("LIT", 0, table[i].value)
        elif table[i].kind == "variable":
            gen("LOD", level - table[i].level, table[i].adr)
        else:
            error(25)
        gen("OPR", 0, 14)
        getsym()
    elif sym == "WRITELN":
        gen("OPR", 0, 15)
        getsym()
# --------------EXPRESSION--------------------------------------
def expression(tx, level):
    global sym
    if sym == "plus" or sym == "minus":
        addop = sym
        getsym()
        term(tx, level)
        if (addop == "minus"):  # if minus sign, do negate operation
            gen("OPR", 0, 1)
    else:
        term(tx, level)

    while sym == "plus" or sym == "minus":
        addop = sym
        getsym()
        term(tx, level)

        if (addop == "plus"):
            gen("OPR", 0, 2)  # add operation
        else:
            gen("OPR", 0, 3)  # subtract operation


# -------------TERM----------------------------------------------------
def term(tx, level):
    global sym
    factor(tx, level)
    while sym == "times" or sym == "slash":
        mulop = sym
        getsym()
        factor(tx, level)
        if mulop == "times":
            gen("OPR", 0, 4)  # multiply operation
        else:
            gen("OPR", 0, 5)  # divide operation


# -------------FACTOR--------------------------------------------------
def factor(tx, level):
    global sym, num, id
    if sym == "ident":
        i = position(tx, id)
        if i == 0:
            error(11)
        if table[i].kind == "const":
            gen("LIT", 0, table[i].value)
        elif table[i].kind == "variable":
            gen("LOD", level - table[i].level, table[i].adr)
        elif table[i].kind == "procedure":
            error(21)
        getsym()
    elif sym == "number":
        gen("LIT", 0, num)
        getsym()
    elif sym == "lparen":
        getsym()
        expression(tx, level)
        if sym != "rparen":
            error(22)
        getsym()
    else:
        error(24)


# -----------CONDITION-------------------------------------------------
def condition(tx, level):
    global sym
    if sym == "ODD":
        getsym()
        expression(tx, level)
        gen("OPR", 0, 6)
    else:
        expression(tx, level)
        if not (sym in ["eql", "neq", "lss", "leq", "gtr", "geq"]):
            error(20)
        else:
            temp = sym
            getsym()
            expression(tx, level)
            if temp == "eql":
                gen("OPR", 0, 8)
            elif temp == "neq":
                gen("OPR", 0, 9)
            elif temp == "lss":
                gen("OPR", 0, 10)
            elif temp == "geq":
                gen("OPR", 0, 11)
            elif temp == "gtr":
                gen("OPR", 0, 12)
            elif temp == "leq":
                gen("OPR", 0, 13)


# -------------------MAIN PROGRAM------------------------------------------------------------#
rword.append('BEGIN')
rword.append('CALL')
rword.append('CONST')
rword.append('DO')
rword.append('END')
rword.append('IF')
rword.append('ODD')
rword.append('PROCEDURE')
rword.append('THEN')
rword.append('VAR')
rword.append('WHILE')
rword.append('ELSE')
rword.append('REPEAT')
rword.append('UNTIL')
rword.append('FOR')
rword.append('TO')
rword.append('DOWNTO')
rword.append('CASE')
rword.append('OF')
rword.append('CEND')
rword.append('WRITE')
rword.append('WRITELN')

ssym = {
     '+': "plus"
    ,'-': "minus"
    ,'*': "times"
    ,'/': "slash"
    ,'(': "lparen"
    ,')': "rparen"
    ,'=': "eql"
    ,',': "comma"
    ,'.': "period"
    ,'#': "neq"
    ,'<': "lss"
    ,'>': "gtr"
    ,'"': "leq"
    ,'@': "geq"
    ,';': "semicolon"
    ,':': "colon"
}
charcnt = 0
whichChar = 0
linelen = 0
ch = ' '
kk = al
a = []
id = '     '
errorFlag = 0
table.append(0) # Original code
# table.append(tableValue(0, "INIT", 0, "INIT", "INIT"))  # making the first position in the symbol table empty
sym = ' '
codeIndx = 0  # first line of assembly code starts at 1
prevIndx = 0

getsym()  # get first symbol
block(0, 0)  # call block initializing with a table index of zero
# Note: To ensure all lines from 1 to program end get printed:
# Comment out line ~495 (the printCode call at the end of def block
# and uncomment the below:
# codeIndx0 = 1
# printCode()
if sym != "period":  # period expected after block is completed
    error(9)
print("\n") # Original code was `print`
if errorFlag == 0:
    outfile.write("Successful compilation!\n")

Interpret()
