package xiue.utils;

public class Command {
    public static final String SPLITTER = " ";
    public String command;
    private String[] patterns;
    private int cursor = -1;//索引

    public Command(String command) {
        this.command = command;
        parse();
    }

    public void moveToFirst() {
        if (patterns != null)
            setCursor(0);
    }

    public boolean moveToNext() {
        if (patterns != null)
            if (cursor + 1 < length()) {
                setCursor(++cursor);
                return true;
            }
        return false;
    }

    public void moveToEnd() {
        setCursor(length());
    }

    public String current() {
        return patterns[cursor];
    }

    public long curLong() throws NumberFormatException {
        return Long.parseLong(current());
    }

    public int curInt() {
        return Integer.parseInt(current());
    }

    public String toString() {
        return this.command;
    }

    public int length() {
        if (patterns != null)
            return patterns.length;
        return 0;
    }

    public int cursor() {
        return cursor;
    }

    public void setCursor(int cursor) {
        this.cursor = cursor;
    }

    public boolean hasNext() {
        return patterns != null && length() > 0 && cursor < length();
    }

    public String next() {
        if (moveToNext())
            return current();
        return null;
    }

    public long nextLong() throws NumberFormatException {
        return Long.parseLong(next());
    }

    public int nextInt() {
        return Integer.parseInt(next());
    }

    public boolean nextBoolean() {
        return Boolean.parseBoolean(next());
    }

    public void parse() {
        parse(this.command);
    }

    public void parse(String cmd) {
        cmd = cmd.trim();
        patterns = cmd.split(SPLITTER);
        moveToFirst();
    }

}
