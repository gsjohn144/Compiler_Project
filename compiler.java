import java.util.*;
import java.io.*;

public class compiler {
		static private final int TRUE = 1;
		static private final int FALSE = 0;
		static private final int NORW = 11;
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
			,WRITESYM  //added write
			,WRITELNSYM //added writeln
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

		public static SYMBOL[] wsym =
		{
			 SYMBOL.BEGINSYM
			,SYMBOL.CALLSYM
		//,SYMBOL.CASESYM  //added case
			,SYMBOL.CONSTSYM
			,SYMBOL.DOSYM
		//,SYMBOL.DOWNTO //added downto
		//,SYMBOL.ELSESYM  //added else
			,SYMBOL.ENDSYM
			,SYMBOL.FORSYM //added for
			,SYMBOL.IFSYM
			,SYMBOL.ODDSYM
			,SYMBOL.PROCSYM
			,SYMBOL.THENSYM
		//	,SYMBOL.TOSYM  //added to
			,SYMBOL.VARSYM
			,SYMBOL.WHILESYM
			,SYMBOL.WRITESYM //added write
			,SYMBOL.WRITELNSYM //added writeln
		};

		public static class table_struct {
				public String name;
				public OBJECTS kind;

				public table_struct(String init_name, OBJECTS init_kind {
					this.name = init_name;
					this.kind = init_kind;
				}
		}

		public static String Word[] = {
			 "BEGIN"
			,"CALL"
			,"CONST"
			,"DO"
			,"END"
			,"FOR" //added for
			,"IF"
			,"ODD"
			,"PROCEDURE"
			,"THEN"
			,"VAR"
			,"WHILE"
			,"WRITE" //added write
			,"WRITELN" //added writeln
		};

		public static char Char_Word[][] = {
			 {'B', 'E', 'G', 'I', 'N'}
			,{ 'C', 'A', 'L', 'L'}
			,{ 'C', 'O', 'N', 'S', 'T'}
			,{ 'D', 'O', }
			,{ 'E', 'N', 'D'}
			,{ 'F', 'O', 'R'} //added for
			,{ 'I', 'F'}
			,{ 'O', 'D', 'D'}
			,{ 'P', 'R', 'O', 'C', 'E', 'D', 'U', 'R', 'E'}
			,{ 'T', 'H', 'E', 'N'}
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
			System.out.println(ErrMsg[ErrorNumber - 1]);
			System.exit(-1);
		}

		public static void GetChar() {
		   // System.out.println("In GetChar\n");
			if (cc == ll) {
				if (input.hasNext()) {
					ll = 0;
					cc = 0;

					str_line = input.nextLine();
					line = str_line.toCharArray(); //converts string to char array
				//	System.out.println("printing str_line");
					System.out.println(str_line);
				//	System.out.println("\n");

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
		}

		public static void GetSym() {
		  // 	System.out.println("\n\n\nEntering GetSym\n");

			int i, j, k;

      //skipping through whitespaces until an acutal char is read
			while (ch == ' ' || ch == '\r' || ch == '\n')
				GetChar();

        //	System.out.println("Char is");
        //	System.out.println(ch);
        //	System.out.println("\n");

            //if equal to a letter
			if (ch >= 'A' && ch <= 'Z') {
				k = 0;
				int x = 0;

        //int AL = 20 (global)
        //fills a[] and id[] with '\0' (both of size 20)
				for (; x < AL; x++) {
					a[x]  = '\0';
					id[x] = '\0';
				}


				do { //while ch is a letter or a number A-Z or 0-9
			  	//k starts at 0, AL = 20
			  	//fills a[] with char
					if (k < AL) {
					// System.out.println("Filling a[]\n");
						a[k++] = ch;
				  }
				  //gets last char of current sequence then breaks
					if (cc == ll) {
					  //  System.out.println(" cc == ll\n");
						GetChar();
						break;
					} else
						GetChar();
				} while ((ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9'));

        //id[] now had all chars in it from a
				id = a;
				//	System.out.println(" ID = ");
				//   System.out.println(id);
        //   System.out.println("\n ");

				i = 0;
				j = NORW - 1; //NORW = 11 so j = 10

				if (k == 1)
					id_length = 1;
				else
					id_length = k--;

        //tries to match temp_id with a Word
				do {
					k = i + j; //i=0 and j =10 so k =10
					k = k / 2; //k = 5

          //temp_id had id in it now
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
        // if temp id is printing the same as ID then it found the match
				// System.out.println("temp_id is ");
        // System.out.println(temp_id);

				if (i - 1 > j)
					sym = wsym[k];
				else
					sym = SYMBOL.IDENT;

			// System.out.println("\nsym is ");
			// System.out.println(sym);

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
			  // System.out.println("/n in semi/n");
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
			if (sym == SYMBOL.CONSTSYM) {
				GetSym();
				tx = ConstDeclaration(tx);
				while (sym == SYMBOL.COMMA) {
					GetSym();
					tx = ConstDeclaration(tx);
				}
				if (sym == SYMBOL.SEMICOLON)
					GetSym();
				else
					Error(5);
			}

			if (sym == SYMBOL.VARSYM) {
				GetSym();
				tx = VarDeclaration(tx);
				while (sym == SYMBOL.COMMA) {
					GetSym();
					tx = VarDeclaration(tx);
				}
				if (sym == SYMBOL.SEMICOLON)
					GetSym();
				else
					Error(5);
			}

			while (sym == SYMBOL.PROCSYM) {
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
			}
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
							 (sym == SYMBOL.EQL)
						|| (sym == SYMBOL.GTR)
						|| (sym == SYMBOL.LSS)
						|| (sym == SYMBOL.NEQ)
						|| (sym == SYMBOL.LEQ)
						|| (sym == SYMBOL.GEQ)
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
      //add statement syms here
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
					if ((i = Position(id, tx)) == FALSE)
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

			for (int q = 0; q < TXMAX; q) {
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