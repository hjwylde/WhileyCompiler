import * from whiley.lang.*

define point as {int x, int y} where $.x > 0 && $.y > 0

void ::main(System sys,[string] args):
    p = {x:1,y:1}
    sys.out.println(toString(p))
