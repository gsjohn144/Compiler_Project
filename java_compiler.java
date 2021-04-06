import java.util.*;
import java.io.*;

public class compiler {
		static private final int TRUE = 1;
		static private final int FALSE = 0;
		static private final int NORW = 21;
		static private final int TXMAX = 100;
		static private final int NMAX = 14;
		static private final int AL = 20;
		static private final int AMAX = 2047;

		public static enum OBJECTS {
			Constant,
			Variable,
			Procedure,
			None
		}

		public static enum SYMBOL {
			 BEGINSYM
			,CALLSYM
			,CASESYM //added case
			,CENDSYM //added cend
			,CONSTSYM
			,DOSYM
			,DOWNTO //added downto
			,ELSESYM //added else
			,ENDSYM
			,FORSYM //added for
			,IFSYM
			,ODDSYM
			,OFSYM //added of
			,PROCSYM
			,REPEAT //added repeat
			,THENSYM
			,TOSYM  //added tosym
			,UNTIL //added until
			,VARSYM
			,WHILESYM
			,WRITELNSYM //added writeln
			,WRITESYM  //added write
			,NUL
			,IDENT
			,NUMBER
			,PLUS
			,MINUS
			,TIMES
			,SLASH
			,EQL //equal
			,NEQ //not equal
			,LSS //less than
			,LEQ //less than or equal to
			,GTR //greater than
			,GEQ //greater than or equal to
			,LPAREN //left parenthesis
			,RPAREN //right parenthesis
			,COMMA
			,SEMICOLON
			,COLON
			,PERIOD
			,BECOMES
		}

		// NORW needs to be updated to keep an accurate count of these
		// Must be in lexicographical order
		public static SYMBOL[] wsym =
		{
			 SYMBOL.BEGINSYM
			,SYMBOL.CALLSYM
			,SYMBOL.CASESYM  // added case
			,SYMBOL.CENDSYM // added cend
			,SYMBOL.CONSTSYM
			,SYMBOL.DOSYM
		  ,SYMBOL.DOWNTO // added downto
		  ,SYMBOL.ELSESYM  // added else
			,SYMBOL.ENDSYM
			,SYMBOL.FORSYM // added for
			,SYMBOL.IFSYM
			,SYMBOL.ODDSYM
			,SYMBOL.PROCSYM
			,SYMBOL.REPEAT // added repeat
			,SYMBOL.THENSYM
			,SYMBOL.TOSYM  // added to
			,SYMBOL.UNTIL // added until
			,SYMBOL.VARSYM
			,SYMBOL.WHILESYM
			,SYMBOL.WRITELNSYM // added writeln
			,SYMBOL.WRITESYM // added write
		};

		public static class table_struct {
				public String name;
				public OBJECTS kind;

				public table_struct(String init_name, OBJECTS init_kind) {
					this.name = init_name;
					this.kind = init_kind;
				}
		}

		public static String Word[] = {
			 "BEGIN"
			,"CALL"
			,"CASE"	// added case
			,"CEND" // added cend
			,"CONST"
			,"DO"
			,"DOWNTO"	// added downto
			,"ELSE"	// added else
			,"END"
			,"FOR" //added for
			,"IF"
			,"ODD"
			,"PROCEDURE"
			,"REPEAT" // added repeat
			,"THEN"
			,"TO"	// added to
			,"UNTIL" // added until
			,"VAR"
			,"WHILE"
			,"WRITE" //added write
			,"WRITELN" //added writeln
		};

		public static char Char_Word[][] = {
			 {'B', 'E', 'G', 'I', 'N'}
			,{ 'C', 'A', 'L', 'L'}
			,{ 'C', 'A', 'S', 'E'} // added case
			,{ 'C', 'E', 'N', 'D'} // added cend
			,{ 'C', 'O', 'N', 'S', 'T'}
			,{ 'D', 'O', }
			,{ 'D', 'O', 'W', 'N', 'T', 'O'} // added downto
			,{ 'E', 'L', 'S', 'E'} // added else
			,{ 'E', 'N', 'D'}
			,{ 'F', 'O', 'R'} //added for
			,{ 'I', 'F'}
			,{ 'O', 'D', 'D'}
			,{ 'P', 'R', 'O', 'C', 'E', 'D', 'U', 'R', 'E'}
			,{ 'R', 'E', 'P', 'E', 'A', 'T'} // added repeat
			,{ 'T', 'H', 'E', 'N'}
			,{ 'T', 'O'}	// added to
			,{ 'U', 'N', 'T', 'I', 'L'} // added until
			,{ 'V', 'A', 'R'}
			,{ 'W', 'H', 'I', 'L', 'E'}
			,{ 'W', 'R', 'I', 'T', 'E'} //added write
			,{ 'W', 'R', 'I', 'T', 'E', 'L', 'N'} //added writeln
		};

		static Scanner input;
		static table_struct []table = new table_struct[TXMAX];
		static SYMBOL sym;
		static char ch;
		static char id[] = new char[AL];
		static char a[] = new char[AL];
		static char line[] = new char[80];
		static String temp_id, str_line;
		static int id_length, cc, ll, kk, num;

		/* Initializing list of error messages */
		public static String ErrMsg[] = {
			 "Use = instead of :="	// 1
			,"= must be followed by a number"
			,"Identifier must be followed by ="
			,"Const, Var, Procedure must be followed by an identifier"
			,"Semicolon or comma missing"		// 5
			,"Incorrect symbol after procedure declaration"
			,"Statement expected"
			,"Incorrect symbol after statement part in block"
			,"Period expected"
			,"Semicolon between statements is missing"		// 10
			,"Undeclared identifier"
			,"Assignment to constant or procedure is not allowed"
			,"Assignment operator := expected"
			,"Call must be followed by an identifier"
			,"Call of a constant or a variable is meaningless"		// 15
			,"Then expected"
			,"Semicolon or end expected"
			,"Do expected"
			,"Incorrect symbol following statement"
			,"Relational operator expected"			// 20
			,"Procedure cannot return a value"
			,"Right parenthesis or relational operator expected"
			,"Number is too large"
			,"Identifier expected"
			,"An expression cannot begin with this symbol"		// 25
		};

		/* Simple Error Outputting Function */
		public static void Error(int ErrorNumber) {
			try {
				System.out.println(ErrMsg[ErrorNumber - 1]);
			} catch (IndexOutOfBoundsException e){
				System.out.println("An unknown error '" + Integer.toString(ErrorNumber) + "' occured");
			}
			System.exit(-1);
		}

		public static void GetChar() {
			if (cc == ll) {
				if (input.hasNext()) {
					ll = 0;
					cc = 0;

					str_line = input.nextLine();
					line = str_line.toCharArray(); //converts string to char array
					System.out.println(str_line);
					ll = str_line.length();

					if (ll > 0) {
						if (line[ll-1] == 13) ll--;
						if (line[ll-1] == 10) ll--;
						if (line[ll-1] == 13) ll--;
						if (line[ll-1] == 10) ll--;
						ch = line[cc++];
					} else
						ch = ' ';
				}
			} else
				ch = line[cc++];

			while (ch == '\t')
				ch = line[cc++];
			ch = Character.toUpperCase(ch);
		}

		public static void GetSym() {
			int i, j, k;

      //skipping through whitespaces until an actual char is read
			while (ch == ' ' || ch == '\r' || ch == '\n')
				GetChar();

			if (ch >= 'A' && ch <= 'Z') {
				for (int x = 0; x < AL; x++) {
					a[x]  = '\0';
					id[x] = '\0';
				}

				k = 0;
				do { //while ch in [A-Z0-9]
					if (k < AL) {
						a[k] = ch;
						k++;
				  }
				  // at the end of the line
					if (cc == ll) {
						GetChar();
						break;
					}
					GetChar();
				} while ((ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9'));

				id = a;
				i = 0;
				j = NORW - 1; // max index of wsym
				id_length = k; // k is index of first \0

				// performs a binary search
				do {
					k = i + j;
					k = k / 2;

					temp_id = String.copyValueOf(id, 0, id_length);

					if (id[0] <= Char_Word[k][0]) {
						if (temp_id.compareTo(Word[k]) <= 0) {
							j = k - 1;
						}
					}
					if (id[0] >= Char_Word[k][0]) {
						if (temp_id.compareTo(Word[k]) >= 0) {
							i = k + 1;
						}
					}
				} while (i <= j);

				if (i - 1 > j)
					sym = wsym[k];
				else
					sym = SYMBOL.IDENT;

			//end of if (A-Z)
			} else if (ch >= '0' && ch <= '9') {
				k = 0;
				num = 0;
				sym = SYMBOL.NUMBER;
				do {
					if (k >= NMAX)
						Error(23);
					num = 10 * num + (ch - '0');
					k++;
					GetChar();
				} while (ch >= '0' && ch <= '9');

			} else if (ch == ':') {
				GetChar();
				if (ch == '=') {
					sym = SYMBOL.BECOMES;
					GetChar();
				} else
					sym = SYMBOL.COLON;

			} else if (ch == '>') {
				GetChar();
				if (ch == '=') {
					sym = SYMBOL.GEQ;
					GetChar();
				} else
					sym = SYMBOL.GTR;

			} else if (ch == '<') {
				GetChar();
				if (ch == '=') {
					sym = SYMBOL.LEQ;
					GetChar();
				} else if (ch == '>') {
					sym = SYMBOL.NEQ;
					GetChar();
				} else
					sym = SYMBOL.LSS;

			} else {
				if (ch == '+')
					sym = SYMBOL.PLUS;
				else if (ch == '-')
					sym = SYMBOL.MINUS;
				else if (ch == '*')
					sym = SYMBOL.TIMES;
				else if (ch == '/')
					sym = SYMBOL.SLASH;
				else if (ch == '(')
					sym = SYMBOL.LPAREN;
				else if (ch == ')')
					sym = SYMBOL.RPAREN;
				else if (ch == '=')
					sym = SYMBOL.EQL;
				else if (ch == '.')
					sym = SYMBOL.PERIOD;
				else if (ch == ',')
					sym = SYMBOL.COMMA;
				else if (ch == ';')
					sym = SYMBOL.SEMICOLON;

				GetChar();
			}
		} // End GetSym()

		public static int Enter(OBJECTS k, int tx) {
			tx++;
			table[tx].name = String.valueOf(id);
			table[tx].kind = k;
			return tx;
		}

		public static int Position(char id[], int tx) {
			int i = tx;
			table[0].name = String.valueOf(id);
			while (!table[i].name.equals(String.valueOf(id)))
				i--;
			return i;
		}

		public static void Block(int tx) {
			while ( // Declare procedures, consts, and vars
						 sym == SYMBOL.CONSTSYM
					|| sym == SYMBOL.VARSYM
					|| sym == SYMBOL.PROCSYM
			) {
				switch (sym){
					case PROCSYM:
						GetSym();
						if (sym == SYMBOL.IDENT) {
							tx = Enter(OBJECTS.Procedure, tx);
							GetSym();
						} else
							Error(6);
						if (sym == SYMBOL.SEMICOLON)
							GetSym();
						else
							Error(5);

						Block(tx);
						if (sym == SYMBOL.SEMICOLON)
							GetSym();
						else
							Error(5);
						break;

					case CONSTSYM:
						do {
							GetSym();
							tx = ConstDeclaration(tx);
						} while (sym == SYMBOL.COMMA);
						if (sym != SYMBOL.SEMICOLON)
							Error(5);
						GetSym();
						break;

					case VARSYM:
						do {
							GetSym();
							tx = VarDeclaration(tx);
						} while (sym == SYMBOL.COMMA);
						if (sym != SYMBOL.SEMICOLON)
							Error(5);
						GetSym();
						break;
				}
			} // end while
			Statement(tx);
		} // End Block()

		public static void Factor(int tx) {
			int i;
			if (sym == SYMBOL.IDENT) {
				if ((i = Position(id, tx)) == FALSE)
					Error(11);
				GetSym();
			} else if (sym == SYMBOL.NUMBER)
				GetSym();
			else if (sym == SYMBOL.LPAREN) {
				GetSym();
				Expression(tx);
				if (sym == SYMBOL.RPAREN)
					GetSym();
				else
					Error(22);
			} else
				Error(25);
		}

		public static void Term(int tx) {
			Factor(tx);
			while (sym == SYMBOL.TIMES || sym == SYMBOL.SLASH) {
				GetSym();
				Factor(tx);
			}
		}

		public static void Expression(int tx) {
			if (sym == SYMBOL.PLUS || sym == SYMBOL.MINUS) {
				GetSym();
				Term(tx);
			} else
				Term(tx);
			while (sym == SYMBOL.PLUS || sym == SYMBOL.MINUS) {
				GetSym();
				Term(tx);
			}
		}

		public static void Condition(int tx) {
			if (sym == SYMBOL.ODDSYM) {
				GetSym();
				Expression(tx);

			} else {
				Expression(tx);
				if (
							 sym == SYMBOL.EQL
						|| sym == SYMBOL.GTR
						|| sym == SYMBOL.LSS
						|| sym == SYMBOL.NEQ
						|| sym == SYMBOL.LEQ
						|| sym == SYMBOL.GEQ
				) {
					GetSym();
					Expression(tx);
				} else
					Error(20);
			}
		}

		public static int ConstDeclaration(int tx) {
			if (sym == SYMBOL.IDENT) {
				GetSym();
				if (sym == SYMBOL.EQL) {
					GetSym();
					if (sym == SYMBOL.NUMBER) {
						tx = Enter(OBJECTS.Constant, tx);
						GetSym();
					} else
						Error(2);
				} else
					Error(3);
			} else
				Error(4);
			return tx;
		}

		public static int VarDeclaration(int tx) {
			if (sym == SYMBOL.IDENT) {
				tx = Enter(OBJECTS.Variable, tx);
				GetSym();
			} else
				Error(4);
			return tx;
		}

		public static void Statement(int tx) {
			int i;
			switch (sym) {
				case BEGINSYM:
					GetSym();
					Statement(tx);
					while (sym == SYMBOL.SEMICOLON) {
						GetSym();
						Statement(tx);
					}
					if (sym == SYMBOL.ENDSYM)
						GetSym();
					else
						Error(17);
					break;

				case IDENT:
					i = Position(id, tx);
					if (i == 0)
						Error(11);
					else
						if (table[i].kind != OBJECTS.Variable)
							Error(12);
					GetSym();
					if (sym == SYMBOL.BECOMES)
						GetSym();
					else
						Error(13);
					Expression(tx);
					break;

        //added FORSYM
        case FORSYM:
          GetSym();
          if (sym != SYMBOL.IDENT)
            Error(14);
          i = Position(id, tx);
          if (i == 0)
            Error(11);
          GetSym();
          if (sym != SYMBOL.BECOMES)
            Error(13);
          GetSym();
          Expression(tx);

          if (sym == SYMBOL.TOSYM) {
            GetSym();
            Expression(tx);
            if (sym != SYMBOL.DOSYM) {
              System.out.println(sym);
              Error(18);
            }
            GetSym();
            Statement(tx);

					} else if (sym == SYMBOL.DOWNTO) {
            GetSym();
            Expression(tx);
            if (sym != SYMBOL.DOSYM)
              Error(18);
            GetSym();
            Statement(tx);

					} else // Not TO nor DOWNTO
            Error(31);
          break; /* FORSYM */

				case IFSYM:
					GetSym();
					Condition(tx);
					if (sym == SYMBOL.THENSYM)
						GetSym();
					else
						Error(16);
					Statement(tx);

					//added ELSE
					if (sym == SYMBOL.ELSESYM) {
				    GetSym();
				    Statement(tx);
					}
					break; /* IF THEN ELSE */

				//added REPEAT
				case REPEAT:
			    GetSym();
			    Statement(tx);
			    while (sym == SYMBOL.SEMICOLON) {
		        GetSym();
		        Statement(tx);
			    }
			    if (sym != SYMBOL.UNTIL)
		        Error(27);
			    GetSym();
			    Condition(tx);
			    break; /*REPEAT UNTIL*/

				case WHILESYM:
					GetSym();
					Condition(tx);
					if (sym == SYMBOL.DOSYM) {
						GetSym();
						Statement(tx);
					} else
						Error(18);
					break; /* WHILESYM */

      	//added WRITESYM
      	case WRITESYM:
        	GetSym();
          if (sym != SYMBOL.LPAREN)
            Error(33);
          GetSym();
          Expression(tx);
          while (sym == SYMBOL.COMMA) {
            GetSym();
            Expression(tx);
          }
          if (sym != SYMBOL.RPAREN)
            Error(22);
          GetSym();
          break; /*WRITESYM*/


        //added WRITELNSYM
        case WRITELNSYM:
          GetSym();
          if (sym != SYMBOL.LPAREN)
            Error(33);
          GetSym();
          Expression(tx);
          while (sym == SYMBOL.COMMA) {
            GetSym();
            Expression(tx);
          }
          if (sym != SYMBOL.RPAREN)
            Error(22);
          GetSym();
          break; /*WRITELNSYM*/


        case CASESYM:
          GetSym();
          Expression(tx);
          if (sym == SYMBOL.OFSYM)
            Error(28);
          GetSym(); //expression 1
          while (sym != SYMBOL.CENDSYM && sym != SYMBOL.ELSESYM) {
            Expression(tx);
            if (sym != SYMBOL.COLON)
              Error(29);
            GetSym();
            Statement(tx);
            GetSym(); //next expression or end
          }
          if (sym == SYMBOL.ELSESYM) {
            GetSym();
            Statement(tx);
          }
          if (sym != SYMBOL.CENDSYM)
            Error(30);
          GetSym();
          break; /*CASE*/

				case CALLSYM:
					GetSym();
					if (sym == SYMBOL.IDENT) {
						if ((i = Position(id, tx)) == FALSE)
							Error(11);
						else if (table[i].kind != OBJECTS.Procedure)
								Error(15);
						GetSym();
					} else
						Error(14);
					break; //CALLSYM
			} // End switch
		} //end statement()

		public static void main(String[] args) {
			try {
				input = new Scanner(System.in);
			} catch (Exception e) {
				System.err.println("Error Getting Input");
				System.exit(1);
			}

			cc = ll;
			ll = 0;
			ch = ' ';
			kk = AL;

			for (int q = 0; q < TXMAX; q++) {
				table[q] = new table_struct("", OBJECTS.None);
			}

			GetSym();

			Block(0);

			if (sym != SYMBOL.PERIOD)
				Error(9);
			else
				System.out.println("\nSuccessful compilation!\n");

			input.close();
		}
}
