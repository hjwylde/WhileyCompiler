import * from whiley.lang.*

define pos as real
define neg as int
define expr as pos|neg|[int]

string f(expr e):
    if e is pos && e > 0:
        return "POSITIVE: " + toString(e)
    else if e is neg:
        return "NEGATIVE: " + toString(e)
    else:
        return "OTHER"

void ::main(System sys,[string] args):
    sys.out.println(f(-1))
    sys.out.println(f(1.0))
    sys.out.println(f(1.234))
    sys.out.println(f([1,2,3]))
 
