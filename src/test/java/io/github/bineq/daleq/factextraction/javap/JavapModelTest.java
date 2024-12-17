package io.github.bineq.daleq.factextraction.javap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.List;

public class JavapModelTest {

    private JavapClassModel classModel;

    @BeforeEach
    public void setup() throws IOException {
        classModel = JavapClassModel.parse("mypck.MyClass", Path.of(JavapModelTest.class.getResource("/basic/mypck/MyClass.javap").getFile()));
    }

    @Test
    public void testClassName() {
        assertEquals("mypck.MyClass",classModel.getName());
    }

    //    public mypck.MyClass();
    //    Code:
    //       0: aload_0
    //       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
    //       4: return
    @Test
    public void testMethod1() {
        JavapMethodModel method = classModel.getMethods().stream().filter(m -> m.getName().equals("<init>")).findFirst().get();
        assertNotNull(method);
        List<JavapInstructionModel> instructions = method.getInstructions();
        assertEquals(3, instructions.size());

        JavapInstructionModel instruction1 = instructions.get(0);
        assertEquals(0, instruction1.getLabel());
        assertEquals("aload_0", instruction1.getInstruction());
        assertEquals(-1, instruction1.getConstantPoolRef());
        assertEquals(null, instruction1.getValue());

        JavapInstructionModel instruction2 = instructions.get(1);
        assertEquals(1, instruction2.getLabel());
        assertEquals("invokespecial", instruction2.getInstruction());
        assertEquals(1, instruction2.getConstantPoolRef());
        assertEquals("java/lang/Object.\"<init>\":()V", instruction2.getValue());

        JavapInstructionModel instruction3 = instructions.get(2);
        assertEquals(4, instruction3.getLabel());
        assertEquals("return", instruction3.getInstruction());
        assertEquals(-1, instruction3.getConstantPoolRef());
        assertEquals(null, instruction3.getValue());
    }

    //    public static void main(java.lang.String[]);
    //    Code:
    //        0: getstatic     #7                  // Field java/lang/System.out:Ljava/io/PrintStream;
    //        3: ldc           #13                 // String Hello World
    //        5: invokevirtual #15                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
    //        8: return
    @Test
    public void testMethod2() {
        JavapMethodModel method = classModel.getMethods().stream().filter(m -> m.getName().equals("main")).findFirst().get();
        assertNotNull(method);
        List<JavapInstructionModel> instructions = method.getInstructions();
        assertEquals(4, instructions.size());

        JavapInstructionModel instruction1 = instructions.get(0);
        assertEquals(0, instruction1.getLabel());
        assertEquals("getstatic", instruction1.getInstruction());
        assertEquals(7, instruction1.getConstantPoolRef());
        assertEquals("java/lang/System.out:Ljava/io/PrintStream;", instruction1.getValue());

        JavapInstructionModel instruction2 = instructions.get(1);
        assertEquals(3, instruction2.getLabel());
        assertEquals("ldc", instruction2.getInstruction());
        assertEquals(13, instruction2.getConstantPoolRef());
        assertEquals("Hello World", instruction2.getValue());

        JavapInstructionModel instruction3 = instructions.get(2);
        assertEquals(5, instruction3.getLabel());
        assertEquals("invokevirtual", instruction3.getInstruction());
        assertEquals(15, instruction3.getConstantPoolRef());
        assertEquals("java/io/PrintStream.println:(Ljava/lang/String;)V", instruction3.getValue());

        JavapInstructionModel instruction4 = instructions.get(3);
        assertEquals(8, instruction4.getLabel());
        assertEquals("return", instruction4.getInstruction());
        assertEquals(-1, instruction4.getConstantPoolRef());
        assertEquals(null, instruction4.getValue());
    }
}
