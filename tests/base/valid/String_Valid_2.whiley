import * from whiley.lang.*

// replace all occurrences of "old" with "new" in string "str".  TO BE
// DEPRECATED
public bool has(char c1, string str):
    for c2 in str:
        if c1 == c2:
            return true
    return false

void ::main(System sys,[string] args):
    s = "Hello World"
    sys.out.println(toString(has('l',s)))
    sys.out.println(toString(has('e',s)))
    sys.out.println(toString(has('h',s)))
    sys.out.println(toString(has('z',s)))
    sys.out.println(toString(has('H',s)))
