import * from whiley.lang.*

define fr2nat as int where $ >= 0

string f(fr2nat x):
    return toString(x)

void ::main(System sys,[string] args):
    y = 1
    sys.out.println(f(y))
