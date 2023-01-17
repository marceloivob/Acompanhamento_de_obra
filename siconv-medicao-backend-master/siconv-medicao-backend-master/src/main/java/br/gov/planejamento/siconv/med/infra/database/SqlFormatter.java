package br.gov.planejamento.siconv.med.infra.database;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import javax.enterprise.context.Dependent;

@Dependent
public class SqlFormatter {

    private static final Set<String> BEGIN_CLAUSES = new HashSet<>();
    private static final Set<String> END_CLAUSES = new HashSet<>();
    private static final Set<String> LOGICAL = new HashSet<>();
    private static final Set<String> QUANTIFIERS = new HashSet<>();
    private static final Set<String> DML = new HashSet<>();
    private static final Set<String> MISC = new HashSet<>();

    public static final String WHITESPACE = " \n\r\f\t";

    static {
        BEGIN_CLAUSES.add("left");
        BEGIN_CLAUSES.add("right");
        BEGIN_CLAUSES.add("inner");
        BEGIN_CLAUSES.add("outer");
        BEGIN_CLAUSES.add("group");
        BEGIN_CLAUSES.add("order");

        END_CLAUSES.add("where");
        END_CLAUSES.add("set");
        END_CLAUSES.add("having");
        END_CLAUSES.add("join");
        END_CLAUSES.add("from");
        END_CLAUSES.add("by");
        END_CLAUSES.add("join");
        END_CLAUSES.add("into");
        END_CLAUSES.add("union");

        LOGICAL.add("and");
        LOGICAL.add("or");
        LOGICAL.add("when");
        LOGICAL.add("else");
        LOGICAL.add("end");

        QUANTIFIERS.add("in");
        QUANTIFIERS.add("all");
        QUANTIFIERS.add("exists");
        QUANTIFIERS.add("some");
        QUANTIFIERS.add("any");

        DML.add("insert");
        DML.add("update");
        DML.add("delete");

        MISC.add("select");
        MISC.add("on");
    }

    private static final String INDENT_STRING = "    ";
    private static final String INITIAL = System.lineSeparator() + INDENT_STRING;

    public String format(String source) {
        return new FormatProcess(source).perform();
    }

    private static class FormatProcess {
        boolean beginLine = true;
        boolean afterBeginBeforeEnd;
        boolean afterByOrSetOrFromOrSelect;
        @SuppressWarnings("unused")
        boolean afterValues;
        boolean afterOn;
        boolean afterBetween;
        boolean afterInsert;
        int inFunction;
        int parensSinceSelect;
        private LinkedList<Integer> parenCounts = new LinkedList<>();
        private LinkedList<Boolean> afterByOrFromOrSelects = new LinkedList<>();

        int indent = 1;

        StringBuilder result = new StringBuilder();
        StringTokenizer tokens;
        String lastToken;
        StringBuffer token;
        String lcToken;

        public FormatProcess(String sql) {
            tokens = new StringTokenizer(
                    sql,
                    "()+*/-=<>'`\"[]," + WHITESPACE,
                    true);
        }

        @SuppressWarnings("all")
        public String perform() {

            result.append(INITIAL);

            while (tokens.hasMoreTokens()) {
                token = new StringBuffer(tokens.nextToken());
                lcToken = token.toString().toLowerCase(Locale.ROOT);

                if ("'".equals(token.toString())) {
                    findToken("'");
                } else if ("\"".equals(token.toString())) {
                	findToken("\"");
                }
                // SQL Server uses "[" and "]" to escape reserved words
                // see SQLServerDialect.openQuote and SQLServerDialect.closeQuote
                else if ("[".equals(token.toString())) {
                	findToken("]");
                }

                if (afterByOrSetOrFromOrSelect && ",".equals(token.toString())) {
                    commaAfterByOrFromOrSelect();
                } else if (afterOn && ",".equals(token.toString())) {
                    commaAfterOn();
                }

                else if ("(".equals(token.toString())) {
                    openParen();
                } else if (")".equals(token.toString())) {
                    closeParen();
                }

                else if (BEGIN_CLAUSES.contains(lcToken)) {
                    beginNewClause();
                }

                else if (END_CLAUSES.contains(lcToken)) {
                    endNewClause();
                }

                else if ("select".equals(lcToken)) {
                    select();
                }

                else if (DML.contains(lcToken)) {
                    updateOrInsertOrDelete();
                }

                else if ("values".equals(lcToken)) {
                    values();
                }

                else if ("on".equals(lcToken)) {
                    on();
                }

                else if (afterBetween && lcToken.equals("and")) {
                    misc();
                    afterBetween = false;
                }

                else if (LOGICAL.contains(lcToken)) {
                    logical();
                }

                else if (isWhitespace(token.toString())) {
                    white();
                }

                else {
                    misc();
                }

                if (!isWhitespace(token.toString())) {
                    lastToken = lcToken;
                }

            }
            return result.toString();
        }

		private void findToken(String element) {
			String t;
			do {
			    t = tokens.nextToken();
			    token.append(t);
			}
			// cannot handle single quotes
			while (!element.equals(t) && tokens.hasMoreTokens());
		}

        private void commaAfterOn() {
            out();
            indent--;
            newline();
            afterOn = false;
            afterByOrSetOrFromOrSelect = true;
        }

        private void commaAfterByOrFromOrSelect() {
            out();
            newline();
        }

        private void logical() {
            if ("end".equals(lcToken)) {
                indent--;
            }
            newline();
            out();
            beginLine = false;
        }

        private void on() {
            indent++;
            afterOn = true;
            newline();
            out();
            beginLine = false;
        }

        private void misc() {
            out();
            if ("between".equals(lcToken)) {
                afterBetween = true;
            }
            if (afterInsert) {
                newline();
                afterInsert = false;
            } else {
                beginLine = false;
                if ("case".equals(lcToken)) {
                    indent++;
                }
            }
        }

        private void white() {
            if (!beginLine) {
                result.append(" ");
            }
        }

        private void updateOrInsertOrDelete() {
            out();
            indent++;
            beginLine = false;
            if ("update".equals(lcToken)) {
                newline();
            }
            if ("insert".equals(lcToken)) {
                afterInsert = true;
            }
        }

        private void select() {
            out();
            indent++;
            newline();
            parenCounts.addLast(parensSinceSelect);
            afterByOrFromOrSelects.addLast(afterByOrSetOrFromOrSelect);
            parensSinceSelect = 0;
            afterByOrSetOrFromOrSelect = true;
        }

        private void out() {
            result.append(token);
        }

        private void endNewClause() {
            if (!afterBeginBeforeEnd) {
                indent--;
                if (afterOn) {
                    indent--;
                    afterOn = false;
                }
                newline();
            }
            out();
            if (!"union".equals(lcToken)) {
                indent++;
            }
            newline();
            afterBeginBeforeEnd = false;
            afterByOrSetOrFromOrSelect = "by".equals(lcToken)
                    || "set".equals(lcToken)
                    || "from".equals(lcToken);
        }

        private void beginNewClause() {
            if (!afterBeginBeforeEnd) {
                if (afterOn) {
                    indent--;
                    afterOn = false;
                }
                indent--;
                newline();
            }
            out();
            beginLine = false;
            afterBeginBeforeEnd = true;
        }

        private void values() {
            indent--;
            newline();
            out();
            indent++;
            newline();
            afterValues = true;
        }

        private void closeParen() {
            parensSinceSelect--;
            if (parensSinceSelect < 0) {
                indent--;
                parensSinceSelect = parenCounts.removeLast();
                afterByOrSetOrFromOrSelect = afterByOrFromOrSelects.removeLast();
            }
            if (inFunction > 0) {
                inFunction--;
                out();
            } else {
                if (!afterByOrSetOrFromOrSelect) {
                    indent--;
                    newline();
                }
                out();
            }
            beginLine = false;
        }

        private void openParen() {
            if (isFunctionName(lastToken) || inFunction > 0) {
                inFunction++;
            }
            beginLine = false;
            if (inFunction > 0) {
                out();
            } else {
                out();
                if (!afterByOrSetOrFromOrSelect) {
                    indent++;
                    newline();
                    beginLine = true;
                }
            }
            parensSinceSelect++;
        }

        private static boolean isFunctionName(String tok) {
            if (tok == null || tok.length() == 0) {
                return false;
            }

            final char begin = tok.charAt(0);
            final boolean isIdentifier = Character.isJavaIdentifierStart(begin) || '"' == begin;
            return isIdentifier &&
                    !LOGICAL.contains(tok) &&
                    !END_CLAUSES.contains(tok) &&
                    !QUANTIFIERS.contains(tok) &&
                    !DML.contains(tok) &&
                    !MISC.contains(tok);
        }

        private static boolean isWhitespace(String token) {
            return WHITESPACE.contains(token);
        }

        private void newline() {
            result.append(System.lineSeparator());
            for (int i = 0; i < indent; i++) {
                result.append(INDENT_STRING);
            }
            beginLine = true;
        }
    }
    
}
    