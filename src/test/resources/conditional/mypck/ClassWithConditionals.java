package mypck;
public class ClassWithConditionals {
    public int foo(int x,int y) {
        if (x<y) {
            return 1;
        }
        else if (x>y) {
            return -1;
        }
        else {
            return 0;
        }
    }
}
