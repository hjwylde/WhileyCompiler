import whiley.io.*

void System::main([string] args):
    if |args| == 0:
        this->usage()
        return
    file = this->openFile(args[0])
    contents = file->read()
    game = parseChessGame(contents)
    if game ~= SyntaxError:
        out->println("syntax error: " + game.msg)
    else:     
        board = startingChessBoard  
        r = ""       
        i = 0
        invalid = false
        sign = false
        while i < |game| && !invalid:
            move = game[i]
            if validMove(move,board):
                if !sign:
                    r = move2str(move)
                else:
                    out->println(r + " " + move2str(move))
                sign = !sign
                i = i +1
            else:
                invalid = true
        if invalid:
            out->println(board2str(board))
            out->println("The following move is invalid")
            out->println(move2str(game[i]))
        else if sign:
            out->println(r)

void System::usage():
    out->println("usage: chess file")

define BLACK_PIECE_CHARS as [ 'P', 'N', 'B', 'R', 'Q', 'K' ]

string board2str(Board b):
    r = ""
    i=8
    while i >= 1:
        r = r + str(i) + row2str(b[i-1])
        i = i - 1
    return r + "  a b c d e f g h\n"

string row2str(Row row):
    r = ""
    for square in row:
        r = r + "|" + square2str(square)
    return r + "|\n"

string square2str(Square p):
    if p ~= null:
        return "_"
    else if p.colour:
        return [PIECE_CHARS[p.kind]]
    else:
        return [BLACK_PIECE_CHARS[p.kind]]

string move2str(Move m):
    if m ~= SingleTake: 
        return piece2str(m.piece) + pos2str(m.from) + "x" + piece2str(m.taken) + pos2str(m.to)
    else if m ~= SingleMove:
        return piece2str(m.piece) + pos2str(m.from) + "-" + pos2str(m.to)   
    else:
        // check move
        return move2str(m.move) + "+"  

string piece2str(Piece p):
    if p.kind == PAWN:
        return ""
    else:
        return [PIECE_CHARS[p.kind]]

string pos2str(Pos p):
    return ['a' + p.col,'0' + p.row]
