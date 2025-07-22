package pck;
@ClassAnnotation(cF1=42,cF2="bar")
public class ClassWithAnnotations {

    @MethodAnnotation(mF1=42,mF2="bar")
    public void foo() {}

    @FieldAnnotation(fF1=42,fF2="bar")
    public String f = null;

}
