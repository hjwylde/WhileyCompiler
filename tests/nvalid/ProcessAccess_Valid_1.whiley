import println from whiley.lang.System

type etype is {int rest, int mode}

type Ptype is ref etype

method get(Ptype this) => (int, string):
    this->mode = 1
    this->rest = 123
    return (this->mode, Any.toString(*this))

method main(System.Console sys) => void:
    p = new {rest: 2, mode: 1}
    sys.out.println(Any.toString(*p))
    (x, s) = p.get()
    sys.out.println(s)
    sys.out.println(Any.toString(*p))
    sys.out.println(Any.toString(x))