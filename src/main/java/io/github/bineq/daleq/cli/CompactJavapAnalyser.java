package io.github.bineq.daleq.cli;

/**
 * Analyser based on comparing the output of javap -c -p
 * @author jens dietrich
 */
public class CompactJavapAnalyser extends AbstractJavapAnalyser {

    @Override
    public String[] getJavapArgs() {
        return new String[]{"-c","-p"};
    }


    @Override
    public String name() {
        return "javap -c -p";
    }

    @Override
    public String description() {
        return "using the standard java disassembler: \"javap -c -p\"";
    }
}
