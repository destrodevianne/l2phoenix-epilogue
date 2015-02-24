package l2p.extensions.listeners.events;

public interface PropertyEvent
{
	public Object getObject();

	public Object getOldValue();

	public Object getNewValue();

	public String getProperty();
}
