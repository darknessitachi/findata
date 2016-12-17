package michael.findata.algoquant.execution.component.depthprovider;

import michael.findata.algoquant.execution.listener.DepthListener;

public interface DepthProvider {
	void setDepthListener (DepthListener listener);
}
