import * from whiley.lang.*

define intreal as real | int

string f(intreal e):
    if e is int:
        return "int"
    else:
        return "real"

void ::main(System sys,[string] args):
    sys.out.println(f(1))
    sys.out.println(f(1.134))
    sys.out.println(f(1.0))
