package shared;

public interface TracerFactory {
	public abstract Tracer start(String algorithmId, int run);
}