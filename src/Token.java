import java.util.Objects;

public class Token implements Comparable<Token>{
    public String name;
    public String type;
    private int index = -1;

    public Token(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public Token(String name, String type, int index) {
        this.name = name;
        this.type = type;
        this.index = index;
    }

    public Token(Token token, int index) {
        this.name = token.name;
        this.type = token.type;
        this.index = index;
    }

    @Override
    public String toString() {
        return String.format("{'%s'}", name, type, index);
    }

    @Override
    public int compareTo(Token token) {
        return Integer.compare(index, token.index);
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return Objects.equals(name, token.name) &&
                Objects.equals(type, token.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }
}
