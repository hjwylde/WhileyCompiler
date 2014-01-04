import println from whiley.lang.System

type Expr is int | real | [Expr] | ListAccess

type ListAccess is {Expr index, Expr src}

type Value is int | real | [Value]

function evaluate(Expr e) => null | Value:
    if (e is real) || (e is int):
        return e
    else:
        if e is [Expr]:
            r = []
            for i in e:
                v = evaluate(i)
                if v is null:
                    return v
                else:
                    r = r + [v]
            return r
        else:
            src = evaluate(e.src)
            index = evaluate(e.index)
            if (src is [Expr]) && ((index is int) && ((index >= 0) && (index < |src|))):
                return src[index]
            else:
                return null

public method main(System.Console sys) => void:
    sys.out.println(Any.toString(evaluate(123)))
    sys.out.println(Any.toString(evaluate({index: 0, src: [112, 212332, 342]})))
    sys.out.println(Any.toString(evaluate({index: 2, src: [112312, -289712, 312242]})))
    sys.out.println(Any.toString(evaluate([123, 223, 323])))