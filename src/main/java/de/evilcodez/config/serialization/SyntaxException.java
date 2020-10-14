package de.evilcodez.config.serialization;

public class SyntaxException extends RuntimeException {
	
	private int line;
	
	public SyntaxException(int line, String message) {
		super("Syntax error at line " + line + ". " + message);
		this.line = line;
	}
	
	public int getLine() {
		return line;
	}
	
	public void setLine(int line) {
		this.line = line;
	}
}
