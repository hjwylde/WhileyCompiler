import * from whiley.lang.*

define plistv6 as [int] where no { x in $ | x < 0 } 

int f(plistv6 xs):
    return |xs|

int g(plistv6 left, [int] right):
    return f(left + right)

void ::main(System sys,[string] args):
    r = g([1,2,3],[-1,7,8])
    debug toString(r)
