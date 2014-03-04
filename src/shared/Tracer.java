package shared;

public interface Tracer {

	public abstract void start(String algorithmId, int run);
	public abstract void trace(int iteration, double optimal, double trainResult);

}