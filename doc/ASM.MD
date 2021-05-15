例如ILOAD表述读取一个int、boolean、byte、char、short类型的局部变量，并将它的值压入操作数栈中。

LLOAD对应long类型、FLOAD对应float、DLOAD对应double类型、ALOAD对应Java对象

因此一下用xLOAD来泛指这一类的操作

在操作数栈和局部变量见传递值的指令

xLOAD：读取一个局部变量的值，并将其压入操作数栈

xSTORE：从操作数栈弹出一个值，存储至局部变量中

POP：弹出顶部栈

DUP：压入顶部栈值的副本

SWAP：交换顶部两个值

xCONST_0：压入一个常量值0

xCONST_NULL：压入一个null

LDC：压入任意 int、float、long、double、String、class常量值

xADD、xSUB、xMUL、xDIV、xREM：弹出顶部两个值，进行加减乘除取余等操作，并将结果压回栈内

I2f、F2D、L2D：弹出一个值，类型转换后重新压入栈内

CHECKCAST t：弹出一个值，引用类型转换为t之后重新压入栈内

NEW：将一个指定类型的新对象压入栈内

GETFIELD：弹出一个对象引用，并压入其指定字段的值

PUTFIELD：弹出一个值和一个对象引用，并将值存入对象的指定字段中

INVOKExxx：弹出目标对象与参数所需的对象个数，调用方法（根据xxx的不同调用不同类型的方法），并将返回值压入栈中

xALOAD：弹出一个索引和一个数组，并压入数组指定索引处的值

xASTORE：弹出一个值、一个索引、一个数组，并将数组的指定索引处存入对应值。

IFEQ 、IFNE、IFGE：从栈中弹出一个int值，这个值等于/小于/大于0则跳转至指定lable，否则正常执行下一个指令

RETURN、xRETURN：用于终止一个方法执行，返回对应值