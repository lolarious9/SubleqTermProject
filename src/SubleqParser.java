
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileNotFoundException;

import java.io.FileWriter;
import java.io.IOException;

public class SubleqParser {

    public class Variable {
        private String key;
        private int val, start, it, sz, type;
        private LinkedList<Integer> vals;

        Variable(String k, String str) {
            vals = new LinkedList<>();
            val = -2;
            this.key = k;

            if (str.matches("[+-]?[\\d]+")) {
                this.type = 0;
                val = Integer.parseInt(str);
                sz = 1;
            } else {
                for (char v : str.toCharArray()) {
                    vals.add(Character.getNumericValue(v));
                }
                this.type = 1;
                sz = vals.size();
            }

        }

        public Integer getType() {
            return type;
        }

        public void setStart(int i) {
            start = i;
            it = i;
        }

        public Integer getIndex() {
            if (type == 0) {
                return it;
            }
            if (it - start > sz) {
                return -2;
            }
            int tmp = it;
            it += 1;
            return tmp;
        }

        public Integer getVal() {
            if (vals.isEmpty()) {
                return val;
            } else {
                return vals.removeFirst();
            }
        }

        public String getKey() {
            return key;
        }

    }

    private class Macro extends SubleqParser {

        // parses given filename
        private Map<String, String> params;
        private int paramCount;

        public int getParamCount() {
            return paramCount;
        }

        Macro(String fileIn) {
            super();

            if (!fileIn.endsWith(".slq")) {
                fileIn = fileIn.concat(".slq");
            }
            try (Scanner s = new Scanner(new File(fileIn))) {

                while (s.hasNextLine()) {
                    String tmp = s.nextLine();
                    if (!this.add(tmp)) {
                        throw new Exception("Something went wrong parsing the file." + fileIn);
                    }
                }

                s.close();

                // populate parameters
                Matcher m = COMMAND_PATTERN.matcher(super.header);

                if (m.matches()) {

                    params = new Hashtable<>(1);
                    paramCount = 1;
                    params.put(m.group("lop"), m.group("lop"));
                    if (m.group("rop") != null) {
                        params.put(m.group("rop"), m.group("rop"));
                        paramCount = 2;
                    } else if (m.group("third") != null) {
                        params.put(m.group("rop"), m.group("rop"));
                        params.put(m.group("third"), m.group("third"));
                        paramCount = 3;
                    }

                }

            } catch (FileNotFoundException e) {
                System.out.println(e);
                System.exit(-1);
            } catch (Exception E) {
                System.out.println(E);
            }

        }

        public boolean bind(ArrayList<String> callParams) {

            try {
                params.replaceAll((k, v) -> callParams.removeFirst());
            } catch (Exception e) {
                System.out.println(e);
                return false;
            }
            return true;
        }

        private String nextParam(ArrayList<String> s, int i) {
            String str = s.get(i);
            if (str == null || str.equals("")) {
                return "";
            }
            if (params.containsKey(str)) {
                String st = params.get(str);
                if (Character.isDigit(st.charAt(0)) || Character.isDigit(st.charAt(1))) {
                    return st;
                }
            }
            if (str.equals("z")) {
                return "0";
            }

            return str;
        }

        public ArrayList<String> bindAndReturn(String fileOut, ArrayList<String> callParams) {

            ArrayList<String> tmp = new ArrayList<>();
            bind(callParams);

            for (int i = 0; i < super.commands.size(); i++) {
                String lop = nextParam(super.lops, i);

                String rop = nextParam(super.rops, i);
                if (rop == null) {
                    rop = "";
                }
                String third = nextParam(super.thirdOp, i);
                if (third == null) {
                    third = "";
                }
                tmp.add(lop + " " + rop + " " + third);

            }

            return tmp;
        }

    }

    private ArrayList<String> commands, lops, rops, thirdOp;
    private Hashtable<String, Variable> vars;
    private Map<String, Macro> macros;

    private String header;
    private static final Pattern INCLUDABLE = Pattern.compile("^@(?<command>[a-zA-Z]+)");
    private static final Pattern COMMAND_PATTERN = Pattern
            .compile(
                    "((?<command>#?[a-zA-Z]+)(?:\\s+)(?<lop>[-\\w]+)(?:$|\\s+(?<rop>[-\\w]+)|\\s+\\{$)(?:$|\\s+(?<third>[-\\w]+)|(?:\\s+\\{$|$)))");
    private static final Pattern Variable = Pattern
            .compile(";([a-zA-Z_]+)\\s(?:(?<num>[-+]?\\d+)|(?:\\\"(?<str>[a-zA-Z]+)\\\"))$");

    private void init() {
        this.header = new String();
        this.commands = new ArrayList<String>();
        this.lops = new ArrayList<String>();
        this.rops = new ArrayList<String>();
        this.thirdOp = new ArrayList<String>();
        this.macros = new Hashtable<String, Macro>();
        this.vars = new Hashtable<>();
    }

    SubleqParser() {
        init();
    }

    // parses given filename
    SubleqParser(String fileIn, String fileOut) {
        init();

        if (!fileIn.endsWith(".slq")) {
            fileIn = fileIn.concat(".slq");
        }
        try (Scanner s = new Scanner(new File(fileIn))) {
            SubleqParser parser = new SubleqParser();

            while (s.hasNextLine()) {
                String tmp = s.nextLine();
                if (!parser.add(tmp)) {
                    throw new Exception("Something went wrong parsing the file." + fileIn);
                }
            }
            System.out.println(parser);
            s.close();
            parser.build(fileOut);
        } catch (FileNotFoundException e) {
            System.out.println(e);
            System.exit(-1);
        } catch (Exception E) {
            System.out.println(E);
        }
    }

    public boolean isEmpty() {
        return commands.isEmpty();
    }

    public ArrayList<String> nextLineList() {
        ArrayList<String> tmp = new ArrayList<String>();
        tmp.add(useVar(lops.removeFirst()));

        tmp.add(useVar(rops.removeFirst()));
        tmp.add(useVar(thirdOp.removeFirst()));
        return tmp;
    }

    public String useVar(String s) {
        if (s != null && vars.containsKey(s)) {
            return Integer.toString(vars.get(s).getIndex());
        }
        if (s == null) {
            return "";
        }
        return s;

    }

    public String nextLine() {
        StringBuilder tmp = new StringBuilder();
        tmp.append(useVar(lops.removeFirst()) + " ");
        tmp.append(useVar(rops.removeFirst() + " "));
        tmp.append(useVar(thirdOp.removeFirst()));
        return tmp.toString();
    }

    private void writeLine(String line, int pc, FileWriter fw) throws IOException {
        int len = line.split(" ").length;
        // assume next pc
        if (len == 2) {
            line += pc + 3;
        }
        fw.write(line);
        fw.write('\n');
    }

    private int buildVars(int pc, FileWriter fw) throws IOException {
        if (pc == 0) {
            fw.write(";z 0\n");
            return buildVars(1, fw);

        }
        for (Variable v : vars.values()) {
            v.setStart(pc);

            if (v.getType() == 0) {
                fw.write(";" + v.getKey() + " " + v.getVal());
                fw.write('\n');
                pc += 1;
                continue;
            } else {
                while (true) {

                    String tmpstri = ";";
                    int i = v.getVal();
                    if (i == -2) {
                        break;
                    }
                    char c;
                    if (v.getKey().length() == 1) {
                        c = v.getKey().charAt(0);
                    } else {
                        c = (char) (i + 87);
                    }
                    tmpstri += c + " " + (i + 87);
                    fw.write(tmpstri);
                    fw.write("\n");
                    pc += 1;
                }
            }
        }
        vars.put("z", new Variable("z", "0"));
        return pc;
    }

    //
    public boolean build(String fileOutName) {
        String tmp = fileOutName;
        if (!fileOutName.endsWith(".asq")) {
            tmp = tmp.concat(".asq");
        }

        try (FileWriter fw = new FileWriter(new File(tmp))) {
            int pc = 0;
            buildVars(pc, fw);

            fw.write("{\n");

            while (commands != null && !commands.isEmpty())

            {
                String command = commands.removeFirst();
                if (!command.equals("slq")) {

                    // macro
                    if (macros.containsKey(command)) {
                        ArrayList<String> macro = macros.get(command).bindAndReturn(fileOutName,
                                nextLineList());
                        for (int i = 0; i < macro.size(); i++) {
                            writeLine(macro.get(i), pc, fw);
                            pc += 3;

                        }
                    }

                } else {
                    writeLine(nextLine(), pc, fw);
                    pc += 3;
                }

            }
            fw.write("\n}");

            fw.close();
        } catch (

        IOException e) {
            System.out.println("Failed to make file");
            return false;
        }

        return true;

    }

    public boolean add(String s) {
        s = s.trim();
        Matcher m = INCLUDABLE.matcher(s);

        // Handle includes
        if (m.matches()) {
            macros.putIfAbsent(m.group("command"), new Macro(m.group("command")));
            return true;
        }
        m = Variable.matcher(s);
        // handle variables
        if (m.matches()) {
            if (m.group("num") != null) {
                vars.put(m.group(1), new Variable(m.group(1), m.group("num")));

            } else {
                vars.put(m.group(1), new Variable(m.group(1), m.group("str")));
            }
        }

        else if (s.startsWith("#") && s.endsWith("{")) {
            header = s;
            return true;
        } else {
            // normal commands
            m = COMMAND_PATTERN.matcher(s);
            if (m.matches()) {
                commands.add(m.group("command"));
                lops.add(m.group("lop"));
                rops.add(m.group("rop"));
                thirdOp.add(m.group("third"));

            } else if (s.equals("}")) {
                return true;
            } else {
                return false;
            }

        }

        return true;
    }
}
