package io.github.bineq.daleq.cli;

/**
 * Analyser based on comparing the output of javap -c -p
 * @author jens dietrich
 */
public class VerboseJavapAnalyser extends AbstractJavapAnalyser {

    @Override
    public String[] getJavapArgs() {
        return new String[]{"-v"};
    }


    @Override
    public String name() {
        return "javap -v";
    }

    @Override
    public String description() {
        return "using the standard java disassembler: \"javap -v\"";
    }
}
